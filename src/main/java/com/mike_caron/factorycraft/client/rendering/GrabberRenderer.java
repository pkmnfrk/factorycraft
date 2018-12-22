package com.mike_caron.factorycraft.client.rendering;

import com.mike_caron.factorycraft.tileentity.TileEntityGrabber;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;

import javax.annotation.Nonnull;

public class GrabberRenderer
    extends TileEntitySpecialRenderer<TileEntityGrabber>
{
    protected static BlockRendererDispatcher blockRenderer;

    @Override
    public void render(TileEntityGrabber te, double x, double y, double z, float partialTicks, int destroyStage, float partial) {
        if (blockRenderer == null) {
            blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(7425);
        } else {
            GlStateManager.shadeModel(7424);
        }

        buffer.begin(7, DefaultVertexFormats.BLOCK);

        //buffer.setTranslation(x, y, z);
        //buffer.setTranslation(-0.5, 2f/16f, 0.5);
        this.renderArm(te, x, y, z, partialTicks, destroyStage, partial, buffer);
        buffer.setTranslation(0, 0, 0);

        Vec3i tFacing = te.getFacing().getDirectionVec();
        Vec3i rFacing = te.getFacing().rotateY().getDirectionVec();

        float angle = (float)te.getAngle(partialTicks);

        BlockPos pos = te.getPos();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0.5, 1.5f/16f, 0.5);
        GlStateManager.scale(tFacing.getX() != 0 ? -1 : 1, -1, tFacing.getZ() != 0 ? -1 : 1);
        GlStateManager.rotate(angle, rFacing.getX(), 0, rFacing.getZ());
        GlStateManager.translate(-0.5, -1.5f/16f, -0.5);

        tessellator.draw();

        GlStateManager.popMatrix();
        RenderHelper.enableStandardItemLighting();


        if(!te.getHeld().isEmpty())
        {
            final float armLength = 1f;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5, y + 1/16f, z + 0.5);
            GlStateManager.rotate(angle, rFacing.getX(), 0, rFacing.getZ());
            //GlStateManager.translate(0, -3/16f,0);
            GlStateManager.translate(tFacing.getX() * armLength, -2/16f, tFacing.getZ() * armLength);
            //GlStateManager.rotate(-angle, rFacing.getX(), 0, rFacing.getZ());
            GlStateManager.rotate(te.getFacing().getZOffset() * 90, 0, 1, 0);


            Minecraft.getMinecraft().getRenderItem().renderItem(te.getHeld(), ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }

    }

    public void renderArm(@Nonnull TileEntityGrabber te, double x, double y, double z, float partialTick, int destroyStage, float partial, @Nonnull BufferBuilder renderer)
    {

        BlockPos pos = te.getPos();
        IBlockAccess world = MinecraftForgeClient.getRegionRenderCache(te.getWorld(), pos);
        IBlockState state = world.getBlockState(pos);
        if (state.getPropertyKeys().contains(Properties.StaticProperty)) {
            state = state.withProperty(Properties.StaticProperty, false);
        }

        IExtendedBlockState exState = (IExtendedBlockState)state;

        //float time = Animation.getWorldTime(this.getWorld(), partialTick);

        IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(exState.getClean());

        //renderer.setTranslation(x - (double)pos.getX(), y - (double)pos.getY(), z - (double)pos.getZ());

        blockRenderer.getBlockModelRenderer().renderModel(world, model, exState, BlockPos.ORIGIN, renderer, false);



    }
}
