package com.keecker.embedded.stm.interfaces;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Thomas Gallagher <thomas@keecker.com> on 8/24/17.
 */

public class FanSpeed implements Parcelable {

    public static final int PROJECTOR_FAN = 0;
    public static final int REAR_FAN = 1;

    public final int fanId;
    public final int speed;

    public FanSpeed(int fanId, int speed) {
        this.fanId = fanId;
        this.speed = speed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.fanId);
        dest.writeInt(this.speed);
    }

    protected FanSpeed(Parcel in) {
        this.fanId = in.readInt();
        this.speed = in.readInt();
    }

    public static final Parcelable.Creator<FanSpeed> CREATOR = new Parcelable.Creator<FanSpeed>() {
        @Override
        public FanSpeed createFromParcel(Parcel source) {
            return new FanSpeed(source);
        }

        @Override
        public FanSpeed[] newArray(int size) {
            return new FanSpeed[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FanSpeed that = (FanSpeed) o;

        if (fanId != that.fanId) return false;
        return speed == that.speed;

    }

    @Override
    public int hashCode() {
        int result = fanId;
        result = 31 * result + speed;
        return result;
    }

    @Override
    public String toString() {
        return "FanSpeed{" +
                "fan=" + (fanId == PROJECTOR_FAN ? "PROJ" : fanId == REAR_FAN ? "REAR" : "UNKNOWN???") +
                ", speed=" + speed +
                '}';
    }
}
