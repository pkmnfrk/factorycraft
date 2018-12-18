package com.mike_caron.factorycraft.world;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.block.BoulderBlockBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WorldGen
    implements IWorldGenerator
{
    Lock lock = new ReentrantLock();

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
    {
        if(world.provider.getDimension() == 0)
        {
            generateOverworld(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
        }
    }

    private void generateOverworld(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
    {
        HashMap<String, NoiseGeneratorSimplex> noise = getNoise(world);

        //Chunk chunk = null;

        for(int z = 0; z < 4; z++)
        {
            for(int x = 0; x < 4; x++)
            {
                OreKind winnerKind = null;
                double winnerValue = 0.0;

                for(OreKind ore : OreKind.ALL_ORES)
                {
                    double clumpedValue = 0.0;

                    for(int sz = 0; sz < 4; sz ++)
                    {
                        for(int sx = 0; sx < 4; sx ++)
                        {
                            double sample = ore.getSample(noise.get(ore.seedName), chunkX + x * 4 + sx, chunkZ + z * 4 + sz);

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
                        // generate in the middle of the microchunk to avoid accidentally causing extra world gen
                        int posX = chunkX * 16 + x * 4 + random.nextInt(2) + 1;
                        int posZ = chunkZ * 16 + z * 4 + random.nextInt(2) + 1;

                        //if(chunk == null)
                        //{
                        //    chunk = chunkProvider.provideChunk(chunkX, chunkZ);
                        //}
                        FactoryCraft.logger.info("Generating at {},{} for chunk {},{}", posX, posZ, chunkX, chunkZ);
                        int posY = findBestYLevel(world, posX, posZ);

                        Block block = winnerKind.getBlock();
                        IBlockState defaultState = block.getDefaultState();
                        IBlockState boulder = defaultState.withProperty(BoulderBlockBase.SIZE, 4);
                        BlockPos newPos = new BlockPos(posX, posY, posZ);

                        world.setBlockState(newPos, boulder);
                    }
                }
            }
        }

    }

    private int findBestYLevel(World world, int posX, int posZ)
    {
        int lastGood = 127;

        for(int y = 127; y > 0; y--)
        {
            BlockPos pos = new BlockPos(posX, y, posZ);
            IBlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();
            if(block.isAir(blockState, world, pos)
                || block == Blocks.WATER
                || block == Blocks.SNOW_LAYER
                || block == Blocks.TALLGRASS
                || block == Blocks.RED_FLOWER
                || block == Blocks.YELLOW_FLOWER
                || block.isLeaves(blockState, world, pos)
                || block.isFoliage(world, pos)
                )
            {
                lastGood = y;
            }
            else
            {
                return lastGood;
            }
        }
        return 64;
    }

    private HashMap<String, NoiseGeneratorSimplex> getNoise(World world)
    {
        HashMap<String, NoiseGeneratorSimplex> seeds;
        lock.lock();
        GlobalWorldData worldData = (GlobalWorldData)world.getPerWorldStorage().getOrLoadData(GlobalWorldData.class, FactoryCraft.modId);

        if(worldData == null)
        {
            worldData = new GlobalWorldData();
            world.getPerWorldStorage().setData(FactoryCraft.modId, worldData);
        }

        if(worldData.seeds.isEmpty())
        {
            long seed = world.getSeed() + 1;

            for(OreKind oreKind : OreKind.ALL_ORES)
            {
                worldData.seeds.put(oreKind.seedName, seed);
                worldData.noise.put(oreKind.seedName, new NoiseGeneratorSimplex(new Random(seed)));

                seed += 1;
            }

            worldData.markDirty();
        }

        seeds = worldData.noise;

        lock.unlock();
        return seeds;
    }
}
