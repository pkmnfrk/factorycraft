package com.mike_caron.factorycraft.api;

import com.google.common.collect.ImmutableMap;
import com.mike_caron.factorycraft.util.Tuple2i;
import com.mike_caron.factorycraft.world.OreDeposit;
import com.mike_caron.factorycraft.world.OreKind;
import net.minecraft.world.gen.NoiseGeneratorSimplex;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public interface IOreDeposit
{
    @Nullable
    OreDeposit getOreDeposit(int sx, int sz);
    @Nonnull
    ImmutableMap<Tuple2i, OreDeposit> getAllDeposits();
    void clearDeposits();
    void putDeposit(int sx, int sz, @Nonnull OreDeposit deposit);
    @Nullable
    OreKind mineOne(int sx, int sz);
    public void generateIfNeeded(int chunkX, int chunkZ, Map<String, NoiseGeneratorSimplex> noise);
    void setGenerated();
}
