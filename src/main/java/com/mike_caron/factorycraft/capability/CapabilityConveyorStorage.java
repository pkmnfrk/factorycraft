package com.mike_caron.factorycraft.capability;

import com.mike_caron.factorycraft.api.IConveyorBelt;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class CapabilityConveyorStorage
    implements Capability.IStorage<IConveyorBelt>
{
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IConveyorBelt> capability, IConveyorBelt iConveyorBelt, EnumFacing enumFacing)
    {
        return new NBTTagCompound();
    }

    @Override
    public void readNBT(Capability<IConveyorBelt> capability, IConveyorBelt iConveyorBelt, EnumFacing enumFacing, NBTBase nbtBase)
    {

    }
}
