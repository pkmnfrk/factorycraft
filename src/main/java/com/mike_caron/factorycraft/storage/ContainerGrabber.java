package com.mike_caron.factorycraft.storage;

import com.mike_caron.factorycraft.client.gui.GuiGrabber;
import com.mike_caron.factorycraft.energy.SolidEnergyAppliance;
import com.mike_caron.factorycraft.tileentity.TileEntityGrabber;
import com.mike_caron.mikesmodslib.block.TEContainerBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerGrabber
    extends TEContainerBase<TileEntityGrabber>
{
    public float fuelPercent = 0f;

    public ContainerGrabber(IInventory player, TileEntityGrabber te)
    {
        super(player, te);

        init();
    }

    @Override
    protected void addOwnSlots()
    {
        super.addOwnSlots();
        IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);

        addSlotToContainer(new SlotItemHandler(inv, 0, 11, 21));
    }

    @Override
    protected int numOwnSlots()
    {
        return 1;
    }

    @Override
    public int getId()
    {
        return ContainerConst.CONTAINER_INSERTER;
    }

    @Override
    protected int playerInventoryY()
    {
        return GuiGrabber.getHeight(te.getType()) - 82;
    }

    @Override
    protected int playerInventoryX()
    {
        return GuiGrabber.getWidth(te.getType()) / 2 - 80;
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        SolidEnergyAppliance energyAppliance = (SolidEnergyAppliance)te.getEnergyAppliance();

        if(fuelPercent != energyAppliance.getEnergyPercent())
        {
            fuelPercent = energyAppliance.getEnergyPercent();
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
    }

    @Override
    protected void onReadNBT(NBTTagCompound tag)
    {
        super.onReadNBT(tag);

        fuelPercent = tag.getFloat("fuelPercent");
    }
}
