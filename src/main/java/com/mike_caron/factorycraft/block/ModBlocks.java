package com.mike_caron.factorycraft.block;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.mikesmodslib.block.BlockBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Stream;

@Mod.EventBusSubscriber
@GameRegistry.ObjectHolder (FactoryCraft.modId)
public class ModBlocks
{
    private ModBlocks(){}

    @GameRegistry.ObjectHolder("iron_boulder")
    public static final BoulderBlockBase iron_boulder = null;
    @GameRegistry.ObjectHolder("copper_boulder")
    public static final BoulderBlockBase copper_boulder = null;
    @GameRegistry.ObjectHolder("coal_boulder")
    public static final BoulderBlockBase coal_boulder = null;
    @GameRegistry.ObjectHolder("test")
    public static final BlockBase test = null;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        IForgeRegistry<Block> registry = event.getRegistry();

        registry.register(new BoulderBlockBase(Material.IRON, "iron_boulder"));
        registry.register(new BoulderBlockBase(Material.IRON, "copper_boulder"));
        registry.register(new BoulderBlockBase(Material.IRON, "coal_boulder"));
        registry.register(new BlockBase(Material.IRON, "test").setCreativeTab(FactoryCraft.creativeTab));

    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        IForgeRegistry<Item> registry = event.getRegistry();

        getAllBlocks().forEach(block -> {
            if (block != null)
                registry.register(
                    new ItemBlock(block)
                        .setRegistryName(block.getRegistryName())
                );
        });

        //OreDictionary.registerOre("blockMoney", money_block);
        //OreDictionary.registerOre("blockDenseMoney", dense_money_block);
    }

    @SideOnly(Side.CLIENT)
    public static void initModels()
    {
        //ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(money), 0, new ModelResourceLocation(money.getRegistryName(), "normal"));
        //ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(dense_money), 0, new ModelResourceLocation(dense_money.getRegistryName(), "normal"));

        getAllBlocks().filter(b -> b instanceof BlockBase).map(b -> (BlockBase)b).forEach(BlockBase::initModel);
    }

    public static Stream<Block> getAllBlocks()
    {
        return Arrays.stream(ModBlocks.class.getDeclaredFields()).filter(f -> Modifier.isStatic(f.getModifiers()) && Block.class.isAssignableFrom(f.getType())).map(f -> {
            try
            {
                Block ret = (Block)f.get(null);

                if(ret == null)
                {
                    //MegaCorpMod.logger.error("Block " + f.getName() + " is null");
                    return null;
                }
                return ret;
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException("Unable to reflect upon myself??");
            }
        });
    }

}
