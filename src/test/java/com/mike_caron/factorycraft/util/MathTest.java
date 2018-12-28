package com.mike_caron.factorycraft.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lwjgl.util.vector.Vector3f;

import java.util.stream.Stream;

public class MathTest
{
    private static Stream<Arguments> verifyRotationsSource()
    {
        return Stream.of(
            Arguments.of(new Vector3f(1,0,0), new Vector3f(0, 1, 0), new Vector3f(0, 0, -1), (float)Math.toRadians(90f)),
            Arguments.of(new Vector3f(1,0,0), new Vector3f(1, 1, 0), new Vector3f(0, 0, -0.70710677f),(float) Math.toRadians(45f))
        );
    }

    @ParameterizedTest
    @MethodSource("verifyRotationsSource")
    void verifyRotations(Vector3f first, Vector3f second, Vector3f expectedAxis, float expectedAngle)
    {
        Vector3f result = new Vector3f();
        float resultAngle = MathUtil.angle(first, second, result);

        assertApproximately(expectedAngle, resultAngle);
        assertApproximately(expectedAxis, result);
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
