package com.mike_caron.factorycraft.block;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.item.ItemFurnaceBlock;
import com.mike_caron.factorycraft.tileentity.*;
import com.mike_caron.factorycraft.world.OreKind;
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

    @GameRegistry.ObjectHolder("ore_copper")
    public static final BlockOre ore_copper = null;
    @GameRegistry.ObjectHolder("ore_uranium")
    public static final BlockOre ore_uranium = null;

    @GameRegistry.ObjectHolder("boulder_iron")
    public static final BlockBoulder boulder_iron = null;
    @GameRegistry.ObjectHolder("boulder_copper")
    public static final BlockBoulder boulder_copper = null;
    @GameRegistry.ObjectHolder("boulder_coal")
    public static final BlockBoulder boulder_coal = null;
    @GameRegistry.ObjectHolder("boulder_stone")
    public static final BlockBoulder boulder_stone = null;
    @GameRegistry.ObjectHolder("boulder_uranium")
    public static final BlockBoulder boulder_uranium = null;

    @GameRegistry.ObjectHolder("drill_burner")
    public static final BlockDrill drill_burner = null;
    @GameRegistry.ObjectHolder("drill")
    public static final BlockDrill drill = null;

    @GameRegistry.ObjectHolder("grabber_burner")
    public static final BlockGrabber grabber_burner = null;
    @GameRegistry.ObjectHolder("grabber")
    public static final BlockGrabber grabber = null;

    @GameRegistry.ObjectHolder("conveyor_slow")
    public static final BlockConveyor conveyor_slow = null;
    @GameRegistry.ObjectHolder("conveyor_medium")
    public static final BlockConveyor conveyor_medium = null;
    @GameRegistry.ObjectHolder("conveyor_fast")
    public static final BlockConveyor conveyor_fast = null;

    @GameRegistry.ObjectHolder("small_electrical_pole")
    public static final BlockElectricalPole small_electrical_pole = null;
    //@GameRegistry.ObjectHolder("medium_electrical_pole")
    //public static final BlockElectricalPole medium_electrical_pole = null;
    //@GameRegistry.ObjectHolder("large_electrical_pole")
    //public static final BlockElectricalPole large_electrical_pole = null;

    @GameRegistry.ObjectHolder("furnace_stone")
    public static final BlockFurnace furnace_stone = null;

    @GameRegistry.ObjectHolder("creative_power")
    public static final BlockCreativePower creative_power = null;


    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        IForgeRegistry<Block> registry = event.getRegistry();

        registry.register(new BlockBoulder(Material.IRON, "boulder_iron", OreKind.IRON));
        registry.register(new BlockBoulder(Material.IRON, "boulder_copper", OreKind.COPPER));
        registry.register(new BlockBoulder(Material.IRON, "boulder_coal", OreKind.COAL));
        registry.register(new BlockBoulder(Material.IRON, "boulder_stone", OreKind.STONE));
        registry.register(new BlockBoulder(Material.IRON, "boulder_uranium", OreKind.URANIUM).setLightLevel(0.125f));

        registry.register(new BlockDrill("drill_burner", 0));
        registry.register(new BlockDrill("drill", 1));

        registry.register(new BlockGrabber("grabber_burner", TileEntityGrabber.TYPE_BURNER));
        registry.register(new BlockGrabber("grabber", TileEntityGrabber.TYPE_REGULAR));

        registry.register(new BlockConveyor("conveyor_slow", 0));
        registry.register(new BlockConveyor("conveyor_medium", 1));
        registry.register(new BlockConveyor("conveyor_fast", 2));

        registry.register(new BlockElectricalPole("small_electrical_pole", 0));

        registry.register(new BlockFurnace("furnace_stone", 0));

        registry.register(new BlockOre("ore_copper"));
        registry.register(new BlockOre("ore_uranium").setHardness(15f).setLightLevel(2/16f));

        registry.register(new BlockCreativePower("creative_power"));

        GameRegistry.registerTileEntity(TileEntityDrill.class, new ResourceLocation(FactoryCraft.modId, "drill"));
        GameRegistry.registerTileEntity(TileEntityGrabber.class, new ResourceLocation(FactoryCraft.modId, "grabber"));
        GameRegistry.registerTileEntity(TileEntityConveyor.class, new ResourceLocation(FactoryCraft.modId, "conveyor"));
        GameRegistry.registerTileEntity(TileEntityElectricalPole.class, new ResourceLocation(FactoryCraft.modId, "electrical_pole"));
        GameRegistry.registerTileEntity(TileEntityCreativePower.class, new ResourceLocation(FactoryCraft.modId, "creative_power"));
        GameRegistry.registerTileEntity(TileEntityFurnace.class, new ResourceLocation(FactoryCraft.modId, "furnace"));
        GameRegistry.registerTileEntity(TileEntityRedirect.class, new ResourceLocation(FactoryCraft.modId, "redirect"));

        ModBlocksBase.registerBlocks(ModBlocksBase.class);

    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        ModBlocksBase.registerItems(event, ModBlocks.class, block -> {
            if(block instanceof BlockFurnace)
                return new ItemFurnaceBlock(block);
            return null;
        });
    }

    @SideOnly(Side.CLIENT)
    public static void initModels()
    {
        ModBlocksBase.registerModels(ModBlocks.class);
    }


}
