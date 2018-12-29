package com.mike_caron.factorycraft.tileentity;

import com.mike_caron.factorycraft.energy.EnergyAppliance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyTileEntity
    extends TypedTileEntity
    implements ILimitedInputItems
{
    protected EnergyAppliance energyAppliance;

    EnergyTileEntity()
    {
        super();
    }

    EnergyTileEntity(int type)
    {
        super(type);
    }

    public EnergyAppliance getEnergyAppliance()
    {
        return energyAppliance;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null && energyAppliance != null)
            return true;

        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null && energyAppliance != null)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(energyAppliance.getInventory());

        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        if(energyAppliance != null)
        {
            energyAppliance.deserializeNBT(nbt.getCompoundTag("energy"));
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound)
    {
        compound = super.writeToNBT(compound);

        if(energyAppliance != null)
        {
            compound.setTag("energy", energyAppliance.serializeNBT());
        }

        return compound;
    }

    @Override
    public NonNullList<ItemStack> getLimitedItems()
    {
        NonNullList<ItemStack> ret = NonNullList.create();

        if(energyAppliance != null)
        {
            ret.addAll(energyAppliance.getLimitedItems());
        }

        return ret;
    }

    @Override
    public void addItemsToDrop(NonNullList<ItemStack> items)
    {
        super.addItemsToDrop(items);

        if(energyAppliance != null)
        {
            IItemHandler inv = energyAppliance.getInventory();
            if(inv != null)
            {
                for (int i = 0; i < inv.getSlots(); i++)
                {
                    if (!inv.getStackInSlot(i).isEmpty())
                    {
                        items.add(inv.getStackInSlot(i));
                    }
                }
            }
        }
    }
}
