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
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @SideOnly(Side.CLIENT)
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
        if(turn == EnumTurn.Down)
        {
            ret += 4;
        }
        else if(turn == EnumTurn.Up)
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
            ret = ret.withProperty(TURN, EnumTurn.Down);
        }
        else if(meta == 8)
        {
            ret = ret.withProperty(TURN, EnumTurn.Up);
        }

        return ret;
    }

    private static Stream<BlockPos> findSpacesBehindUs(BlockPos pos, EnumFacing facing, EnumTurn turn)
    {
        BlockPos ret;
        switch(turn)
        {
            case Left:
                return Stream.of(
                    pos.offset(facing.rotateY()),
                    pos.offset(facing.rotateY()).offset(EnumFacing.DOWN)
                );
            case Right:
                return Stream.of(
                    pos.offset(facing.rotateYCCW()),
                    pos.offset(facing.rotateYCCW()).offset(EnumFacing.DOWN)
                );
            case Up:
                return Stream.of(
                    pos.offset(facing.getOpposite()),
                    pos.offset(facing.getOpposite()).offset(EnumFacing.UP)
                );
            case Down:
                return Stream.of(
                    pos.offset(facing.getOpposite()),
                    pos.offset(facing.getOpposite()).offset(EnumFacing.DOWN)
                );
            case Straight:
                return Stream.of(
                    pos.offset(facing.getOpposite()),
                    pos.offset(facing.getOpposite()).offset(EnumFacing.DOWN)
                );
        }

        throw new Error("Impossible");
    }

    private static boolean shouldWeFaceDown(IBlockAccess world, BlockPos pos, EnumFacing facing)
    {
        //if there is a conveyor above and in front of us, but not directly in front of us,
        //then yes.
        BlockPos otherPos = pos.offset(facing);
        IBlockState other = world.getBlockState(otherPos);

        if(other.getBlock() instanceof BlockConveyor)
        {
            return false;
        }

        otherPos = otherPos.offset(EnumFacing.UP);
        other = world.getBlockState(otherPos);

        if(other.getBlock() instanceof BlockConveyor)
        {
            return true;
        }

        return false;
    }

    private static boolean shouldWeFaceUp(IBlockAccess world, BlockPos pos, EnumFacing facing)
    {
        //if there is a conveyor above and behind us, and facing us,
        // but not directly behind us (or they're not facing us), then yes.

        BlockPos otherPos = pos.offset(facing.getOpposite());
        IBlockState other = world.getBlockState(otherPos);

        if(other.getBlock() instanceof BlockConveyor)
        {
            if(otherPos.offset(other.getValue(FACING)).equals(pos))
            {
                return false;
            }
        }

        otherPos = otherPos.offset(EnumFacing.UP);
        other = world.getBlockState(otherPos);

        if(other.getBlock() instanceof BlockConveyor)
        {
            return otherPos.offset(other.getValue(FACING)).offset(EnumFacing.DOWN).equals(pos);
        }

        return false;
    }

    private static boolean isFacingMe(IBlockAccess worldIn, BlockPos pos, EnumFacing facing, EnumTurn turn)
    {
        List<BlockPos> otherBlocks = findSpacesBehindUs(pos, facing, turn).collect(Collectors.toList());

        for(BlockPos other : otherBlocks)
        {
            IBlockState ret = worldIn.getBlockState(other);

            if (!(ret.getBlock() instanceof BlockConveyor))
                continue;

            if(ret.getValue(TURN) == EnumTurn.Down && other.getY() == pos.getY())
                return false;

            other = other.add(0, pos.getY() - other.getY(), 0);

            return other.offset(ret.getValue(FACING)).equals(pos);
        }

        return false;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing()), 2);

        state = worldIn.getBlockState(pos);
        ensureOrientedCorrectly(worldIn, pos, state);
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
    {
        boolean ret = super.rotateBlock(world, pos, axis);

        ensureOrientedCorrectly(world, pos, world.getBlockState(pos));

        return ret;
    }

    @Override
    protected void getExtraDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state)
    {
        TileEntityConveyor te = (TileEntityConveyor)world.getTileEntity(pos);

        if(te != null)
        {
            te.addItemsToDrop(drops);
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        EnumFacing facing = state.getValue(FACING);
        BlockPos otherPos = pos.offset(facing);
        if(state.getValue(TURN) == EnumTurn.Down)
        {
            otherPos = otherPos.offset(EnumFacing.UP);
        }

        IBlockState otherState = worldIn.getBlockState(otherPos);
        if(otherState.getBlock() instanceof BlockConveyor)
        {
            IBlockState newState = calculateCorrectBlockState(worldIn, otherPos, otherState);
            if(newState != null)
            {
                worldIn.setBlockState(otherPos, newState, 2);
            }
        }

        super.breakBlock(worldIn, pos, state);
    }

    private void ensureOrientedCorrectly(World worldIn, BlockPos pos, IBlockState state)
    {
        IBlockState newState = calculateCorrectBlockState(worldIn, pos, state);
        EnumTurn myTurn = newState.getValue(TURN);
        if(newState != state)
        {
            worldIn.setBlockState(pos, newState, 2);
        }

        //we also need to ensure the block we're now facing is correct
        EnumFacing facing = newState.getValue(FACING);
        pos = pos.offset(facing);
        if(myTurn == EnumTurn.Down)
        {
            pos = pos.offset(EnumFacing.UP);
        }
        state = worldIn.getBlockState(pos);
        if(state.getBlock() instanceof BlockConveyor)
        {
            newState = calculateCorrectBlockState(worldIn, pos, state);
            if(newState != null)
            {
                worldIn.setBlockState(pos, newState, 2);
            }
        }
        else if(myTurn != EnumTurn.Down)
        {
            //maybe they're below us
            pos = pos.offset(EnumFacing.DOWN);
            state = worldIn.getBlockState(pos);
            if(state.getBlock() instanceof BlockConveyor)
            {
                newState = calculateCorrectBlockState(worldIn, pos, state);
                if(newState != null)
                {
                    worldIn.setBlockState(pos, newState, 2);
                }
            }
        }
    }

    @Override
    @Deprecated
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntity teP = worldIn.getTileEntity(pos);
        if(!(teP instanceof TileEntityConveyor))
        {
            return state;
        }

        return state.withProperty(TURN, ((TileEntityConveyor) teP).getTurn());
    }

    public static IBlockState calculateCorrectBlockState(IBlockAccess worldIn, BlockPos pos, IBlockState state)
    {
        /*
        RULES:
          1. Straight connections take priority
         1a. A level connection takes priority over an up/down one
             (this means you can stack conveyors on top of eachother,
             even though that doesn't make much sense)
          2. If left and right both connect, we are straight
          3. If left xor right connect, we turn in that direction
          4. We are straight
         */
        EnumFacing facing = state.getValue(FACING);

        IBlockState newState = state.withProperty(TURN, EnumTurn.Straight);

        boolean left =   isFacingMe(worldIn, pos, facing, EnumTurn.Left);
        boolean right =  isFacingMe(worldIn, pos, facing, EnumTurn.Right);
        boolean behind = isFacingMe(worldIn, pos, facing, EnumTurn.Straight);
        boolean shouldFaceUp = shouldWeFaceUp(worldIn, pos, facing);
        boolean shouldFaceDown = shouldWeFaceDown(worldIn, pos, facing);

        // Rule 1: Straight connections take priority
        if(behind || shouldFaceUp || shouldFaceDown)
        {
            //1a. A level connection takes priority over an up/down one
            // (the should* functions take this into account)
            if(shouldFaceUp)
            {
                newState = newState.withProperty(TURN, EnumTurn.Up);
            }
            else if (shouldFaceDown)
            {
                newState = newState.withProperty(TURN, EnumTurn.Down);
            }
            else
            {
                newState = newState.withProperty(TURN, EnumTurn.Straight);
            }
        }
        // Rule 2: If left and right both connect, we are straight
        else if(left && right)
        {
            newState = newState.withProperty(TURN, EnumTurn.Straight);
        }
        // Rule 3: If left xor right connect, we turn in that direction
        else if(left ^ right)
        {
            if(left)
            {
                newState = newState.withProperty(TURN, EnumTurn.Left);
            }
            else
            {
                newState = newState.withProperty(TURN, EnumTurn.Right);
            }
        }
        // Rule 4: We are straight
        else
        {
            newState = newState.withProperty(TURN, EnumTurn.Straight);
        }

        return newState;
    }

    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        EnumTurn t = getActualState(state, source, pos).getValue(TURN);

        if(t == EnumTurn.Up || t == EnumTurn.Down)
        {
            return FULL_BLOCK_AABB;
        }
        return collisionBoundingBox;
    }

    @Override
    @Deprecated
    public boolean isFullBlock(IBlockState state)
    {
        return false;
    }

    @Override
    @Deprecated
    public boolean isNormalCube(IBlockState state)
    {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    @Deprecated
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
        Straight,
        Up,
        Down;

        @Override
        @Nonnull
        public String getName()
        {
            return this.toString().substring(0, 1).toLowerCase();
        }
    }

    public enum Type
    {
        Slow,
        Medium,
        Fast,
    }
}
