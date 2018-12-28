package com.mike_caron.factorycraft.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.function.IntConsumer;

public abstract class EnergyAppliance
    implements INBTSerializable<NBTTagCompound>
{
    protected TileEntity host;

    public EnergyAppliance(TileEntity host)
    {
        this.host = host;
    }

    public abstract void requestEnergy(int amount, @Nonnull IntConsumer callback);

    public final void requestEnergy(int amount)
    {
        requestEnergy(amount, this::onEnergyProvided);
    }

    protected void onEnergyProvided(int amount)
    {

    }

    public abstract void provideEnergy(int maxEnergy, @Nonnull IntConsumer callback);

    public final void provideEnergy(int maxEnergy)
    {
        provideEnergy(maxEnergy, this::onEnergyRequested);
    }

    protected void onEnergyRequested(int actualEnergy)
    {

    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound)
    {

    }
}
