package com.mike_caron.factorycraft.util;

import net.minecraft.nbt.NBTBase;

public interface INBTSerializer<T, N extends NBTBase>
{
    N serializeNBT(T obj);
    T deserializeNBT(N nbt);
}
