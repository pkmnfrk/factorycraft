package com.mike_caron.factorycraft.client.rendering;

import com.mike_caron.factorycraft.tileentity.TileEntityConveyor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class ConveyorRenderer
    extends TileEntitySpecialRenderer<TileEntityConveyor>
{
    protected static BlockRendererDispatcher blockRenderer;

    @Override
    public void render(TileEntityConveyor te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        if (blockRenderer == null) {
            blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        }

        TileEntityConveyor.ItemPosition position = te.itemPositions();

        position.visitAllPositions((itemStack, itemPos) -> {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + itemPos.x, y + 0.125f, z + itemPos.z);
            GlStateManager.rotate(itemPos.y, 0, 1, 0);
            Minecraft.getMinecraft().getRenderItem().renderItem(itemStack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        });
    }
}
