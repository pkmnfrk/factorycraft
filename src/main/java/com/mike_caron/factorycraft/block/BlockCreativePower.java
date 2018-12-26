package com.mike_caron.factorycraft.block;

import com.mike_caron.factorycraft.tileentity.TileEntityCreativePower;
import com.mike_caron.mikesmodslib.block.BlockBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockCreativePower
    extends BlockBase
{
    public BlockCreativePower(String name)
    {
        super(Material.DRAGON_EGG, name);
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileEntityCreativePower();
    }
}
