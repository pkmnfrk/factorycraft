package com.mike_caron.factorycraft.capability;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.mike_caron.factorycraft.api.IOreDeposit;
import com.mike_caron.factorycraft.util.Tuple2i;
import com.mike_caron.factorycraft.world.OreDeposit;
import com.mike_caron.factorycraft.world.OreKind;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.NoiseGeneratorSimplex;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OreDepositDefaultImpl
    implements IOreDeposit
{
    private final ConcurrentHashMap<Tuple2i, OreDeposit> deposits = new ConcurrentHashMap<>();
    private boolean generated = false;

    private final float GLOBAL_MULTIPLIER = 4f; //increase to make ores rarer

    @Override
    @Nullable
    public OreDeposit getOreDeposit(int sx, int sz)
    {
        validateParameters(sx, sz);

        Tuple2i key = new Tuple2i(sx, sz);
        if(deposits.containsKey(key))
        {
            OreDeposit ret = deposits.get(key);

            if(ret.getSize() == 0)
            {
                deposits.remove(key);
                return null;
            }

            return ret;
        }
        return null;
    }

    @Override
    @Nonnull
    public ImmutableMap<Tuple2i, OreDeposit> getAllDeposits()
    {
        return ImmutableMap.copyOf(deposits);
    }

    @Override
    public void clearDeposits()
    {
        deposits.clear();
    }

    @Override
    public void putDeposit(int sx, int sz, @Nonnull OreDeposit deposit)
    {
        validateParameters(sx, sz);
        Tuple2i key = new Tuple2i(sx, sz);
        deposits.put(key, deposit);
    }

    @Nullable
    @Override
    public OreKind mineOne(int sx, int sz)
    {
        validateParameters(sx, sz);
        Tuple2i key = new Tuple2i(sx, sz);
        OreDeposit deposit = deposits.getOrDefault(key, null);
        if(deposit != null)
        {
            deposit.mineOne();
            return deposit.getOreKind();
        }
        return null;
    }

    @Override
    public void generateIfNeeded(int chunkX, int chunkZ, Map<String, NoiseGeneratorSimplex> noise, BlockPos spawn)
    {
        int cx = chunkX * 16;
        int cz = chunkZ * 16;

        for(int z = 0; z < 4; z++)
        {
            int mz = cz + z * 4;
            for(int x = 0; x < 4; x++)
            {
                int mx = cx + x * 4;

                OreKind winnerKind = null;
                double winnerValue = 0.0;

                for(OreKind ore : OreKind.ALL_ORES)
                {
                    double clumpedValue = 0.0;

                    for(int sz = 0; sz < 4; sz ++)
                    {
                        int pz = mz + sz;
                        for(int sx = 0; sx < 4; sx ++)
                        {
                            int px = mx + sx;

                            double sample = ore.getSample(noise.get(ore.seedName), px, pz, spawn);

                            clumpedValue += sample;
                        }
                    }

                    if(clumpedValue > winnerValue)
                    {
                        winnerKind = ore;
                        winnerValue = clumpedValue;
                    }
                }

                if(winnerKind != null)
                {
                    long finalSize = (long)Math.ceil(winnerKind.magnitude * winnerValue);

                    if(finalSize > 0)
                    {
                        OreDeposit deposit = new OreDeposit(winnerKind, finalSize, finalSize);

                        deposits.put(new Tuple2i(x, z), deposit);
                    }
                }
            }
        }

        generated = true;
    }

    @Override
    public void setGenerated()
    {
        generated = true;
    }

    private void validateParameters(int sx, int sz)
    {
        Preconditions.checkArgument(sx >= 0 && sx <= 3, "sx must be between 0 and 3");
        Preconditions.checkArgument(sz >= 0 && sz <= 3, "sz must be between 0 and 3");
    }


}
