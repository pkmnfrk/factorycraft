package com.mike_caron.factorycraft.util;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class BlockPosNBTSerializer
    implements INBTSerializer<BlockPos>
{
    @Override
    public NBTBase serializeNBT(BlockPos obj)
    {
        NBTTagCompound ret = new NBTTagCompound();

        ret.setInteger("X", obj.getX());
        ret.setInteger("Y", obj.getY());
        ret.setInteger("Z", obj.getZ());

        return ret;
    }

    @Override
    public BlockPos deserializeNBT(NBTBase nbtBase)
    {
        NBTTagCompound nbt = (NBTTagCompound)nbtBase;
        int x = nbt.getInteger("X");
        int y = nbt.getInteger("Y");
        int z = nbt.getInteger("Z");

        return new BlockPos(x, y, z);
    }
}
