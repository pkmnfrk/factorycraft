package com.mike_caron.factorycraft.capability;

import com.mike_caron.factorycraft.energy.IEnergyConnector;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityEnergyConnector
    implements ICapabilityProvider
{
    @CapabilityInject(IEnergyConnector.class)
    public static Capability<IEnergyConnector> ENERGY_CONNECTOR;

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing enumFacing)
    {
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing enumFacing)
    {
        return null;
    }
}
