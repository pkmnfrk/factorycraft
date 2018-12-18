package com.mike_caron.factorycraft.proxy;

import com.mike_caron.factorycraft.block.ModBlocks;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy
    extends CommonProxy
{
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        ModBlocks.initModels();
        //ModItems.initModels();

        //ModBlocks.renderFluids();

        //ModelLoaderRegistry.registerLoader(ModelBottle.CustomModelLoader.INSTANCE);
        //ModelBakery.registerItemVariants(ModItems.bottle, ModelBottle.LOCATION);
    }
}
