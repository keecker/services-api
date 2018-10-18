/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.projection.interfaces;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * DisplayMode represents preset setting of the projector
 */
public enum DisplayMode implements Parcelable {
    BRIGHTEST(0x00),
    VIVID(0x01),
    MOVIE(0x02),
    PICTURE(0x03),
    USER(0x04),;

    private final byte value;

    DisplayMode(int value) {
        this.value = (byte) value;
    }

    public byte getByte() {
        return value;
    }

    public static DisplayMode fromByte(byte b) {
        for (DisplayMode d : values()) {
            if (d.getByte() == b) {
                return d;
            }
        }
        return null;
    }

    public static final Parcelable.Creator<DisplayMode> CREATOR =
            new Parcelable.Creator<DisplayMode>() {
        public DisplayMode createFromParcel(Parcel in) {
            return DisplayMode.values()[in.readInt()];
        }

        public DisplayMode[] newArray(int size) {
            return new DisplayMode[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(ordinal());
    }
}
