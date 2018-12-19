package com.mike_caron.factorycraft.world;

import java.util.concurrent.atomic.AtomicLong;

public class OreDeposit
{
    private final OreKind oreKind;
    private final AtomicLong size;
    private final long maxSize;

    public OreDeposit(OreKind kind, long size, long maxSize)
    {
        this.oreKind = kind;
        this.size = new AtomicLong(size);
        this.maxSize = maxSize;
    }

    public OreKind getOreKind()
    {
        return oreKind;
    }

    public long getSize()
    {
        return size.get();
    }

    public long getMaxSize() { return maxSize; }

    public long mineOne() {
        return this.size.decrementAndGet();
    }
}
