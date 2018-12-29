package com.mike_caron.factorycraft.storage;

import com.mike_caron.factorycraft.client.gui.GuiDrill;
import com.mike_caron.factorycraft.energy.EnergyAppliance;
import com.mike_caron.factorycraft.energy.SolidEnergyAppliance;
import com.mike_caron.factorycraft.tileentity.TileEntityDrill;
import com.mike_caron.mikesmodslib.block.TEContainerBase;
import com.mike_caron.mikesmodslib.util.ItemUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerDrill
    extends TEContainerBase<TileEntityDrill>
{
    public float fuelPercent = 0f;
    public float progressPercent = 0f;
    public ItemStack miningTarget = ItemStack.EMPTY;

    public ContainerDrill(IInventory player, TileEntityDrill te)
    {
        super(player, te);

        init();
    }

    @Override
    protected void addOwnSlots()
    {
        super.addOwnSlots();
        IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);

        if(te.getType() == 0)
        {
            addSlotToContainer(new SlotItemHandler(inv, 0, 11, 21));
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
        return ContainerConst.CONTAINER_DRILL;
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

        if(!ItemStack.areItemStacksEqual(miningTarget, te.getCurrentMiningTarget()))
        {
            miningTarget = te.getCurrentMiningTarget();
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
        tag.setString("miningTarget", ItemUtils.getTagFromStack(miningTarget));
    }

    @Override
    protected void onReadNBT(NBTTagCompound tag)
    {
        super.onReadNBT(tag);

        fuelPercent = tag.getFloat("fuelPercent");
        progressPercent = tag.getFloat("progressPercent");
        miningTarget = ItemUtils.getStackFromTag(tag.getString("miningTarget"));
    }
}
