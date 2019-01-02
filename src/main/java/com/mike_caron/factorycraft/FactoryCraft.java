package com.mike_caron.factorycraft;

import com.mike_caron.factorycraft.api.IConveyorBelt;
import com.mike_caron.factorycraft.api.IOreDeposit;
import com.mike_caron.factorycraft.api.IPlayerCrafting;
import com.mike_caron.factorycraft.api.capabilities.CapabilityEnergyManager;
import com.mike_caron.factorycraft.api.capabilities.CapabilityOreDeposit;
import com.mike_caron.factorycraft.api.capabilities.CapabilityPlayerCrafting;
import com.mike_caron.factorycraft.api.energy.IEnergyConnector;
import com.mike_caron.factorycraft.api.energy.IEnergyManager;
import com.mike_caron.factorycraft.capability.*;
import com.mike_caron.factorycraft.client.GuiEventHandler;
import com.mike_caron.factorycraft.energy.EnergyManager;
import com.mike_caron.factorycraft.item.ModItems;
import com.mike_caron.factorycraft.network.ManualCraftingMessage;
import com.mike_caron.factorycraft.proxy.GuiProxy;
import com.mike_caron.factorycraft.proxy.IModProxy;
import com.mike_caron.factorycraft.util.ClientUtil;
import com.mike_caron.factorycraft.world.OreKind;
import com.mike_caron.factorycraft.world.WorldGen;
import com.mike_caron.mikesmodslib.integrations.MainCompatHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
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

    public static final CreativeTabs creativeTab = new CreativeTab();

    public static boolean nonLiveEnvironment = false;

    public static GuiEventHandler guiEventHandler = null;

    @SuppressWarnings("unused")
    @Mod.Instance(modId)
    public static FactoryCraft instance;

    @SidedProxy(
            serverSide = "com.mike_caron.factorycraft.proxy.ServerProxy",
            clientSide = "com.mike_caron.factorycraft.proxy.ClientProxy"
    )
    public static IModProxy proxy;

    public static SimpleNetworkWrapper networkWrapper;

    static {
        if(!TestEnvironment.isTestEnvironment)
        {
            FluidRegistry.enableUniversalBucket();
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit(event);

        MainCompatHandler.registerAllPreInit();

        OreKind.registerDefaultOreKinds();

        GameRegistry.registerWorldGenerator(new WorldGen(), 3);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);

        NetworkRegistry.INSTANCE.registerGuiHandler(FactoryCraft.instance, new GuiProxy());

        CapabilityManager.INSTANCE.register(IOreDeposit.class, new OreDepositCapabilityStorage(), OreDepositDefaultImpl::new);
        CapabilityManager.INSTANCE.register(IConveyorBelt.class, new CapabilityConveyorStorage(), () -> null);
        CapabilityManager.INSTANCE.register(IEnergyManager.class, new CapabilityEnergyManagerStorage(), () -> null);
        CapabilityManager.INSTANCE.register(IEnergyConnector.class, new NullStorage<>(), () -> null);
        CapabilityManager.INSTANCE.register(IPlayerCrafting.class, new NBTSerializableStorage<>(), () -> null);

        ModItems.registerOreDict();

        MainCompatHandler.registerAllInit();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);

        networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(modId);
        networkWrapper.registerMessage(ManualCraftingMessage.Handler.class, ManualCraftingMessage.class, 1, Side.SERVER);
    }

    public void serverStopped(FMLServerStoppedEvent event)
    {
        EnergyManager.cleanUp();
    }


    @SubscribeEvent
    public static void attachDepositCapabilities(AttachCapabilitiesEvent<Chunk> event)
    {
        if(event.getObject().getWorld().isRemote) return;

        event.addCapability(new ResourceLocation(FactoryCraft.modId, "oredeposit"), new CapabilityOreDeposit());

    }

    @SubscribeEvent
    public static void attachWorldCapabilities(AttachCapabilitiesEvent<World> event)
    {
        if(event.getObject().isRemote) return;

        event.addCapability(new ResourceLocation(FactoryCraft.modId, "energyManager"), new CapabilityEnergyManager(event.getObject()));

    }

    @SubscribeEvent
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event)
    {
        if(/*!event.getObject().world.isRemote && */event.getObject() instanceof EntityPlayer)
        {
            event.addCapability(new ResourceLocation(FactoryCraft.modId, "playerCrafting"), new CapabilityPlayerCrafting((EntityPlayer)event.getObject()));
        }
    }

    @SubscribeEvent
    static void onRenderTick(TickEvent.RenderTickEvent evt)
    {
        ClientUtil.tick();
    }

    @SubscribeEvent
    static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END || event.player.world.isRemote)
            return;

        IPlayerCrafting crafting = event.player.getCapability(CapabilityPlayerCrafting.PLAYER_CRAFTING, null);

        if(crafting != null)
            crafting.tick();
    }
}
