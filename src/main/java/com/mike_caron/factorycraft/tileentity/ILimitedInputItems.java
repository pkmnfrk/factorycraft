package com.mike_caron.factorycraft.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface ILimitedInputItems
{
    NonNullList<ItemStack> getLimitedItems();
}
