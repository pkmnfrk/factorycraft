package com.mike_caron.factorycraft.api.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.IntConsumer;

public interface IEnergyManager
    extends INBTSerializable<NBTTagCompound>
{
    void registerConnector(@Nonnull BlockPos pos);
    void deleteConnector(@Nonnull BlockPos pos);
    void makeConnection(@Nonnull BlockPos pos, @Nonnull BlockPos other);
    void removeConnection(@Nonnull BlockPos pos, @Nonnull BlockPos other);

    boolean isConnected(@Nonnull BlockPos src, @Nonnull BlockPos other);

    @Nullable
    BlockPos findConnector(@Nonnull BlockPos src);

    void requestEnergy(UUID network, int amount, IntConsumer callback);
    void provideEnergy(UUID network, int amount, IntConsumer callback);

    @Nonnull
    List<BlockPos> getConnections(@Nonnull BlockPos orig);
}
