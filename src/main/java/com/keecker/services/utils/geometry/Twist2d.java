/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Created by Thomas Gallagher on 08/02/16.
 */
package com.keecker.services.utils.geometry;

import android.os.Parcel;
import android.os.Parcelable;

/** @hide */
public class Twist2d implements Parcelable {

    protected double linear = 0.0;
    protected double angular = 0.0;

    public static final Parcelable.Creator<Twist2d> CREATOR = new Parcelable.Creator<Twist2d>() {
        public Twist2d createFromParcel(Parcel in) {
            return new Twist2d(in);
        }

        public Twist2d[] newArray(int size) {
            return new Twist2d[size];
        }
    };

    public Twist2d() {}

    public Twist2d(double linear, double angular) {
        this.linear = linear;
        this.angular = angular;
    }

    public Twist2d(Parcel in) {
        linear = in.readDouble();
        angular = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(linear);
        out.writeDouble(angular);
    }

    public double getLinear() {
        return linear;
    }

    public double getAngular() {
        return angular;
    }

    public Twist2d setLinear(double linear) {
        this.linear = linear;
        return this;
    }

    public Twist2d setAngular(double angular) {
        this.angular = angular;
        return this;
    }

    @Override
    public String toString() {
        return "Twist2d{" +
                "linear=" + linear +
                ", angular=" + angular +
                '}';
    }
}
