package com.mike_caron.factorycraft.client.gui;

import com.mike_caron.factorycraft.storage.ContainerFurnace;
import com.mike_caron.factorycraft.tileentity.TileEntityFurnace;
import com.mike_caron.mikesmodslib.gui.GuiProgressBar;
import com.mike_caron.mikesmodslib.gui.GuiUtil;

import java.awt.Color;

public class GuiFurnace
    extends GuiTEContainerBase<ContainerFurnace, TileEntityFurnace>
{
    GuiProgressBar fuelSupply    = new GuiProgressBar(30, 46, 116, 4);
    GuiProgressBar drillProgress = new GuiProgressBar(30, 28, 116, 4);

    public GuiFurnace(ContainerFurnace inventorySlotsIn, TileEntityFurnace te)
    {
        super(inventorySlotsIn, te, getWidth(te.getType()), getHeight(te.getType()));

        initControls();
    }

    @Override
    protected void addControls()
    {
        super.addControls();

        if(tileEntity.getType() != 2)
        {
            fuelSupply.setBackColor(Color.BLACK);
            fuelSupply.setForeColor(Color.RED);
            addControl(fuelSupply);
        }

        drillProgress.setBackColor(Color.BLACK);
        drillProgress.setForeColor(Color.GREEN);

        addControl(drillProgress);
    }

    @Override
    protected void onContainerRefresh()
    {
        super.onContainerRefresh();

        if(tileEntity.getType() != 2)
        {
            fuelSupply.setProgress(container.fuelPercent);
        }

        drillProgress.setProgress(container.progressPercent);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i1)
    {
        super.drawGuiContainerBackgroundLayer(v, i, i1);

        GuiUtil.drawTexturePart(guiLeft + 10, guiTop + 20, 18, 18, 0, 180, 256, 256);
        GuiUtil.drawTexturePart(guiLeft + 149, guiTop + 20, 18, 18, 0, 180, 256, 256);

        if(tileEntity.getType() != 2)
        {
            GuiUtil.drawTexturePart(guiLeft + 10, guiTop + 38, 18, 18, 0, 180, 256, 256);
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
