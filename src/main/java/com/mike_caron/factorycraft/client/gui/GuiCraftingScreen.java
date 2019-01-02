package com.mike_caron.factorycraft.client.gui;

import com.google.common.base.Preconditions;
import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.api.IPlayerCrafting;
import com.mike_caron.factorycraft.api.capabilities.CapabilityPlayerCrafting;
import com.mike_caron.factorycraft.item.crafting.NormalRecipes;
import com.mike_caron.factorycraft.network.ManualCraftingMessage;
import com.mike_caron.mikesmodslib.gui.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class GuiCraftingScreen
    extends GuiBase
    implements GuiButton.ClickedListener
{
    private List<Item> craftingRecipes;
    private final List<GuiButton> craftingButtons = new ArrayList<>();
    private InventoryPlayer inventoryPlayer;
    private int invChangeCount;

    public GuiCraftingScreen(int width, int height, ResourceLocation background, InventoryPlayer inventoryPlayer)
    {
        super(width, height, background);

        craftingRecipes = new ArrayList<>(NormalRecipes.INSTANCE.craftingItems());
        this.inventoryPlayer = inventoryPlayer;
        invChangeCount = this.inventoryPlayer.getTimesChanged();

        initControls();
    }

    @Override
    protected void addControls()
    {
        //super.addControls();

        setForeColor(Color.WHITE);

        for(int i = 0; i < craftingRecipes.size(); i++)
        {
            GuiImage img = new GuiImageItemStack(0, 0, new ItemStack(craftingRecipes.get(i), 1));

            GuiButton button = new GuiButton(i + 1, 0, 0, 20, 20, null, img);

            button.addListener(this);
            this.addControl(button);
            this.craftingButtons.add(button);
        }

        layoutButtons();
    }

    @Override
    public void clicked(GuiButton.ClickedEvent clickedEvent)
    {
        if(clickedEvent.id >= 1)
        {
            Item item = craftingRecipes.get(clickedEvent.id - 1);
            int amt = 1;

            if(isShiftKeyDown())
            {
                amt = 5;
            }

            ManualCraftingMessage msg = new ManualCraftingMessage(item, amt);
            FactoryCraft.networkWrapper.sendToServer(msg);
        }
    }

    private void layoutButtons()
    {
        int bx = 5;
        int by = 5;

        IPlayerCrafting playerCrafting = inventoryPlayer.player.getCapability(CapabilityPlayerCrafting.PLAYER_CRAFTING, null);

        Preconditions.checkNotNull(playerCrafting);

        final int spacing = 19;

        for(int i = 0; i < craftingButtons.size(); i++)
        {
            GuiButton btn = craftingButtons.get(i);
            btn.setX(bx);
            btn.setY(by);

            bx += spacing;
            if(bx + spacing > xSize - 5)
            {
                bx = 5;
                by += spacing;
            }

            int max = playerCrafting.maxCouldCraft(craftingRecipes.get(i));
            if(max <= 0)
            {
                btn.setEnabled(false);
            }
            else
            {
                btn.setEnabled(true);
            }
        }
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height)
    {
        super.setWorldAndResolution(mc, width, height);

        layoutButtons();
    }

    public void setPosition(int x, int y, int xSize, int ySize)
    {
        this.guiLeft = x;
        this.guiTop = y;
        int oldXSize = this.xSize;
        int oldYSize = this.ySize;
        this.xSize = xSize;
        this.ySize = ySize;
        if(oldXSize != xSize || oldYSize != ySize || invChangeCount != inventoryPlayer.getTimesChanged())
        {
            layoutButtons();
            invChangeCount = inventoryPlayer.getTimesChanged();

        }
    }

    @Override
    public void drawGuiContainerBackgroundLayer(int mouseX, int mouseY)
    {
        GuiUtil.setGLColor(Color.WHITE);
        GuiUtil.bindTexture(background);
        GuiUtil.draw3x3Stretched(guiLeft, guiTop, xSize, ySize, 32, 32);
    }

    public void drawForeground(int mouseX, int mouseY)
    {
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}
