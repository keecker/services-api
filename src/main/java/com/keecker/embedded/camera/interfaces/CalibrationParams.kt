package com.keecker.embedded.camera.interfaces

import android.os.Parcel
import android.os.Parcelable

data class CalibrationParams(
        val width : Double,
        val height : Double,
        val centerX : Double,
        val centerY : Double,
        val radius : Double
): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble())

    constructor(params: List<Double>) : this(
            params[0],
            params[1],
            params[2],
            params[3],
            params[4])

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(width)
        parcel.writeDouble(height)
        parcel.writeDouble(centerX)
        parcel.writeDouble(centerY)
        parcel.writeDouble(radius)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CalibrationParams> {
        override fun createFromParcel(parcel: Parcel): CalibrationParams {
            return CalibrationParams(parcel)
        }

        override fun newArray(size: Int): Array<CalibrationParams?> {
            return arrayOfNulls(size)
        }
    }
}
