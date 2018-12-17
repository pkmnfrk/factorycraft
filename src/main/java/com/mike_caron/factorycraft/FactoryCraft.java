package com.mike_caron.factorycraft;

import com.mike_caron.mikesmodslib.integrations.MainCompatHandler;
import com.mike_caron.mikesmodslib.network.CtoSMessage;
import com.mike_caron.mikesmodslib.network.MessageUpdateGui;
import com.mike_caron.factorycraft.proxy.IModProxy;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
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

    @SuppressWarnings("unused")
    @net.minecraftforge.fml.common.Mod.Instance(modId)
    public static FactoryCraft instance;

    @SidedProxy(
            serverSide = "CommonProxy",
            clientSide = "ClientProxy"
    )
    public static IModProxy proxy;

    public static SimpleNetworkWrapper networkWrapper;

    @net.minecraftforge.fml.common.Mod.EventHandler
    public  void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit(event);

        MainCompatHandler.registerAllPreInit();
    }

    @net.minecraftforge.fml.common.Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);

        MainCompatHandler.registerAllInit();
    }

    @net.minecraftforge.fml.common.Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(modId);
        networkWrapper.registerMessage(CtoSMessage.Handler.class, CtoSMessage.class, 2, Side.SERVER);
        networkWrapper.registerMessage(MessageUpdateGui.Handler.class, MessageUpdateGui.class, 3, Side.CLIENT);
    }

}
