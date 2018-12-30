package com.mike_caron.factorycraft.tileentity;

import com.google.common.collect.ImmutableMap;
import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.api.IConveyorBelt;
import com.mike_caron.factorycraft.api.IOreDeposit;
import com.mike_caron.factorycraft.api.capabilities.CapabilityConveyor;
import com.mike_caron.factorycraft.api.capabilities.CapabilityOreDeposit;
import com.mike_caron.factorycraft.block.BlockDrill;
import com.mike_caron.factorycraft.energy.ElectricalEnergyAppliance;
import com.mike_caron.factorycraft.energy.SolidEnergyAppliance;
import com.mike_caron.factorycraft.world.OreDeposit;
import com.mike_caron.mikesmodslib.block.IAnimationEventHandler;
import com.mike_caron.mikesmodslib.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityDrill
    extends EnergyTileEntity
    implements ITickable, IAnimationEventHandler
{
    private int progress = 0;
    private int maxProgress = 0;
    private int lastMaxProgress = 0;

    private IAnimationStateMachine asm = null;
    private final TimeValues.VariableValue animProgress = new TimeValues.VariableValue(0f);

    private ItemStack miningTarget = ItemStack.EMPTY;

    public TileEntityDrill()
    {
        super();
    }

    @Override
    protected void onKnowingType()
    {
        super.onKnowingType();

        if(this.type == 0)
        {
            this.energyAppliance = new SolidEnergyAppliance(this);
        }
        else
        {
            this.energyAppliance = new ElectricalEnergyAppliance(this);
        }
    }

    public TileEntityDrill(int type)
    {
        super(type);
    }

    public void loadAsm()
    {
        String currentState = "inactive";
        if(asm != null)
        {
            currentState = asm.currentState();
        }
        String animation = "asms/block/drill_burner.json";

        if(type == 1)
        {
            animation = "asms/block/drill.json";
        }
        asm = ModelLoaderRegistry.loadASM(new ResourceLocation(FactoryCraft.modId, animation), ImmutableMap.of("progress", animProgress));

        if(!asm.currentState() .equals(currentState))
            asm.transition(currentState);
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

        progress = compound.getInteger("progress");
        if(compound.hasKey("lastMaxProgress"))
            lastMaxProgress = compound.getInteger("lastMaxProgress");

        int newMaxProgress = compound.getInteger("maxProgress");
        if(asm != null)
        {
            if(newMaxProgress != maxProgress)
            {
                if(newMaxProgress > 0)
                {
                    if(!asm.currentState().equals("active"))
                        asm.transition("active");
                }
                else
                {
                    if(!asm.currentState().equals("inactive"))
                    asm.transition("inactive");
                }
            }
        }
        maxProgress = newMaxProgress;
        if(maxProgress > 0)
        {
            animProgress.setValue(((float)progress) / maxProgress);
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
        return ret;
    }

    @Override
    public void update()
    {
        if(world.isRemote)
        {
            loadAsm();

            if(maxProgress > 0)
            {
                progress += 1f;
                animProgress.setValue(((float)progress) / maxProgress);
            }
            return;
        }

        energyAppliance.requestEnergy(getEnergyUsage(), this::update);
    }

    public void update(int energy)
    {
        int startMaxProgress = maxProgress;

        maxProgress = 0;


        if(energy > 0)
        {
            miningTarget = ItemStack.EMPTY;

            IOreDeposit oreDeposit = getIOreDeposit();
            if (oreDeposit != null)
            {
                OreDeposit deposit = getDeposit(oreDeposit);

                if (deposit != null && deposit.getSize() > 0)
                {
                    IConveyorBelt outputConveyor = getOuptutConveyor();
                    IItemHandler outputInventory = getOuptutInventory();

                    ItemStack ore = deposit.getOreKind().ore.get();
                    miningTarget = ore;

                    if((outputInventory == null && outputConveyor == null) || (outputConveyor != null && tryInsert(ore, outputConveyor, true) != ore) || (outputInventory != null && tryInsert(ore, outputInventory, true) != ore))
                    {
                        maxProgress = getTicksPerOre(deposit) * getEnergyUsage() / energy;

                        if(lastMaxProgress != 0)
                        {
                            progress = progress * maxProgress / lastMaxProgress;
                        }

                        lastMaxProgress = maxProgress;

                        progress += 1;

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
                                    ItemUtils.insertItemIfPossible(deposit.getOreKind().ore.get(), outputInventory);
                                }
                                else
                                {
                                    Vec3d output = getOutput();

                                    ItemUtils.dropItem(world, deposit.getOreKind().ore.get(), output.x, output.y, output.z);
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

    private float getProgress(int energy)
    {
        if(type == 0) return 1;
        return ((float)energy) / getEnergyUsage();
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
        return chunk.getCapability(CapabilityOreDeposit.OREDEPOSIT, null);
    }

    @Nullable
    private OreDeposit getDeposit(@Nonnull IOreDeposit deposit)
    {
        int x = (pos.getX() & 15) / 4;
        int z = (pos.getZ() & 15) / 4;

        return deposit.getOreDeposit(x, z);
    }

    public int getEnergyUsage()
    {
        return 15000;
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

    public float getProgress()
    {
        if(maxProgress == 0)
            return 0;
        return ((float)progress) / maxProgress;
    }

    public ItemStack getCurrentMiningTarget()
    {
        return miningTarget;
    }

}
