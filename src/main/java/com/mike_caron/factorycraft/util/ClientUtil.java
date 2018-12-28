package com.mike_caron.factorycraft.util;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientUtil
{
    private static final Set<Tuple2<Tuple3i, Tuple3i>> renderedConnections = new HashSet<>();
    private static final Color COPPER = new Color(255, 122, 0, 255);
    private static int frame = 0;

    private static final byte[] wireIndices = new byte[] {
        0, 1, 3,
        0, 3, 2,
        2, 3, 5,
        2, 5, 4,
        4, 5, 7,
        4, 7, 6,
        6, 7, 1,
        6, 1, 0
    };

    private static final Vector3f[] wireVertices = new Vector3f[] {
        new Vector3f( 0, -1, 0), //0
        new Vector3f( 0, -1, 1), //1
        new Vector3f(-1,  0, 0), //2
        new Vector3f(-1,  0, 1), //3
        new Vector3f( 0,  1, 0), //4
        new Vector3f( 0,  1, 1), //5
        new Vector3f( 1,  0, 0), //6
        new Vector3f( 1,  0, 1), //7
    };

    public static void renderConnection(Vector3f start, Vector3f end, float radius, Color col, BufferBuilder buffer)
    {
        //FactoryCraft.logger.info("Rendering {} to {}", start, end);

        Vector3f rotation = new Vector3f();
        Vector3f forward = new Vector3f(0, 0, 1);
        Vector3f delta = new Vector3f();
        Vector3f.sub(end, start, delta);
        float angle = MathUtil.angle(forward, delta, rotation);
        float length = MathUtil.distance(start, end);

        //FactoryCraft.logger.info("Rotation vector: {}", rotation);

        //length = 1f;
        Matrix4f transformMatrix = new Matrix4f();
        transformMatrix.setIdentity();
        transformMatrix.translate(start);
        //transformMatrix.rotate((float)Math.toRadians(frame / 3.0), new Vector3f(0, 1, 0));
        transformMatrix.rotate(angle, rotation);
        transformMatrix.scale(new Vector3f(radius, radius, length));

        //transformMatrix.translate(new Vector3f(-start.x, -start.y, -start.z));

        Vector3f[] wireVertexBuffer = new Vector3f[wireVertices.length];

        MathUtil.applyMatrix(wireVertices, wireVertexBuffer, transformMatrix);

        for(int i = 0; i < wireIndices.length; i ++)
        {
            buffer
                .pos(wireVertexBuffer[wireIndices[i]].x, wireVertexBuffer[wireIndices[i]].y, wireVertexBuffer[wireIndices[i]].z)
                .color(col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha())
                .endVertex();
        }


    }

    public static void tick()
    {
        renderedConnections.clear();
        frame += 1;
    }

    public static void renderConnections(List<BlockPos> connections, double x, double y, double z)
    {
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();

        Tessellator tes = ClientUtil.tes();
        BufferBuilder buffer = tes.getBuffer();

        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        buffer.setTranslation(-TileEntityRendererDispatcher.staticPlayerX, -TileEntityRendererDispatcher.staticPlayerY, -TileEntityRendererDispatcher.staticPlayerZ);

        x += TileEntityRendererDispatcher.staticPlayerX;
        y += TileEntityRendererDispatcher.staticPlayerY;
        z += TileEntityRendererDispatcher.staticPlayerZ;

        for(BlockPos pos : connections)
        {
            Vector3f start = new Vector3f(
                (float)(x + 4.0 / 16),
                (float)(y + 0.5),
                (float)(z + 0.5)
            );

            Vector3f end = new Vector3f(
                (float)(pos.getX() + 4.0 / 16),
                (float)(pos.getY() + 0.5),
                (float)(pos.getZ() + 0.5)
            );

            Tuple3i startBlock = new Tuple3i((int)(x), (int)(y), (int)(z));
            Tuple3i endBlock = new Tuple3i(pos.getX(), pos.getY(), pos.getZ());

            if(!renderedConnections.contains(new Tuple2<>(endBlock, startBlock)))
            {
                renderedConnections.add(new Tuple2<>(startBlock, endBlock));

                renderConnection(start, end, 0.05f, COPPER, buffer);
            }
        }

        tes.draw();
        buffer.setTranslation(0,0,0);

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
    }

    public static Tessellator tes()
    {
        return Tessellator.getInstance();
    }
}
