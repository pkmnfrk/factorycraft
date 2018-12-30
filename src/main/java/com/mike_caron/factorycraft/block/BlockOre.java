package com.mike_caron.factorycraft.block;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.mikesmodslib.block.BlockBase;
import net.minecraft.block.material.Material;

public class BlockOre
    extends BlockBase
{
    public BlockOre(String name)
    {
        super(Material.ROCK, name);
        setHardness(5f);
        setCreativeTab(FactoryCraft.creativeTab);
    }

}
