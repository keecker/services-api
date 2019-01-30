/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Created by Florent Remis on 05/09/17.
 *
 */
package com.keecker.services.interfaces.navigation;

import android.os.Parcel;
import android.os.Parcelable;

/** @hide */
public class SafeModeState implements Parcelable {

    public boolean state;

    public SafeModeState(boolean state) {
        this.state = state;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(state ? (byte) 1 : (byte) 0);
    }

    protected SafeModeState(Parcel in) {
        state = in.readByte() == 1;
    }

    public static final Parcelable.Creator<SafeModeState> CREATOR = new Parcelable.Creator<SafeModeState>() {
        @Override
        public SafeModeState createFromParcel(Parcel source) {
            return new SafeModeState(source);
        }

        @Override
        public SafeModeState[] newArray(int size) {
            return new SafeModeState[size];
        }
    };

    @Override
    public String toString() {
        return "SafeModeState{" +
                "state=" + state +
                '}';
    }
}

