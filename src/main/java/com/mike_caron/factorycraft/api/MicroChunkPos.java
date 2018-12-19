package com.mike_caron.factorycraft.api;

import com.google.common.base.Preconditions;

import java.util.Objects;

public class MicroChunkPos
{
    public final int dimension;
    public final int chunkX, chunkZ;
    public final int subX, subZ;

    public MicroChunkPos(int dim, int chunkX, int chunkZ, int subX, int subZ)
    {
        Preconditions.checkArgument(subX >= 0 && subX <= 3, "0 <= subX <= 3");
        Preconditions.checkArgument(subZ >= 0 && subZ <= 3, "0 <= subZ <= 3");

        this.dimension = dim;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.subX = subX;
        this.subZ = subZ;
    }

    public MicroChunkPos(int dim, int x, int z)
    {
        this.dimension = dim;
        this.chunkX = (x >> 2);
        this.chunkZ = (z >> 2);
        this.subX = x & 3;
        this.subZ = z & 3;
    }

    public int getX()
    {
        return (this.chunkX << 2) + this.subX;
    }

    public int getZ()
    {
        return (this.chunkZ << 2) + this.subZ;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MicroChunkPos that = (MicroChunkPos) o;
        return dimension == that.dimension &&
            chunkX == that.chunkX &&
            chunkZ == that.chunkZ &&
            subX == that.subX &&
            subZ == that.subZ;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(dimension, chunkX, chunkZ, subX, subZ);
    }
}
