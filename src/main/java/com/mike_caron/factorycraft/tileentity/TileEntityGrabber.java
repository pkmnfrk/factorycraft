package com.mike_caron.factorycraft.tileentity;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.api.IConveyorBelt;
import com.mike_caron.factorycraft.api.capabilities.CapabilityConveyor;
import com.mike_caron.factorycraft.energy.ElectricalEnergyAppliance;
import com.mike_caron.factorycraft.energy.SolidEnergyAppliance;
import com.mike_caron.factorycraft.storage.EnumSlotKind;
import com.mike_caron.factorycraft.storage.ISlotKind;
import com.mike_caron.mikesmodslib.block.FacingBlockBase;
import com.mike_caron.mikesmodslib.util.ItemUtils;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TileEntityGrabber
    extends EnergyTileEntity
    implements ITickable
{
    private static final AxisAlignedBB renderingBoundingBox = new AxisAlignedBB(-1, 0, -1, 2, 2, 2);

    public static final int TYPE_BURNER  = 0;
    public static final int TYPE_REGULAR = 1;
    public static final int TYPE_LONG    = 2;
    public static final int TYPE_FAST    = 3;
    public static final int TYPE_STACK   = 4;
    public static final int TYPE_FILTER  = 5;
    public static final int TYPE_STACK_FILTER = 6;


    private ItemStack held = ItemStack.EMPTY;
    private State state = State.GRABBING;
    private int progress = 0;
    private int maxProgress = 0;
    private int lastMaxProgress = 0;

    public TileEntityGrabber()
    {
        super();
    }

    public TileEntityGrabber(int type)
    {
        super(type);
    }

    @Override
    protected void onKnowingType()
    {
        super.onKnowingType();

        if(type == TYPE_BURNER)
        {
            energyAppliance = new SolidEnergyAppliance(this, getEnergyUsage(State.GRABBING) * 100);
        }
        else
        {
            energyAppliance = new ElectricalEnergyAppliance(this);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        progress = compound.getInteger("progress");
        maxProgress = compound.getInteger("maxProgress");
        if(compound.hasKey("lastMaxProgress"))
            lastMaxProgress = compound.getInteger("lastMaxProgress");
        state = State.values()[compound.getInteger("state")];

        held = ItemStack.EMPTY;
        if(compound.hasKey("held"))
        {
            held = new ItemStack(compound.getCompoundTag("held"));
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound)
    {
        NBTTagCompound ret = super.writeToNBT(compound);

        ret.setInteger("progress", progress);
        ret.setInteger("maxProgress", maxProgress);
        ret.setInteger("lastMaxProgress", lastMaxProgress);
        if(!held.isEmpty())
        {
            ret.setTag("held", held.serializeNBT());
        }
        ret.setInteger("state", state.ordinal());
        return ret;
    }

    @Override
    public void update()
    {
        if(world.isRemote) {
            switch(state)
            {
                case RETURNING:
                case GRABBING:

                    if(progress < maxProgress)
                    {
                        progress ++;
                    }
            }

            return;
        }

        energyAppliance.requestEnergy(getEnergyUsage(state), this::update);
    }

    public void update(int energy)
    {

        int startMaxProgress = maxProgress;
        State startState = state;

        int usage = getEnergyUsage(state);

        if(energy == 0 && usage != 0)
        {
            maxProgress = 0;
        }
        else
        {

            if(usage != 0)
            {
                maxProgress = (int) (getSpeed() * 20 * usage / energy);
            }
            else
            {
                maxProgress = 0;
            }


            if(lastMaxProgress != 0)
            {
                progress = progress * maxProgress / lastMaxProgress;
            }

            lastMaxProgress = maxProgress;


            switch (state)
            {
                case GRABBING:
                    progress += 1;

                    if (progress >= maxProgress)
                    {
                        state = State.WAITING_TO_GRAB;
                    }
                    break;
                case RETURNING:
                    progress += 1;

                    tryEatFuel();

                    if (progress >= maxProgress)
                    {
                        state = State.WAITING_TO_INSERT;
                    }
                    break;
                case WAITING_TO_GRAB:
                {
                    //ok, look for an item source.
                    BlockPos inputSpace = getInputSpace();
                    TileEntity inputTileEntity = getInputTileEntity();
                    IConveyorBelt inputConveyorBelt = null;
                    IItemHandler inputItemHandler = null;

                    ItemStack prospectiveItem = ItemStack.EMPTY;
                    int slotNum = -1;
                    float beltPos = 0f;
                    EntityItem looseItem = null;

                    BlockPos outputSpace = getOutputSpace();
                    TileEntity outputTileEntity = getOutputTileEntity();
                    IConveyorBelt outputConveyorBelt = null;
                    IItemHandler outputItemHandler = null;

                    if (inputTileEntity != null)
                    {
                        inputConveyorBelt = inputTileEntity
                                .getCapability(CapabilityConveyor.CONVEYOR, getFacing().getOpposite());
                        if (inputConveyorBelt == null)
                            inputItemHandler = inputTileEntity
                                    .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing());
                    }

                    if (outputTileEntity != null)
                    {
                        outputConveyorBelt = outputTileEntity
                                .getCapability(CapabilityConveyor.CONVEYOR, getFacing().getOpposite());
                        if (outputConveyorBelt == null)
                            outputItemHandler = outputTileEntity
                                    .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing()
                                            .getOpposite());
                    }


                    if (inputConveyorBelt != null)
                    {
                        int track = inputConveyorBelt.trackClosestTo(getFacing().getOpposite());
                        beltPos = inputConveyorBelt.trackLength(track) / 2;

                        prospectiveItem = inputConveyorBelt.extract(track, beltPos, beltPos * 2, true);

                        if (prospectiveItem
                                    .isEmpty() || !isValidToOutput(prospectiveItem, outputSpace, outputItemHandler, outputConveyorBelt, outputTileEntity))
                        {
                            track = 1 - track;
                            beltPos = inputConveyorBelt.trackLength(track) / 2;
                            prospectiveItem = inputConveyorBelt.extract(track, beltPos, beltPos * 2, true);
                        }

                        if (!prospectiveItem
                                .isEmpty() && isValidToOutput(prospectiveItem, outputSpace, outputItemHandler, outputConveyorBelt, outputTileEntity))
                        {
                            slotNum = track;
                        }
                        else
                        {
                            prospectiveItem = ItemStack.EMPTY;
                        }
                    }
                    else if (inputItemHandler != null)
                    {
                        for (int i = 0; i < inputItemHandler.getSlots(); i++)
                        {
                            prospectiveItem = inputItemHandler.extractItem(i, 1, true);
                            if (!prospectiveItem
                                    .isEmpty() && isValidToOutput(prospectiveItem, outputSpace, outputItemHandler, outputConveyorBelt, outputTileEntity))
                            {
                                slotNum = i;
                                break;
                            }
                        }
                    }

                    if (prospectiveItem.isEmpty())
                    {
                        //no inventory, or it's empty... maybe there's a loose item?
                        List<EntityItem> items = world
                                .getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(inputSpace));

                        for (EntityItem item : items)
                        {
                            if (isValidToOutput(item.getItem(), outputSpace, outputItemHandler, outputConveyorBelt, outputTileEntity))
                            {
                                prospectiveItem = item.getItem();
                                looseItem = item;
                                break;
                            }
                        }
                    }

                    if (!prospectiveItem.isEmpty())
                    {
                        //cool beans!
                        if (slotNum != -1 && inputConveyorBelt != null)
                        {
                            held = inputConveyorBelt.extract(slotNum, beltPos, beltPos * 2, false);
                        }
                        else if (slotNum != -1)
                        {
                            held = inputItemHandler.extractItem(slotNum, 1, false);
                        }
                        else if (looseItem != null)
                        {
                            held = prospectiveItem.copy();
                            held.setCount(1);

                            looseItem.getItem().shrink(held.getCount());
                        }
                    }

                    if (!held.isEmpty())
                    {
                        state = State.RETURNING;
                        progress = 0;
                    }
                }
                break;
                case WAITING_TO_INSERT:
                    BlockPos outputSpace = getOutputSpace();
                    TileEntity outputTileEntity = getOutputTileEntity();
                    IConveyorBelt conveyorBelt = null;
                    IItemHandler outputItemHandler = null;

                    if (outputTileEntity != null)
                    {
                        conveyorBelt = outputTileEntity.getCapability(CapabilityConveyor.CONVEYOR, null);
                        outputItemHandler = outputTileEntity
                                .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing()
                                        .getOpposite());
                    }

                    if (conveyorBelt != null)
                    {
                        int oppositeTrack = conveyorBelt.trackClosestTo(getFacing().getOpposite());
                        held = conveyorBelt
                                .insert(oppositeTrack, conveyorBelt.trackLength(oppositeTrack) * 0.4f, held, false);
                    }
                    else if (outputItemHandler != null)
                    {
                        held = ItemUtils.insertItemIfPossible(held, outputItemHandler);
                    }
                    else
                    {
                        List<EntityItem> items = world
                                .getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(outputSpace));
                        if (items.isEmpty())
                        {
                            ItemStack newStack = held.copy();
                            newStack.setCount(1);
                            held.shrink(1);

                            EntityItem newItem = new EntityItem(world, outputSpace.getX() + 0.5, outputSpace
                                    .getY(), outputSpace.getZ() + 0.5, newStack);
                            newItem.setVelocity(0, 0, 0);

                            world.spawnEntity(newItem);
                        }
                    }

                    if(!held.isEmpty())
                    {
                        tryEatFuel();
                    }

                    if (held.isEmpty())
                    {
                        state = State.GRABBING;
                        progress = 0;
                    }

                    break;
            }
        }

        if(startMaxProgress != maxProgress || startState != state)
        {
            markAndNotify();
        }

        //if(startState != state)
        //{
        //    FactoryCraft.logger.info("Transition from {} to {} @ {}", startState, state, progress);
        //}
    }

    private void tryEatFuel()
    {
        if (type == TYPE_BURNER && !held.isEmpty() && TileEntityFurnace
                .isItemFuel(held) && (progress >= maxProgress / 2 /*&& progress < maxProgress / 2 + 2*/))
        {
            ItemStack fuel = ((SolidEnergyAppliance)energyAppliance).getInventory().getStackInSlot(0);
            ItemStack newFuel;
            if (fuel.getCount() < 5)
            {
                newFuel = ((SolidEnergyAppliance)energyAppliance).getInventory().insertItem(0, held, true);

                if (newFuel != held)
                {
                    held = ((SolidEnergyAppliance)energyAppliance).getInventory().insertItem(0, held, false);
                    state = State.GRABBING;
                    progress = maxProgress - progress;
                }
            }
        }
    }

    private int getEnergyUsage(State state)
    {
        switch(type)
        {
            case TYPE_BURNER:
                switch(state)
                {
                    case WAITING_TO_GRAB:
                    case WAITING_TO_INSERT:
                        return 0;
                    case GRABBING:
                    case RETURNING:
                        return 9400;
                }
            case TYPE_REGULAR:
                switch (state)
                {
                    case WAITING_TO_GRAB:
                    case WAITING_TO_INSERT:
                        return 20;
                    case GRABBING:
                    case RETURNING:
                        return 650;
                }
            case TYPE_FAST:
                switch(state)
                {
                    case WAITING_TO_GRAB:
                    case WAITING_TO_INSERT:
                        return 25;
                    case GRABBING:
                    case RETURNING:
                        return 2300;
                }
            case TYPE_LONG:
                switch(state)
                {
                    case WAITING_TO_GRAB:
                    case WAITING_TO_INSERT:
                        return 20;
                    case GRABBING:
                    case RETURNING:
                        return 900;
                }
            case TYPE_STACK:
            case TYPE_STACK_FILTER:
                switch(state)
                {
                    case WAITING_TO_GRAB:
                    case WAITING_TO_INSERT:
                        return 50;
                    case GRABBING:
                    case RETURNING:
                        return 6600;
                }
            case TYPE_FILTER:
                switch(state)
                {
                    case WAITING_TO_GRAB:
                    case WAITING_TO_INSERT:
                        return 25;
                    case GRABBING:
                    case RETURNING:
                        return 2600;
                }
            default:
                throw new Error("Unknown type");
        }
    }

    private TileEntity getTileEntityNear(BlockPos pos)
    {
        TileEntity ret = world.getTileEntity(pos);
        if(ret != null) return ret;

        pos = pos.offset(EnumFacing.UP);

        ret = world.getTileEntity(pos);
        return ret;
    }

    private TileEntity getInputTileEntity()
    {
        BlockPos pos = getInputSpace();
        return getTileEntityNear(pos);
    }

    private TileEntity getOutputTileEntity()
    {
        BlockPos pos = getOutputSpace();
        return getTileEntityNear(pos);
    }

    private boolean isValidToOutput(@Nonnull ItemStack itemStack, BlockPos outputSpace, @Nullable IItemHandler outputHandler, @Nullable IConveyorBelt conveyorBelt, @Nullable TileEntity outputTileEntity)
    {
        if(conveyorBelt != null)
        {
            return true;
            /*
            int track = conveyorBelt.trackClosestTo(getFacing());
            float len = conveyorBelt.trackLength(track);
            ItemStack ret = conveyorBelt.insert(track, len / 2, itemStack, true);

            return ret != itemStack;
            */
        }
        else if(outputHandler != null)
        {
            assert outputTileEntity != null;

            for(int i = 0; i < outputHandler.getSlots(); i++)
            {
                if(!outputHandler.isItemValid(i, itemStack))
                    continue;

                ItemStack ret = outputHandler.insertItem(i, itemStack, true);

                if(ret != itemStack)
                {
                    if(outputHandler instanceof ISlotKind)
                    {
                        ItemStack existingStack = outputHandler.getStackInSlot(i);
                        boolean isFuel = TileEntityFurnace.isItemFuel(itemStack);

                        EnumSlotKind slotKind = ((ISlotKind) outputHandler).getSlotKind(i);
                        int desiredCount = ((ISlotKind) outputHandler).desiredMaximum(i);

                        if(existingStack.getCount() >= desiredCount)
                            continue;

                        switch (slotKind)
                        {
                            case OUTPUT:
                            case NONE:
                                continue;
                            case FUEL:
                                if(!isFuel)
                                    continue;
                            case INPUT:
                                return true;
                        }
                    }
                    return true;
                }
            }

            return false;
        }

        List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(outputSpace));


        return items.isEmpty();
    }

    public float getAngle(float partial)
    {
        switch(state)
        {
            case WAITING_TO_GRAB:
                return 180f;
            case WAITING_TO_INSERT:
                return 0f;
            case RETURNING:
            case GRABBING:

                float v;
                if (maxProgress == 0)
                {
                    v = ((float)progress) / lastMaxProgress;
                }
                else {
                    v = (progress + partial) / maxProgress;
                }

                if(state == State.RETURNING)
                {
                    v = 1 - v;
                }

                return v * 180f;
        }

        return 0f;
    }

    public EnumFacing getFacing()
    {
        return world.getBlockState(pos).getValue(FacingBlockBase.FACING);
    }

    private BlockPos getInputSpace()
    {
        EnumFacing facing = getFacing().getOpposite();

        return pos.offset(facing);
    }

    private BlockPos getOutputSpace()
    {
        EnumFacing facing = getFacing();

        return pos.offset(facing);
    }

    private double getSpeed()
    {
        switch (type)
        {
            case TYPE_BURNER:
                return 0.845;
            case TYPE_REGULAR:
                return 0.6;
            case TYPE_LONG:
                return 0.4335;
            case TYPE_FAST:
            case TYPE_FILTER:
            case TYPE_STACK:
            case TYPE_STACK_FILTER:
                return 0.21675;
        }
        return 10;
    }

    public void handleAnimationEvent(float time, Iterable<Event> pastEvents)
    {
        for(Event event : pastEvents)
        {
            FactoryCraft.logger.info("Got animation event {} at {}", event.event(), time);
        }
    }

    public ItemStack getHeld()
    {
        return held;
    }

    public State getState()
    {
        return state;
    }

    @Override
    public void addItemsToDrop(NonNullList<ItemStack> items)
    {
        super.addItemsToDrop(items);

        if(!held.isEmpty())
        {
            items.add(held);
        }
    }

    public enum State
    {
        GRABBING,          // -> WAITING_TO_GRAB
        WAITING_TO_GRAB,   // -> RETURNING
        RETURNING,         // -> WAITING_TO_INSERT
        WAITING_TO_INSERT  // -> GRABBIGN
    }

}
