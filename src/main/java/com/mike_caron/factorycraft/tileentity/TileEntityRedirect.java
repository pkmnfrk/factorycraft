package com.mike_caron.factorycraft.tileentity;

import com.mike_caron.mikesmodslib.block.TileEntityBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class TileEntityRedirect
    extends TileEntityBase
{
    private BlockPos other;
    private EnumFacing facing;

    public TileEntityRedirect()
    {

    }

    public TileEntityRedirect(BlockPos other)
    {
        this.other = other;
    }

    public TileEntityRedirect(EnumFacing facing)
    {
        this.facing = facing;
    }

    public TileEntity getRealTileEntity()
    {
        if(other == null)
        {
            if(facing != null)
            {
                other = pos.offset(facing);
            }
        }

        if(other == null)
            return null;

        return world.getTileEntity(other);
    }

    public BlockPos getRealTileEntityPos()
    {
        if(other == null)
        {
            if(facing != null)
            {
                other = pos.offset(facing);
            }
        }

        return other;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        TileEntity real = getRealTileEntity();
        if(real != null)
        {
            return real.hasCapability(capability, facing);
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        TileEntity real = getRealTileEntity();
        if(real != null)
        {
            return real.getCapability(capability, facing);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        other = null;
        if(compound.hasKey("other"))
        {
            NBTTagCompound o = compound.getCompoundTag("other");

            other = new BlockPos(o.getInteger("x"), o.getInteger("y"), o.getInteger("z"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        NBTTagCompound ret = super.writeToNBT(compound);

        if(other == null)
        {
            if(facing != null)
            {
                other = pos.offset(facing);
            }
        }

        if(other != null)
        {
            NBTTagCompound o = new NBTTagCompound();
            o.setInteger("x", other.getX());
            o.setInteger("y", other.getY());
            o.setInteger("z", other.getZ());
            ret.setTag("other", o);
        }

        return ret;
    }
}
