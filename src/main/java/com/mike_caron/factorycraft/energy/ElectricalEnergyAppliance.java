package com.mike_caron.factorycraft.energy;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.api.capabilities.CapabilityEnergyConnector;
import com.mike_caron.factorycraft.api.capabilities.CapabilityEnergyManager;
import com.mike_caron.factorycraft.api.energy.IEnergyConnector;
import com.mike_caron.factorycraft.api.energy.IEnergyManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.function.IntConsumer;

public class ElectricalEnergyAppliance
    extends EnergyAppliance
{
    private BlockPos connector;

    public ElectricalEnergyAppliance(TileEntity host)
    {
        super(host);
    }

    private void hookupMains()
    {
        if(connector != null)
        {
            IEnergyConnector conn = getConnector();

            if(conn != null)
            {
                return;
            }
        }

        IEnergyManager manager = getManager();

        connector = manager.findConnector(host.getPos());

        if(connector != null)
        {
            IEnergyConnector conn = getConnector();

            if(conn == null)
            {
                FactoryCraft.logger.warn("Recieved invalid connector from Energy Manager.");
                connector = null;
            }
        }
    }

    private IEnergyConnector getConnector()
    {
        TileEntity te = host.getWorld().getTileEntity(connector);
        if(te == null) return null;

        return te.getCapability(CapabilityEnergyConnector.ENERGY_CONNECTOR, null);
    }

    private IEnergyManager getManager()
    {
        return host.getWorld().getCapability(CapabilityEnergyManager.ENERGY_MANAGER, null);
    }

    @Override
    public final void requestEnergy(int amount, @Nonnull IntConsumer callback)
    {
        hookupMains();

        if(connector == null)
        {
            callback.accept(0);
        }
        else
        {
            IEnergyConnector conn = getConnector();

            conn.requestEnergy(amount, callback);
            //callback.accept(amount);
        }
    }

    @Override
    public final void provideEnergy(int maxEnergy, @Nonnull IntConsumer callback)
    {
        hookupMains();

        if(connector == null)
        {
            callback.accept(0);
        }
        else
        {
            IEnergyConnector conn = getConnector();

            conn.provideEnergy(maxEnergy, callback);
        }
    }

}
