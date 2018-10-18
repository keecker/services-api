/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.utils.sharedmemory;

import android.os.Parcel;

public class SharedFruit extends SharedMemoryBuffer {

    private String name;

    public SharedFruit(int size) {
        super(size);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.name);
    }

    protected SharedFruit(Parcel in) {
        super(in);
        this.name = in.readString();
    }

    public static final Creator<SharedFruit> CREATOR = new Creator<SharedFruit>() {
        @Override
        public SharedFruit createFromParcel(Parcel source) {
            return new SharedFruit(source);
        }

        @Override
        public SharedFruit[] newArray(int size) {
            return new SharedFruit[size];
        }
    };
}
