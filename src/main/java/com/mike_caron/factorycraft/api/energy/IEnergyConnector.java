package com.mike_caron.factorycraft.api.energy;

import java.util.UUID;
import java.util.function.IntConsumer;

public interface IEnergyConnector
{
    int getConnectRadius();
    int getPowerRadius();
    void notifyNetworkChange(UUID newNetwork);
    UUID getNetworkId();

    void requestEnergy(int amount, IntConsumer callback);
    void provideEnergy(int amount, IntConsumer callback);
}
