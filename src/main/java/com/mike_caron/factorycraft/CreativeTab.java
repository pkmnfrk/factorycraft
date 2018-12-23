package com.mike_caron.factorycraft;

import com.mike_caron.factorycraft.block.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class CreativeTab
    extends CreativeTabs
{
    public CreativeTab()
    {
        super(FactoryCraft.modId);
    }

    @Override
    @Nonnull
    public ItemStack createIcon()
    {
        return new ItemStack(ModBlocks.boulder_iron);
    }
}
