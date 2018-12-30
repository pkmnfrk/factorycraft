package com.mike_caron.factorycraft.proxy;

import com.mike_caron.factorycraft.client.gui.GuiDrill;
import com.mike_caron.factorycraft.client.gui.GuiFurnace;
import com.mike_caron.factorycraft.client.gui.GuiGrabber;
import com.mike_caron.factorycraft.storage.ContainerDrill;
import com.mike_caron.factorycraft.storage.ContainerFurnace;
import com.mike_caron.factorycraft.storage.ContainerGrabber;
import com.mike_caron.factorycraft.tileentity.TileEntityDrill;
import com.mike_caron.factorycraft.tileentity.TileEntityFurnace;
import com.mike_caron.factorycraft.tileentity.TileEntityGrabber;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiProxy
    implements IGuiHandler
{
    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer entityPlayer, World world, int x, int y, int z)
    {
        BlockPos pos = new BlockPos(x,y,z);
        TileEntity te = world.getTileEntity(pos);

        if(te instanceof TileEntityGrabber)
        {
            return new ContainerGrabber(entityPlayer.inventory, (TileEntityGrabber)te);
        }
        else if(te instanceof TileEntityDrill)
        {
            return new ContainerDrill(entityPlayer.inventory, (TileEntityDrill)te);
        }
        else if(te instanceof TileEntityFurnace)
        {
            return new ContainerFurnace(entityPlayer.inventory, (TileEntityFurnace)te);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer entityPlayer, World world, int x, int y, int z)
    {
        BlockPos pos = new BlockPos(x,y,z);
        TileEntity te = world.getTileEntity(pos);

        if(te instanceof TileEntityGrabber)
        {
            return new GuiGrabber(new ContainerGrabber(entityPlayer.inventory, (TileEntityGrabber)te), (TileEntityGrabber)te);
        }
        else if(te instanceof TileEntityDrill)
        {
            return new GuiDrill(new ContainerDrill(entityPlayer.inventory, (TileEntityDrill)te), (TileEntityDrill)te);
        }
        else if(te instanceof TileEntityFurnace)
        {
            return new GuiFurnace(new ContainerFurnace(entityPlayer.inventory, (TileEntityFurnace)te), (TileEntityFurnace)te);
        }
        return null;
    }
}
