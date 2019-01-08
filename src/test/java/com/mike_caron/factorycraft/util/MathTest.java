package com.mike_caron.factorycraft.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.util.stream.Stream;

public class MathTest
{
    private static Stream<Arguments> verifyRotationsSource()
    {
        return Stream.of(
            Arguments.of(new Vector3f(1,0,0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1), (float)Math.toRadians(90f)),
            Arguments.of(new Vector3f(1,0,0), new Vector3f(1, 1, 0), new Vector3f(0, 0, 1),(float) Math.toRadians(45f))
        );
    }

    private static Stream<Arguments> verifyNormalizationSource()
    {
        return Stream.of(
            Arguments.of(new Vector3f(0.5f,0,0), new Vector3f(1, 0, 0)),
            Arguments.of(new Vector3f(25f,0,0), new Vector3f(1, 0, 0)),
            Arguments.of(new Vector3f(0,25f,0), new Vector3f(0, 1, 0)),
            Arguments.of(new Vector3f(0,0, 25f), new Vector3f(0, 0, 1)),
            Arguments.of(new Vector3f(-0.5f,0,0), new Vector3f(-1, 0, 0)),
            Arguments.of(new Vector3f(2f,2f,0), new Vector3f((float)Math.sqrt(2) / 2, (float)Math.sqrt(2) / 2, 0))
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

    @ParameterizedTest
    @MethodSource("verifyNormalizationSource")
    void verifyNormalization(Vector3f vector, Vector3f expected)
    {
        Vector3f normal = MathUtil.normalize(vector);

        assertApproximately(1, normal.length());
        assertApproximately(expected, normal);
    }

    @Test
    void translationIsCorrect()
    {
        Matrix4f identity = new Matrix4f();
        identity.setIdentity();

        Matrix4f translated = Matrix4f.translate(new Vector3f(1, 2, 3), identity, null);

        Vector4f test = new Vector4f(1, 1, 1, 1);

        Vector4f result = Matrix4f.transform(translated, test, null);

        assertApproximately(new Vector4f(2, 3, 4, 1), result);
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

    private void assertApproximately(Vector4f expected, Vector4f actual)
    {
        float delta = Math.abs(actual.x - expected.x);
        if(delta >= 0.0001) Assertions.assertEquals(expected, actual);
        delta = Math.abs(actual.y - expected.y);
        if(delta >= 0.0001) Assertions.assertEquals(expected, actual);
        delta = Math.abs(actual.z - expected.z);
        if(delta >= 0.0001) Assertions.assertEquals(expected, actual);
        delta = Math.abs(actual.w - expected.w);
        if(delta >= 0.0001) Assertions.assertEquals(expected, actual);
    }
}
