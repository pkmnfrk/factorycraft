package com.mike_caron.factorycraft.energy;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.function.IntConsumer;

public class SolidEnergyAppliance
    extends EnergyAppliance
{
    private ItemStackHandler fuel = new FuelItemStackHandler(1);
    private int joulesBanked = 0;

    public SolidEnergyAppliance(TileEntity host)
    {
        super(host);
    }

    @Override
    public void requestEnergy(int amount, @Nonnull IntConsumer callback)
    {
        while(true)
        {
            ItemStack f = fuel.getStackInSlot(0);
            if(f.isEmpty())
                break;

            int value = fuelValue(f);

            if(value > 0)
            {
                f.shrink(1);
                joulesBanked += value;

                host.markDirty();

                if(joulesBanked >= amount || f.isEmpty())
                {
                    break;
                }
            }
            else
            {
                break;
            }
        }

        amount = Math.min(amount, joulesBanked);

        joulesBanked -= amount;

        callback.accept(amount);
    }

    @Override
    public void provideEnergy(int maxEnergy, @Nonnull IntConsumer callback)
    {
        callback.accept(0);
    }

    public ItemStackHandler getInventory()
    {
        return fuel;
    }

    private int fuelValue(ItemStack itemStack)
    {
        // Coal burns for 1600 ticks in minecraft, and provides 8MJ in Factorio
        // 8,000,000 / 1,600 = 5000 J / tick
        return 5000 * TileEntityFurnace.getItemBurnTime(itemStack);
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound ret = super.serializeNBT();

        ret.setInteger("joulesBanked", joulesBanked);
        ret.setTag("inventory", fuel.serializeNBT());

        return ret;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound)
    {
        joulesBanked = compound.getInteger("joulesBanked");
        fuel.deserializeNBT(compound.getCompoundTag("inventory"));
    }

    class FuelItemStackHandler
        extends ItemStackHandler
    {

        public FuelItemStackHandler()
        {
            super();
        }

        public FuelItemStackHandler(int size)
        {
            super(size);
        }

        public FuelItemStackHandler(NonNullList<ItemStack> stacks)
        {
            super(stacks);
        }

        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
        }
    }
}