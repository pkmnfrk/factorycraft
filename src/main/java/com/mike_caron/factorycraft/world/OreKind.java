package com.mike_caron.factorycraft.world;

import com.mike_caron.factorycraft.block.BoulderBlockBase;
import com.mike_caron.factorycraft.block.ModBlocks;
import net.minecraft.world.gen.NoiseGeneratorSimplex;

public class OreKind
{
    public final String seedName;
    public final double scale;
    public final double threshold;
    public final long magnitude;
    public final BoulderBlockBase block;

    private OreKind(String seedName, double scale, double threshold, long magnitude, BoulderBlockBase block)
    {
        this.seedName = seedName;
        this.scale = scale;
        this.threshold = threshold;
        this.magnitude = magnitude;
        this.block = block;
    }

    public double getSample(NoiseGeneratorSimplex noise, int x, int z)
    {
        double sample = noise.getValue(x / scale, z / scale);

        if(sample < threshold) return 0.0;

        return (sample - threshold) / (1.0 - threshold);
    }

    public final static OreKind IRON = new OreKind("iron", 32, 0.7, 10000, ModBlocks.iron_boulder);
    public final static OreKind COPPER = new OreKind("copper", 16, 0.7, 15000, ModBlocks.copper_boulder);
    public final static OreKind COAL = new OreKind("coal", 8, 0.7, 8000, ModBlocks.coal_boulder);

    public final static OreKind[] ALL_ORES = {
        IRON,
        COPPER,
        COAL
    };
}
