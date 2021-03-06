package com.mike_caron.factorycraft.world;

import com.mike_caron.factorycraft.FactoryCraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GlobalWorldData
    extends WorldSavedData
{
    public final HashMap<String, Long> seeds = new HashMap<>();
    public final HashMap<String, NoiseGeneratorSimplex> noise = new HashMap<>();
    public BlockPos spawnPoint;

    public GlobalWorldData(String name)
    {
        super(name);
    }

    public GlobalWorldData()
    {
        super(FactoryCraft.modId);

    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt)
    {
        seeds.clear();
        noise.clear();
        if(nbt.hasKey("seeds"))
        {
            NBTTagCompound seedList = nbt.getCompoundTag("seeds");
            for(String seed : seedList.getKeySet())
            {
                seeds.put(seed, seedList.getLong(seed));
                noise.put(seed, new NoiseGeneratorSimplex(new Random(seedList.getLong(seed))));
            }
        }
        spawnPoint = null;
        if(nbt.hasKey("spawnPoint"))
        {
            NBTTagCompound spawnPointTag = nbt.getCompoundTag("spawnPoint");
            spawnPoint = new BlockPos(spawnPointTag.getInteger("x"),  spawnPointTag.getInteger("y"), spawnPointTag.getInteger("z"));
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt)
    {
        NBTTagCompound seedList = new NBTTagCompound();
        for(Map.Entry<String, Long> seed : seeds.entrySet())
        {
            seedList.setDouble(seed.getKey(), seed.getValue());
        }
        nbt.setTag("seeds", seedList);

        NBTTagCompound spawnTag = new NBTTagCompound();
        spawnTag.setInteger("x", spawnPoint.getX());
        spawnTag.setInteger("y", spawnPoint.getY());
        spawnTag.setInteger("z", spawnPoint.getZ());
        nbt.setTag("spawnPoint", spawnTag);

        return nbt;
    }
}
