package com.mike_caron.factorycraft.item;

import com.mike_caron.factorycraft.block.BlockConveyor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemConveyorBlock
    extends ItemBlock
{
    public ItemConveyorBlock(Block block)
    {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack)
    {
        if(side == EnumFacing.UP && !player.isSneaking())
        {
            IBlockState state = worldIn.getBlockState(pos);
            if (state.getBlock() instanceof BlockConveyor)
            {
                return false;
            }
        }
        return super.canPlaceBlockOnSide(worldIn, pos, side, player, stack);
    }
}
