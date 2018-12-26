package com.mike_caron.factorycraft.tileentity;

import com.mike_caron.mikesmodslib.block.TileEntityBase;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public abstract class TypedTileEntity
    extends TileEntityBase
{
    protected int type = -1;

    TypedTileEntity()
    {

    }

    TypedTileEntity(int type)
    {
        this.type = type;

        onKnowingType();
    }

    protected void onKnowingType()
    {

    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        if(type == -1)
        {
            type = nbt.getInteger("type");

            onKnowingType();
        }


    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound)
    {
        NBTTagCompound ret = super.writeToNBT(compound);

        ret.setInteger("type", type);

        return ret;
    }

    @Override
    public final void deserializeNBT(NBTTagCompound nbt)
    {
        super.deserializeNBT(nbt);
    }

    @Override
    public final NBTTagCompound serializeNBT()
    {
        return super.serializeNBT();
    }
}
