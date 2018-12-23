package com.mike_caron.factorycraft.block;

import com.mike_caron.factorycraft.world.OreKind;
import com.mike_caron.mikesmodslib.block.BlockBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class BlockBoulder
    extends BlockBase
{
    public static final PropertyInteger SIZE = PropertyInteger.create("size", 0, 4);

    private OreKind oreKind;

    public BlockBoulder(Material material, String name, OreKind oreKind)
    {
        super(material, name);
        this.oreKind = oreKind;

        this.setHardness(8f);
        this.setResistance(10000f);

    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
    {
        return true;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isTopSolid(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isTranslucent(IBlockState state)
    {
        return true;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        switch(state.getValue(SIZE))
        {
            case 0:
                return new AxisAlignedBB(6.0 / 16, 0, 6.0 / 16, 9.0 / 16, 1.0 / 8.0, 9.0 / 16);
            case 1:
                return new AxisAlignedBB(1.0 / 16, 0, 6.0 / 16, 9.0 / 16, 1.0 / 8.0, 15.0 / 16);
            case 2:
            case 3:
                return new AxisAlignedBB(1.0 / 16, 0, 3.0 / 16, 14.0 / 16, 1.0 / 8.0, 15.0 / 16);
            default:
                return new AxisAlignedBB(1.0 / 16, 0, 2.0 / 16, 14.0 / 16, 1.0 / 8.0, 15.0 / 16);
        }

    }

    @Override
    protected IBlockState addStateProperties(IBlockState blockState)
    {
        return super.addStateProperties(blockState)
            .withProperty(SIZE, 4);
    }

    @Override
    protected void addAdditionalPropeties(List<IProperty<?>> properties)
    {
        super.addAdditionalPropeties(properties);

        properties.add(SIZE);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(SIZE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return blockState.getBaseState().withProperty(SIZE, meta);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {/*
        if(worldIn.isRemote) return true;

        int size = state.getValue(SIZE);

        size += 1;

        if(size > 4) size = 0;

        worldIn.setBlockState(pos, state.withProperty(SIZE, size));
*/
        return false;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        drops.add(oreKind.ore.copy());
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        super.onBlockHarvested(worldIn, pos, state, player);

        //worldIn.setBlockState(pos, state, 2);
    }

    @Override
    public void onPlayerDestroy(World worldIn, BlockPos pos, IBlockState state)
    {
        super.onPlayerDestroy(worldIn, pos, state);

        worldIn.setBlockState(pos, state, 2);
    }
}