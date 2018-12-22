package com.mike_caron.factorycraft;

import com.mike_caron.factorycraft.api.IConveyorBelt;
import com.mike_caron.factorycraft.api.IOreDeposit;
import com.mike_caron.factorycraft.capability.CapabilityConveyorStorage;
import com.mike_caron.factorycraft.capability.OreDepositCapabilityProvider;
import com.mike_caron.factorycraft.capability.OreDepositCapabilityStorage;
import com.mike_caron.factorycraft.capability.OreDepositDefaultImpl;
import com.mike_caron.factorycraft.proxy.IModProxy;
import com.mike_caron.factorycraft.world.WorldGen;
import com.mike_caron.mikesmodslib.integrations.MainCompatHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
@net.minecraftforge.fml.common.Mod(
        modid = FactoryCraft.modId,
        name = FactoryCraft.name,
        version = FactoryCraft.version,
        acceptedMinecraftVersions = "[1.12.2]"
        ,dependencies = "" +
                        ";after:theoneprobe" +
                        ";after:waila"
)
@net.minecraftforge.fml.common.Mod.EventBusSubscriber
public class FactoryCraft
{
    public static final String modId = "factorycraft";
    public static final String name = "FactoryCraft";
    public static final String version = "0.0.1";

    public static final Logger logger = LogManager.getLogger(modId);

    public static final CreativeTabs creativeTab = new CreativeTab();

    @SuppressWarnings("unused")
    @Mod.Instance(modId)
    public static FactoryCraft instance;

    @SidedProxy(
            serverSide = "com.mike_caron.factorycraft.proxy.ServerProxy",
            clientSide = "com.mike_caron.factorycraft.proxy.ClientProxy"
    )
    public static IModProxy proxy;

    //public static SimpleNetworkWrapper networkWrapper;

    static {
        FluidRegistry.enableUniversalBucket();
    }

    @Mod.EventHandler
    public  void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit(event);

        MainCompatHandler.registerAllPreInit();

        GameRegistry.registerWorldGenerator(new WorldGen(), 3);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);

        CapabilityManager.INSTANCE.register(IOreDeposit.class, new OreDepositCapabilityStorage(), OreDepositDefaultImpl::new);
        CapabilityManager.INSTANCE.register(IConveyorBelt.class, new CapabilityConveyorStorage(), () -> null);

        MainCompatHandler.registerAllInit();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }



    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Chunk> event)
    {
        if(event.getObject().getWorld().isRemote) return;

        event.addCapability(new ResourceLocation(FactoryCraft.modId, "oredeposit"), new OreDepositCapabilityProvider());

    }

}
