package com.mike_caron.factorycraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class AnimatedMachineBase
    extends WeirdModelBlockBase
{

    public AnimatedMachineBase(Material material, String name)
    {
        super(material, name);
    }

    @Override
    protected void addAdditionalPropeties(List<IProperty<?>> properties)
    {
        super.addAdditionalPropeties(properties);
        properties.add(Properties.StaticProperty);
    }

    @Override
    protected void addAdditionalUnlistedProperties(List<IUnlistedProperty<?>> properties)
    {
        super.addAdditionalUnlistedProperties(properties);
        properties.add(Properties.AnimationProperty);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return super.getActualState(state, worldIn, pos).withProperty(Properties.StaticProperty, true);
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public abstract TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state);

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

}
