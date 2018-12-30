package com.mike_caron.factorycraft.client.gui;

import com.mike_caron.factorycraft.storage.ContainerDrill;
import com.mike_caron.factorycraft.tileentity.TileEntityDrill;
import com.mike_caron.mikesmodslib.gui.GuiImageItemStack;
import com.mike_caron.mikesmodslib.gui.GuiProgressBar;
import com.mike_caron.mikesmodslib.gui.GuiUtil;
import net.minecraft.item.ItemStack;

import java.awt.Color;

public class GuiFurnace
    extends GuiTEContainerBase<ContainerDrill, TileEntityDrill>
{
    GuiProgressBar fuelSupply = new GuiProgressBar(30, 28, 116, 4);
    GuiProgressBar drillProgress = new GuiProgressBar(10, 28, 136, 4);
    GuiImageItemStack miningTarget = new GuiImageItemStack(149, 21, ItemStack.EMPTY);

    public GuiFurnace(ContainerDrill inventorySlotsIn, TileEntityDrill te)
    {
        super(inventorySlotsIn, te, getWidth(te.getType()), getHeight(te.getType()));

        initControls();
    }

    @Override
    protected void addControls()
    {
        super.addControls();

        if(tileEntity.getType() == 0)
        {
            fuelSupply.setBackColor(Color.BLACK);
            fuelSupply.setForeColor(Color.RED);
            addControl(fuelSupply);

            drillProgress.setY(46);
            miningTarget.setY(37);
            drillProgress.setX(30);
            drillProgress.setWidth(116);
        }

        drillProgress.setBackColor(Color.BLACK);
        drillProgress.setForeColor(Color.GREEN);

        addControl(drillProgress);
        addControl(miningTarget);
    }

    @Override
    protected void onContainerRefresh()
    {
        super.onContainerRefresh();

        if(tileEntity.getType() == 0)
        {
            fuelSupply.setProgress(container.fuelPercent);
        }

        drillProgress.setProgress(container.progressPercent);
        miningTarget.setItemStack(container.miningTarget);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i1)
    {
        super.drawGuiContainerBackgroundLayer(v, i, i1);

        if(tileEntity.getType() == 0)
        {
            GuiUtil.drawTexturePart(guiLeft + 10, guiTop + 20, 18, 18, 0, 180, 256, 256);
            GuiUtil.drawTexturePart(guiLeft + 148,guiTop + 36, 18, 18, 0, 180, 256, 256);
        }
        else
        {
            GuiUtil.drawTexturePart(guiLeft + 148,guiTop + 20, 18, 18, 0, 180, 256, 256);
        }

    }

    public static int getWidth(int type)
    {
        return 176;
    }

    public static int getHeight(int type)
    {
        switch(type)
        {
            case 0:
                return 150;
            default:
                return 130;
        }
    }
}
