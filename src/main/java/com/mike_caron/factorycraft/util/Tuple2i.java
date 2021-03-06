package com.mike_caron.factorycraft.util;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;

public class Tuple2i
{
    public final int x;
    public final int z;

    public Tuple2i(int x, int z)
    {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Tuple2i tuple2i = (Tuple2i) o;
        return x == tuple2i.x &&
            z == tuple2i.z;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(x, z);
    }

    public static class Serializer
        implements  INBTSerializer<Tuple2i>
    {
        @Override
        public NBTBase serializeNBT(Tuple2i obj)
        {
            NBTTagCompound ret = new NBTTagCompound();

            ret.setInteger("x", obj.x);
            ret.setInteger("z", obj.z);

            return ret;
        }

        @Override
        public Tuple2i deserializeNBT(NBTBase nbtBase)
        {
            NBTTagCompound nbt = (NBTTagCompound)nbtBase;
            return new Tuple2i(nbt.getInteger("x"), nbt.getInteger("z"));
        }
    }
}
