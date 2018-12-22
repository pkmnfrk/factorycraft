package com.mike_caron.factorycraft.api;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IConveyorBelt
{
    int numTracks();
    float trackLength(int track);

    int totalItems();
    @Nonnull
    ItemStack extract(int track, int position, boolean simulate);
    @Nonnull
    ItemStack insert(int track, int position, @Nonnull ItemStack itemStack, boolean simulate);

}
