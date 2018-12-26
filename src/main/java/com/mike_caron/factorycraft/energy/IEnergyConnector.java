package com.mike_caron.factorycraft.energy;

import java.util.UUID;

public interface IEnergyConnector
{
    int getRadius();
    void notifyNetworkChange(UUID newNetwork);
    UUID getNetworkId();
}
