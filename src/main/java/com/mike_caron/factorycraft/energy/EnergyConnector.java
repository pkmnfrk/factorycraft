package com.mike_caron.factorycraft.energy;

import com.mike_caron.factorycraft.api.capabilities.CapabilityEnergyManager;
import com.mike_caron.factorycraft.api.energy.IEnergyConnector;
import com.mike_caron.factorycraft.api.energy.IEnergyManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;
import java.util.function.IntConsumer;

public class EnergyConnector
    implements IEnergyConnector, INBTSerializable<NBTTagCompound>
{
    private UUID networkId;
    private TileEntity host;
    private final int connectRadius;
    private final int powerRadius;

    public EnergyConnector(TileEntity host, int connectRadius, int powerRadius)
    {
        this.host = host;
        this.connectRadius = connectRadius;
        this.powerRadius = powerRadius;
    }

    public UUID getNetworkId()
    {
        return networkId;
    }

    public void disconnect()
    {
        if(networkId != null)
        {
            IEnergyManager manager = getEnergyManager();

            manager.deleteConnector(host.getPos());
        }
    }

    public void connectIfNeeded()
    {
        //if(networkId == null)
        //{
            IEnergyManager manager = getEnergyManager();

            manager.registerConnector(host.getPos());
        //}

    }

    private IEnergyManager getEnergyManager()
    {
        return host.getWorld().getCapability(CapabilityEnergyManager.ENERGY_MANAGER, null);
    }

    @Override
    public int getConnectRadius()
    {
        return connectRadius;
    }

    @Override
    public int getPowerRadius()
    {
        return powerRadius;
    }

    @Override
    public void notifyNetworkChange(UUID newNetwork)
    {
        this.networkId = newNetwork;
    }

    @Override
    public void requestEnergy(int amount, IntConsumer callback)
    {
        getEnergyManager().requestEnergy(networkId, amount, callback);
    }

    @Override
    public void provideEnergy(int amount, IntConsumer callback)
    {
        getEnergyManager().provideEnergy(networkId, amount, callback);
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound ret = new NBTTagCompound();

        if(networkId != null)
        {
            ret.setString("networkId", networkId.toString());
        }

        return ret;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbtTagCompound)
    {
        this.networkId = null;
        if(nbtTagCompound.hasKey("networkId"))
        {
            this.networkId = UUID.fromString(nbtTagCompound.getString("networkId"));
        }

    }
}
