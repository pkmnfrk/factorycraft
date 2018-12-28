package com.mike_caron.factorycraft.client.rendering;

import com.mike_caron.factorycraft.tileentity.TileEntityElectricalPole;
import com.mike_caron.factorycraft.util.ClientUtil;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class ElectricalPoleRenderer
    extends TileEntitySpecialRenderer<TileEntityElectricalPole>
{

    @Override
    public void render(TileEntityElectricalPole te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        List<BlockPos> connections = te.getConnections();
        if(connections == null || connections.isEmpty()) return;

        ClientUtil.renderConnections(connections, x, y, z);
    }


}
