package com.keecker.hardware.camera.interfaces

import android.os.Parcel
import android.os.Parcelable


data class DewarpParameters(
        val thetaDeg: Float = 90F,
        val apertureDeg: Float = 70F,
        val Rmin: Float = 0.4F,
        val Rmax: Float = 0.98F,
        val phi: Float = 0F,
        val scale: Float = 1F): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readFloat())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(thetaDeg)
        parcel.writeFloat(apertureDeg)
        parcel.writeFloat(Rmin)
        parcel.writeFloat(Rmax)
        parcel.writeFloat(phi)
        parcel.writeFloat(scale)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DewarpParameters> {
        override fun createFromParcel(parcel: Parcel): DewarpParameters {
            return DewarpParameters(parcel)
        }

        override fun newArray(size: Int): Array<DewarpParameters?> {
            return arrayOfNulls(size)
        }
    }
}
