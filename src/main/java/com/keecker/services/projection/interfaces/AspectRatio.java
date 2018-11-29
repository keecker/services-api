/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.projection.interfaces;

import android.os.Parcel;
import android.os.Parcelable;

public enum AspectRatio implements Parcelable {
    R4_3(0x01),
    R16_9(0x02),
    R16_10(0x03),
    AUTO(0x00),
    ;

    private final byte value;

    AspectRatio(int value) {
        this.value = (byte) value;
    }

    public byte getByte() {
        return this.value;
    }

    public static AspectRatio fromByte(byte b) {
        for (AspectRatio a : values()) {
            if (a.getByte() == b) {
                return a;
            }
        }
        return null;
    }

    public static final Parcelable.Creator<AspectRatio> CREATOR =
            new Parcelable.Creator<AspectRatio>() {
                public AspectRatio createFromParcel(Parcel in) {
                    return AspectRatio.values()[in.readInt()];
                }

                public AspectRatio[] newArray(int size) {
                    return new AspectRatio[size];
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
