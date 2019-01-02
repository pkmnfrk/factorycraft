package com.mike_caron.factorycraft.client.gui;

import com.mike_caron.factorycraft.FactoryCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiFCCraftingInventory
    extends GuiInventory
{
    protected static final ResourceLocation background = new ResourceLocation(FactoryCraft.modId, "textures/gui/misc.png");

    private GuiCraftingScreen gui;
    private boolean inited = false;

    public GuiFCCraftingInventory(EntityPlayer player)
    {
        super(player);
        gui = new GuiCraftingScreen(0, 0, background, player.inventory);
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height)
    {
        super.setWorldAndResolution(mc, width, height);

        gui.setWorldAndResolution(mc, width, height);

        inited = true;
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        gui.updateScreen();

        int craftingWidth = Math.min(guiLeft - 10, 176);

        gui.setPosition(guiLeft - craftingWidth - 5, guiTop, craftingWidth, ySize);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        if(inited)
        {
            gui.drawGuiContainerBackgroundLayer(mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        if(inited)
        {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) -this.guiLeft, (float) -this.guiTop, 0.0F);

            gui.drawForeground(mouseX, mouseY);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void initGui()
    {
        super.initGui();

        if(inited)
        {
            gui.initGui();
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        if(inited)
        {
            gui.handleMouseInput();
        }
    }


    /*
    @Override
    public void drawGuiContainerBackgroundLayer(int mouseX, int mouseY)
    {
        GuiUtil.setGLColor(Color.WHITE);
        GuiUtil.bindTexture(background);
        GuiUtil.draw3x3Stretched(guiLeft, guiTop, xSize, ySize, 32, 32);
        //GuiUtil.drawTexturePart(guiLeft + xSize / 2 - 81, guiTop + ySize - 83, 162, 76, 0, 180, 256, 256);
    }
    */
}
