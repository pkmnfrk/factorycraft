package com.mike_caron.factorycraft.tileentity;

import com.mike_caron.factorycraft.energy.EnergyAppliance;
import com.mike_caron.mikesmodslib.block.TileEntityBase;
import net.minecraft.util.ITickable;

public class TileEntityCreativePower
    extends TileEntityBase
    implements ITickable
{
    private EnergyAppliance energyAppliance = new EnergyAppliance(this);
    private static final int energyPerTick = 100000;

    @Override
    public void update()
    {
        if(world.isRemote) return;

        energyAppliance.provideEnergy(energyPerTick, (actual) -> {});
    }
}
