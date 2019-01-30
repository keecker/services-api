/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.interfaces.navigation;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * State when Keecker is going back to its charging station
 */
public enum GoToChargingStationStatus implements Parcelable {

    STARTED, DOCKING, DOCKED, FAILED;

    public static final Creator<GoToChargingStationStatus> CREATOR = new Creator<GoToChargingStationStatus>() {
        public GoToChargingStationStatus createFromParcel(Parcel in) {
            return GoToChargingStationStatus.valueOf(in.readString());
        }

        public GoToChargingStationStatus[] newArray(int size) {
            return new GoToChargingStationStatus[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name());
    }
}
