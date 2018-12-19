package com.mike_caron.factorycraft.tileentity;

import com.google.common.collect.ImmutableMap;
import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.api.IOreDeposit;
import com.mike_caron.factorycraft.block.DrillBlock;
import com.mike_caron.factorycraft.capability.OreDepositCapabilityProvider;
import com.mike_caron.factorycraft.world.OreDeposit;
import com.mike_caron.mikesmodslib.block.TileEntityBase;
import com.mike_caron.mikesmodslib.util.ItemUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DrillTileEntity
    extends TileEntityBase
    implements ITickable
{
    private int type = -1;
    private int progress = 0;
    private int maxProgress = 0;

    private IAnimationStateMachine asm;
    private final TimeValues.VariableValue anim_cycle = new TimeValues.VariableValue(1f);

    public DrillTileEntity()
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

    public DrillTileEntity(int type)
    {
        this();
        this.type = type;
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

        return ret;
    }

    @Override
    public void update()
    {
        if(world.isRemote) {
            return;
        }

        int startMaxProgress = maxProgress;

        IOreDeposit oreDeposit = getIOreDeposit();
        if(oreDeposit != null)
        {
            OreDeposit deposit = getDeposit(oreDeposit);

            if (deposit != null)
            {
                maxProgress = getTicksPerOre(deposit);

                progress += 1;
                markDirty();

                if (progress >= maxProgress)
                {
                    if (deposit.mineOne())
                    {
                        Vec3d output = getOutput();
                        ItemUtils.dropItem(world, deposit.getOreKind().ore.copy(), output.x, output.y, output.z);
                    }

                    progress = 0;
                }

            }
        }

        if(startMaxProgress != maxProgress)
        {
            markAndNotify();
        }
    }

    private Vec3d getOutput()
    {
        EnumFacing facing = world.getBlockState(pos).getValue(DrillBlock.FACING);
        Vec3i vec = facing.getDirectionVec();
        return new Vec3d(pos.getX() + (vec.getX() * 1.3 + 1) / 2.0, pos.getY() + 0.25, pos.getZ() + (vec.getZ() * 1.3 + 1) / 2.0);
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
}
