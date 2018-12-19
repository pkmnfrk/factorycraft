package com.mike_caron.factorycraft.world;

import com.mike_caron.factorycraft.block.BoulderBlockBase;
import com.mike_caron.factorycraft.block.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.NoiseGeneratorSimplex;

import java.util.HashMap;

public class OreKind
{
    public final String seedName;
    public final double scale;
    public final double threshold;
    public final long magnitude;
    public final ItemStack ore;
    public final double hardness;
    public final double miningTime;

    private OreKind(String seedName, double scale, double threshold, long magnitude, ItemStack ore, double hardness, double miningTime)
    {
        this.seedName = seedName;
        this.scale = scale;
        this.threshold = threshold;
        this.magnitude = magnitude;
        this.ore = ore;
        this.hardness = hardness;
        this.miningTime = miningTime;
    }

    public double getSample(NoiseGeneratorSimplex noise, int x, int z)
    {
        double sample = noise.getValue(x / scale, z / scale);

        if(sample < threshold) return 0.0;

        return (sample - threshold) / (1.0 - threshold);
    }

    public final static OreKind IRON = new OreKind("iron", 64, 0.7, 10000, new ItemStack(Blocks.IRON_ORE, 1), 0.9, 2);
    public final static OreKind COPPER = new OreKind("copper", 48, 0.7, 15000, new ItemStack(Blocks.GOLD_ORE, 1), 0.9, 2);
    public final static OreKind COAL = new OreKind("coal", 32, 0.7, 8000, new ItemStack(Items.COAL, 1), 0.9, 2);

    public final static OreKind[] ALL_ORES = {
        IRON,
        COPPER,
        COAL
    };

    public final static HashMap<String, OreKind> ALL_ORES_BY_NAME = new HashMap<>();

    static
    {
        for(OreKind kind : ALL_ORES)
        {
            ALL_ORES_BY_NAME.put(kind.seedName, kind);
        }
    }

    public BoulderBlockBase getBlock()
    {
        switch(seedName)
        {
            case "iron":
                return ModBlocks.iron_boulder;
            case "copper":
                return ModBlocks.copper_boulder;
            case "coal":
                return ModBlocks.coal_boulder;
        }

        return null;
    }
}
