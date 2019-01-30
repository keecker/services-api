/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Created by Cyril Lugan on 19/02/16.
 *
 * Coordinates transformation class, mostly used in native code for now.
 * It it represented by a translation (x,y,z vector) and a rotation (x,y,z,w quaternion).
 * See frame_transforms unit tests cpp code to understand the conventions
 *
 * Some methods taken from https://github.com/rosjava/rosjava_core/blob/indigo/rosjava_geometry/src/main/java/org/ros/rosjava_geometry/Transform.java
 */
package com.keecker.services.interfaces.utils.geometry;

import android.os.Parcel;
import android.os.Parcelable;

import com.keecker.services.interfaces.common.utils.map.Pose;

/** @hide */
public class Transform implements Parcelable {

    public final Vector3 translation;
    public final Quaternion rotation;

    public Transform(Vector3 t, Quaternion r) {
        this.translation = t;
        this.rotation = r;
    }

    public Transform(double tx, double ty, double tz, double rx, double ry, double rz, double rw) {
        this(new Vector3(tx, ty, tz), new Quaternion(rx, ry, rz, rw));
    }

    public Transform(double x, double y, double theta) {
        this(new Vector3(x, y, 0.0), Quaternion.fromTheta(theta));
    }

    public static Transform from2DPose(double x, double y, double theta) {
        return new Transform(new Vector3(x, y, 0.0), Quaternion.fromTheta(theta));
    }

    public static Transform from2DPose(Pose pose) {
        return from2DPose(pose.x, pose.y, pose.theta);
    }

    public static Transform identity() {
        return new Transform(new Vector3(0.0, 0.0, 0.0), Quaternion.identity());
    }

    public static Transform withTranslation(double x, double y, double z) {
        return new Transform(new Vector3(x, y, z), Quaternion.identity());
    }

    public Transform multiply(Transform other) {
        return new Transform(
                rotation.rotateVector(other.translation).add(translation),
                rotation.multiply(other.rotation));
    }

    public Transform invert() {
        Quaternion inverseRotationAndScale = rotation.invert();
        return new Transform(inverseRotationAndScale.rotateVector(translation.invert()),
                inverseRotationAndScale);
    }

    public Vector3 apply(Vector3 vector) {
        return rotation.rotateVector(vector).add(translation);
    }

    public Quaternion apply(Quaternion quaternion) {
        return rotation.multiply(quaternion);
    }

    public boolean almostEquals(Transform other, double epsilon) {
        return translation.almostEquals(other.translation, epsilon)
                && rotation.almostEquals(other.rotation, epsilon);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(translation, flags);
        out.writeParcelable(rotation, flags);
    }

    public static final Parcelable.Creator<Transform> CREATOR
            = new Parcelable.Creator<Transform>() {
        public Transform createFromParcel(Parcel in) {
            return new Transform(in);
        }

        public Transform[] newArray(int size) {
            return new Transform[size];
        }
    };

    public Transform(Parcel in) {
        translation = in.readParcelable(Vector3.class.getClassLoader());
        rotation = in.readParcelable(Quaternion.class.getClassLoader());
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Transform{");
        sb.append("translation=").append(translation);
        sb.append(", rotation=").append(rotation);
        sb.append('}');
        return sb.toString();
    }
}
