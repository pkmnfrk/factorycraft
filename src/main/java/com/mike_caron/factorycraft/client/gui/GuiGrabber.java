package com.mike_caron.factorycraft.client.gui;

import com.mike_caron.factorycraft.storage.ContainerGrabber;
import com.mike_caron.factorycraft.tileentity.TileEntityGrabber;
import com.mike_caron.mikesmodslib.gui.GuiProgressBar;
import com.mike_caron.mikesmodslib.gui.GuiUtil;

import java.awt.Color;

public class GuiGrabber
    extends GuiBase<ContainerGrabber, TileEntityGrabber>
{
    GuiProgressBar fuelSupply = new GuiProgressBar(30, 28, 176 - 10 - 10 - 20, 4);

    public GuiGrabber(ContainerGrabber inventorySlotsIn, TileEntityGrabber te)
    {
        super(inventorySlotsIn, te, getWidth(te.getType()), getHeight(te.getType()));

        initControls();
    }

    @Override
    protected void addControls()
    {
        super.addControls();

        fuelSupply.setBackColor(Color.BLACK);
        fuelSupply.setForeColor(Color.RED);
        fuelSupply.setProgress(0.5f);

        addControl(fuelSupply);
    }

    @Override
    protected void onContainerRefresh()
    {
        super.onContainerRefresh();

        fuelSupply.setProgress(container.fuelPercent);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i1)
    {
        super.drawGuiContainerBackgroundLayer(v, i, i1);

        GuiUtil.drawTexturePart(guiLeft  + 10,guiTop + 20, 18, 18, 0, 180, 256, 256);
    }

    public static int getWidth(int type)
    {
        return 176;
    }

    public static int getHeight(int type)
    {
        switch(type)
        {
            default:
                return 130;
        }
    }

    @Override
    protected String getTitleKey()
    {
        return "tile.factorycraft:grabber_burner.name";
    }
}
