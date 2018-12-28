package com.mike_caron.factorycraft.energy;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    @Nullable
    public IItemHandler getInventory()
    {
        return null;
    }

    @Nonnull
    public NonNullList<ItemStack> getLimitedItems()
    {
        return NonNullList.create();
    }
}
