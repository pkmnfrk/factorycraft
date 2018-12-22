package com.mike_caron.factorycraft.capability;

import com.mike_caron.factorycraft.api.IConveyorBelt;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityConveyor
    implements ICapabilityProvider
{
    @CapabilityInject(IConveyorBelt.class)
    public static Capability<IConveyorBelt> CONVEYOR;

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
