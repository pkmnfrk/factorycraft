package com.mike_caron.factorycraft.item;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.mikesmodslib.item.ItemBase;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Stream;

@GameRegistry.ObjectHolder(FactoryCraft.modId)
@Mod.EventBusSubscriber
public class ModItems
{
    @GameRegistry.ObjectHolder("ingot_copper")
    public static final ItemResource ingot_copper = null;
    @GameRegistry.ObjectHolder("iron_gear")
    public static final ItemResource iron_gear = null;
    @GameRegistry.ObjectHolder("copper_cable")
    public static final ItemResource copper_cable = null;
    @GameRegistry.ObjectHolder("electronic_circuit")
    public static final ItemResource electronic_circuit = null;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        IForgeRegistry<Item> registry = event.getRegistry();

        registry.register(new ItemResource("ingot_copper", "ingotCopper"));
        registry.register(new ItemResource("iron_gear"));
        registry.register(new ItemResource("copper_cable"));
        registry.register(new ItemResource("electronic_circuit"));
    }

    public static void registerOreDict()
    {
        getAllItems().filter(item -> !(item instanceof ItemResource)).map(item -> (ItemResource)item).forEach(item ->
        {
            for(String oreDict : item.getOreDicts())
            {
                OreDictionary.registerOre(oreDict, item);
            }
        });
    }

    @SideOnly(Side.CLIENT)
    public static void initModels()
    {
        getAllItems().forEach(ItemBase::initModel);
    }

    public static Stream<ItemBase> getAllItems()
    {
        return Arrays.stream(ModItems.class.getDeclaredFields()).filter(f -> Modifier.isStatic(f.getModifiers()) && ItemBase.class.isAssignableFrom(f.getType())).map(f -> {
            try
            {
                ItemBase ret = (ItemBase) f.get(null);

                if(ret == null)
                {
                    FactoryCraft.logger.error("The item " + f.getName() + " is null??");
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
