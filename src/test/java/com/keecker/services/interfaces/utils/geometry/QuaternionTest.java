package com.keecker.services.interfaces.utils.geometry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Created by Thomas Gallagher <thomas@keecker.com> on 12/05/16.
 */
public class QuaternionTest {
    @Test
    public void testAxisAngleToQuaternion() {
        Quaternion quaternion;

        quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), 0);
        assertEquals(0, quaternion.x, 1e-9);
        assertEquals(0, quaternion.y, 1e-9);
        assertEquals(0, quaternion.z, 1e-9);
        assertEquals(1, quaternion.w, 1e-9);

        quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), Math.PI);
        assertEquals(0, quaternion.x, 1e-9);
        assertEquals(0, quaternion.y, 1e-9);
        assertEquals(1, quaternion.z, 1e-9);
        assertEquals(0, quaternion.w, 1e-9);

        quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), Math.PI / 2);
        assertEquals(0, quaternion.x, 1e-9);
        assertEquals(0, quaternion.y, 1e-9);
        assertEquals(0.7071067811865475, quaternion.z, 1e-9);
        assertEquals(0.7071067811865475, quaternion.w, 1e-9);

        quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), -Math.PI / 2);
        assertEquals(0, quaternion.x, 1e-9);
        assertEquals(0, quaternion.y, 1e-9);
        assertEquals(-0.7071067811865475, quaternion.z, 1e-9);
        assertEquals(0.7071067811865475, quaternion.w, 1e-9);

        quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), 0.75 * Math.PI);
        assertEquals(0, quaternion.x, 1e-9);
        assertEquals(0, quaternion.y, 1e-9);
        assertEquals(0.9238795325112867, quaternion.z, 1e-9);
        assertEquals(0.38268343236508984, quaternion.w, 1e-9);

        quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), -0.75 * Math.PI);
        assertEquals(0, quaternion.x, 1e-9);
        assertEquals(0, quaternion.y, 1e-9);
        assertEquals(-0.9238795325112867, quaternion.z, 1e-9);
        assertEquals(0.38268343236508984, quaternion.w, 1e-9);

        quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), 1.5 * Math.PI);
        assertEquals(0, quaternion.x, 1e-9);
        assertEquals(0, quaternion.y, 1e-9);
        assertEquals(0.7071067811865475, quaternion.z, 1e-9);
        assertEquals(-0.7071067811865475, quaternion.w, 1e-9);
    }

    @Test
    public void testInvert() {
        Quaternion inverse = Quaternion.fromAxisAngle(Vector3.zAxis(), Math.PI / 2).invert();
        assertEquals(0, inverse.x, 1e-9);
        assertEquals(0, inverse.y, 1e-9);
        assertEquals(-0.7071067811865475, inverse.z, 1e-9);
        assertEquals(0.7071067811865475, inverse.w, 1e-9);
    }

    @Test
    public void testMultiply() {
        Quaternion quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), Math.PI / 2);
        Quaternion inverse = quaternion.invert();
        Quaternion rotated = quaternion.multiply(inverse);
        assertEquals(1, rotated.w, 1e-9);
    }

    @Test
    public void testRotateVector() {
        Quaternion quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), Math.PI / 2);
        Vector3 vector = new Vector3(1, 0, 0);
        Vector3 rotated = quaternion.rotateVector(vector);
        assertEquals(0, rotated.x, 1e-9);
        assertEquals(1, rotated.y, 1e-9);
        assertEquals(0, rotated.z, 1e-9);
    }

    @Test
    public void testGetTheta() {
        // No singular stuff
        assertEquals(0, Quaternion.fromTheta(0).getTheta(), 1e-9);
        assertEquals(-Math.PI, Quaternion.fromTheta(-Math.PI).getTheta(), 1e-9);
        assertEquals(Math.PI, Quaternion.fromTheta(Math.PI).getTheta(), 1e-9);
        assertEquals(Math.PI/2., Quaternion.fromTheta(Math.PI/2.).getTheta(), 1e-9);

        // Arbitrary usage
        for (double theta = -Math.PI; theta<=Math.PI; theta+=Math.PI/10.) {
            assertEquals(theta, Quaternion.fromTheta(theta).getTheta(), 1e-9);
        }
        // Normalize angle correctly
        assertEquals(0, Quaternion.fromTheta(Math.PI*2.).getTheta(), 1e-9);
        assertEquals(-Math.PI + 0.1, Quaternion.fromTheta(Math.PI + 0.1).getTheta(), 1e-9);
    }

}
