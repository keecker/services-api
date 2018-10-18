/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Created by Thomas Gallagher <thomas@keecker.com> on 19/04/16.
 */
package com.keecker.services.utils.geometry;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/** @hide */
public class Point2D implements Parcelable, Serializable {

    public final double x;
    public final double y;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.x);
        dest.writeDouble(this.y);
    }

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    protected Point2D(Parcel in) {
        this.x = in.readDouble();
        this.y = in.readDouble();
    }

    public static final Parcelable.Creator<Point2D> CREATOR = new Parcelable.Creator<Point2D>() {
        @Override
        public Point2D createFromParcel(Parcel source) {
            return new Point2D(source);
        }

        @Override
        public Point2D[] newArray(int size) {
            return new Point2D[size];
        }
    };
}
