package com.mike_caron.factorycraft.block;

import com.mike_caron.factorycraft.tileentity.TileEntityElectricalPole;
import com.mike_caron.mikesmodslib.block.BlockBase;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockElectricalPole
    extends BlockBase
    implements com.mike_caron.mikesmodslib.integrations.ITOPInfoProvider
{
    private final int type;

    public BlockElectricalPole(String name, int type)
    {
        super(getMaterialForType(type), name);

        this.type = type;
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
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
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
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if(worldIn.isRemote)
            return;

        TileEntityElectricalPole te = (TileEntityElectricalPole)worldIn.getTileEntity(pos);
        te.connect();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        if(!worldIn.isRemote)
        {
            TileEntityElectricalPole te = (TileEntityElectricalPole)worldIn.getTileEntity(pos);
            te.disconnect();
        }
        super.breakBlock(worldIn, pos, state);

    }
}
