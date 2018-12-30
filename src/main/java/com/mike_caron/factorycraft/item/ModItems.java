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

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        IForgeRegistry<Item> registry = event.getRegistry();

        registry.register(new ItemResource("ingot_copper"));

        /*
        OreDictionary.registerOre("ingotMoney", ingotMoney);
        OreDictionary.registerOre("ingotDenseMoney", ingotDenseMoney);
        */
    }

    @SideOnly(Side.CLIENT)
    public static void initModels()
    {
        //initModel(ingotMoney);
        //initModel(ingotDenseMoney);

        getAllItems().forEach(item ->
        {
            item.initModel();

            if(item instanceof ItemResource)
            {
                for(String oreDict : ((ItemResource) item).getOreDicts())
                {
                    OreDictionary.registerOre(oreDict, item);
                }
            }
        });
    }

    public static Stream<ItemBase> getAllItems()
    {
        return Arrays.stream(ModItems.class.getDeclaredFields()).filter(f -> Modifier.isStatic(f.getModifiers()) && ItemBase.class.isAssignableFrom(f.getType())).map(f -> {
            try
            {
                return (ItemBase) f.get(null);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException("Unable to reflect upon myself??");
            }
        });
    }
}
