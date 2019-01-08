package com.mike_caron.factorycraft.client.rendering;

import com.mike_caron.factorycraft.tileentity.TileEntityConveyor;
import com.mike_caron.factorycraft.util.MathUtil;
import com.mike_caron.factorycraft.util.Tuple2;
import com.mike_caron.mikesmodslib.gui.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConveyorRenderer
    extends TileEntitySpecialRenderer<TileEntityConveyor>
{
    protected static BlockRendererDispatcher blockRenderer;
    static final Map<Tuple2<Item, Integer>, IBakedModel> modelCache = new HashMap<>();

    static final Vector3f SCALE_FACTOR = new Vector3f(0.3f, 0.3f, 0.3f);
    static final Vector3f TRANSLATE_FACTOR = new Vector3f(-0.5f, 0, -0.5f);

    @Override
    public void render(TileEntityConveyor te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        if (blockRenderer == null) {
            blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        }

        TileEntityConveyor.ItemPosition position = te.itemPositions();

        Vector3f normal = position.getNormal();
        float normalAngle = MathUtil.angle(new Vector3f(0, 1, 0), normal, normal);

        EntityPlayer player = Minecraft.getMinecraft().player;
        float distSq = MathUtil.distanceSq(player.posX, player.posY, player.posZ, te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());

        if(distSq > 32*32)
            return;


        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

        //MyVertexConsumer vertexConsumer = new MyVertexConsumer(buffer, null);

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

        Matrix4f transform = new Matrix4f();

        transform.setIdentity();

        transform.translate(new Vector3f((float) x, (float)y, (float)z));

        Matrix4f localTransform = new Matrix4f();

        position.visitAllPositions((itemStack, itemPos) -> {
            IBakedModel model = getModel(itemStack);

            //vertexConsumer.setTransform(localTransform);

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
                Matrix4f.load(transform, localTransform);
                localTransform.translate(new Vector3f(itemPos.x, itemPos.y, itemPos.z));
                localTransform.scale(SCALE_FACTOR);
                localTransform.rotate((float)Math.toRadians(itemPos.w), new Vector3f(0, 1, 0));
                //localTransform.rotate(normalAngle, normal);
                localTransform.translate(TRANSLATE_FACTOR);

                renderQuads(buffer, model, localTransform);
            }


            //GlStateManager.popMatrix();
            return true;
        });
        buffer.setTranslation(0, 0, 0);


        //GlStateManager.pushMatrix();
        //GlStateManager.translate(x, y, z);
        GuiUtil.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        tessellator.draw();

        //GlStateManager.popMatrix();

    }

    private void renderQuads(BufferBuilder buffer, IBakedModel model, Matrix4f transform)
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
    private void renderQuads(BufferBuilder buffer, List<BakedQuad> quads, Matrix4f transform)
    {
        if(quads.isEmpty()) return;

        for(int i = 0; i < quads.size(); i++)
        {
            renderQuad(buffer, quads.get(i), transform);
        }
    }

    private void renderQuad(BufferBuilder buffer, BakedQuad quad, Matrix4f transform)
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

        if(positionOffset != -1)
        {
            int[] newData = Arrays.copyOf(data, data.length);

            for(int i = 0; i < newData.length; i += totalSize)
            {
                Vector4f pos = new Vector4f(
                    Float.intBitsToFloat(newData[i + positionOffset]),
                    Float.intBitsToFloat(newData[i + positionOffset + 1]),
                    Float.intBitsToFloat(newData[i + positionOffset + 2]),
                    1
                );

                Matrix4f.transform(transform, pos, pos);

                newData[i + positionOffset] = Float.floatToIntBits(pos.x);
                newData[i + positionOffset + 1] = Float.floatToIntBits(pos.y);
                newData[i + positionOffset + 2] = Float.floatToIntBits(pos.z);

            }

            data = newData;
        }

        buffer.addVertexData(data);
    }

    private IBakedModel getModel(ItemStack itemStack)
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

    class MyVertexConsumer
        implements IVertexConsumer
    {
        private BufferBuilder buffer;
        private Matrix4f transform;
        TextureAtlasSprite textureAtlasSprite;

        int sequence = 0;

        public MyVertexConsumer(BufferBuilder buffer, Matrix4f transform)
        {
            this.buffer = buffer;
            this.transform = transform;
        }

        @Override
        @Nonnull
        public VertexFormat getVertexFormat()
        {
            return buffer.getVertexFormat();
        }

        @Override
        public void setQuadTint(int i)
        {

        }

        @Override
        public void setQuadOrientation(@Nonnull EnumFacing enumFacing)
        {

        }

        @Override
        public void setApplyDiffuseLighting(boolean b)
        {

        }

        @Override
        public void setTexture(@Nonnull TextureAtlasSprite textureAtlasSprite)
        {
            this.textureAtlasSprite = textureAtlasSprite;
        }

        public void setTransform(Matrix4f transform)
        {
            this.transform = transform;
        }

        @Override
        public void put(int element, @Nonnull float... datum)
        {
            VertexFormat format = getVertexFormat();
            VertexFormatElement el = format.getElement(element);
            VertexFormatElement.EnumType type = el.getType();
            switch(el.getUsage())
            {
                case POSITION:
                    if (type != VertexFormatElement.EnumType.FLOAT)
                        throw new RuntimeException("Unexpected position format " + el.getType());

                    Vector4f pos = new Vector4f(datum[0], datum[1], datum[2], 0);

                    Matrix4f.transform(transform, pos, pos);

                    buffer.pos(pos.x, pos.y, pos.z);
                    break;

                case COLOR:
                    if (type == VertexFormatElement.EnumType.FLOAT)
                    {
                        buffer.color(datum[0], datum[1], datum[2], datum[3]);
                    }
                    else if (type == VertexFormatElement.EnumType.BYTE || type == VertexFormatElement.EnumType.UBYTE || type == VertexFormatElement.EnumType.UINT || type == VertexFormatElement.EnumType.USHORT || type == VertexFormatElement.EnumType.INT || type == VertexFormatElement.EnumType.SHORT)
                    {
                        buffer.color((int) datum[0], (int) datum[1], (int) datum[2], (int) datum[3]);
                    }
                    else
                        throw new RuntimeException("Unexpected color format " + el.getType());

                    break;

                case UV:
                    if (type == VertexFormatElement.EnumType.FLOAT)
                    {
                        buffer.tex(datum[0], datum[1]);
                    }
                    else if(type == VertexFormatElement.EnumType.BYTE)
                    {
                        buffer.tex((float)(byte)datum[0], (float)(byte)datum[1]);
                    }
                    else
                        throw new RuntimeException("Unexpected uv format " + el.getType());


                    break;

                case NORMAL:
                    if (type == VertexFormatElement.EnumType.FLOAT)
                    {
                        buffer.normal(datum[0], datum[1], datum[2]);
                    }
                    else if(type == VertexFormatElement.EnumType.BYTE)
                    {
                        buffer.normal((float)(byte)datum[0], (float)(byte)datum[1], (float)(byte)datum[2]);
                    }
                    else
                        throw new RuntimeException("Unexpected normal format " + el.getType());

                    break;
                case PADDING:
                    break;
                case GENERIC:
                default:
                    throw new RuntimeException("Can't handle " + el.getUsage());
            }

            sequence += 1;

            if(sequence >= format.getElementCount())
            {
                buffer.endVertex();
                sequence = 0;
            }
        }
    }
}
