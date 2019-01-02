package com.mike_caron.factorycraft.api;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.common.util.INBTSerializable;

public interface IPlayerCrafting
    extends INBTSerializable<NBTBase>
{
    void enqueueCrafting(Item item, int amount);
    void tick();
    int maxCouldCraft(Item item);
}
