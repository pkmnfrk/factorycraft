package com.mike_caron.factorycraft.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public interface ITileEntityFinder
{
    TileEntity getTileEntityAt(BlockPos pos);
}
