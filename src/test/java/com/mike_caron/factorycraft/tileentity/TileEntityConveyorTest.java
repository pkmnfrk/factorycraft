package com.mike_caron.factorycraft.tileentity;

import com.mike_caron.factorycraft.block.BlockConveyor;
import net.minecraft.util.EnumFacing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lwjgl.util.vector.Vector3f;


class TileEntityConveyorTest
{
    final Vector3f ORIGIN = new Vector3f(0, 0, 0);
    final Vector3f HALF = new Vector3f(0.5f, 0, 0.5f);

    @ParameterizedTest(name = "0,0,0 r {0} = 0,0,0")
    @CsvSource({"0","45","90","180"})
    void rotateOriginIsOrigin(int theta)
    {
        TileEntityConveyor.ItemPosition pos = getItemPosition();

        Vector3f newPos = pos.rotate(ORIGIN, theta, ORIGIN);

        Assertions.assertEquals(0, newPos.x);
        Assertions.assertEquals(0, newPos.y);
        Assertions.assertEquals(0, newPos.z);
    }

    @ParameterizedTest(name = "{0},{1} r {2} = {3},{4}")
    @CsvSource({
        "0, 0, 180, 1, 1",
        "0, 1, 180, 1, 0",
        "1, 0, 180, 0, 1",
        "1, 1, 180, 0, 0",
        "0, 0, -90, 0, 1",
        "0, 1, -90, 1, 1",
        "1, 1, -90, 1, 0",
        "1, 0, -90, 0, 0",
        "0.25, 0.25,  180, 0.75, 0.75"
    })
    void rotateAroundCenter(float x, float z, int theta, float expectedx, float expectedz)
    {
        TileEntityConveyor.ItemPosition pos = getItemPosition();

        Vector3f newPos = pos.rotate(HALF, theta, new Vector3f(x, 0, z));

        assertApproximately(new Vector3f(expectedx, 0, expectedz), newPos);
    }


    private TileEntityConveyor.ItemPosition getItemPosition()
    {
        TileEntityConveyor conveyor = new TileEntityConveyor();

        return conveyor.itemPositions(EnumFacing.NORTH, BlockConveyor.EnumTurn.Straight);
    }

    private void assertApproximately(float expected, float actual)
    {
        float delta = Math.abs(actual - expected);
        Assertions.assertTrue(delta < 0.0001, () -> "Expected " + expected + ", got " + actual);
    }

    private void assertApproximately(Vector3f expected, Vector3f actual)
    {
        float delta = Math.abs(actual.x - expected.x);
        Assertions.assertTrue(delta < 0.0001, () -> "Expected " + expected + ", got " + actual);
        delta = Math.abs(actual.y - expected.y);
        Assertions.assertTrue(delta < 0.0001, () -> "Expected " + expected + ", got " + actual);
        delta = Math.abs(actual.z - expected.z);
        Assertions.assertTrue(delta < 0.0001, () -> "Expected " + expected + ", got " + actual);
    }

}