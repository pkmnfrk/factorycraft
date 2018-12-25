package com.mike_caron.factorycraft.capability;

import com.mike_caron.factorycraft.energy.IEnergyManager;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class CapabilityEnergyManagerStorage
    implements Capability.IStorage<IEnergyManager>

{
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IEnergyManager> capability, IEnergyManager iEnergyManager, EnumFacing enumFacing)
    {
        return iEnergyManager.serializeNBT();
    }

    @Override
    public void readNBT(Capability<IEnergyManager> capability, IEnergyManager iEnergyManager, EnumFacing enumFacing, NBTBase nbtBase)
    {
        iEnergyManager.deserializeNBT((NBTTagCompound)nbtBase);
    }
}
