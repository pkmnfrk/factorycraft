package com.mike_caron.factorycraft.client.rendering;

import com.mike_caron.factorycraft.tileentity.GrabberTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

public class GrabberRenderer
    extends TileEntitySpecialRenderer<GrabberTileEntity>
{
    protected static BlockRendererDispatcher blockRenderer;

    @Override
    public void render(GrabberTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float partial) {
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
        this.renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, partial, buffer);
        buffer.setTranslation(0.0D, 0.0D, 0.0D);
        tessellator.draw();
        RenderHelper.enableStandardItemLighting();


        if(!te.getHeld().isEmpty())
        {
            Vec3i tFacing = te.getFacing().getDirectionVec();
            Vec3i rFacing = te.getFacing().rotateY().getDirectionVec();
            final float armLength = 1f;

            float angle = (float)te.getAngle() / 180f * 195f;

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

    public void renderTileEntityFast(@Nonnull GrabberTileEntity te, double x, double y, double z, float partialTick, int destroyStage, float partial, @Nonnull BufferBuilder renderer)
    {
        if (blockRenderer == null) {
            blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        }
        BlockPos pos = te.getPos();
        IBlockAccess world = MinecraftForgeClient.getRegionRenderCache(te.getWorld(), pos);
        IBlockState state = world.getBlockState(pos);
        if (state.getPropertyKeys().contains(Properties.StaticProperty)) {
            state = state.withProperty(Properties.StaticProperty, false);
        }

        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState exState = (IExtendedBlockState)state;
            if (exState.getUnlistedNames().contains(Properties.AnimationProperty)) {
                float time = Animation.getWorldTime(this.getWorld(), partialTick);
                IAnimationStateMachine capability = (IAnimationStateMachine)te.getCapability(CapabilityAnimation.ANIMATION_CAPABILITY, (EnumFacing)null);
                if (capability != null) {
                    Pair<IModelState, Iterable<Event>> pair = capability.apply(time);
                    te.handleAnimationEvent(time, pair.getRight());
                    IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(exState.getClean());
                    exState = exState.withProperty(Properties.AnimationProperty, pair.getLeft());
                    renderer.setTranslation(x - (double)pos.getX(), y - (double)pos.getY(), z - (double)pos.getZ());
                    blockRenderer.getBlockModelRenderer().renderModel(world, model, exState, pos, renderer, false);
                }
            }
        }
    }
}
