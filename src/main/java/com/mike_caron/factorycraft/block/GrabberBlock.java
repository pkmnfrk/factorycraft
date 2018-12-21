package com.mike_caron.factorycraft.block;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.client.rendering.GrabberRenderer;
import com.mike_caron.factorycraft.tileentity.GrabberTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class GrabberBlock
    extends AnimatedMachineBase
{
    private final int type;

    private static final AxisAlignedBB collisionBoundingBox = new AxisAlignedBB(0, 0, 0, 1, 4/16f, 1);

    public GrabberBlock(String name, int type)
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

        ClientRegistry.bindTileEntitySpecialRenderer(GrabberTileEntity.class, new GrabberRenderer());
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state)
    {
        return new GrabberTileEntity(type);
    }

    @Override
    public AxisAlignedBB getCachedBoundingBox()
    {
        return collisionBoundingBox;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(worldIn.isRemote)
        {
            GrabberTileEntity te = (GrabberTileEntity)worldIn.getTileEntity(pos);
            te.loadAsm();
            return true;
        }

        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        if(type != 0) return;

        GrabberTileEntity te = (GrabberTileEntity)worldIn.getTileEntity(pos);

        if(te.getState() != GrabberTileEntity.State.GRABBING && te.getState() != GrabberTileEntity.State.RETURNING)
            return;

        for(int i = 0; i < 1; i++)
        {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1/16f;
            double z = pos.getZ() + 0.5;
            double vx = (rand.nextFloat() - 0.5) * 0.1;
            double vy = 0.1;
            double vz = (rand.nextFloat() - 0.5) * 0.1;

            worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, vx, vy, vz);
        }
    }

    @Override
    public boolean hasInfo(EntityPlayer player)
    {
        return true;
    }

    @Override
    protected void addBlockProbeInfo(ProbeMode mode, IProbeInfo info, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data)
    {
        GrabberTileEntity te = (GrabberTileEntity)world.getTileEntity(data.getPos());

        info
            .text(te.getState().toString());
        if(type == 0)
        {
            info.horizontal().text("Fuel").text(Integer.toString(te.getFuelTicks()));
        }
    }
}
