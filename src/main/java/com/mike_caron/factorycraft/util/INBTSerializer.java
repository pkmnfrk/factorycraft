package com.mike_caron.factorycraft.util;

import net.minecraft.nbt.NBTBase;

public interface INBTSerializer<T>
{
    NBTBase serializeNBT(T obj);
    T deserializeNBT(NBTBase nbt);
}
