package com.mike_caron.factorycraft.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public interface IConveyorBelt
{
    int numTracks();
    float trackLength(int track);

    int totalItems();
    @Nonnull
    ItemStack extract(int track, float position, boolean simulate);
    @Nonnull
    ItemStack insert(int track, float position, @Nonnull ItemStack itemStack, boolean simulate);

    int trackClosestTo(@Nonnull EnumFacing facing);
}
