package com.mike_caron.factorycraft.storage;

import javax.annotation.Nonnull;

public interface ISlotKind
{
    @Nonnull
    EnumSlotKind getSlotKind(int slot);
    int desiredMaximum(int slot);
}
