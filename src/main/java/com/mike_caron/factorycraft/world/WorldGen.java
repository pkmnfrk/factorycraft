package com.mike_caron.factorycraft.world;

import com.google.common.base.Preconditions;
import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.api.IOreDeposit;
import com.mike_caron.factorycraft.api.capabilities.CapabilityOreDeposit;
import com.mike_caron.factorycraft.util.Tuple2i;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.HashMap;
import java.util.Map;
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
        Chunk chunk = chunkProvider.provideChunk(chunkX, chunkZ);

        IOreDeposit oreDeposit = chunk.getCapability(CapabilityOreDeposit.OREDEPOSIT, null);

        Preconditions.checkNotNull(oreDeposit);

        GlobalWorldData worldData = getNoise(world);

        oreDeposit.generateIfNeeded(chunkX, chunkZ, worldData.noise, worldData.spawnPoint);

        for(Map.Entry<Tuple2i, OreDeposit> deposit : oreDeposit.getAllDeposits().entrySet())
        {
            // generate in the middle of the microchunk to avoid accidentally causing extra world gen
            int posX = chunkX * 16 + deposit.getKey().x * 4 + random.nextInt(3) + 1;
            int posZ = chunkZ * 16 + deposit.getKey().z * 4 + random.nextInt(3) + 1;

            //FactoryCraft.logger.info("Generating at {},{} for chunk {},{}", posX, posZ, chunkX, chunkZ);
            int posY = findBestYLevel(world, posX, posZ);

            Block block = deposit.getValue().getOreKind().getBlock();
            IBlockState boulder = block.getDefaultState();
            BlockPos newPos = new BlockPos(posX, posY, posZ);

            world.setBlockState(newPos, boulder);
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

    private GlobalWorldData getNoise(World world)
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

        if(worldData.spawnPoint == null)
        {
            worldData.spawnPoint = world.getSpawnPoint();
        }

        seeds = worldData.noise;

        lock.unlock();
        return worldData;
    }
}
