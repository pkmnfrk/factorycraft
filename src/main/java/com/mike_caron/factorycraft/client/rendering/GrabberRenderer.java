package com.mike_caron.factorycraft.client.rendering;

import com.mike_caron.factorycraft.tileentity.GrabberTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

public class GrabberRenderer
    extends FastTESR<GrabberTileEntity>
{
    protected static BlockRendererDispatcher blockRenderer;

    @Override
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
