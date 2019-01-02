/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Created by Thomas Gallagher on 26/04/16.
 */
package com.keecker.services.utils.geometry;

import android.os.Parcel;

/** @hide */
public class Point2DStamped extends Point2D {

    public final double stamp;

    public Point2DStamped(double stamp, double x, double y) {
        super(x, y);
        this.stamp = stamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.stamp);
    }

    protected Point2DStamped(Parcel in) {
        super(in);
        this.stamp = in.readDouble();
    }

    public static final Creator<Point2DStamped> CREATOR = new Creator<Point2DStamped>() {
        @Override
        public Point2DStamped createFromParcel(Parcel source) {
            return new Point2DStamped(source);
        }

        @Override
        public Point2DStamped[] newArray(int size) {
            return new Point2DStamped[size];
        }
    };
}
