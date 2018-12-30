package com.mike_caron.factorycraft.client.rendering;

import com.mike_caron.factorycraft.tileentity.TileEntityConveyor;
import com.mike_caron.factorycraft.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.util.vector.Vector3f;

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

        Vector3f normal = position.getNormal();
        float normalAngle = MathUtil.angle(new Vector3f(0, 1, 0), normal, normal);

        position.visitAllPositions((itemStack, itemPos) -> {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + itemPos.x, y + itemPos.y, z + itemPos.z);
            GlStateManager.rotate(itemPos.w, 0, 1, 0);
            GlStateManager.rotate(normalAngle, normal.x, normal.y, normal.z);
            Minecraft.getMinecraft().getRenderItem().renderItem(itemStack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        });
    }
}
