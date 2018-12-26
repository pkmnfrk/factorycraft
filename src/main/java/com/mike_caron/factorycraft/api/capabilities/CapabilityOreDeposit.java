package com.mike_caron.factorycraft.api.capabilities;

import com.mike_caron.factorycraft.api.IOreDeposit;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityOreDeposit
    implements ICapabilitySerializable<NBTBase>
{
    @CapabilityInject(IOreDeposit.class)
    public static Capability<IOreDeposit> OREDEPOSIT;

    private IOreDeposit instance = OREDEPOSIT.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing enumFacing)
    {
        return capability == OREDEPOSIT;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing enumFacing)
    {
        if(capability == OREDEPOSIT)
        {
            return OREDEPOSIT.cast(instance);
        }

        return null;
    }

    @Override
    public NBTBase serializeNBT()
    {
        return OREDEPOSIT.getStorage().writeNBT(OREDEPOSIT, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbtBase)
    {
        OREDEPOSIT.getStorage().readNBT(OREDEPOSIT, instance, null, nbtBase);
    }
}
