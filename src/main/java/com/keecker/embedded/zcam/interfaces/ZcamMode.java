/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.embedded.zcam.interfaces;

import android.os.Parcel;
import android.os.Parcelable;

/** @hide
 * The zcam can be used in different modes, for instance short range, long range, several FPS, etc...
 * This class lists the possible modes.
 *
 * The name of the enums must match the constants defined in royale API.
 * Be careful when changing!!
 */
public enum ZcamMode implements Parcelable {

    /**
     * 5 FPS, long range
     */
    MODE_9_5FPS_2000,
    /**
     * 10 FPS
     */
    MODE_9_10FPS_1000,
    /**
     * 15 FPS, short range
     */
    MODE_9_15FPS_700,
    /**
     * 25 FPS
     */
    MODE_9_25FPS_450,
    /**
     * 35 FPS, shortest range
     */
    MODE_5_35FPS_600,
    /**
     * 45 FPS
     */
    MODE_5_45FPS_500;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name());
    }

    public static final Creator<ZcamMode> CREATOR = new Creator<ZcamMode>() {
        public ZcamMode createFromParcel(Parcel source) {
            return ZcamMode.valueOf(source.readString());
        }

        public ZcamMode[] newArray(int size) {
            return new ZcamMode[size];
        }
    };
}
