package com.mike_caron.factorycraft.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldTileEntityFinder
    implements ITileEntityFinder
{
    private final World world;

    public WorldTileEntityFinder(World world)
    {
        this.world = world;
    }

    @Override
    public TileEntity getTileEntityAt(BlockPos pos)
    {
        return world.getTileEntity(pos);
    }
}
