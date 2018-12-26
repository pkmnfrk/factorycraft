package com.mike_caron.factorycraft.api.capabilities;

import com.mike_caron.factorycraft.energy.EnergyManager;
import com.mike_caron.factorycraft.api.energy.IEnergyManager;
import com.mike_caron.factorycraft.util.WorldTileEntityFinder;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityEnergyManager
        implements ICapabilitySerializable<NBTBase>
{
    @CapabilityInject(IEnergyManager.class)
    public static Capability<IEnergyManager> ENERGY_MANAGER;

    private final EnergyManager instance;

    public CapabilityEnergyManager(World world)
    {
        this.instance = new EnergyManager(new WorldTileEntityFinder(world));
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing enumFacing)
    {
        return capability == ENERGY_MANAGER;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing enumFacing)
    {
        if (capability == ENERGY_MANAGER)
        {
            return ENERGY_MANAGER.cast(instance);
        }
        return null;
    }


    @Override
    public NBTBase serializeNBT()
    {
        return ENERGY_MANAGER.getStorage().writeNBT(ENERGY_MANAGER, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbtBase)
    {
        ENERGY_MANAGER.getStorage().readNBT(ENERGY_MANAGER, instance, null, nbtBase);
    }
}
