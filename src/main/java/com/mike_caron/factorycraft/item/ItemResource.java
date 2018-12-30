package com.mike_caron.factorycraft.item;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.mikesmodslib.item.ItemBase;

public class ItemResource
    extends ItemBase
{
    private final String[] oreDicts;

    public ItemResource(String name, String... oreDicts)
    {
        super();
        this.setRegistryName(name);
        this.setTranslationKey("factorycraft:" + name);
        this.setCreativeTab(FactoryCraft.creativeTab);

        this.oreDicts = oreDicts;
    }

    public String[] getOreDicts()
    {
        return oreDicts;
    }
}
