package com.mike_caron.factorycraft.energy;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Consumer;

public class EnergyConsumer
{
    private UUID networkId;

    public UUID getNetworkId()
    {
        return networkId;
    }

    public final void requestEnergy(int amount, @Nonnull Consumer<Integer> callback)
    {
        callback.accept(amount);
    }

    public final void requestEnergy(int amount)
    {
        onEnergyProvided(amount);
    }

    public void onEnergyProvided(int amount)
    {

    }
}
