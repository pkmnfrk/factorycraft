package com.mike_caron.factorycraft.item;

import com.mike_caron.factorycraft.block.BlockFurnace;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.stream.Collectors;

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

        EnumFacing blockFacing = player.getHorizontalFacing().getOpposite();

        for(int part = 0; part < 4; part++)
        {
            List<BlockPos> parts = BlockFurnace.getOtherBlocks(pos, blockFacing, 0).collect(Collectors.toList());

            boolean allGood = true;

            for(int i = 0; i < parts.size(); i++)
            {
                BlockPos p = parts.get(i);
                IBlockState blockState = worldIn.getBlockState(p);
                if(!worldIn.mayPlace(this.block, p, false, side, null))
                {
                    allGood = false;
                    break;
                }
            }

            if(!allGood)
                continue;

            return true;
        }

        return false;
    }
}
