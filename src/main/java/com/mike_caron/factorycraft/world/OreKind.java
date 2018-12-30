package com.mike_caron.factorycraft.world;

import com.mike_caron.factorycraft.block.BlockBoulder;
import com.mike_caron.factorycraft.block.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.NoiseGeneratorSimplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class OreKind
{
    public final String seedName;
    public final double scale;
    public final double threshold;
    public final long magnitude;
    public final Supplier<ItemStack> ore;
    public final double hardness;
    public final double miningTime;
    private final Supplier<BlockBoulder> blockSupplier;
    private BlockBoulder block;
    private double minDistance;

    private OreKind(String seedName, double scale, double threshold, long magnitude, Supplier<ItemStack> ore, double hardness, double miningTime, double minDistance, Supplier<BlockBoulder> block)
    {
        this.seedName = seedName;
        this.scale = scale;
        this.threshold = threshold;
        this.magnitude = magnitude;
        this.ore = ore;
        this.hardness = hardness;
        this.miningTime = miningTime;
        this.blockSupplier = block;
        this.minDistance = minDistance;
    }

    public double getSample(NoiseGeneratorSimplex noise, int x, int z, BlockPos origin)
    {
        double distance = origin.getDistance(x, origin.getY(), z);

        if(distance < minDistance)
            return 0.0;

        double mod = Math.log10(distance * 2);

        if(mod < 1)
            mod = 1;

        mod *= scale;

        mod /= 3;
        mod *= 4;

        double sample = noise.getValue((x - origin.getX()) / mod, (z - origin.getZ()) / mod);

        if(sample < threshold) return 0.0;

        return (sample - threshold) / (1.0 - threshold);
    }

    public static OreKind IRON;
    public static OreKind COPPER;
    public static OreKind COAL;
    public static OreKind STONE;
    public static OreKind URANIUM;

    public final static List<OreKind> ALL_ORES = new ArrayList<>();

    public final static HashMap<String, OreKind> ALL_ORES_BY_NAME = new HashMap<>();

    public static void registerDefaultOreKinds()
    {
        IRON = registerOreKind("iron", 64, 0.7, 10000, 0.9, 2, 200, () -> new ItemStack(Blocks.IRON_ORE, 1), () -> ModBlocks.boulder_iron);
        COPPER = registerOreKind("copper", 48, 0.7, 15000, 0.9, 2, 200, () -> new ItemStack(ModBlocks.ore_copper, 1),  () -> ModBlocks.boulder_copper);
        COAL = registerOreKind("coal", 32, 0.7, 8000, 0.9, 2, 200, () -> new ItemStack(Items.COAL, 1),  () -> ModBlocks.boulder_coal);
        STONE = registerOreKind("stone", 24, 0.75, 9000, 0.4, 2, 200, () -> new ItemStack(Blocks.COBBLESTONE, 1),  () -> ModBlocks.boulder_stone);
        URANIUM = registerOreKind("uranium", 128, 0.80, 4000, 0.9, 4, 1000, () -> new ItemStack(ModBlocks.ore_uranium, 1),  () -> ModBlocks.boulder_uranium);
    }

    public static OreKind registerOreKind(String seedName, double scale, double threshold, long magnitude, double hardness, double miningTime, double minDistance, Supplier<ItemStack> ore, Supplier<BlockBoulder> block)
    {
        OreKind oreKind = new OreKind(seedName, scale, threshold, magnitude, ore, hardness, miningTime, minDistance, block);

        ALL_ORES.add(oreKind);
        ALL_ORES_BY_NAME.put(oreKind.seedName, oreKind);

        return oreKind;
    }

    public BlockBoulder getBlock()
    {
        if(block == null)
        {
            block = blockSupplier.get();
        }
        return block;
    }
}
