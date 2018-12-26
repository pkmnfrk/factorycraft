package com.mike_caron.factorycraft.block;

import com.mike_caron.factorycraft.api.IOreDeposit;
import com.mike_caron.factorycraft.api.capabilities.CapabilityOreDeposit;
import com.mike_caron.factorycraft.world.OreDeposit;
import com.mike_caron.factorycraft.world.OreKind;
import com.mike_caron.mikesmodslib.block.BlockBase;
import com.mike_caron.mikesmodslib.integrations.ITOPInfoProvider;
import com.mike_caron.mikesmodslib.util.ItemUtils;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBoulder
    extends BlockBase
    implements ITOPInfoProvider
{
    private OreKind oreKind;
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(1.0 / 16, 0, 1.0 / 16, 15.0 / 16, 2.0 / 8.0, 15.0 / 16);

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
        return BOUNDING_BOX;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {

    }

    @Override
    public void onPlayerDestroy(World worldIn, BlockPos pos, IBlockState state)
    {
        OreDeposit oreDeposit = getOreDeposit(worldIn, pos);
        if(oreDeposit != null)
        {
            if(oreDeposit.getSize() > 0)
            {
                worldIn.setBlockState(pos, state, 2);
            }
        }
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        if(world.isRemote)
            return;

        if(!player.isCreative())
        {
            //TODO: deduct the ore from the world
            OreDeposit oreDeposit = getOreDeposit(world, pos);

            if(oreDeposit != null)
            {
                if(oreDeposit.mineOne())
                {
                    ItemUtils.dropItem(world, oreDeposit.getOreKind().ore.copy(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                }
            }

        }
        else
        {
            world.setBlockToAir(pos);
        }
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, World world, IBlockState iBlockState, IProbeHitData iProbeHitData)
    {
        OreDeposit oreDeposit = getOreDeposit(world, iProbeHitData.getPos());

        if(oreDeposit != null)
        {
            iProbeInfo.horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER)).item(oreDeposit.getOreKind().ore).text(Long.toString(oreDeposit.getSize()));
        }

    }

    private OreDeposit getOreDeposit(World world, BlockPos pos)
    {
        IOreDeposit deposit = world.getChunk(pos).getCapability(CapabilityOreDeposit.OREDEPOSIT, null);

        if(deposit != null)
        {
            int sx = (pos.getX() & 12) >> 2;
            int sz = (pos.getZ() & 12) >> 2;
            return deposit.getOreDeposit(sx, sz);
        }

        return null;
    }

    @Override
    public boolean hasInfo(EntityPlayer player)
    {
        return true;
    }
}
