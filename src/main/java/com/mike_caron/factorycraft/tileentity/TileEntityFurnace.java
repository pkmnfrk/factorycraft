package com.mike_caron.factorycraft.tileentity;

import com.mike_caron.factorycraft.energy.ElectricalEnergyAppliance;
import com.mike_caron.factorycraft.energy.SolidEnergyAppliance;
import com.mike_caron.factorycraft.item.crafting.FurnaceRecipe;
import com.mike_caron.factorycraft.storage.EnumSlotKind;
import com.mike_caron.factorycraft.storage.ISlotKind;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityFurnace
    extends EnergyTileEntity
    implements ITickable
{
    private final ItemStackHandler inventory = new MyItemStackHandler();

    private float progress;
    private boolean isActive;

    public TileEntityFurnace()
    {
        super();
    }

    public TileEntityFurnace(int type)
    {
        super(type);
    }

    @Override
    public void update()
    {
        if(world.isRemote)
            return;

        ItemStack input = inventory.getStackInSlot(0);
        ItemStack output = inventory.getStackInSlot(1);

        if(FurnaceRecipe.INSTANCE.hasRecipe(input))
        {
            int recipeInput = FurnaceRecipe.INSTANCE.getInputCount(input);
            ItemStack recipeOutput = FurnaceRecipe.INSTANCE.getOutput(input);

            if (input.getCount() >= recipeInput
                && (
                       output.isEmpty()
                    || (
                           output.isItemEqual(recipeOutput)
                        && output.getCount() + recipeOutput.getCount() <= 64
                    )
                )
            )
            {
                energyAppliance.requestEnergy(getActiveEnergyUsage(), this::updateActive);
                return;
            }

        }

        energyAppliance.requestEnergy(getIdleEnergyUsage(), this::updateIdle);
    }

    private void updateActive(int amount)
    {
        int target = getActiveEnergyUsage();
        ItemStack input = inventory.getStackInSlot(0);

        float recipeTime = FurnaceRecipe.INSTANCE.getCraftingTime(input);
        float speed = getSmeltSpeed();

        float craftingTime = 1 / (20f * recipeTime / speed);

        progress += amount * craftingTime / target;

        markDirty();

        if(!isActive && amount > 0)
        {
            isActive = true;
            markAndNotify();
        }
        else if(isActive && amount == 0)
        {
            isActive = false;
            markAndNotify();
        }

        if(progress >= 1)
        {
            ItemStack output = inventory.getStackInSlot(1);
            ItemStack recipeOutput = FurnaceRecipe.INSTANCE.getOutput(input);
            int inputCount = FurnaceRecipe.INSTANCE.getInputCount(input);

            input.shrink(inputCount);
            if(output.isEmpty())
            {
                output = recipeOutput.copy();
                inventory.setStackInSlot(1, output);
            }
            else
            {
                output.grow(recipeOutput.getCount());
            }

            progress = 0;
        }


    }

    private void updateIdle(int amount)
    {
        if(isActive)
        {
            isActive = false;
            markAndNotify();
        }
    }

    private int getActiveEnergyUsage()
    {
        return 9000 + getIdleEnergyUsage();
    }

    private int getIdleEnergyUsage()
    {
        if(type == 2)
            return 300;

        return 0;
    }

    private int getSmeltSpeed()
    {
        switch(type)
        {
            case 0:
                return 1;
            default:
                return 2;
        }
    }

    @Override
    protected void onKnowingType()
    {
        super.onKnowingType();

        switch(type)
        {
            case 0:
            case 1:
                energyAppliance = new SolidEnergyAppliance(this);
                break;
            case 2:
                energyAppliance = new ElectricalEnergyAppliance(this);
                break;
        }
    }

    public float getProgress()
    {
        return progress;
    }

    public boolean getIsActive()
    {
        return isActive;
    }

    public ItemStack getSmeltingResult(ItemStack itemStack)
    {
        return FurnaceRecipe.INSTANCE.getOutput(itemStack);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null)
        {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null)
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        inventory.deserializeNBT(nbt.getCompoundTag("inventory"));
        progress = nbt.getFloat("progress");
        boolean oldIsActive = isActive;
        isActive = nbt.getBoolean("active");

        if(world != null && world.isRemote)
        {
            if(oldIsActive != isActive)
            {
                world.markBlockRangeForRenderUpdate(pos, pos);
            }
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound)
    {
        compound = super.writeToNBT(compound);

        compound.setTag("inventory", inventory.serializeNBT());
        compound.setFloat("progress", progress);
        compound.setBoolean("active", isActive);
        return compound;
    }

    @Override
    public void addItemsToDrop(NonNullList<ItemStack> items)
    {
        super.addItemsToDrop(items);

        for(int i = 0; i < 2; i++)
        {
            if(!inventory.getStackInSlot(i).isEmpty())
            {
                items.add(inventory.getStackInSlot(i));
            }
        }

    }

    private int getLimitCount(ItemStack item)
    {
        if(item.isEmpty()) return 64;

        int cnt = FurnaceRecipe.INSTANCE.getInputCount(item);
        if(cnt == 0)
            return 64;

        return cnt * 5;
    }

    class MyItemStackHandler
        extends ItemStackHandler
        implements ISlotKind
    {

        public MyItemStackHandler()
        {
            super(2);
        }

        @Override
        public int getSlots()
        {
            int mod = 0;
            if(type != 2)
                mod = 1;
            return super.getSlots() + mod;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            if(slot == 2)
            {
                if(type != 2)
                {
                    return energyAppliance.getInventory().extractItem(slot - 2, amount, simulate);
                }
            }

            return super.extractItem(slot, amount, simulate);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
        {
            if(slot == 2)
            {
                if(type != 2)
                {
                    return energyAppliance.getInventory().insertItem(slot - 2, stack, simulate);
                }
            }

            if(slot == 1)
            {
                return stack;
            }

            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack)
        {
            if(slot == 2)
            {
                if(type != 2)
                {
                    ((ItemStackHandler)energyAppliance.getInventory()).setStackInSlot(slot - 2, stack);
                    return;
                }
            }

            super.setStackInSlot(slot, stack);
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot)
        {
            if(slot == 2)
            {
                if(type != 2)
                {
                    return energyAppliance.getInventory().getStackInSlot(slot - 2);
                }
            }

            return super.getStackInSlot(slot);
        }

        @Override
        public int getSlotLimit(int slot)
        {
            if(slot == 2)
            {
                if(type != 2)
                {
                    return energyAppliance.getInventory().getSlotLimit(slot - 2);
                }
            }

            return super.getSlotLimit(slot);
        }

        /*
        @Override
        protected int getStackLimit(int slot, @Nonnull ItemStack stack)
        {
            if(slot == 2)
            {
                if(type != 2)
                {
                    return ((ItemStackHandler)energyAppliance.getInventory()).getStackLimit(slot - 2, stack);
                }
            }

            return super.getStackLimit(slot, stack);
        }
        */

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack)
        {
            if(slot == 2)
            {
                if(type != 2)
                {
                    return energyAppliance.getInventory().isItemValid(slot - 2, stack);
                }
            }

            if(slot == 0)
            {
                return !getSmeltingResult(stack).isEmpty();
            }

            return false;
        }

        @Override
        protected void validateSlotIndex(int slot)
        {
            if(type >= 2 && slot == 2)
            {
                throw new RuntimeException("Slot " + slot + " is not in valid range [0,2)");
            }

            else
            {
                super.validateSlotIndex(slot);
            }
        }

        @Override
        protected void onContentsChanged(int slot)
        {
            markDirty();
        }

        @Nonnull
        @Override
        public EnumSlotKind getSlotKind(int slot)
        {
            if(type != 2 && slot == 2 && getEnergyAppliance().getInventory() instanceof ISlotKind)
            {
                return ((ISlotKind) getEnergyAppliance().getInventory()).getSlotKind(slot - 2);
            }
            if(slot == 0)
            {
                return EnumSlotKind.INPUT;
            }
            else if(slot == 1)
            {
                return EnumSlotKind.OUTPUT;
            }
            return EnumSlotKind.NONE;
        }

        @Override
        public int desiredMaximum(int slot)
        {
            if(type != 2 && slot == 2 && getEnergyAppliance().getInventory() instanceof ISlotKind)
            {
                return ((ISlotKind) getEnergyAppliance().getInventory()).desiredMaximum(slot - 2);
            }
            return getLimitCount(getStackInSlot(slot));
        }
    }
}
