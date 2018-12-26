package com.mike_caron.factorycraft.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class NBTSerializableStorage<T extends INBTSerializable<N>, N extends NBTBase>
    implements Capability.IStorage<T>
{

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<T> capability, T t, EnumFacing enumFacing)
    {
        return t.serializeNBT();
    }

    @Override
    public void readNBT(Capability<T> capability, T t, EnumFacing enumFacing, NBTBase nbtBase)
    {
        t.deserializeNBT((N)nbtBase);
    }
}
