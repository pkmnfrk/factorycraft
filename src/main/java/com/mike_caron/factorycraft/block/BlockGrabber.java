package com.mike_caron.factorycraft.block;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.client.gui.GuiConst;
import com.mike_caron.factorycraft.client.rendering.GrabberRenderer;
import com.mike_caron.factorycraft.tileentity.TileEntityGrabber;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mike_caron.factorycraft.tileentity.TileEntityGrabber.TYPE_BURNER;

public class BlockGrabber
    extends WeirdModelBlockBase
{
    private final int type;

    private static final AxisAlignedBB collisionBoundingBox = new AxisAlignedBB(0, 0, 0, 1, 4/16f, 1);

    public BlockGrabber(String name, int type)
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

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGrabber.class, new GrabberRenderer());
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
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state)
    {
        return new TileEntityGrabber(type);
    }

    @Override
    public AxisAlignedBB getCachedBoundingBox()
    {
        return collisionBoundingBox;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(worldIn.isRemote) return false;

        TileEntityGrabber te = (TileEntityGrabber)worldIn.getTileEntity(pos);

        if(te == null)
            return false;

        if(te.getType() != 0)
            return false;

        playerIn.openGui(FactoryCraft.instance, GuiConst.GUI_GRABBER, worldIn, pos.getX(), pos.getY(), pos.getZ());

        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        if(type != TYPE_BURNER) return;

        TileEntityGrabber te = (TileEntityGrabber)worldIn.getTileEntity(pos);

        if(te.getState() != TileEntityGrabber.State.GRABBING && te.getState() != TileEntityGrabber.State.RETURNING)
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
    protected void getExtraDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state)
    {
        TileEntityGrabber te = (TileEntityGrabber)world.getTileEntity(pos);

        if(te != null)
        {
            te.addItemsToDrop(drops);
        }
    }
}
