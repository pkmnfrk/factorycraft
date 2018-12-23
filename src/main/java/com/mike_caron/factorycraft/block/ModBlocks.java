package com.mike_caron.factorycraft.block;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.tileentity.TileEntityConveyor;
import com.mike_caron.factorycraft.tileentity.TileEntityDrill;
import com.mike_caron.factorycraft.tileentity.TileEntityGrabber;
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

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        IForgeRegistry<Block> registry = event.getRegistry();

        registry.register(new BlockBoulder(Material.IRON, "boulder_iron", OreKind.IRON));
        registry.register(new BlockBoulder(Material.IRON, "boulder_copper", OreKind.COPPER));
        registry.register(new BlockBoulder(Material.IRON, "boulder_coal", OreKind.COAL));
        registry.register(new BlockBoulder(Material.IRON, "boulder_stone", OreKind.STONE));
        registry.register(new BlockBoulder(Material.IRON, "boulder_uranium", OreKind.URANIUM));

        registry.register(new BlockDrill("drill_burner", 0));
        registry.register(new BlockGrabber("grabber_burner", 0));
        registry.register(new BlockGrabber("grabber", 1));

        registry.register(new BlockConveyor("conveyor_slow", 0));
        registry.register(new BlockConveyor("conveyor_medium", 1));
        registry.register(new BlockConveyor("conveyor_fast", 2));

        GameRegistry.registerTileEntity(TileEntityDrill.class, new ResourceLocation(FactoryCraft.modId, "drill"));
        GameRegistry.registerTileEntity(TileEntityGrabber.class, new ResourceLocation(FactoryCraft.modId, "grabber"));
        GameRegistry.registerTileEntity(TileEntityConveyor.class, new ResourceLocation(FactoryCraft.modId, "conveyor"));

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
