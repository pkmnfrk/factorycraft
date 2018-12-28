package com.mike_caron.factorycraft.tileentity;

import com.mike_caron.factorycraft.energy.ElectricalEnergyAppliance;
import com.mike_caron.factorycraft.energy.EnergyAppliance;
import com.mike_caron.mikesmodslib.block.TileEntityBase;
import net.minecraft.util.ITickable;

public class TileEntityCreativePower
    extends TileEntityBase
    implements ITickable
{
    private EnergyAppliance energyAppliance = new ElectricalEnergyAppliance(this);
    private static final int energyPerTick = 600;

    @Override
    public void update()
    {
        if(world.isRemote) return;

        energyAppliance.provideEnergy(12000, (actual) -> {});
    }
}
