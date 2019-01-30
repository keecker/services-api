/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.interfaces.projection;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * DisplayPosition represents all possible flip for the projected image
 */
public enum DisplayPosition implements Parcelable {
    FRONT_TABLE(0x00),
    FRONT_CEILING(0x01),
    REAR_TABLE(0x02),
    REAR_CEILING(0x03),
    ;

    private final byte value;

    DisplayPosition(int value) {
        this.value = (byte) value;
    }

    public byte getByte() {
        return this.value;
    }

    public static DisplayPosition fromByte(byte b) {
        for (DisplayPosition d : values()) {
            if (d.getByte() == b) {
                return d;
            }
        }
        return null;
    }

    public static final Parcelable.Creator<DisplayPosition> CREATOR =
            new Parcelable.Creator<DisplayPosition>() {
                public DisplayPosition createFromParcel(Parcel in) {
                    return DisplayPosition.values()[in.readInt()];
                }

                public DisplayPosition[] newArray(int size) {
                    return new DisplayPosition[size];
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
