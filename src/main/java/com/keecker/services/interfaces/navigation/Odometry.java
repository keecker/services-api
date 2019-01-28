/*
 * Copyright (C) 2018 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Created by Cyril Lugan <cyril@keecker.com> on 2018-10-16
 */

package com.keecker.services.interfaces.navigation;

import android.os.Parcel;
import android.os.Parcelable;

import com.keecker.services.interfaces.utils.geometry.Twist2d;

/** @hide */
public class Odometry implements Parcelable {

    public final double timestamp;
    public final Twist2d velocity;

    public static final Parcelable.Creator<Odometry> CREATOR = new Creator<Odometry>() {
        public Odometry createFromParcel(Parcel in) {
            return new Odometry(in);
        }

        public Odometry[] newArray(int size) {
            return new Odometry[size];
        }
    };

    public Odometry(double timestamp, Twist2d velocity) {
        this.timestamp = timestamp;
        this.velocity = velocity;
    }

    public Odometry(Parcel in) {
        velocity = in.readParcelable(Twist2d.class.getClassLoader());
        timestamp = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(velocity, flags);
        out.writeDouble(timestamp);
    }
}
