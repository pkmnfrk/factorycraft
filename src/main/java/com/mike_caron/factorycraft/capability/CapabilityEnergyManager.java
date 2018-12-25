package com.mike_caron.factorycraft.capability;

import com.mike_caron.factorycraft.api.IOreDeposit;
import com.mike_caron.factorycraft.energy.IEnergyManager;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityEnergyManager
        implements ICapabilitySerializable<NBTBase>
{
    @CapabilityInject(IEnergyManager.class)
    public static Capability<IEnergyManager> ENERGYMANAGER;

    private IEnergyManager instance = ENERGYMANAGER.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing enumFacing)
    {
        return capability == ENERGYMANAGER;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing enumFacing)
    {
        if (capability == ENERGYMANAGER)
        {
            return ENERGYMANAGER.cast(instance);
        }
        return null;
    }


    @Override
    public NBTBase serializeNBT()
    {
        return ENERGYMANAGER.getStorage().writeNBT(ENERGYMANAGER, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbtBase)
    {
        ENERGYMANAGER.getStorage().readNBT(ENERGYMANAGER, instance, null, nbtBase);
    }
}
