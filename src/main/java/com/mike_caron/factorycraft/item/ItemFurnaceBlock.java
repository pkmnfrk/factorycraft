package com.mike_caron.factorycraft.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemFurnaceBlock
    extends ItemBlock
{
    public ItemFurnaceBlock(Block block)
    {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack)
    {
        Block block = worldIn.getBlockState(pos).getBlock();
        if (block == Blocks.SNOW_LAYER && block.isReplaceable(worldIn, pos)) {
            side = EnumFacing.UP;
        } else if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(side);
        }

        if(!worldIn.mayPlace(this.block, pos, false, side, null))
        {
            return false;
        }

        //also need to check the block to the right.
        EnumFacing newFacing = player.getHorizontalFacing().rotateY();
        pos = pos.offset(newFacing);
        if(!worldIn.mayPlace(this.block, pos, false, side, null))
        {
            //maybe to the left???
            newFacing = newFacing.getOpposite();
            pos = pos.offset(newFacing, 2);

            if(!worldIn.mayPlace(this.block, pos, false, side, null))
            {
                return false;
            }
        }

        return true;
    }
}
