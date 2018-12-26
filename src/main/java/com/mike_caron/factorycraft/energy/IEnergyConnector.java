package com.mike_caron.factorycraft.energy;

import java.util.UUID;

public interface IEnergyConnector
{
    int getConnectRadius();
    int getPowerRadius();
    void notifyNetworkChange(UUID newNetwork);
    UUID getNetworkId();
}
