package com.mike_caron.factorycraft.energy;

import java.util.UUID;
import java.util.function.Consumer;

public class EnergyProducer
{
    private UUID networkId;

    public UUID getNetworkId()
    {
        return networkId;
    }

    public void provideEnergy(int maxEnergy, Consumer<Integer> callback)
    {
        callback.accept(0);
    }

    public void provideEnergy(int maxEnergy)
    {
        onEnergyProvided(0);
    }

    protected void onEnergyProvided(int actualEnergy)
    {

    }
}
