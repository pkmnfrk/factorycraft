package com.mike_caron.factorycraft.tileentity;

import com.google.common.collect.ImmutableMap;
import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.mikesmodslib.block.FacingBlockBase;
import com.mike_caron.mikesmodslib.block.IAnimationEventHandler;
import com.mike_caron.mikesmodslib.block.TileEntityBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.common.animation.TimeValues;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class GrabberTileEntity
    extends TileEntityBase
    implements ITickable, IAnimationEventHandler
{
    private int type = -1;

    private ItemStack held = ItemStack.EMPTY;
    private State state = State.GRABBING;
    private int progress = 0;
    private int maxProgress = 0;

    private IAnimationStateMachine asm;
    //private final TimeValues.VariableValue anim_cycle = new TimeValues.VariableValue(1f);
    private final TimeValues.VariableValue anim_progress = new TimeValues.VariableValue(0f);

    public GrabberTileEntity()
    {
        if(FMLCommonHandler.instance().getSide() == Side.CLIENT)
        {
            loadAsm();
        }
        else
        {
            asm = null;
        }
    }

    public GrabberTileEntity(int type)
    {
        this();
        this.type = type;
    }

    public void loadAsm()
    {
        String currentState = "grab";
        if(asm != null)
        {
            currentState = asm.currentState();
        }

        asm = ModelLoaderRegistry.loadASM(new ResourceLocation(FactoryCraft.modId, "asms/block/grabber1.json"), ImmutableMap.of("cycle_progress", anim_progress));

        if(!asm.currentState() .equals(currentState))
            asm.transition(currentState);
    }

    @Override
    public boolean hasFastRenderer()
    {
        return true;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityAnimation.ANIMATION_CAPABILITY)
            return true;

        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityAnimation.ANIMATION_CAPABILITY)
        {
            return CapabilityAnimation.ANIMATION_CAPABILITY.cast(asm);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        type = compound.getInteger("type");
        progress = compound.getInteger("progress");

        State newState = State.values()[compound.getInteger("state")];

        held = ItemStack.EMPTY;
        if(compound.hasKey("held"))
        {
            held = new ItemStack(compound.getCompoundTag("held"));
        }

        maxProgress = compound.getInteger("maxProgress");
        state = newState;

    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound)
    {
        NBTTagCompound ret = super.writeToNBT(compound);

        ret.setInteger("type", type);
        ret.setInteger("progress", progress);
        ret.setInteger("maxProgress", maxProgress);
        if(!held.isEmpty())
        {
            ret.setTag("held", held.serializeNBT());
        }
        ret.setInteger("state", state.ordinal());

        return ret;
    }

    private final float ANIM_MIN = 0.2f;
    private final float ANIM_MAX = 0.75f;

    @Override
    public void update()
    {
        if(world.isRemote) {
            switch(state)
            {
                case WAITING_TO_GRAB:
                    anim_progress.setValue(ANIM_MIN);
                    //FactoryCraft.logger.info( "t = 0f " );
                    break;
                case WAITING_TO_INSERT:
                    anim_progress.setValue(ANIM_MAX);
                    //FactoryCraft.logger.info( "t = 1f " );
                    break;
                case RETURNING:
                case GRABBING:

                    if(progress < maxProgress)
                    {
                        progress ++;

                        float v = ANIM_MIN + (progress * 1f / maxProgress) * (ANIM_MAX - ANIM_MIN);

                        if(state == State.GRABBING)
                        {
                            v = 1 - v;
                        }

                        anim_progress.setValue(v);
                        //FactoryCraft.logger.info( "t = " + v );

                    }
            }

            return;
        }

        int startMaxProgress = maxProgress;
        State startState = state;

        maxProgress = 0;

        switch(state)
        {
            case GRABBING:
                maxProgress = (int)(getSpeed() * 20);
                progress += 1;
                if(progress >= maxProgress)
                {
                    state = State.WAITING_TO_GRAB;
                }
                break;
            case RETURNING:
                maxProgress = (int)(getSpeed() * 20);
                progress += 1;
                if(progress >= maxProgress)
                {
                    state = State.WAITING_TO_INSERT;
                }
                break;
            case WAITING_TO_GRAB:
            {
                //ok, look for an item source.
                BlockPos inputSpace = getInputSpace();
                TileEntity inputTileEntity = world.getTileEntity(inputSpace);
                IItemHandler inputItemHandler = null;

                ItemStack prospectiveItem = ItemStack.EMPTY;
                int slotNum = -1;
                EntityItem looseItem = null;

                BlockPos outputSpace = getOutputSpace();
                TileEntity outputTileEntity = world.getTileEntity(outputSpace);
                IItemHandler outputItemHandler = null;

                if (inputTileEntity != null && inputTileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing().getOpposite()))
                {
                    inputItemHandler = inputTileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing().getOpposite());
                }

                if (outputTileEntity != null && outputTileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing()))
                {
                    outputItemHandler = outputTileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing());
                }


                if (inputItemHandler != null)
                {
                    for (int i = 0; i < inputItemHandler.getSlots(); i++)
                    {
                        prospectiveItem = inputItemHandler.extractItem(i, 1, true);
                        if (!prospectiveItem.isEmpty() && isValidToOutput(prospectiveItem, outputSpace, outputItemHandler))
                        {
                            slotNum = i;
                            break;
                        }
                    }
                }

                if (prospectiveItem.isEmpty())
                {
                    //no inventory, or it's empty... maybe there's a loose item?
                    List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(inputSpace));

                    for (EntityItem item : items)
                    {
                        if (isValidToOutput(item.getItem(), outputSpace, outputItemHandler))
                        {
                            prospectiveItem = item.getItem();
                            looseItem = item;
                            break;
                        }
                    }
                }

                if(!prospectiveItem.isEmpty())
                {
                    //cool beans!
                    if(slotNum != -1)
                    {
                        held = inputItemHandler.extractItem(slotNum, 1, false);
                    }
                    else if(looseItem != null)
                    {
                        held = prospectiveItem.copy();
                        held.setCount(1);

                        looseItem.getItem().shrink(held.getCount());
                    }
                }

                if(!held.isEmpty())
                {
                    state = State.RETURNING;
                    progress = 0;
                }
            }
            break;
            case WAITING_TO_INSERT:
                BlockPos outputSpace = getOutputSpace();
                TileEntity outputTileEntity = world.getTileEntity(outputSpace);
                IItemHandler outputItemHandler = null;

                if(outputTileEntity != null)
                {
                    outputItemHandler = outputTileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing());
                }

                if(outputItemHandler != null)
                {
                    for(int i = 0; i < outputItemHandler.getSlots(); i++)
                    {
                        ItemStack newHeld = outputItemHandler.insertItem(i, held, true);

                        if(newHeld != held)
                        {
                            held = outputItemHandler.insertItem(i, held, false);
                            break;
                        }
                    }
                }
                else
                {
                    List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(outputSpace));
                    if(items.isEmpty())
                    {
                        ItemStack newStack = held.copy();
                        newStack.setCount(1);
                        held.shrink(1);

                        EntityItem newItem = new EntityItem(world, outputSpace.getX() + 0.5, outputSpace.getY() + 0.5, outputSpace.getZ() + 0.5, newStack);
                        newItem.setVelocity(0, 0, 0);

                        world.spawnEntity(newItem);
                    }
                }

                if(held.isEmpty())
                {
                    state = State.GRABBING;
                    progress = 0;
                }

                break;
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

    private boolean isValidToOutput(@Nonnull ItemStack itemStack, BlockPos outputSpace, @Nullable IItemHandler outputHandler)
    {
        if(outputHandler != null)
        {
            for(int i = 0; i < outputHandler.getSlots(); i++)
            {
                ItemStack ret = outputHandler.insertItem(i, itemStack, true);

                if(ret != itemStack)
                    return true;
            }
        }

        List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(outputSpace));


        return items.isEmpty();
    }

    private EnumFacing getFacing()
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
            case 0:
                return 1.69;
            case 1:
                return 1.2;
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

    public enum State
    {
        GRABBING,          // -> WAITING_TO_GRAB
        WAITING_TO_GRAB,   // -> RETURNING
        RETURNING,         // -> WAITING_TO_INSERT
        WAITING_TO_INSERT  // -> GRABBIGN
    }
}
