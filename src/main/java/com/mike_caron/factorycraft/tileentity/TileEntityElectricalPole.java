package com.mike_caron.factorycraft.tileentity;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.api.capabilities.CapabilityEnergyConnector;
import com.mike_caron.factorycraft.api.energy.IEnergyConnector;
import com.mike_caron.factorycraft.energy.EnergyConnector;
import com.mike_caron.factorycraft.util.BlockPosNBTSerializer;
import com.mike_caron.factorycraft.util.INBTSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityElectricalPole
    extends TypedTileEntity
{
    private final EnergyConnector energyConnector = new MyEnergyConnector(this);
    private List<BlockPos> connections = null;

    public TileEntityElectricalPole()
    {
        super();
    }

    public TileEntityElectricalPole(int type)
    {
        super(type);

    }

    @Override
    public boolean hasFastRenderer()
    {
        return false;
    }

    public void connect()
    {
        energyConnector.connectIfNeeded();
    }

    public void disconnect()
    {
        energyConnector.disconnect();
    }

    @Override
    protected void onKnowingType()
    {
        super.onKnowingType();
    }

    public int getConnectRadius()
    {
        switch(type)
        {
            default:
                return 5;
        }
    }

    public int getPowerRadius()
    {
        switch(type)
        {
            default:
                return 3;
        }
    }

    @Nonnull
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        SPacketUpdateTileEntity ret = super.getUpdatePacket();

        writeConnections(ret.getNbtCompound());

        return ret;
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag()
    {
        NBTTagCompound ret = super.getUpdateTag();

        FactoryCraft.logger.info("getUpdateTag for {}", getPos());

        writeConnections(ret);

        return ret;
    }

    private void writeConnections(NBTTagCompound ret)
    {
        List<BlockPos> connections = energyConnector.getConnections();

        NBTTagList connectionList = new NBTTagList();
        INBTSerializer<BlockPos> serializer = new BlockPosNBTSerializer();

        for(BlockPos pos : connections)
        {
            connectionList.appendTag(serializer.serializeNBT(pos));
        }

        ret.setTag("connections", connectionList);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        energyConnector.deserializeNBT(nbt.getCompoundTag("energy"));

        connections = null;
        if(nbt.hasKey("connections"))
        {
            NBTTagList list = (NBTTagList)nbt.getTag("connections");

            connections = new ArrayList<>();
            INBTSerializer<BlockPos> serializer = new BlockPosNBTSerializer();

            for(int i = 0; i < list.tagCount(); i++)
            {
                BlockPos pos = serializer.deserializeNBT(list.getCompoundTagAt(i));
                connections.add(pos);
            }
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound)
    {
        NBTTagCompound ret = super.writeToNBT(compound);

        ret.setTag("energy", energyConnector.serializeNBT());

        return ret;
    }

    public IEnergyConnector getConnector()
    {
        return energyConnector;
    }

    public List<BlockPos> getConnections()
    {
        if(connections != null)
            return connections;

        return energyConnector.getConnections();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityEnergyConnector.ENERGY_CONNECTOR)
            return true;
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityEnergyConnector.ENERGY_CONNECTOR)
            return CapabilityEnergyConnector.ENERGY_CONNECTOR.cast(energyConnector);

        return super.getCapability(capability, facing);
    }

    class MyEnergyConnector
        extends EnergyConnector
    {
        public MyEnergyConnector(TileEntity host)
        {
            super(host);
        }

        @Override
        public int getConnectRadius()
        {
            return TileEntityElectricalPole.this.getConnectRadius();
        }

        @Override
        public int getPowerRadius()
        {
            return TileEntityElectricalPole.this.getPowerRadius();
        }

        @Override
        protected void onConnectionsChanged()
        {
            markAndNotify();
        }
    }
}
