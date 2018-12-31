package com.mike_caron.factorycraft.item.crafting;

import com.mike_caron.mikesmodslib.util.ItemUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class NormalRecipes
{
    public static final NormalRecipes INSTANCE = new NormalRecipes();

    private final Map<Item, NonNullList<ItemStack>> inputList = new HashMap<>();
    private final Map<Item, Integer> outputCountList = new HashMap<>();
    private final Map<Item, Float> timeList = new HashMap<>();

    private NormalRecipes()
    {
        addRecipe("fc:iron_gear", 0.5f,"iron_ingot#2");
        addRecipe("fc:conveyor_slow#2", 0.5f, "fc:iron_gear", "iron_ingot");
        addRecipe("fc:grabber_burner", 0.5f, "fc:iron_gear", "iron_ingot");
        addRecipe("fc:copper_cable", 0.5f, "fc:ingot_copper");
        addRecipe("fc:electronic_circuit", 0.5f, "fc:copper_cable#3", "iron_ingot");
        addRecipe("fc:grabber", 0.5f, "fc:iron_gear", "iron_ingot", "fc:electronic_circuit");
    }

    public void addRecipe(String output, float craftingTime, String... inputs)
    {
        addRecipe(
            getStackWithCountFromTag(output),
            craftingTime,
            Stream.of(inputs).map(this::getStackWithCountFromTag).toArray(ItemStack[]::new)
        );
    }

    public void addRecipe(ItemStack output, float craftingTime, ItemStack... inputs)
    {
        Item outputItem = output.getItem();
        int outputCount = output.getCount();
        if(inputList.containsKey(outputItem))
        {
            throw new RuntimeException("Recipe is already set!");
        }

        NonNullList<ItemStack> input = NonNullList.from(ItemStack.EMPTY, inputs);

        inputList.put(outputItem, input);
        outputCountList.put(outputItem, outputCount);
        timeList.put(outputItem, craftingTime);
    }

    private ItemStack getStackWithCountFromTag(String tag)
    {
        String[] parts = tag.split("#", 2);

        ItemStack ret = ItemUtils.getStackFromTag(parts[0].replace("fc:", "factorycraft:"));

        if(parts.length > 1)
        {
            ret.setCount(Integer.parseInt(parts[1]));
        }

        return ret;
    }

    public NonNullList<ItemStack> inputsForRecipe(Item output)
    {
        return inputList.get(output);
    }

    public float craftingTimeForRecipe(Item output)
    {
        return timeList.get(output);
    }

    public int outputSizeForRecipe(Item output)
    {
        return outputCountList.get(output);
    }

    public boolean hasRecipe(Item output)
    {
        return inputList.containsKey(output);
    }

    public Set<Item> craftingItems()
    {
        return inputList.keySet();
    }
}
