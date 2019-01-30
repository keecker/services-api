/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.interfaces.utils.geometry;

import android.os.Parcel;
import android.os.Parcelable;

/** @hide */
public class Vector3 implements Parcelable {

    private static final Vector3 X_AXIS = new Vector3(1, 0, 0);
    private static final Vector3 Y_AXIS = new Vector3(0, 1, 0);
    private static final Vector3 Z_AXIS = new Vector3(0, 0, 1);

    public final double x;
    public final double y;
    public final double z;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.x);
        dest.writeDouble(this.y);
        dest.writeDouble(this.z);
    }

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    protected Vector3(Parcel in) {
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.z = in.readDouble();
    }

    public static Vector3 xAxis() {
        return X_AXIS;
    }

    public static Vector3 yAxis() {
        return Y_AXIS;
    }

    public static Vector3 zAxis() {
        return Z_AXIS;
    }

    public Vector3 add(Vector3 other) {
        return new Vector3(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3 subtract(Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }

    public Vector3 normalize() {
        return new Vector3(x / getMagnitude(), y / getMagnitude(), z / getMagnitude());
    }

    public Vector3 invert() {
        return new Vector3(-x, -y, -z);
    }

    public double dotProduct(Vector3 other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public double getMagnitudeSquared() {
        return x * x + y * y + z * z;
    }

    public double getMagnitude() {
        return Math.sqrt(getMagnitudeSquared());
    }

    public boolean almostEquals(Vector3 other, double epsilon) {
        double[] epsilons = new double[] {x - other.x, y - other.y, z - other.z};
        for (double e : epsilons) {
            if (Math.abs(e) > epsilon) {
                return false;
            }
        }
        return true;
    }

    public static final Parcelable.Creator<Vector3> CREATOR = new Parcelable.Creator<Vector3>() {
        @Override
        public Vector3 createFromParcel(Parcel source) {
            return new Vector3(source);
        }

        @Override
        public Vector3[] newArray(int size) {
            return new Vector3[size];
        }
    };

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Vector3{");
        sb.append("x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", z=").append(z);
        sb.append('}');
        return sb.toString();
    }
}
