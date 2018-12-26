package com.mike_caron.factorycraft.energy;

import com.mike_caron.factorycraft.api.energy.IEnergyConnector;
import com.mike_caron.factorycraft.api.energy.IEnergyManager;
import com.mike_caron.factorycraft.util.ITileEntityFinder;
import com.mike_caron.factorycraft.util.Tuple3i;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.HashMap;

public class EnergyManagerTest
{
    @org.junit.jupiter.api.BeforeEach
    void preTest()
    {

    }

    @Test
    void registeringConnectorGetsNetworkId()
    {
        BlockPos zero = new BlockPos(0,0,0);
        DummyTileEntity te1 = new DummyTileEntity();


        ITileEntityFinder world = returnTileEntities(zero, te1);

        IEnergyManager manager = getManager(world);

        manager.registerConnector(zero);

        Assertions.assertNotEquals(null, te1.connector.getNetworkId());
    }

    @Test
    void closeConnectorsAutomaticallyConnect()
    {
        BlockPos zero = new BlockPos(0,0,0);
        BlockPos one = new BlockPos(1,1,1);
        DummyTileEntity te1 = new DummyTileEntity();
        DummyTileEntity te2 = new DummyTileEntity();


        ITileEntityFinder world = returnTileEntities(zero, te1, one, te2);

        IEnergyManager manager = getManager(world);

        manager.registerConnector(zero);
        manager.registerConnector(one);

        Assertions.assertTrue(manager.isConnected(zero, one));
    }

    @Test
    void connectedConnectorsHaveSameNetworkId()
    {
        BlockPos zero = new BlockPos(0,0,0);
        BlockPos one = new BlockPos(1,1,1);
        DummyTileEntity te1 = new DummyTileEntity();
        DummyTileEntity te2 = new DummyTileEntity();


        ITileEntityFinder world = returnTileEntities(zero, te1, one, te2);

        IEnergyManager manager = getManager(world);

        manager.registerConnector(zero);
        manager.registerConnector(one);

        Assertions.assertEquals(te1.connector.getNetworkId(), te2.connector.getNetworkId());
    }

    @Test
    void disconnectingConnectorsDisconnectsConectors()
    {
        BlockPos zero = new BlockPos(0,0,0);
        BlockPos one = new BlockPos(1,1,1);
        DummyTileEntity te1 = new DummyTileEntity();
        DummyTileEntity te2 = new DummyTileEntity();

        ITileEntityFinder world = returnTileEntities(zero, te1, one, te2);

        IEnergyManager manager = getManager(world);

        manager.registerConnector(zero);
        manager.registerConnector(one);

        manager.removeConnection(zero, one);

        Assertions.assertFalse(manager.isConnected(zero, one));
    }

    @Test
    void disconnectingConnectorsSplitsNetwork()
    {
        BlockPos zero = new BlockPos(0,0,0);
        BlockPos one = new BlockPos(1,1,1);
        DummyTileEntity te1 = new DummyTileEntity();
        DummyTileEntity te2 = new DummyTileEntity();

        ITileEntityFinder world = returnTileEntities(zero, te1, one, te2);

        IEnergyManager manager = getManager(world);

        manager.registerConnector(zero);
        manager.registerConnector(one);

        manager.removeConnection(zero, one);

        Assertions.assertNotEquals(te1.connector.getNetworkId(), te2.connector.getNetworkId());
    }

    EnergyManager getManager(ITileEntityFinder world)
    {
        return new EnergyManager(world);
    }

    ITileEntityFinder returnTileEntities(Object... tes)
    {
        DummyTileEntityFinder ret = new DummyTileEntityFinder();
        for(int i = 0; i < tes.length; i += 2)
        {
            BlockPos pos = (BlockPos)tes[i];
            TileEntity te = (TileEntity)tes[i + 1];
            ret.te.put(new Tuple3i(pos.getX(), pos.getY(), pos.getZ()), te);
        }

        return ret;
    }

    class DummyTileEntityFinder
        implements ITileEntityFinder
    {
        final HashMap<Tuple3i, TileEntity> te = new HashMap<>();

        @Override
        public TileEntity getTileEntityAt(BlockPos pos)
        {
            Tuple3i vec = new Tuple3i(pos.getX(), pos.getY(), pos.getZ());
            if(te.containsKey(vec))
                return te.get(vec);

            return null;
        }
    }

    class DummyTileEntity
        extends TileEntity
    {
        IEnergyConnector connector = new EnergyConnector(this, 5, 3);

        @Override
        public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
        {
            return connector != null;
        }

        @Nullable
        @Override
        public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
        {
            return (T)connector;
        }
    }
}
