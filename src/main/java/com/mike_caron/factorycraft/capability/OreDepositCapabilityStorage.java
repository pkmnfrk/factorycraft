package com.mike_caron.factorycraft.capability;

import com.mike_caron.factorycraft.api.IOreDeposit;
import com.mike_caron.factorycraft.world.OreDeposit;
import com.mike_caron.factorycraft.world.OreKind;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.Map;

public class OreDepositCapabilityStorage
    implements Capability.IStorage<IOreDeposit>
{
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IOreDeposit> capability, IOreDeposit iOreDeposit, EnumFacing enumFacing)
    {
        NBTTagList list = new NBTTagList();

        for(Map.Entry<Tuple<Integer, Integer>, OreDeposit> deposit : iOreDeposit.getAllDeposits().entrySet())
        {
            NBTTagCompound entry = new NBTTagCompound();

            entry.setInteger("x", deposit.getKey().getFirst());
            entry.setInteger("z", deposit.getKey().getFirst());
            entry.setString("kind", deposit.getValue().getOreKind().seedName);
            entry.setLong("size", deposit.getValue().getSize());
            entry.setLong("max", deposit.getValue().getMaxSize());

            list.appendTag(entry);
        }

        return list;
    }

    @Override
    public void readNBT(Capability<IOreDeposit> capability, IOreDeposit iOreDeposit, EnumFacing enumFacing, NBTBase nbtBase)
    {
        NBTTagList list = (NBTTagList)nbtBase;

        iOreDeposit.clearDeposits();

        for(int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound entry = list.getCompoundTagAt(i);

            int x = entry.getInteger("x");
            int z = entry.getInteger("z");
            String kind = entry.getString("kind");
            long size = entry.getLong("size");
            long maxSize = entry.getLong("max");

            OreKind oreKind = OreKind.ALL_ORES_BY_NAME.get(kind);

            OreDeposit deposit = new OreDeposit(oreKind, size, maxSize);

            iOreDeposit.putDeposit(x, z, deposit);
        }

        iOreDeposit.setGenerated();
    }
}
