package com.mike_caron.factorycraft.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface IEnergyManager
    extends INBTSerializable<NBTTagCompound>
{
    UUID registerConnector(@Nonnull BlockPos pos);
    void deleteConnector(@Nonnull BlockPos pos);
    void makeConnection(@Nonnull BlockPos pos, @Nonnull BlockPos other);
    void removeConnection(@Nonnull BlockPos pos, @Nonnull BlockPos other);

    boolean isConnected(@Nonnull BlockPos src, @Nonnull BlockPos other);
}
