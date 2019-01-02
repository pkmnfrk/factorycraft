package com.mike_caron.factorycraft.client;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.client.gui.GuiFCCraftingInventory;
import com.mike_caron.factorycraft.client.rendering.CraftingQueueRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiEventHandler
{
    CraftingQueueRenderer craftingQueueRenderer;

    public GuiEventHandler()
    {
        craftingQueueRenderer = new CraftingQueueRenderer();
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event)
    {
        Gui gui = event.getGui();
        if(gui == null)
            return;

        FactoryCraft.logger.info("Gui opened: {}", gui.getClass().getName());

        if(gui instanceof GuiInventory && !(gui instanceof GuiFCCraftingInventory))
        {
            event.setGui(new GuiFCCraftingInventory(Minecraft.getMinecraft().player));
        }
    }

    /*
    @SubscribeEvent
    public void onDrawForeground(RenderGameOverlayEvent event)
    {
        if(event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR)
            return;

        craftingQueueRenderer.render();
    }
    */

}
