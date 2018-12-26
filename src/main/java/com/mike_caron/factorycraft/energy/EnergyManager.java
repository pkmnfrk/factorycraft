package com.mike_caron.factorycraft.energy;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.api.capabilities.CapabilityEnergyConnector;
import com.mike_caron.factorycraft.api.energy.IEnergyConnector;
import com.mike_caron.factorycraft.api.energy.IEnergyManager;
import com.mike_caron.factorycraft.util.Graph;
import com.mike_caron.factorycraft.util.INBTSerializer;
import com.mike_caron.factorycraft.util.ITileEntityFinder;
import com.mike_caron.factorycraft.util.Tuple3i;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.IntConsumer;

public class EnergyManager
        implements IEnergyManager
{
    private final ITileEntityFinder world;
    private final Graph<Tuple3i, Node> graph = new Graph<>(new Tuple3i.Serializer(), new Node.Serializer());

    private final Map<UUID, List<Request>> supply = new HashMap<>();
    private final Map<UUID, List<Request>> demand = new HashMap<>();

    private final static List<EnergyManager> registeredInstances = new ArrayList<>();

    public EnergyManager(ITileEntityFinder world)
    {
        this.world = world;

        registeredInstances.add(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void registerConnector(@Nonnull BlockPos pos)
    {
        Tuple3i tup = makeTuple(pos);
        IEnergyConnector connector = getConnectorAt(tup);

        if (connector == null)
        {
            throw new IllegalStateException("Trying to register missing connector at " + tup);
        }

        int radiusSq = connector.getConnectRadius();

        Set<Tuple3i> nearbyNodes = graph
                .nodesMatching((node, value) -> {
                    IEnergyConnector con = getConnectorAt(node);
                    if(con == null) return false;
                    return getDistance(tup, node) < Math.min(radiusSq, con.getConnectRadius());
                });

        UUID newNetwork = null;
        Set<UUID> oldNeworks = null;

        for (Tuple3i node : nearbyNodes)
        {
            Node value = graph.getValue(node);

            if (newNetwork == null)
            {
                newNetwork = value.network;
            }
            else if (!value.network.equals(newNetwork))
            {
                //ah shit
                if (oldNeworks == null)
                {
                    oldNeworks = new HashSet<>();
                }
                oldNeworks.add(value.network);
            }
        }

        if (newNetwork == null)
        {
            newNetwork = UUID.randomUUID();
        }
        else if (oldNeworks != null)
        {
            //...
            final Set<UUID> tmp = oldNeworks;
            Set<Tuple3i> toMigrate = graph.nodesMatching((node, value) -> tmp.contains(value.network));

            for (Tuple3i node : toMigrate)
            {
                graph.getValue(node).network = newNetwork;
                notifyNetworkChange(node, newNetwork);
            }
        }

        Node newNode = new Node();

        newNode.network = newNetwork;

        graph.addNode(tup, newNode);

        for (Tuple3i neighbor : nearbyNodes)
        {
            graph.addEdge(tup, neighbor);
        }

        try
        {
            notifyNetworkChange(tup, newNetwork);
        }
        catch (IllegalStateException ex)
        {
            graph.removeNode(tup);
        }
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

    @Nullable
    @Override
    public BlockPos findConnector(@Nonnull BlockPos src)
    {
        Tuple3i srcT = makeTuple(src);

        Set<Tuple3i> nodes = graph.nodesMatching((node, value) -> {
            IEnergyConnector con = getConnectorAt(node);
            if(con == null) return false;
            return getDistance(srcT, node) <= con.getPowerRadius();
        });

        return nodes.stream()
                    .min(Comparator.comparingDouble(a -> getDistanceSq(srcT, a)))
                    .map(this::makeBlockPos)
                    .orElse(null);
    }

    @Override
    public void requestEnergy(UUID network, int amount, IntConsumer callback)
    {
        if(amount <= 0)
        {
            callback.accept(0);
            return;
        }

        if(!demand.containsKey(network))
        {
            demand.put(network, new ArrayList<>());
        }
        demand.get(network).add(new Request(amount, callback));
    }

    @Override
    public void provideEnergy(UUID network, int amount, IntConsumer callback)
    {
        if(amount <= 0)
        {
            callback.accept(0);
            return;
        }

        if(!supply.containsKey(network))
        {
            supply.put(network, new ArrayList<>());
        }
        supply.get(network).add(new Request(amount, callback));
    }

    @SubscribeEvent
    public void serverTick(TickEvent.WorldTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START)
        {
            supply.clear();
            demand.clear();
        }
        else if(event.phase == TickEvent.Phase.END)
        {
            Set<UUID> networks = new HashSet<>(supply.keySet());
            networks.addAll(demand.keySet());

            for(UUID network : networks)
            {
                int totalSupply = 0;
                int totalDemand = 0;

                if(supply.containsKey(network))
                {
                    totalSupply = supply.get(network).stream().map(r -> r.amount).reduce(0, (a, b) -> a + b);
                }

                if(demand.containsKey(network))
                {
                    totalDemand = demand.get(network).stream().map(r -> r.amount).reduce(0, (a, b) -> a + b);
                }

                if(totalSupply == 0)
                {
                    demand.get(network).forEach(r -> r.callback.accept(0));
                }
                else if(totalDemand == 0)
                {
                    supply.get(network).forEach(r -> r.callback.accept(0));
                }
                else if(totalSupply > totalDemand)
                {
                    double ratio = ((double)totalDemand) / totalSupply;

                    demand.get(network).forEach(r -> r.callback.accept(r.amount));
                    supply.get(network).forEach(r -> r.callback.accept((int)Math.round(r.amount * ratio)));
                }
                else
                {
                    double ratio = ((double)totalSupply) / totalDemand;

                    supply.get(network).forEach(r -> r.callback.accept(r.amount));
                    demand.get(network).forEach(r -> r.callback.accept((int)Math.round(r.amount * ratio)));
                }
            }

            supply.clear();
            demand.clear();
        }
    }

    private void ensureNetworkIntegrity()
    {
        List<Set<Tuple3i>> sets = graph.getDiscreteGraphs();
        Set<UUID> burned = new HashSet<>();
        Set<Tuple3i> error = new HashSet<>();

        for (Set<Tuple3i> network : sets)
        {
            UUID networkId = null;

            for (Tuple3i tup : network)
            {
                Node node = graph.getValue(tup);

                if (networkId == null)
                {
                    if (burned.contains(node.network))
                    {
                        networkId = UUID.randomUUID();
                    }
                    else
                    {
                        networkId = node.network;
                    }
                }

                if (!node.network.equals(networkId))
                {
                    node.network = networkId;

                    try
                    {
                        notifyNetworkChange(tup, networkId);
                    }
                    catch (IllegalStateException ex)
                    {
                        FactoryCraft.logger.error(ex);
                        error.add(tup);
                    }
                }

            }

            burned.add(networkId);
        }

        for (Tuple3i tup : error)
        {
            graph.removeNode(tup);
        }
    }

    private void notifyNetworkChange(Tuple3i node, UUID newNetwork)
    {
        IEnergyConnector connector = getConnectorAt(node);

        if (connector == null)
        {
            throw new IllegalStateException("Missing connector for registered energy node at " + node.toString());
        }

        connector.notifyNetworkChange(newNetwork);


    }

    private IEnergyConnector getConnectorAt(Tuple3i node)
    {
        TileEntity te = world.getTileEntityAt(makeBlockPos(node));
        if (te == null)
        {
            return null;
        }

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

    private double getDistance(@Nonnull Tuple3i src, @Nonnull Tuple3i dest)
    {
        return Math.sqrt(Math.pow(dest.x - src.x, 2) + Math.pow(dest.z - src.z, 2));
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

        static class Serializer
                implements INBTSerializer<Node>
        {
            @Override
            public NBTBase serializeNBT(Node obj)
            {
                NBTTagCompound ret = new NBTTagCompound();

                ret.setString("network", obj.network.toString());

                return ret;
            }

            @Override
            public Node deserializeNBT(NBTBase nbt)
            {
                NBTTagCompound c = (NBTTagCompound) nbt;

                Node ret = new Node();

                ret.network = UUID.fromString(c.getString("network"));

                return ret;
            }
        }
    }

    public static void cleanUp()
    {
        for(EnergyManager mg : registeredInstances)
        {
            MinecraftForge.EVENT_BUS.unregister(mg);
        }
        registeredInstances.clear();
    }

    static class Request
    {
        IntConsumer callback;
        int amount;

        Request(int amount, IntConsumer callback)
        {
            this.amount = amount;
            this.callback = callback;
        }
    }
}
