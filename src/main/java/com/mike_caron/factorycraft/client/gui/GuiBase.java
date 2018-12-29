package com.mike_caron.factorycraft.client.gui;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.mikesmodslib.block.ContainerBase;
import com.mike_caron.mikesmodslib.block.TileEntityBase;
import com.mike_caron.mikesmodslib.gui.GuiContainerBase;
import com.mike_caron.mikesmodslib.gui.GuiUtil;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;

public class GuiBase<C extends ContainerBase, T extends TileEntityBase>
    extends GuiContainerBase
{
    protected static final ResourceLocation background = new ResourceLocation(FactoryCraft.modId, "textures/gui/misc.png");

    protected C container;
    protected T tileEntity;

    public GuiBase(C inventorySlotsIn, T tileEntity, int width, int height)
    {
        super(inventorySlotsIn, width, height);
        this.container = inventorySlotsIn;
        this.tileEntity = tileEntity;
        this.setForeColor(Color.WHITE);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i1)
    {
        GuiUtil.setGLColor(Color.WHITE);
        GuiUtil.bindTexture(background);
        GuiUtil.draw3x3Stretched(guiLeft, guiTop, xSize, ySize, 32, 32);
        GuiUtil.drawTexturePart(guiLeft + xSize / 2 - 81, guiTop + ySize - 83, 162, 76, 0, 180, 256, 256);
    }
}
