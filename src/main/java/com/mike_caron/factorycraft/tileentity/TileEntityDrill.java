package com.mike_caron.factorycraft.tileentity;

import com.google.common.collect.ImmutableMap;
import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.api.IConveyorBelt;
import com.mike_caron.factorycraft.api.IOreDeposit;
import com.mike_caron.factorycraft.block.BlockDrill;
import com.mike_caron.factorycraft.capability.CapabilityConveyor;
import com.mike_caron.factorycraft.capability.OreDepositCapabilityProvider;
import com.mike_caron.factorycraft.world.OreDeposit;
import com.mike_caron.mikesmodslib.block.IAnimationEventHandler;
import com.mike_caron.mikesmodslib.block.TileEntityBase;
import com.mike_caron.mikesmodslib.util.ItemUtils;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.chunk.Chunk;
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
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityDrill
    extends TileEntityBase
    implements ITickable, IAnimationEventHandler, ILimitedInputItems
{
    private int type = -1;
    private int progress = 0;
    private int maxProgress = 0;
    private int fuelTicks = 0;

    private IAnimationStateMachine asm;
    private final TimeValues.VariableValue anim_cycle = new TimeValues.VariableValue(1f);

    ItemStackHandler inventory;

    NonNullList<ItemStack> limitedItems;

    public TileEntityDrill()
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

    public TileEntityDrill(int type)
    {
        this();
        this.type = type;
        this.inventory = new CustomItemStackHandler();
    }

    public void loadAsm()
    {
        String currentState = "inactive";
        if(asm != null)
        {
            currentState = asm.currentState();
        }

        asm = ModelLoaderRegistry.loadASM(new ResourceLocation(FactoryCraft.modId, "asms/block/drill_burner.json"), ImmutableMap.of("cycle_length", anim_cycle));

        if(!asm.currentState() .equals(currentState))
            asm.transition(currentState);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityAnimation.ANIMATION_CAPABILITY)
            return true;

        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null)
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
        else if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);

        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        type = compound.getInteger("type");
        progress = compound.getInteger("progress");
        if(type == 0)
        {
            fuelTicks = compound.getInteger("fuelTicks");
        }
        if(inventory == null)
        {
            inventory = new CustomItemStackHandler();
        }
        inventory.deserializeNBT(compound.getCompoundTag("inv"));

        int newMaxProgress = compound.getInteger("maxProgress");
        if(asm != null)
        {
            if(newMaxProgress != maxProgress)
            {
                if(newMaxProgress > 0)
                {
                    if(asm.currentState() != "active")
                        asm.transition("active");

                    anim_cycle.setValue(newMaxProgress / 20f);
                }
                else
                {
                    asm.transition("inactive");
                }
            }
        }
        maxProgress = newMaxProgress;
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound)
    {
        NBTTagCompound ret = super.writeToNBT(compound);

        ret.setInteger("type", type);
        ret.setInteger("progress", progress);
        ret.setInteger("maxProgress", maxProgress);
        if(type == 0)
        {
            ret.setInteger("fuelTicks", fuelTicks);
        }
        ret.setTag("inv", inventory.serializeNBT());

        return ret;
    }

    @Override
    public void update()
    {
        if(world.isRemote) {
            return;
        }

        int startMaxProgress = maxProgress;

        maxProgress = 0;

        if(type == 0 && fuelTicks <= 0)
        {
            ItemStack fuel = inventory.getStackInSlot(0);
            if(!fuel.isEmpty())
            {
                fuelTicks += TileEntityFurnace.getItemBurnTime(fuel);

                Item item = fuel.getItem();
                fuel.shrink(1);
                if (fuel.isEmpty()) {
                    ItemStack item1 = item.getContainerItem(fuel);
                    inventory.setStackInSlot(0, item1);
                }
            }
        }



        if(type != 0 || fuelTicks > 0)
        {

            IOreDeposit oreDeposit = getIOreDeposit();
            if (oreDeposit != null)
            {
                OreDeposit deposit = getDeposit(oreDeposit);

                if (deposit != null && deposit.getSize() > 0)
                {
                    IConveyorBelt outputConveyor = getOuptutConveyor();
                    IItemHandler outputInventory = getOuptutInventory();

                    ItemStack ore = deposit.getOreKind().ore;

                    if((outputInventory == null && outputConveyor == null) || (outputConveyor != null && tryInsert(ore, outputConveyor, true) != ore) || (outputInventory != null && tryInsert(ore, outputInventory, true) != ore))
                    {
                        maxProgress = getTicksPerOre(deposit);

                        progress += 1;
                        if (type == 0)
                        {
                            fuelTicks -= 1;
                        }
                        markDirty();

                        if (progress >= maxProgress)
                        {
                            if (deposit.mineOne())
                            {
                                if(outputConveyor != null)
                                {
                                    tryInsert(ore, outputConveyor, false);
                                }
                                else if(outputInventory != null)
                                {
                                    ItemUtils.insertItemIfPossible(deposit.getOreKind().ore, outputInventory);
                                }
                                else
                                {
                                    Vec3d output = getOutput();

                                    ItemUtils.dropItem(world, deposit.getOreKind().ore.copy(), output.x, output.y, output.z);
                                }

                                if(deposit.getSize() <= 0)
                                {
                                    //world.setBlockToAir(pos.down());
                                    world.destroyBlock(pos.down(), false);
                                }
                            }

                            progress = 0;
                        }
                    }
                }
            }
        }

        if(startMaxProgress != maxProgress)
        {
            markAndNotify();
        }
    }

    private ItemStack tryInsert(ItemStack stack, IConveyorBelt handler, boolean simulate)
    {
        int track = handler.trackClosestTo(getFacing());
        float len = handler.trackLength(track);
        return handler.insert(track, len / 2, stack, simulate);
    }

    private ItemStack tryInsert(ItemStack stack, IItemHandler handler, boolean simulate)
    {
        for(int i = 0; i < handler.getSlots(); i++)
        {
            if(!handler.isItemValid(i, stack))
                continue;

            ItemStack res = handler.insertItem(i, stack, simulate);
            if(res != stack)
                return res;
        }
        return stack;
    }

    private IItemHandler getOuptutInventory()
    {
        Vec3d output = getOutput();

        BlockPos blockPos = new BlockPos(output);

        TileEntity te = world.getTileEntity(blockPos);

        if(te != null)
        {
            return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing().getOpposite());
        }

        blockPos = blockPos.offset(EnumFacing.DOWN);

        te = world.getTileEntity(blockPos);

        if(te != null)
        {
            return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing().getOpposite());
        }

        return null;
    }

    private IConveyorBelt getOuptutConveyor()
    {
        Vec3d output = getOutput();

        BlockPos blockPos = new BlockPos(output);

        TileEntity te = world.getTileEntity(blockPos);

        if(te != null)
        {
            return te.getCapability(CapabilityConveyor.CONVEYOR, getFacing());
        }

        blockPos = blockPos.offset(EnumFacing.DOWN);

        te = world.getTileEntity(blockPos);

        if(te != null)
        {
            return te.getCapability(CapabilityConveyor.CONVEYOR, getFacing());
        }

        return null;
    }

    private Vec3d getOutput()
    {
        EnumFacing facing = getFacing();
        Vec3i vec = facing.getDirectionVec();
        return new Vec3d(pos.getX() + (vec.getX() * 1.3 + 1) / 2.0, pos.getY() + 0.25, pos.getZ() + (vec.getZ() * 1.3 + 1) / 2.0);
    }

    private EnumFacing getFacing()
    {
        return world.getBlockState(pos).getValue(BlockDrill.FACING);
    }

    private int getTicksPerOre(OreDeposit deposit)
    {
        double power = getPower();
        double speed = getSpeed();
        double hardness = deposit.getOreKind().hardness;
        double miningTime = deposit.getOreKind().miningTime;

        return (int)(miningTime / ((power - hardness) * speed) * 20);

    }

    private IOreDeposit getIOreDeposit()
    {
        Chunk chunk = world.getChunk(pos);
        return chunk.getCapability(OreDepositCapabilityProvider.OREDEPOSIT, null);
    }

    @Nullable
    private OreDeposit getDeposit(@Nonnull IOreDeposit deposit)
    {
        int x = (pos.getX() & 15) / 4;
        int z = (pos.getZ() & 15) / 4;

        return deposit.getOreDeposit(x, z);
    }

    private double getPower()
    {
        switch(type){
            case 0:
                return 2.5;
            case 1:
                return 3.0;
        }
        return 1.0;
    }

    private double getSpeed()
    {
        switch (type)
        {
            case 0:
                return 0.35;
            case 1:
                return 0.5;
        }
        return 0.1;
    }

    public void handleAnimationEvent(float time, Iterable<Event> pastEvents)
    {
        for(Event event : pastEvents)
        {
            FactoryCraft.logger.info("Got animation event {} at {}", event.event(), time);
        }
    }

    @Override
    public NonNullList<ItemStack> getLimitedItems()
    {
        if(limitedItems == null)
        {
            limitedItems = NonNullList.create();
            if(type == 0)
            {
                limitedItems.add(new ItemStack(Items.COAL, 5));
            }
        }
        return limitedItems;
    }

    public void addItemsToDrop(NonNullList<ItemStack> items)
    {
        for(int i = 0; i < inventory.getSlots(); i++)
        {
            if(!inventory.getStackInSlot(i).isEmpty())
            {
                items.add(inventory.getStackInSlot(i));
            }
        }
    }

    class CustomItemStackHandler
        extends ItemStackHandler
    {
        public CustomItemStackHandler()
        {
            super(type == 0 ? 1 : 0);
        }

        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);

            markDirty();
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack)
        {
            if(!super.isItemValid(slot, stack)) return false;

            if(slot == 0) //fuel
            {
                return TileEntityFurnace.isItemFuel(stack);
            }

            return false;
        }
    }
}
