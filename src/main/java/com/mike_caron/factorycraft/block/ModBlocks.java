package com.mike_caron.factorycraft.block;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.tileentity.DrillTileEntity;
import com.mike_caron.factorycraft.tileentity.GrabberTileEntity;
import com.mike_caron.factorycraft.world.OreKind;
import com.mike_caron.mikesmodslib.block.BlockBase;
import com.mike_caron.mikesmodslib.block.ModBlocksBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
@GameRegistry.ObjectHolder (FactoryCraft.modId)
public class ModBlocks
    extends ModBlocksBase
{
    private ModBlocks(){}

    @GameRegistry.ObjectHolder("iron_boulder")
    public static final BoulderBlockBase iron_boulder = null;
    @GameRegistry.ObjectHolder("copper_boulder")
    public static final BoulderBlockBase copper_boulder = null;
    @GameRegistry.ObjectHolder("coal_boulder")
    public static final BoulderBlockBase coal_boulder = null;

    @GameRegistry.ObjectHolder("drill_burner")
    public static final DrillBlock drill_burner = null;

    @GameRegistry.ObjectHolder("grabber1")
    public static final GrabberBlock grabber1 = null;

    @GameRegistry.ObjectHolder("test")
    public static final BlockBase test = null;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        IForgeRegistry<Block> registry = event.getRegistry();

        registry.register(new BoulderBlockBase(Material.IRON, "iron_boulder", OreKind.IRON));
        registry.register(new BoulderBlockBase(Material.IRON, "copper_boulder", OreKind.COPPER));
        registry.register(new BoulderBlockBase(Material.IRON, "coal_boulder", OreKind.COAL));

        registry.register(new DrillBlock("drill_burner", 0));
        registry.register(new GrabberBlock("grabber1", 0));

        registry.register(new BlockBase(Material.IRON, "test").setCreativeTab(FactoryCraft.creativeTab));

        GameRegistry.registerTileEntity(DrillTileEntity.class, new ResourceLocation(FactoryCraft.modId, "drill"));
        GameRegistry.registerTileEntity(GrabberTileEntity.class, new ResourceLocation(FactoryCraft.modId, "grabber"));

        ModBlocksBase.registerBlocks(ModBlocksBase.class);

    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        ModBlocksBase.registerItems(event, ModBlocks.class);
    }

    @SideOnly(Side.CLIENT)
    public static void initModels()
    {
        ModBlocksBase.registerModels(ModBlocks.class);
    }
}
