package com.mike_caron.factorycraft.block;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.client.gui.GuiConst;
import com.mike_caron.factorycraft.tileentity.TileEntityFurnace;
import com.mike_caron.factorycraft.tileentity.TileEntityRedirect;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockFurnace
    extends WeirdModelBlockBase
{
    private int type;

    private static final PropertyInteger PART = PropertyInteger.create("part", 0, 1);
    private static final PropertyBool ACTIVE = PropertyBool.create("active");

    public BlockFurnace(String name, int type)
    {
        super(Material.IRON, name);

        this.type = type;
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
        switch(state.getValue(PART))
        {
            case 0:
                return new TileEntityFurnace(type);
            case 1:
                return new TileEntityRedirect(state.getValue(FACING).rotateY());
        }
        return null;
    }

    @Override
    protected IBlockState addStateProperties(IBlockState blockState)
    {
        return super.addStateProperties(blockState)
            .withProperty(PART, 0)
            .withProperty(ACTIVE, false);
    }

    @Override
    protected void addAdditionalPropeties(List<IProperty<?>> properties)
    {
        super.addAdditionalPropeties(properties);

        properties.add(PART);
        properties.add(ACTIVE);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        int ret = super.getMetaFromState(state);

        ret += state.getValue(PART) << 2;

        return ret;
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
    {
        return false;
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        IBlockState ret = super.getStateFromMeta(meta);

        int part = (meta >> 2) & 1;

        ret = ret.withProperty(PART, part);

        return ret;
    }

    @Override
    protected AxisAlignedBB getCachedBoundingBox()
    {
        return FULL_BLOCK_AABB;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        if(worldIn.isRemote)
            return;

        EnumFacing blockFacing = placer.getHorizontalFacing().getOpposite();

        state = state.withProperty(FACING, blockFacing).withProperty(PART, 0);

        BlockPos other = getOtherBlock(pos, state);

        IBlockState otherState = worldIn.getBlockState(other);

        if(!otherState.getBlock().isReplaceable(worldIn, other))
        {
            // whoops, let's do it the other way
            state = state.withProperty(PART, 1);

            other = getOtherBlock(pos, state);
            otherState = worldIn.getBlockState(other);

            if(!otherState.getBlock().isReplaceable(worldIn, other))
            {
                //... ItemFurnaceBlock! you were supposed to catch this!
                worldIn.destroyBlock(pos, true);
                return;
            }
        }

        worldIn.setBlockState(pos, state, 2);

        state = state
            .withProperty(PART, 1 - state.getValue(PART));

        worldIn.setBlockState(other, state, 2);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        super.breakBlock(worldIn, pos, state);

        BlockPos other = getOtherBlock(pos, state);

        worldIn.destroyBlock(other, false);
    }

    private BlockPos getOtherBlock(BlockPos pos, IBlockState state)
    {
        EnumFacing facing = state.getValue(FACING);
        int part = state.getValue(PART);

        switch(part)
        {
            case 0:
                return pos.offset(facing.rotateYCCW());
            case 1:
                return pos.offset(facing.rotateY());
        }
        return pos;
    }

    private TileEntityFurnace getTileEntity(IBlockAccess world, BlockPos pos)
    {
        TileEntity ret;
        if(world instanceof ChunkCache)
        {
            ret = ((ChunkCache)world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
        }
        else
        {
            ret = world.getTileEntity(pos);
        }

        while(ret instanceof TileEntityRedirect)
        {
            pos = ((TileEntityRedirect) ret).getRealTileEntityPos();

            if(world instanceof ChunkCache)
            {
                ret = ((ChunkCache)world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
            }
            else
            {
                ret = world.getTileEntity(pos);
            }
        }

        return (TileEntityFurnace)ret;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(worldIn.isRemote)
        {
            return false;
        }

        TileEntityFurnace te = getTileEntity(worldIn, pos);

        if(te == null)
            return false;

        playerIn.openGui(FactoryCraft.instance, GuiConst.GUI_FURNACE, worldIn, te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());


        return true;
    }

    @Override
    protected void getExtraDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state)
    {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityFurnace)
        {
            ((TileEntityFurnace) te).addItemsToDrop(drops);
        }
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        if(type == 2)
            return;

        if(stateIn.getValue(PART) != 1)
            return;

        TileEntityFurnace te = getTileEntity(worldIn, pos);

        if(!te.getIsActive())
            return;

        for(int i = 0; i < 2; i++)
        {
            double x = pos.getX() + 9/16f;
            double y = pos.getY() + 25/16f;
            double z = pos.getZ() + 9/16f;
            double vx = (rand.nextFloat() - 0.5) * 0.1;
            double vy = 0.1;
            double vz = (rand.nextFloat() - 0.5) * 0.1;

            worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, vx, vy, vz);
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntityFurnace te = getTileEntity(worldIn, pos);

        state = state.withProperty(ACTIVE, te.getIsActive());

        return state;
    }
}
