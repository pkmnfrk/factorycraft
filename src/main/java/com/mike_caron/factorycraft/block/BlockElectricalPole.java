package com.mike_caron.factorycraft.block;

import com.mike_caron.factorycraft.tileentity.TileEntityElectricalPole;
import com.mike_caron.mikesmodslib.block.BlockBase;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockElectricalPole
    extends BlockBase
    implements com.mike_caron.mikesmodslib.integrations.ITOPInfoProvider
{
    private final int type;
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.25, 0, 0.25, .75, 1, 0.75);

    public static final PropertyInteger PART = PropertyInteger.create("part", 0, 2);

    public BlockElectricalPole(String name, int type)
    {
        super(getMaterialForType(type), name);

        this.type = type;
    }

    @Override
    protected void addAdditionalPropeties(List<IProperty<?>> properties)
    {
        super.addAdditionalPropeties(properties);

        properties.add(PART);
    }

    @Override
    protected IBlockState addStateProperties(IBlockState blockState)
    {
        return super.addStateProperties(blockState)
            .withProperty(PART, 0);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(PART, meta & 3);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(PART);
    }

    private static Material getMaterialForType(int type)
    {
        switch(type)
        {
            case 0:
                return Material.WOOD;
            default:
                return Material.IRON;
        }
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
    public boolean isBlockNormalCube(IBlockState state)
    {
        return false;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
    {
        if(blockState.getValue(PART) == 0)
        {
            return super.getCollisionBoundingBox(blockState, worldIn, pos);
        }
        return null;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return BOUNDING_BOX;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return state.getValue(PART) == 2;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileEntityElectricalPole(type);
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, World world, IBlockState iBlockState, IProbeHitData iProbeHitData)
    {
        TileEntityElectricalPole te = (TileEntityElectricalPole)world.getTileEntity(iProbeHitData.getPos());

        if(te != null)
        {
            iProbeInfo.horizontal().text("Network: ").text(te.getConnector().getNetworkId().toString());
        }
    }

    @Override
    public boolean hasInfo(EntityPlayer player)
    {
        return true;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        if(!super.canPlaceBlockAt(worldIn, pos)) return false;

        //two 1s and a 2
        pos = pos.up();
        if(!canReplace(worldIn, pos))
            return false;
        pos = pos.up();
        if(!canReplace(worldIn, pos))
            return false;
        pos = pos.up();
        if(!canReplace(worldIn, pos))
            return false;

        return true;
    }

    private boolean canReplace(World world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().isAir(state, world, pos);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if(worldIn.isRemote)
            return;

        state = state.withProperty(PART, 1);
        pos = pos.up();
        worldIn.setBlockState(pos, state, 2);
        pos = pos.up();
        worldIn.setBlockState(pos, state, 2);
        state = state.withProperty(PART, 2);
        pos = pos.up();
        worldIn.setBlockState(pos, state, 2);

        TileEntityElectricalPole te = (TileEntityElectricalPole)worldIn.getTileEntity(pos);
        te.connect();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        if(!worldIn.isRemote)
        {
            //first, move to the top
            int failsafe = 0;
            while (state.getBlock() instanceof BlockElectricalPole && state.getValue(PART) != 2 && failsafe < 5)
            {
                pos = pos.up();
                state = worldIn.getBlockState(pos);
                failsafe++;
            }

            if(!(state.getBlock() instanceof BlockElectricalPole))
            {
                return;
            }

            TileEntityElectricalPole te = (TileEntityElectricalPole) worldIn.getTileEntity(pos);
            if (te == null)
            {
                return;
            }

            te.disconnect();

            for (int i = 0; i < 4; i++)
            {
                super.breakBlock(worldIn, pos, state);
                worldIn.destroyBlock(pos, i == 3);
                pos = pos.down();

            }
        }
    }
}
