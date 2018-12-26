package com.mike_caron.factorycraft.energy;

import com.mike_caron.factorycraft.capability.CapabilityEnergyConnector;
import com.mike_caron.factorycraft.util.Graph;
import com.mike_caron.factorycraft.util.INBTSerializer;
import com.mike_caron.factorycraft.util.ITileEntityFinder;
import com.mike_caron.factorycraft.util.Tuple3i;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EnergyManager
    implements IEnergyManager
{
    private final ITileEntityFinder world;
    private final Graph<Tuple3i, Node> graph = new Graph<>(new Tuple3i.Serializer(), new Node.Serializer());

    public EnergyManager(ITileEntityFinder world)
    {
        this.world = world;
    }

    @Override
    public UUID registerConnector(@Nonnull BlockPos pos)
    {
        Tuple3i tup = makeTuple(pos);
        IEnergyConnector connector = getConnectorAt(tup);

        if(connector == null)
            throw new IllegalStateException("Trying to register missing connector at " + tup);

        int radiusSq = connector.getRadius() * connector.getRadius();

        Set<Tuple3i> nearbyNodes = graph.nodesMatching((node, value) -> getDistanceSq(tup, node) < Math.min(radiusSq, value.radius) );

        UUID newNetwork = null;
        Set<UUID> oldNeworks = null;

        for(Tuple3i node : nearbyNodes)
        {
            Node value = graph.getValue(node);

            if(newNetwork == null)
            {
                newNetwork = value.network;
            }
            else if(!value.network.equals(newNetwork))
            {
                //ah shit
                if(oldNeworks == null)
                {
                    oldNeworks = new HashSet<>();
                }
                oldNeworks.add(value.network);
            }
        }

        if(newNetwork == null)
        {
            newNetwork = UUID.randomUUID();
        }
        else if(oldNeworks != null)
        {
            //...
            final Set<UUID> tmp = oldNeworks;
            Set<Tuple3i> toMigrate = graph.nodesMatching((node, value) -> tmp.contains(value.network));

            for(Tuple3i node : toMigrate)
            {
                graph.getValue(node).network = newNetwork;
                notifyNetworkChange(node, newNetwork);
            }
        }

        Node newNode = new Node();

        newNode.network = newNetwork;
        newNode.radius = radiusSq;

        graph.addNode(tup, newNode);

        for(Tuple3i neighbor : nearbyNodes)
        {
            graph.addEdge(tup, neighbor);
        }

        notifyNetworkChange(tup, newNetwork);

        return newNetwork;
    }

    @Override
    public void deleteConnector(@Nonnull BlockPos pos)
    {
        Tuple3i tup = makeTuple(pos);

        Node node = graph.getValue(tup);
        graph.removeNode(tup);

        ensureNetworkIntegrity();
    }

    @Override
    public void makeConnection(@Nonnull BlockPos pos, @Nonnull BlockPos other)
    {
        Tuple3i src = makeTuple(pos);
        Tuple3i dest = makeTuple(other);

        graph.addEdge(src, dest);
    }

    @Override
    public void removeConnection(@Nonnull BlockPos pos, @Nonnull BlockPos other)
    {
        Tuple3i src = makeTuple(pos);
        Tuple3i dest = makeTuple(other);

        graph.removeEdge(src, dest);

        ensureNetworkIntegrity();
    }

    @Override
    public boolean isConnected(@Nonnull BlockPos src, @Nonnull BlockPos other)
    {
        Tuple3i srcT = makeTuple(src);
        Tuple3i otherT = makeTuple(other);

        return graph.getEdges(srcT).contains(otherT);
    }

    private void ensureNetworkIntegrity()
    {
        List<Set<Tuple3i>> sets = graph.getDiscreteGraphs();
        Set<UUID> burned = new HashSet<>();

        for(Set<Tuple3i> network : sets)
        {
            UUID networkId = null;

            for(Tuple3i tup : network)
            {
                Node node = graph.getValue(tup);

                if(networkId == null)
                {
                    if(burned.contains(node.network))
                    {
                        networkId = UUID.randomUUID();
                    }
                    else
                    {
                        networkId = node.network;
                    }
                }

                if(!node.network.equals(networkId))
                {
                    node.network = networkId;
                    notifyNetworkChange(tup, networkId);
                }

            }

            burned.add(networkId);
        }
    }

    private void notifyNetworkChange(Tuple3i node, UUID newNetwork)
    {
        IEnergyConnector connector = getConnectorAt(node);

        if(connector == null)
            throw new IllegalStateException("Missing connector for registered energy node at " + node.toString());

        connector.notifyNetworkChange(newNetwork);

    }

    private IEnergyConnector getConnectorAt(Tuple3i node)
    {
        TileEntity te = world.getTileEntityAt(makeBlockPos(node));
        if(te == null)
            return null;

        return te.getCapability(CapabilityEnergyConnector.ENERGY_CONNECTOR, null);
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        return graph.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbtTagCompound)
    {
        graph.deserializeNBT(nbtTagCompound);
    }

    private double getDistanceSq(@Nonnull Tuple3i src, @Nonnull Tuple3i dest)
    {
        return Math.pow(dest.x - src.x, 2) + Math.pow(dest.z - src.z, 2);
    }

    private BlockPos makeBlockPos(@Nonnull Tuple3i tup)
    {
        return new BlockPos(tup.x, tup.y, tup.z);
    }

    private Tuple3i makeTuple(@Nonnull BlockPos pos)
    {
        return new Tuple3i(pos.getX(), pos.getY(), pos.getZ());
    }

    static class Node
    {
        UUID network;
        int radius;

        static class Serializer
            implements INBTSerializer<Node>
        {
            @Override
            public NBTBase serializeNBT(Node obj)
            {
                NBTTagCompound ret = new NBTTagCompound();

                ret.setString("network", obj.network.toString());
                ret.setInteger("radius", obj.radius);

                return ret;
            }

            @Override
            public Node deserializeNBT(NBTBase nbt)
            {
                NBTTagCompound c = (NBTTagCompound)nbt;

                Node ret = new Node();

                ret.network = UUID.fromString(c.getString("network"));
                ret.radius = c.getInteger("radius");

                return ret;
            }
        }
    }
}
