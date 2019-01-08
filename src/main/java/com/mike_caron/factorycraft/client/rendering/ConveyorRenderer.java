package com.mike_caron.factorycraft.client.rendering;

import com.mike_caron.factorycraft.tileentity.TileEntityConveyor;
import com.mike_caron.factorycraft.util.ClientUtil;
import com.mike_caron.factorycraft.util.MathUtil;
import com.mike_caron.factorycraft.util.Tuple2;
import com.mike_caron.mikesmodslib.gui.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.util.*;

public class ConveyorRenderer
    extends TileEntitySpecialRenderer<TileEntityConveyor>
{
    protected static BlockRendererDispatcher blockRenderer;
    static final Map<Tuple2<Item, Integer>, IBakedModel> modelCache = new HashMap<>();

    static final Vector3f SCALE_FACTOR = new Vector3f(0.3f, 0.3f, 0.3f);
    static final Vector3f TRANSLATE_FACTOR = new Vector3f(-0.5f, 0, -0.5f);

    static final List<Tuple2<ItemStack, Vector4f>> toBeDrawn = new ArrayList<>();
    static int lastRenderTick = -1;

    static final Map<Integer, Deque<int[]>> intBufferPool = new HashMap<>();
    static final Deque<Vector4f> vectorPool = new ArrayDeque<>();

    @Override
    public void render(TileEntityConveyor te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        if (blockRenderer == null) {
            blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        }

        if(lastRenderTick != ClientUtil.getFrame())
        {
            drawAllPending();
        }

        TileEntityConveyor.ItemPosition position = te.itemPositions();

        Vector3f normal = position.getNormal();
        float normalAngle = MathUtil.angle(new Vector3f(0, 1, 0), normal, normal);

        EntityPlayer player = Minecraft.getMinecraft().player;
        float distSq = MathUtil.distanceSq(player.posX, player.posY, player.posZ, te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());

        if(distSq > 32*32)
            return;

        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

        position.visitAllPositions((itemStack, itemPos) -> {
            IBakedModel model = getModel(itemStack);

            if(model.isBuiltInRenderer())
            {
                //...
                GlStateManager.pushMatrix();
                GlStateManager.translate(x + itemPos.x, y + itemPos.y, z + itemPos.z);
                GlStateManager.rotate(itemPos.w, 0, 1, 0);
                renderItem.renderItem(itemStack, ItemCameraTransforms.TransformType.GROUND);
                GlStateManager.popMatrix();
            }
            else
            {
                Vector4f finalPos = acquireVector4f();
                finalPos.set((float)(x + itemPos.x),  (float)(y + itemPos.y), (float)(z + itemPos.z), itemPos.w);

                toBeDrawn.add(new Tuple2<>(itemStack, finalPos));
            }

            return true;
        });
    }

    public static void drawAllPending()
    {
        lastRenderTick = ClientUtil.getFrame();

        if(toBeDrawn.isEmpty())
            return;

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buffer = tes.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

        Matrix4f transform = new Matrix4f();
        Vector3f tmp = new Vector3f();
        for(Tuple2<ItemStack, Vector4f> item : toBeDrawn)
        {
            transform.setIdentity();
            tmp.set(item.second.x, item.second.y, item.second.z);
            transform.translate(tmp);
            transform.scale(SCALE_FACTOR);
            tmp.set(0, 1, 0);
            transform.rotate((float)Math.toRadians(item.second.w), tmp);
            //localTransform.rotate(normalAngle, normal); 
            transform.translate(TRANSLATE_FACTOR);

            IBakedModel model = getModel(item.first);

            renderQuads(buffer, model, transform);

            releaseVector4f(item.second);
        }

        GuiUtil.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        tes.draw();

        toBeDrawn.clear();
    }

    private static void renderQuads(BufferBuilder buffer, IBakedModel model, Matrix4f transform)
    {
        renderQuads(buffer, model.getQuads(null, EnumFacing.UP, 0), transform);
        renderQuads(buffer, model.getQuads(null, EnumFacing.DOWN, 0), transform);
        renderQuads(buffer, model.getQuads(null, EnumFacing.NORTH, 0), transform);
        renderQuads(buffer, model.getQuads(null, EnumFacing.SOUTH, 0), transform);
        renderQuads(buffer, model.getQuads(null, EnumFacing.EAST, 0), transform);
        renderQuads(buffer, model.getQuads(null, EnumFacing.WEST, 0), transform);
        renderQuads(buffer, model.getQuads(null, null, 0), transform);

    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static void renderQuads(BufferBuilder buffer, List<BakedQuad> quads, Matrix4f transform)
    {
        if(quads.isEmpty()) return;

        for(int i = 0; i < quads.size(); i++)
        {
            renderQuad(buffer, quads.get(i), transform);
        }
    }

    private static void renderQuad(BufferBuilder buffer, BakedQuad quad, Matrix4f transform)
    {
        int positionOffset = -1;
        int totalSize = quad.getFormat().getIntegerSize();

        for(int i = 0; i < quad.getFormat().getElementCount(); i++)
        {
            VertexFormatElement el = quad.getFormat().getElement(i);
            if(el.getUsage() == VertexFormatElement.EnumUsage.POSITION)
            {
                positionOffset = quad.getFormat().getOffset(i);
                if(positionOffset % 4 != 0)
                {
                    throw new RuntimeException("Can't be lazy about offsets");
                }
                positionOffset /= 4;
                break;
            }
        }

        int[] data = quad.getVertexData();
        boolean releaseArray = false;

        if(positionOffset != -1)
        {
            releaseArray = true;
            int[] newData = acquireIntBuffer(data);

            for(int i = 0; i < newData.length; i += totalSize)
            {
                Vector4f pos = acquireVector4f();
                pos.set(
                    Float.intBitsToFloat(newData[i + positionOffset]),
                    Float.intBitsToFloat(newData[i + positionOffset + 1]),
                    Float.intBitsToFloat(newData[i + positionOffset + 2]),
                    1
                );

                Matrix4f.transform(transform, pos, pos);

                newData[i + positionOffset] = Float.floatToIntBits(pos.x);
                newData[i + positionOffset + 1] = Float.floatToIntBits(pos.y);
                newData[i + positionOffset + 2] = Float.floatToIntBits(pos.z);

                releaseVector4f(pos);

            }

            data = newData;
        }

        buffer.addVertexData(data);

        if(releaseArray)
        {
            releaseIntBuffer(data);
        }
    }

    private static IBakedModel getModel(ItemStack itemStack)
    {
        Item item = itemStack.getItem();
        int meta = itemStack.getMetadata();

        Tuple2<Item, Integer> key = new Tuple2<>(item, meta);


        if(!modelCache.containsKey(key))
        {
            ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
            IBakedModel model = mesher.getItemModel(itemStack);
            model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GROUND, false);
            modelCache.put(key, model);
            return model;
        }

        return modelCache.get(key);
    }

    private static int[] acquireIntBuffer(int[] copy)
    {
        int size = copy.length;
        if(!intBufferPool.containsKey(size))
        {
            intBufferPool.put(size, new ArrayDeque<>());
        }

        int[] ret;

        if(intBufferPool.get(size).isEmpty())
        {
            ret = new int[size];
        }
        else
        {
            ret = intBufferPool.get(size).pop();
        }

        System.arraycopy(copy, 0, ret, 0, copy.length);

        return ret;
    }

    private static void releaseIntBuffer(int[] buffer)
    {
        int size = buffer.length;
        if(!intBufferPool.containsKey(size))
        {
            intBufferPool.put(size, new ArrayDeque<>());
        }

        intBufferPool.get(size).push(buffer);
    }

    private static Vector4f acquireVector4f()
    {
        if(vectorPool.isEmpty())
        {
            return new Vector4f();
        }

        return vectorPool.pop();
    }

    private static void releaseVector4f(Vector4f vector)
    {
        vectorPool.push(vector);
    }
}
