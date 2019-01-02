package com.mike_caron.factorycraft.capability;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.api.IPlayerCrafting;
import com.mike_caron.factorycraft.item.crafting.NormalRecipes;
import com.mike_caron.factorycraft.util.Tuple3;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

import java.util.*;
import java.util.function.ToIntFunction;

public class PlayerCraftingImpl
    implements IPlayerCrafting
{
    private EntityPlayer player;
    private final Deque<Tuple3<Item, Integer, Boolean>> queue = new ArrayDeque<>();
    private final Map<Item, Integer> heldItems = new HashMap<>();

    private float progress;

    public PlayerCraftingImpl(EntityPlayer player)
    {
        this.player = player;
    }

    public void tick()
    {
        if(queue.isEmpty()) return;

        Tuple3<Item, Integer, Boolean> top = queue.peek();

        float craftingTime = NormalRecipes.INSTANCE.craftingTimeForRecipe(top.first);

        progress += 1/20f;

        if(progress >= craftingTime)
        {
            //success, deduct the inputs from heldItems
            progress = 0;

            NonNullList<ItemStack> inputs = NormalRecipes.INSTANCE.inputsForRecipe(top.first);
            boolean success = true;
            for(ItemStack inp : inputs)
            {
                if(!deductFromHeld(inp.getItem(), inp.getCount()))
                {
                    FactoryCraft.logger.error("Missing held {}?!", inp);
                    success = false;
                }
            }

            int count = NormalRecipes.INSTANCE.outputSizeForRecipe(top.first);

            if(top.third)
            {
                ItemStack stack = new ItemStack(top.first, count);
                player.inventory.placeItemBackInInventory(player.world, stack);
            }
            else
            {
                addToHeld(top.first, count);
            }

            queue.pop();

            if(top.second > 1)
            {
                top = new Tuple3<>(top.first, top.second - 1, top.third);
                queue.push(top);
            }
        }

    }

    private void addToHeld(Item item, int count)
    {
        if(!heldItems.containsKey(item))
        {
            heldItems.put(item, 0);
        }

        int n = heldItems.get(item);

        n += count;

        heldItems.put(item, n);
    }

    private boolean deductFromHeld(Item item, int count)
    {
        if(!heldItems.containsKey(item))
            return false;

        int n = heldItems.get(item);

        if(n < count)
            return false;

        n -= count;

        if(n == 0)
            heldItems.remove(item);
        else
            heldItems.put(item, n);

        return true;
    }

    public void enqueueCrafting(Item item, int count)
    {
        if(player.world.isRemote)
            throw new RuntimeException("Do not try to craft on the client side!");

        Map<Item, Integer> craftingPlan = new HashMap<>();
        Map<Item, Integer> leftovers = new HashMap<>();
        Map<Item, Integer> alreadyHeld = new HashMap<>();
        Map<Item, Integer> priority = new HashMap<>();

        if(!figureOutCrafting(item, count, 0, craftingPlan, leftovers, alreadyHeld, priority))
        {
            return;
        }

        FactoryCraft.logger.info("Final crafting plan:");
        FactoryCraft.logger.info("Items taken from inventory:");
        for(Map.Entry<Item, Integer> inv : alreadyHeld.entrySet())
        {
            FactoryCraft.logger.info("{} x {}", inv.getValue(), inv.getKey().getRegistryName());
        }
        FactoryCraft.logger.info("Used to craft:");
        for(Map.Entry<Item, Integer> inv : craftingPlan.entrySet())
        {
            FactoryCraft.logger.info("{} x {}", inv.getValue(), inv.getKey().getRegistryName());
        }

        for(int slot = 0; slot < player.inventory.getSizeInventory(); slot++)
        {
            ItemStack stack = player.inventory.getStackInSlot(slot);
            Item stackItem = stack.getItem();

            if(alreadyHeld.containsKey(stackItem))
            {
                int n = alreadyHeld.get(stackItem);

                int h = Math.min(n, stack.getCount());

                stack.shrink(h);
                player.inventory.markDirty();

                if(!heldItems.containsKey(stackItem))
                {
                    heldItems.put(stackItem, h);
                }
                else
                {
                    heldItems.put(stackItem, heldItems.get(stackItem) + h);
                }

                n -= h;

                if(n < 0)
                {
                    throw new RuntimeException("Ate too many items");
                }
                else if(n == 0)
                {
                    alreadyHeld.remove(stackItem);
                }
                else
                {
                    alreadyHeld.put(stackItem, n);
                }
            }
        }

        priority.entrySet().stream()
            .sorted(Comparator.comparingInt((ToIntFunction<Map.Entry<Item, Integer>>) Map.Entry::getValue))
            .map(Map.Entry::getKey)
        .forEach(itm -> {
            queue.push(new Tuple3<>(itm, craftingPlan.get(itm), itm == item));
        });

    }

    public int maxCouldCraft(Item item)
    {
        int lastCount = 0;
        while(true)
        {
            if(!figureOutCrafting(item, lastCount + 1))
            {
                return lastCount;
            }
            lastCount += 1;
        }
    }

    private boolean figureOutCrafting(Item item, int count)
    {
        Map<Item, Integer> craftingPlan = new HashMap<>();
        Map<Item, Integer> leftovers = new HashMap<>();
        Map<Item, Integer> alreadyHeld = new HashMap<>();
        Map<Item, Integer> priority = new HashMap<>();

        return figureOutCrafting(item, count, 0, craftingPlan, leftovers, alreadyHeld, priority);
    }

    private boolean figureOutCrafting(Item item, int count, int order, Map<Item, Integer> craftingPlan, Map<Item, Integer> leftovers, Map<Item, Integer> alreadyHeld, Map<Item, Integer> priority)
    {
        if(!NormalRecipes.INSTANCE.hasRecipe(item))
        {
            FactoryCraft.logger.info("Can't craft {} because there's no recipe", item.getRegistryName());
            return false;
        }

        NonNullList<ItemStack> inputs = NormalRecipes.INSTANCE.inputsForRecipe(item);

        priority.put(item, order);

        for(ItemStack input : inputs)
        {
            int toCraft = input.getCount() * count;
            int toEat = 0;

            Item inputItem = input.getItem();


            if(leftovers.containsKey(inputItem))
            {
                int i = Math.min(leftovers.get(inputItem), toCraft);

                toCraft -= i;

                if(i > 0)
                {
                    FactoryCraft.logger.info("Taking {} {} from the leftover pile", i, inputItem.getRegistryName());
                    leftovers.put(inputItem, leftovers.get(inputItem) - i);
                }
            }

            if(alreadyHeld.containsKey(inputItem))
            {
                toEat = -alreadyHeld.get(inputItem);
            }

            for(int slot = 0; slot < player.inventory.getSizeInventory() && toEat < toCraft; slot++)
            {
                ItemStack stack = player.inventory.getStackInSlot(slot);

                if(ItemStack.areItemsEqual(input, stack))
                {
                    int i = Math.min(stack.getCount(), toCraft - toEat);

                    toEat += i;
                }
            }

            if(!alreadyHeld.containsKey(inputItem))
            {
                alreadyHeld.put(inputItem, 0);
            }
            alreadyHeld.put(inputItem, alreadyHeld.get(inputItem) + toEat);

            toCraft -= toEat;
            FactoryCraft.logger.info("Eating {} {} from the player inventory", toEat, inputItem.getRegistryName());


            if(toCraft > 0)
            {
                FactoryCraft.logger.info("Unfortunately, there's still {} {} left to craft", toCraft, inputItem.getRegistryName());
                if(!figureOutCrafting(inputItem, toCraft, order + 1, craftingPlan, leftovers, alreadyHeld, priority))
                {
                    FactoryCraft.logger.info("Oups, can't make {}...", inputItem.getRegistryName());
                    return false;
                }
            }
        }

        //if we got here, we managed to satisfy our dependencies
        if(!craftingPlan.containsKey(item))
        {
            craftingPlan.put(item, count);
        }
        else
        {
            craftingPlan.put(item, craftingPlan.get(item) + count);
        }

        return true;
    }

    @Override
    public NBTBase serializeNBT()
    {
        NBTTagList list = new NBTTagList();

        return list;
    }

    @Override
    public void deserializeNBT(NBTBase nbtBase)
    {
        NBTTagList list = (NBTTagList) nbtBase;


    }
}
