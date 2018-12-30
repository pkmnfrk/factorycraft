package com.mike_caron.factorycraft.item.crafting;

import com.mike_caron.factorycraft.block.ModBlocks;
import com.mike_caron.factorycraft.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class FurnaceRecipe
{
    public static final FurnaceRecipe INSTANCE = new FurnaceRecipe();

    private final Map<Item, ItemStack> smeltingList = new HashMap<>();
    private final Map<Item, Integer> countList = new HashMap<>();
    private final Map<Item, Float> timeList = new HashMap<>();

    private FurnaceRecipe()
    {
        addRecipe(Blocks.IRON_ORE, 1, new ItemStack(Items.IRON_INGOT), 3.5f);
        addRecipe(ModBlocks.ore_copper, 1, new ItemStack(ModItems.ingot_copper), 3.5f);
    }

    private void addRecipe(Block in, int count, ItemStack out, float time)
    {
        addRecipe(Item.getItemFromBlock(in), count, out, time);
    }

    private void addRecipe(Item in, int count, ItemStack out, float time)
    {
        smeltingList.put(in, out);
        timeList.put(in, time);
        countList.put(in, count);
    }

    public boolean hasRecipe(ItemStack input)
    {
        if(input.isEmpty()) return false;

        return smeltingList.containsKey(input.getItem());
    }

    public ItemStack getOutput(ItemStack input)
    {
        if(!input.isEmpty())
        {
            Item key = input.getItem();
            if (smeltingList.containsKey(key))
            {
                return smeltingList.get(key).copy();
            }
        }

        return ItemStack.EMPTY;
    }

    public float getCraftingTime(ItemStack input)
    {
        if(!input.isEmpty())
        {
            Item key = input.getItem();
            if(timeList.containsKey(key))
            {
                return timeList.get(key);
            }
        }

        return 0f;
    }

    public int getInputCount(ItemStack input)
    {
        if(!input.isEmpty())
        {
            Item key = input.getItem();
            if(countList.containsKey(key))
            {
                return countList.get(key);
            }
        }

        return 0;
    }
}
