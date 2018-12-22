package com.mike_caron.factorycraft.block;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.client.rendering.ConveyorRenderer;
import com.mike_caron.factorycraft.tileentity.TileEntityConveyor;
import com.mike_caron.factorycraft.util.Tuple2;
import com.mike_caron.mikesmodslib.block.MachineBlockBase;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockConveyor
    extends MachineBlockBase
{
    private final int type;

    private static final AxisAlignedBB collisionBoundingBox = new AxisAlignedBB(0, 0, 0, 1, 2/16f, 1);

    public static final PropertyEnum<EnumTurn> TURN = PropertyEnum.create("turn", EnumTurn.class);

    public BlockConveyor(String name, int type)
    {
        super(Material.IRON, name);
        setHardness(5f);
        setResistance(20f);
        setCreativeTab(FactoryCraft.creativeTab);

        this.type = type;
    }

    @Override
    public void initModel()
    {
        super.initModel();

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConveyor.class, new ConveyorRenderer());
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state)
    {
       return new TileEntityConveyor(type);
    }

    @Override
    protected void addAdditionalPropeties(List<IProperty<?>> properties)
    {
        super.addAdditionalPropeties(properties);
        properties.add(TURN);
    }

    @Override
    protected IBlockState addStateProperties(IBlockState blockState)
    {
        return super.addStateProperties(blockState).withProperty(TURN, EnumTurn.Straight);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        int ret = super.getMetaFromState(state);

        EnumTurn turn = state.getValue(TURN);
        if(turn == EnumTurn.Left)
        {
            ret += 4;
        }
        else if(turn == EnumTurn.Right)
        {
            ret += 8;
        }

        return ret;
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        IBlockState ret = super.getStateFromMeta(meta);

        meta = meta & 0xC;

        if(meta == 0)
        {
            ret = ret.withProperty(TURN, EnumTurn.Straight);
        }
        else if(meta == 4)
        {
            ret = ret.withProperty(TURN, EnumTurn.Left);
        }
        else if(meta == 8)
        {
            ret = ret.withProperty(TURN, EnumTurn.Right);
        }

        return ret;
    }

    private BlockPos turn(BlockPos pos, EnumFacing facing, EnumTurn turn)
    {
        switch(turn)
        {
            case Left:
                return pos.offset(facing.rotateY());
            case Right:
                return pos.offset(facing.rotateYCCW());
            case Straight:
                return pos.offset(facing);
        }

        throw new Error("Impossible");
    }

    @Nullable
    private EnumFacing getFacingOf(World worldIn, BlockPos pos, EnumFacing facing, EnumTurn turn)
    {
        IBlockState ret = worldIn.getBlockState(turn(pos, facing, turn));

        if(!(ret.getBlock() instanceof BlockConveyor))
            return null;

        return ret.getValue(FACING);
    }

    private boolean isFacingMe(World worldIn, BlockPos pos, EnumFacing facing, EnumTurn turn)
    {
        BlockPos other = turn(pos, facing, turn);

        IBlockState ret = worldIn.getBlockState(other);

        if(!(ret.getBlock() instanceof BlockConveyor))
            return false;

        return other.offset(ret.getValue(FACING)).equals(pos);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        state = worldIn.getBlockState(pos);
        ensureOrientedCorrectly(worldIn, pos, state);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        super.onBlockAdded(worldIn, pos, state);
        //state = worldIn.getBlockState(pos);
        //ensureOrientedCorrectly(worldIn, pos, state);
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
    {
        boolean ret = super.rotateBlock(world, pos, axis);

        ensureOrientedCorrectly(world, pos, world.getBlockState(pos));

        return ret;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        EnumFacing facing = state.getValue(FACING);
        BlockPos otherPos = pos.offset(facing);
        IBlockState otherState = worldIn.getBlockState(otherPos);
        if(otherState.getBlock() instanceof BlockConveyor)
        {
            facing = otherState.getValue(FACING);

            IBlockState newState = calculateCorrectBlockState(worldIn, otherPos, otherState, facing);
            if(newState != null)
            {
                worldIn.setBlockState(otherPos, newState, 2);
                ((TileEntityConveyor)worldIn.getTileEntity(otherPos)).notifyChange();
            }
        }

        super.breakBlock(worldIn, pos, state);
    }

    private void ensureOrientedCorrectly(World worldIn, BlockPos pos, IBlockState state)
    {
        EnumFacing facing = state.getValue(FACING);

        IBlockState newState = calculateCorrectBlockState(worldIn, pos, state, facing);
        if(newState != state)
        {
            worldIn.setBlockState(pos, newState, 2);
            ((TileEntityConveyor)worldIn.getTileEntity(pos)).notifyChange();
        }

        //we also need to ensure the block we're now facing is correct

        pos = pos.offset(facing);
        state = worldIn.getBlockState(pos);
        if(state.getBlock() instanceof BlockConveyor)
        {
            facing = state.getValue(FACING);

            newState = calculateCorrectBlockState(worldIn, pos, state, facing);
            if(newState != null)
            {
                worldIn.setBlockState(pos, newState, 2);
                ((TileEntityConveyor)worldIn.getTileEntity(pos)).notifyChange();
            }
        }
    }

    private IBlockState calculateCorrectBlockState(World worldIn, BlockPos pos, IBlockState state, EnumFacing facing)
    {
        IBlockState newState = state.withProperty(FACING, facing).withProperty(TURN, EnumTurn.Straight);

        boolean left =     isFacingMe(worldIn, pos, facing, EnumTurn.Left);
        boolean right =    isFacingMe(worldIn, pos, facing, EnumTurn.Right);
        boolean behind =   isFacingMe(worldIn, pos, facing.getOpposite(), EnumTurn.Straight);

        // if any of straight, left or right (in that order) are facing me, I want to re-orient myself to account for that
        // on the other hand, if both left and right are facing me, I want to ignore them both

        if(!behind && (left || right)) {
            if(left)
            {
                newState = newState.withProperty(TURN, EnumTurn.Left);
            }
            else
            {
                newState = newState.withProperty(TURN, EnumTurn.Right);
            }
        }

        return newState;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return collisionBoundingBox;
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
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }

    @Override
    protected void addBlockProbeInfo(ProbeMode mode, IProbeInfo info, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data)
    {
        TileEntityConveyor te = (TileEntityConveyor)world.getTileEntity(data.getPos());

        for(TileEntityConveyor.Track t : te.tracks)
        {
            IProbeInfo in = info.vertical();

            for(Tuple2<Float, ItemStack> item : t.getItems())
            {
                in = in.horizontal().item(item.second).text(Float.toString(item.first));
            }
        }
    }

    public enum EnumTurn
        implements IStringSerializable
    {
        Left,
        Right,
        Straight;

        @Override
        @Nonnull
        public String getName()
        {
            return this.toString().substring(0, 1).toLowerCase();
        }
    }
}
