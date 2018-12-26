package com.mike_caron.factorycraft.tileentity;

import com.mike_caron.factorycraft.api.capabilities.CapabilityEnergyConnector;
import com.mike_caron.factorycraft.energy.EnergyConnector;
import com.mike_caron.factorycraft.api.energy.IEnergyConnector;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityElectricalPole
    extends TypedTileEntity
{
    private EnergyConnector energyConnector;

    public TileEntityElectricalPole()
    {
        super();
    }

    public TileEntityElectricalPole(int type)
    {
        super(type);

    }

    public void connect()
    {
        energyConnector.connectIfNeeded();
    }

    public void disconnect()
    {
        energyConnector.disconnect();
    }

    @Override
    protected void onKnowingType()
    {
        super.onKnowingType();

        energyConnector = new EnergyConnector(this, getConnectRadius(), getPowerRadius());
    }

    public int getConnectRadius()
    {
        switch(type)
        {
            default:
                return 5;
        }
    }

    public int getPowerRadius()
    {
        switch(type)
        {
            default:
                return 3;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        energyConnector.deserializeNBT(nbt.getCompoundTag("energy"));
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound)
    {
        NBTTagCompound ret = super.writeToNBT(compound);

        ret.setTag("energy", energyConnector.serializeNBT());

        return ret;
    }

    public IEnergyConnector getConnector()
    {
        return energyConnector;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityEnergyConnector.ENERGY_CONNECTOR)
            return true;
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityEnergyConnector.ENERGY_CONNECTOR)
            return CapabilityEnergyConnector.ENERGY_CONNECTOR.cast(energyConnector);

        return super.getCapability(capability, facing);
    }
}
