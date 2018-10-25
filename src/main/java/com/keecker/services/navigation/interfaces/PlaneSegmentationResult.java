package com.keecker.services.navigation.interfaces;

import android.os.Parcel;
import android.os.Parcelable;

/** @hide */
public class PlaneSegmentationResult implements Parcelable {
    public final double stamp;
    public final double angle;
    public final double distance;
    public final double percentInliers;
    // Plane model (ax+by+cz+d=0)
    public final double a;
    public final double b;
    public final double c;
    public final double d;

    public PlaneSegmentationResult(double stamp, double angle, double distance, double percentInliers,
                                   double a, double b, double c, double d) {
        this.stamp = stamp;
        this.angle = angle;
        this.distance = distance;
        this.percentInliers = percentInliers;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public PlaneSegmentationResult(Parcel in) {
        this.stamp = in.readDouble();
        this.angle = in.readDouble();
        this.distance = in.readDouble();
        this.percentInliers = in.readDouble();
        this.a = in.readDouble();
        this.b = in.readDouble();
        this.c = in.readDouble();
        this.d = in.readDouble();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(this.stamp);
        parcel.writeDouble(this.angle);
        parcel.writeDouble(this.distance);
        parcel.writeDouble(this.percentInliers);
        parcel.writeDouble(this.a);
        parcel.writeDouble(this.b);
        parcel.writeDouble(this.c);
        parcel.writeDouble(this.d);
    }

    public static final Creator<PlaneSegmentationResult> CREATOR
            = new Creator<PlaneSegmentationResult>() {
        public PlaneSegmentationResult createFromParcel(Parcel in) {
            return new PlaneSegmentationResult(in);
        }

        public PlaneSegmentationResult[] newArray(int size) {
            return new PlaneSegmentationResult[size];
        }
    };
}
