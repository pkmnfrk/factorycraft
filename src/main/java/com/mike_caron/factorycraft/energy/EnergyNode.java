package com.mike_caron.factorycraft.energy;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.UUID;

public class EnergyNode
{
    private UUID networkId;
    private TileEntity host;

    public EnergyNode(TileEntity host)
    {
        this.host = host;
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
}
