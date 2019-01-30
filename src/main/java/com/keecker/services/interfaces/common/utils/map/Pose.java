/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.interfaces.common.utils.map;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Describe a pose on current map represented by:
 * - X, Y, Theta: coordinate in the map
 * - ErrorX, ErrorY, ErrorTheta: the estimated error
 */
public class Pose implements Parcelable {

    public final float x;
    public final float y;
    public final float theta;
    public double stamp = 0.0;
    public float errorX = 0f;
    public float errorY = 0f;
    public float errorTheta = 0f;
    public boolean isLost = false;

    public Pose(float x, float y, float theta) {
        this.x = x;
        this.y = y;
        this.theta = theta;
    }

    public Pose(double stamp, float x, float y, float theta, float errorX, float errorY,
                float errorTheta) {
        this.errorTheta = errorTheta;
        this.errorY = errorY;
        this.errorX = errorX;
        this.stamp = stamp;
        this.theta = theta;
        this.y = y;
        this.x = x;
    }

    public Pose(double stamp, float x, float y, float theta, float errorX, float errorY,
                float errorTheta, boolean isLost) {
        this.errorTheta = errorTheta;
        this.errorY = errorY;
        this.errorX = errorX;
        this.stamp = stamp;
        this.theta = theta;
        this.y = y;
        this.x = x;
        this.isLost = isLost;
    }

    public Pose(Parcel in) {
        stamp = in.readDouble();
        x = in.readFloat();
        y = in.readFloat();
        theta = in.readFloat();
        errorX = in.readFloat();
        errorY = in.readFloat();
        errorTheta = in.readFloat();
        isLost = in.readByte() != 0;
    }

    public Pose(Pose pose) {
        this(pose.stamp, pose.x, pose.y, pose.theta, pose.errorX, pose.errorY, pose.errorTheta,
                pose.isLost);
    }

    public Pose(double stamp, double x, double y, double theta, double errorX, double errorY,
                double errorTheta) {
        this(stamp, (float) x, (float) y, (float) theta, (float) errorX, (float) errorY,
                (float) errorTheta);
    }

    public double distanceTo(Pose other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

    public Pose interpolateTo(Pose other) {
        double stamp = Math.min(this.stamp, other.stamp) + Math.abs(this.stamp - other.stamp) / 2.0;
        double x = (this.x + other.x) / 2.0;
        double y = (this.y + other.y) / 2.0;
        // Average theta
        double theta = Math.atan2(0.5 * (Math.sin(this.theta) + Math.sin(other.theta)),
                0.5 * (Math.cos(this.theta) + Math.cos(other.theta)));
        // Be conservative on standard deviations
        double errorX = Math.max(this.errorX, other.errorX);
        double errorY = Math.max(this.errorY, other.errorY);
        double errorTheta = Math.max(this.errorTheta, other.errorTheta);
        return new Pose(stamp, x, y, theta, errorX, errorY, errorTheta);
    }

    public boolean errorIsOver(float translationError, float rotationError) {
        if (errorX >= translationError) {
            return true;
        }
        if (errorY >= translationError) {
            return true;
        }
        if (errorTheta >= rotationError) {
            return true;
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(stamp);
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(theta);
        out.writeFloat(errorX);
        out.writeFloat(errorY);
        out.writeFloat(errorTheta);
        out.writeByte((byte) (isLost ? 1 : 0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pose pose = (Pose) o;

        if (Float.compare(pose.x, x) != 0) return false;
        if (Float.compare(pose.y, y) != 0) return false;
        if (Float.compare(pose.theta, theta) != 0) return false;
        if (Double.compare(pose.stamp, stamp) != 0) return false;
        if (Float.compare(pose.errorX, errorX) != 0) return false;
        if (Float.compare(pose.errorY, errorY) != 0) return false;
        return Float.compare(pose.errorTheta, errorTheta) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (theta != +0.0f ? Float.floatToIntBits(theta) : 0);
        temp = Double.doubleToLongBits(stamp);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (errorX != +0.0f ? Float.floatToIntBits(errorX) : 0);
        result = 31 * result + (errorY != +0.0f ? Float.floatToIntBits(errorY) : 0);
        result = 31 * result + (errorTheta != +0.0f ? Float.floatToIntBits(errorTheta) : 0);
        return result;
    }

    public static final Creator<Pose> CREATOR
            = new Creator<Pose>() {
        public Pose createFromParcel(Parcel in) {
            return new Pose(in);
        }

        public Pose[] newArray(int size) {
            return new Pose[size];
        }
    };

    @Override
    public String toString() {
        return "Pose{" +
                "stamp=" + stamp +
                ", x=" + x +
                ", y=" + y +
                ", theta=" + theta +
                ", errorX=" + errorX +
                ", errorY=" + errorY +
                ", errorTheta=" + errorTheta +
                ", isLost=" + isLost +
                '}';
    }
}
