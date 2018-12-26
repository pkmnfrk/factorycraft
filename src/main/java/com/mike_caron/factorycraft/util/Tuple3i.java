package com.mike_caron.factorycraft.util;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;

public class Tuple3i
{
    public final int x;
    public final int y;
    public final int z;

    public Tuple3i(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Tuple3i tuple3i = (Tuple3i) o;
        return x == tuple3i.x &&
               y == tuple3i.y &&
            z == tuple3i.z;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    public static class Serializer
        implements  INBTSerializer<Tuple3i>
    {
        @Override
        public NBTBase serializeNBT(Tuple3i obj)
        {
            NBTTagCompound ret = new NBTTagCompound();

            ret.setInteger("x", obj.x);
            ret.setInteger("y", obj.z);
            ret.setInteger("z", obj.z);

            return ret;
        }

        @Override
        public Tuple3i deserializeNBT(NBTBase nbtBase)
        {
            NBTTagCompound nbt = (NBTTagCompound)nbtBase;
            return new Tuple3i(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
        }
    }
}
