package com.mike_caron.factorycraft.tileentity;

import com.mike_caron.factorycraft.block.BlockConveyor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec2f;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


class TileEntityConveyorTest
{
    final Vec2f ORIGIN = new Vec2f(0, 0);

    @ParameterizedTest(name = "0,0 r {0} = 0,0")
    @CsvSource({"0","45","90","180"})
    void rotateOriginIsOrigin(int theta)
    {
        TileEntityConveyor.ItemPosition pos = getItemPosition();

        Vec2f newPos = pos.rotate(ORIGIN, theta, ORIGIN);

        Assertions.assertEquals(0, newPos.x);
        Assertions.assertEquals(0, newPos.y);
    }

    @ParameterizedTest(name = "{0},{1} r {2} = {3},{4}")
    @CsvSource({
        "0, 0, 180, 1, 1",
        "0, 1, 180, 1, 0",
        "1, 0, 180, 0, 1",
        "1, 1, 180, 0, 0",
        "0, 0,  90, 0, 1",
        "0, 1,  90, 1, 1",
        "1, 1,  90, 1, 0",
        "1, 0,  90, 0, 0",
        "0.25, 0.25,  180, 0.75, 0.75"
    })
    void rotateAroundCenter(float x, float y, int theta, float expectedx, float expectedy)
    {
        TileEntityConveyor.ItemPosition pos = getItemPosition();

        Vec2f newPos = pos.rotate(ORIGIN, theta, ORIGIN);

        Assertions.assertEquals(0, newPos.x);
        Assertions.assertEquals(0, newPos.y);
    }


    private TileEntityConveyor.ItemPosition getItemPosition()
    {
        TileEntityConveyor conveyor = new TileEntityConveyor();

        return conveyor.itemPositions(EnumFacing.NORTH, BlockConveyor.EnumTurn.Straight);
    }

}