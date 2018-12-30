package com.mike_caron.factorycraft.storage;

import com.mike_caron.factorycraft.client.gui.GuiDrill;
import com.mike_caron.factorycraft.energy.EnergyAppliance;
import com.mike_caron.factorycraft.energy.SolidEnergyAppliance;
import com.mike_caron.factorycraft.tileentity.TileEntityFurnace;
import com.mike_caron.mikesmodslib.block.TEContainerBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerFurnace
    extends TEContainerBase<TileEntityFurnace>
{
    public float fuelPercent = 0f;
    public float progressPercent = 0f;

    public ContainerFurnace(IInventory player, TileEntityFurnace te)
    {
        super(player, te);
        ownSlotUpdates = false;

        init();
    }

    @Override
    protected void addOwnSlots()
    {
        super.addOwnSlots();
        IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);

        if(inv != null)
        {
            addSlotToContainer(new SlotItemHandler(inv, 0, 11, 21));
            addSlotToContainer(new SlotItemHandler(inv, 1, 150, 21));

            if (inv.getSlots() > 2)
            {
                addSlotToContainer(new SlotItemHandler(inv, 2, 11, 39));
            }
        }
    }

    @Override
    protected int numOwnSlots()
    {
        return 1;
    }

    @Override
    public int getId()
    {
        return ContainerConst.CONTAINER_FURNACE;
    }

    @Override
    protected int playerInventoryY()
    {
        return GuiDrill.getHeight(te.getType()) - 82;
    }

    @Override
    protected int playerInventoryX()
    {
        return GuiDrill.getWidth(te.getType()) / 2 - 80;
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        EnergyAppliance energyAppliance = te.getEnergyAppliance();

        if(energyAppliance instanceof SolidEnergyAppliance && fuelPercent != energyAppliance.getEnergyPercent())
        {
            fuelPercent = energyAppliance.getEnergyPercent();
            changed = true;
        }

        if(progressPercent != te.getProgress())
        {
            progressPercent = te.getProgress();
            changed = true;
        }


        if(changed)
        {
            triggerUpdate();
        }
    }

    @Override
    protected void onWriteNBT(NBTTagCompound tag)
    {
        super.onWriteNBT(tag);

        tag.setFloat("fuelPercent", fuelPercent);
        tag.setFloat("progressPercent", progressPercent);
    }

    @Override
    protected void onReadNBT(NBTTagCompound tag)
    {
        super.onReadNBT(tag);

        fuelPercent = tag.getFloat("fuelPercent");
        progressPercent = tag.getFloat("progressPercent");
    }
}
