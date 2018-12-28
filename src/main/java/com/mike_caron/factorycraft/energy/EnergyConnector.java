package com.mike_caron.factorycraft.energy;

import com.mike_caron.factorycraft.api.capabilities.CapabilityEnergyManager;
import com.mike_caron.factorycraft.api.energy.IEnergyConnector;
import com.mike_caron.factorycraft.api.energy.IEnergyManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;
import java.util.UUID;
import java.util.function.IntConsumer;

public abstract class EnergyConnector
    implements IEnergyConnector, INBTSerializable<NBTTagCompound>
{
    private UUID networkId;
    private TileEntity host;
    private List<BlockPos> connections;

    public EnergyConnector(TileEntity host)
    {
        this.host = host;
    }

    public final UUID getNetworkId()
    {
        return networkId;
    }

    public final void disconnect()
    {
        if(networkId != null)
        {
            IEnergyManager manager = getEnergyManager();

            manager.deleteConnector(host.getPos());
        }
    }

    public final void connectIfNeeded()
    {
        //if(networkId == null)
        //{
            IEnergyManager manager = getEnergyManager();

            manager.registerConnector(host.getPos());
        //}

    }

    private IEnergyManager getEnergyManager()
    {
        if(host.getWorld() == null || host.getWorld().isRemote) return null;

        return host.getWorld().getCapability(CapabilityEnergyManager.ENERGY_MANAGER, null);
    }

    @Override
    public abstract int getConnectRadius();

    @Override
    public abstract int getPowerRadius();

    @Override
    public final void notifyNetworkChange(UUID newNetwork)
    {
        this.networkId = newNetwork;
    }

    @Override
    public final void requestEnergy(int amount, IntConsumer callback)
    {
        getEnergyManager().requestEnergy(networkId, amount, callback);
    }

    @Override
    public final void provideEnergy(int amount, IntConsumer callback)
    {
        getEnergyManager().provideEnergy(networkId, amount, callback);
    }

    @Override
    public final NBTTagCompound serializeNBT()
    {
        NBTTagCompound ret = new NBTTagCompound();

        if(networkId != null)
        {
            ret.setString("networkId", networkId.toString());
        }

        return ret;
    }

    @Override
    public final void deserializeNBT(NBTTagCompound nbtTagCompound)
    {
        this.networkId = null;
        if(nbtTagCompound.hasKey("networkId"))
        {
            this.networkId = UUID.fromString(nbtTagCompound.getString("networkId"));
        }

    }

    public final List<BlockPos> getConnections()
    {
        if(connections == null)
        {
            IEnergyManager man = getEnergyManager();

            if(man != null)
            {
                connections = man.getConnections(host.getPos());
            }
        }

        return connections;
    }

    @Override
    public final void notifyConnectionsChanged()
    {
        IEnergyManager man = getEnergyManager();

        if(man != null)
        {
            connections = man.getConnections(host.getPos());
        }

        onConnectionsChanged();
    }

    protected void onConnectionsChanged()
    {

    }
}
