package com.mike_caron.factorycraft.api.capabilities;

import com.mike_caron.factorycraft.api.IPlayerCrafting;
import com.mike_caron.factorycraft.capability.PlayerCraftingImpl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityPlayerCrafting
    implements ICapabilitySerializable<NBTBase>
{
    @CapabilityInject(IPlayerCrafting.class)
    public static Capability<IPlayerCrafting> PLAYER_CRAFTING;

    private IPlayerCrafting instance;

    public CapabilityPlayerCrafting(EntityPlayer player)
    {
        instance = new PlayerCraftingImpl(player);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing enumFacing)
    {
        return capability == PLAYER_CRAFTING;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing enumFacing)
    {
        if(capability == PLAYER_CRAFTING)
            return PLAYER_CRAFTING.cast(instance);
        return null;
    }

    @Override
    public NBTBase serializeNBT()
    {
        return PLAYER_CRAFTING.getStorage().writeNBT(PLAYER_CRAFTING, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbtBase)
    {
        PLAYER_CRAFTING.getStorage().readNBT(PLAYER_CRAFTING, instance, null, nbtBase);
    }
}
