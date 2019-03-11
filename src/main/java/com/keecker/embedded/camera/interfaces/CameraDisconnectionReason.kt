package com.keecker.embedded.camera.interfaces

import android.os.Parcel
import android.os.Parcelable

enum class CameraDisconnectionReason : Parcelable {
    UNKNOWN,
    INTERNAL, // Camera service is closed
    CLOSED, // Camera is closed explicitly
    THEFT, // Another client request a camera
    DEAD; // All camera clients are dead

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CameraDisconnectionReason> {
        override fun createFromParcel(parcel: Parcel): CameraDisconnectionReason {
           return CameraDisconnectionReason.values()[parcel.readInt()]
        }

        override fun newArray(size: Int): Array<CameraDisconnectionReason?> {
            return arrayOfNulls(size)
        }
    }
}
