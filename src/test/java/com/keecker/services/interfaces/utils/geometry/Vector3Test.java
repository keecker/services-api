package com.keecker.services.interfaces.utils.geometry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Created by Thomas Gallagher <thomas@keecker.com> on 12/05/16.
 *
 * Taken from https://github.com/rosjava/rosjava_core/blob/indigo/rosjava_geometry/src/test/java/org/ros/rosjava_geometry/Vector3Test.java
 */
public class Vector3Test {
    @Test
    public void testAdd() {
        Vector3 vector1 = new Vector3(1, 2, 3);
        Vector3 vector2 = new Vector3(2, 3, 4);
        Vector3 result = vector1.add(vector2);
        assertEquals(result.x, 3, 1e-9);
        assertEquals(result.y, 5, 1e-9);
        assertEquals(result.z, 7, 1e-9);
    }

    @Test
    public void testSubtract() {
        Vector3 vector1 = new Vector3(1, 2, 3);
        Vector3 vector2 = new Vector3(2, 3, 4);
        Vector3 result = vector1.subtract(vector2);
        assertEquals(result.x, -1, 1e-9);
        assertEquals(result.y, -1, 1e-9);
        assertEquals(result.z, -1, 1e-9);
    }

    @Test
    public void testInvert() {
        Vector3 result = new Vector3(1, 1, 1).invert();
        assertEquals(result.x, -1, 1e-9);
        assertEquals(result.y, -1, 1e-9);
        assertEquals(result.z, -1, 1e-9);
    }

    @Test
    public void testDotProduct() {
        Vector3 vector1 = new Vector3(1, 2, 3);
        Vector3 vector2 = new Vector3(2, 3, 4);
        assertEquals(20.0, vector1.dotProduct(vector2), 1e-9);
    }

    @Test
    public void testLength() {
        assertEquals(2, new Vector3(2, 0, 0).getMagnitude(), 1e-9);
        assertEquals(2, new Vector3(0, 2, 0).getMagnitude(), 1e-9);
        assertEquals(2, new Vector3(0, 0, 2).getMagnitude(), 1e-9);
        assertEquals(Math.sqrt(3), new Vector3(1, 1, 1).getMagnitude(), 1e-9);
    }
}
