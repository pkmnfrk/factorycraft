package com.mike_caron.factorycraft.api;

import com.mike_caron.factorycraft.util.Tuple3;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Deque;

public interface IPlayerCrafting
    extends INBTSerializable<NBTBase>
{
    void enqueueCrafting(Item item, int amount);
    void tick();
    int maxCouldCraft(Item item);
    void handleQueueMessage(String[] items, int[] counts);
    Deque<Tuple3<Item, Integer, Boolean>> getQueue();
    int getChangeCount();
}
