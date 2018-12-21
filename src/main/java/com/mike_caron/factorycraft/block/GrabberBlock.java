package com.mike_caron.factorycraft.block;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.client.rendering.GrabberRenderer;
import com.mike_caron.factorycraft.tileentity.GrabberTileEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
}
