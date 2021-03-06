package com.mike_caron.factorycraft.block;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.client.gui.GuiConst;
import com.mike_caron.factorycraft.tileentity.TileEntityDrill;
import com.mike_caron.mikesmodslib.client.AnimationAdapter;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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

public class BlockDrill
    extends WeirdModelBlockBase
{
    private final int type;

    public BlockDrill(String name, int type)
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

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDrill.class, new AnimationAdapter<>());
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
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return super.getActualState(state, worldIn, pos).withProperty(Properties.StaticProperty, true);
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
        return new TileEntityDrill(type);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        if(!super.canPlaceBlockAt(worldIn, pos)) return false;

        IBlockState below = worldIn.getBlockState(pos.add(0, -1, 0));
        return below.getBlock() instanceof BlockBoulder;

    }

    @Override
    protected AxisAlignedBB getCachedBoundingBox()
    {
        return new AxisAlignedBB(0, 0, 0, 1, 0.5, 1);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(worldIn.isRemote)
        {
            return false;
        }

        TileEntityDrill te = (TileEntityDrill) worldIn.getTileEntity(pos);

        if(te == null)
            return false;

        playerIn.openGui(FactoryCraft.instance, GuiConst.GUI_DRILL, worldIn, pos.getX(), pos.getY(), pos.getZ());


        return true;
    }

    @Override
    protected void getExtraDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state)
    {
        TileEntityDrill te = (TileEntityDrill)world.getTileEntity(pos);

        if(te != null)
        {
            te.addItemsToDrop(drops);
        }
    }

}
