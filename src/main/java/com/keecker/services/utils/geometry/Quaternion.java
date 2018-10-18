/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Created by Thomas Gallagher <thomas@keecker.com> on 19/04/16.
 *
 * The implementation is taken from https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Quaternion.java
 *
 * Some methods are taken from https://github.com/rosjava/rosjava_core/blob/indigo/rosjava_geometry/src/main/java/org/ros/rosjava_geometry/Quaternion.java
 */
package com.keecker.services.utils.geometry;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/** @hide */
public class Quaternion implements Parcelable {

    private static final String TAG = Quaternion.class.getSimpleName();

    public final double x;
    public final double y;
    public final double z;
    public final double w;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.x);
        dest.writeDouble(this.y);
        dest.writeDouble(this.z);
        dest.writeDouble(this.w);
    }

    public Quaternion(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public static Quaternion identity() {
        return new Quaternion(0.0, 0.0, 0.0, 1.0);
    }

    /** Get the angle around z axis, as projected onto (x, y, theta) space. Don't use for 3D rotations.
     * @return the rotation around the z axis in radians (between -PI and +PI) */
    public double getTheta() {
        return getAxis().z * getAngle();
    }

    public static Quaternion fromTheta(double theta) {
        return fromYPR(0.0, 0.0, theta);
    }

    /** Sets the quaternion to the given euler angles in radians.
     * @param yaw the rotation around the y axis in radians
     * @param pitch the rotation around the x axis in radians
     * @param roll the rotation around the z axis in radians
     * @return this quaternion */
    public static Quaternion fromYPR(double yaw, double pitch, double roll) {
        final double hr = roll * 0.5f;
        final double shr = Math.sin(hr);
        final double chr = Math.cos(hr);
        final double hp = pitch * 0.5f;
        final double shp = Math.sin(hp);
        final double chp = Math.cos(hp);
        final double hy = yaw * 0.5f;
        final double shy = Math.sin(hy);
        final double chy = Math.cos(hy);
        final double chy_shp = chy * shp;
        final double shy_chp = shy * chp;
        final double chy_chp = chy * chp;
        final double shy_shp = shy * shp;

        double x = (chy_shp * chr) + (shy_chp * shr); // cos(yaw/2) * sin(pitch/2) * cos(roll/2) + sin(yaw/2) * cos(pitch/2) * sin(roll/2)
        double y = (shy_chp * chr) - (chy_shp * shr); // sin(yaw/2) * cos(pitch/2) * cos(roll/2) - cos(yaw/2) * sin(pitch/2) * sin(roll/2)
        double z = (chy_chp * shr) - (shy_shp * chr); // cos(yaw/2) * cos(pitch/2) * sin(roll/2) - sin(yaw/2) * sin(pitch/2) * cos(roll/2)
        double w = (chy_chp * chr) + (shy_shp * shr); // cos(yaw/2) * cos(pitch/2) * cos(roll/2) + sin(yaw/2) * sin(pitch/2) * sin(roll/2)
        return new Quaternion(x, y, z, w);
    }

    public static Quaternion fromAxisAngle(Vector3 axis, double angle) {
        Vector3 normalized = axis.normalize();
        double sin = Math.sin(angle / 2.0d);
        double cos = Math.cos(angle / 2.0d);
        return new Quaternion(normalized.x * sin, normalized.y * sin,
                normalized.z * sin, cos);
    }

    public Quaternion multiply(Quaternion other) {
        return new Quaternion(w * other.x + x * other.w + y * other.z - z * other.y, w * other.y + y
                * other.w + z * other.x - x * other.z, w * other.z + z * other.w + x * other.y - y
                * other.x, w * other.w - x * other.x - y * other.y - z * other.z);
    }

    public Vector3 rotateVector(Vector3 vector) {
        Quaternion vectorQuaternion = new Quaternion(vector.x, vector.y, vector.z, 0);
        Quaternion rotatedQuaternion = multiply(vectorQuaternion.multiply(conjugate()));
        return new Vector3(rotatedQuaternion.x, rotatedQuaternion.y, rotatedQuaternion.z);
    }

    public Quaternion conjugate() {
        return new Quaternion(-x, -y, -z, w);
    }

    public Quaternion invert() {
        double mm = getMagnitudeSquared();
        if (mm == 0) {
            Log.e(TAG, "Quaternion magnitude is zero, can't invert!");
            return null;
        }
        return conjugate().scale(1 / mm);
    }

    public double getMagnitudeSquared() {
        return x * x + y * y + z * z + w * w;
    }

    /** Get the angle in radians of the rotation this quaternion represents.
     * @return the angle in radians between -PI and PI*/
    public double getAngle() {
        double angle = 2.0 * Math.acos(this.w);
        if (angle > Math.PI) {
            angle -= 2.0*Math.PI;
        }
        return angle;
    }

    /** Get the axis part of the axis angle represented by this quaternion.
     * @return a Vector3 normalized representing the axis.
     */
    public Vector3 getAxis() {
        double s = Math.sqrt(1 - this.w * this.w); // assuming quaternion normalised then w is less or equal than 1, so term always positive.
        if (s < 1e-6) { // test to avoid divide by zero, s is always positive due to sqrt
            // if s close to zero then direction of axis not important, because angle is zero
            return new Vector3(0., 0., 1.);
        }
        return new Vector3(this.x / s, this.y / s, this.z / s);
    }

    public Quaternion scale(double factor) {
        return new Quaternion(x * factor, y * factor, z * factor, w * factor);
    }

    public boolean almostEquals(Quaternion other, double epsilon) {
        double[] epsilons = new double[] { x - other.x, y - other.y, z - other.z, w - other.w};
        for (double e : epsilons) {
            if (Math.abs(e) > epsilon) {
                return false;
            }
        }
        return true;
    }

    protected Quaternion(Parcel in) {
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.z = in.readDouble();
        this.w = in.readDouble();
    }

    public static final Parcelable.Creator<Quaternion> CREATOR = new Parcelable.Creator<Quaternion>() {
        @Override
        public Quaternion createFromParcel(Parcel source) {
            return new Quaternion(source);
        }

        @Override
        public Quaternion[] newArray(int size) {
            return new Quaternion[size];
        }
    };

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Quaternion{");
        sb.append("x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", z=").append(z);
        sb.append(", w=").append(w);
        sb.append('}');
        return sb.toString();
    }
}
