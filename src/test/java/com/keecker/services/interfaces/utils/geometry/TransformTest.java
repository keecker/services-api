package com.keecker.services.interfaces.utils.geometry;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Created by Thomas Gallagher <thomas@keecker.com> on 12/05/16.
 */
public class TransformTest {

    @Test
    public void testMultiply() {
        Transform transform1 = new Transform(Vector3.xAxis(), Quaternion.identity());
        Transform transform2 =
                new Transform(Vector3.yAxis(), Quaternion.fromAxisAngle(Vector3.zAxis(), Math.PI / 2));

        Transform result1 = transform1.multiply(transform2);
        assertEquals(1.0, result1.translation.x, 1e-9);
        assertEquals(1.0, result1.translation.y, 1e-9);
        assertEquals(0.0, result1.translation.z, 1e-9);
        assertEquals(0.0, result1.rotation.x, 1e-9);
        assertEquals(0.0, result1.rotation.y, 1e-9);
        assertEquals(0.7071067811865475, result1.rotation.z, 1e-9);
        assertEquals(0.7071067811865475, result1.rotation.w, 1e-9);

        Transform result2 = transform2.multiply(transform1);
        assertEquals(0.0, result2.translation.x, 1e-9);
        assertEquals(2.0, result2.translation.y, 1e-9);
        assertEquals(0.0, result2.translation.z, 1e-9);
        assertEquals(0.0, result2.rotation.x, 1e-9);
        assertEquals(0.0, result2.rotation.y, 1e-9);
        assertEquals(0.7071067811865475, result2.rotation.z, 1e-9);
        assertEquals(0.7071067811865475, result2.rotation.w, 1e-9);
    }

    @Test
    public void testInvert() {
        Transform transform =
                new Transform(Vector3.yAxis(), Quaternion.fromAxisAngle(Vector3.zAxis(), Math.PI / 2));
        Transform inverse = transform.invert();

        assertEquals(-1.0, inverse.translation.x, 1e-9);
        assertEquals(0.0, inverse.translation.y, 1e-9);
        assertEquals(0.0, inverse.translation.z, 1e-9);
        assertEquals(0.0, inverse.rotation.x, 1e-9);
        assertEquals(0.0, inverse.rotation.y, 1e-9);
        assertEquals(-0.7071067811865475, inverse.rotation.z, 1e-9);
        assertEquals(0.7071067811865475, inverse.rotation.w, 1e-9);

        Transform neutral = transform.multiply(inverse);
        assertTrue(neutral.almostEquals(Transform.identity(), 1e-9));
    }

    @Test
    public void testInvertRandom() {
        Random random = new Random();
        random.setSeed(42);
        for (int i = 0; i < 10000; i++) {
            Vector3 vector = randomVector(random);
            Quaternion quaternion = randomQuaternion(random);
            Transform transform = new Transform(vector, quaternion);
            Transform inverse = transform.invert();
            Transform neutral = transform.multiply(inverse);
            assertTrue(neutral.almostEquals(Transform.identity(), 1e-9));
        }
    }

    @Test
    public void testMultiplyRandom() {
        Random random = new Random();
        random.setSeed(42);
        for (int i = 0; i < 10000; i++) {
            Vector3 data = randomVector(random);
            Vector3 vector1 = randomVector(random);
            Vector3 vector2 = randomVector(random);
            Quaternion quaternion1 = randomQuaternion(random);
            Quaternion quaternion2 = randomQuaternion(random);
            Transform transform1 = new Transform(vector1, quaternion1);
            Transform transform2 = new Transform(vector2, quaternion2);
            Vector3 result1 = transform1.apply(transform2.apply(data));
            Vector3 result2 = transform1.multiply(transform2).apply(data);
            assertTrue(result1.almostEquals(result2, 1e-9));
        }
    }

    private Quaternion randomQuaternion(Random random) {
        return new Quaternion(random.nextDouble(), random.nextDouble(), random.nextDouble(),
                random.nextDouble());
    }

    private Vector3 randomVector(Random random) {
        return new Vector3(random.nextDouble(), random.nextDouble(), random.nextDouble());
    }

}
