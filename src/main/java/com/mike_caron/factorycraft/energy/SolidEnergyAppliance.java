package com.mike_caron.factorycraft.energy;

import net.minecraft.init.Items;
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
    private static NonNullList<ItemStack> limitedItems;

    private final ItemStackHandler fuel = new FuelItemStackHandler(1);
    private int joulesBanked;
    private float efficiency = 1f;
    private int joulesHigh = 0;

    public SolidEnergyAppliance(TileEntity host, int freeEnergy)
    {
        super(host);
        joulesBanked = freeEnergy;
    }

    public SolidEnergyAppliance(TileEntity host)
    {
        this(host, 0);
    }

    public void setEfficiency(float efficiency)
    {
        this.efficiency = efficiency;
    }

    public float getEfficiency()
    {
        return efficiency;
    }

    @Override
    public void requestEnergy(int amount, @Nonnull IntConsumer callback)
    {
        if(joulesBanked < amount)
        {
            while (true)
            {
                ItemStack f = fuel.getStackInSlot(0);
                if (f.isEmpty())
                    break;

                int value = (int) (fuelValue(f) * efficiency);

                if (value > 0)
                {
                    f.shrink(1);
                    joulesBanked += value;

                    host.markDirty();

                    if (joulesBanked >= amount || f.isEmpty())
                    {
                        break;
                    }
                }
                else
                {
                    break;
                }
            }
        }

        if(joulesBanked > joulesHigh)
            joulesHigh = joulesBanked;

        energyPercent = ((float)joulesBanked) / joulesHigh;

        amount = Math.min(amount, joulesBanked);

        joulesBanked -= amount;

        callback.accept(amount);
    }

    @Override
    public void provideEnergy(int maxEnergy, @Nonnull IntConsumer callback)
    {
        callback.accept(0);
    }

    @Override
    public ItemStackHandler getInventory()
    {
        return fuel;
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> getLimitedItems()
    {
        if(limitedItems == null)
        {
            limitedItems = NonNullList.create();
            limitedItems.add(new ItemStack(Items.COAL, 5));

        }
        return limitedItems;
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
        ret.setInteger("joulesHigh", joulesHigh);
        ret.setFloat("efficiency", efficiency);
        ret.setTag("inventory", fuel.serializeNBT());

        return ret;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound)
    {
        joulesBanked = compound.getInteger("joulesBanked");
        joulesHigh = compound.getInteger("joulesHigh");
        efficiency = compound.getFloat("efficiency");
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

            host.markDirty();
        }
    }
}
