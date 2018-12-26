package com.mike_caron.factorycraft.energy;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.UUID;

public class EnergyConnector
    implements IEnergyConnector
{
    private UUID networkId;
    private TileEntity host;
    private final int radius;

    public EnergyConnector(TileEntity host, int radius)
    {
        this.host = host;
        this.radius = radius;
    }

    public UUID getNetworkId()
    {
        return networkId;
    }

    public void disconnect()
    {

    }

    public void connect(World world)
    {
        //seek out other energy nodes, and join their network

    }

    @Override
    public int getRadius()
    {
        return radius;
    }

    @Override
    public void notifyNetworkChange(UUID newNetwork)
    {
        this.networkId = newNetwork;
    }
}
