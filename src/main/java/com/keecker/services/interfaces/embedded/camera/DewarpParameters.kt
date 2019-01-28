package com.keecker.services.interfaces.embedded.camera

import android.graphics.Point
import android.os.Parcel
import android.os.Parcelable

data class DewarpInputParameters(
        var size : Point = Point(3264, 2448)
) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readParcelable<Point>(Point::class.java.classLoader))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(size, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DewarpInputParameters> {
        override fun createFromParcel(parcel: Parcel): DewarpInputParameters {
            return DewarpInputParameters(parcel)
        }

        override fun newArray(size: Int): Array<DewarpInputParameters?> {
            return arrayOfNulls(size)
        }
    }
}

data class DewarpProcessParameters(
        var thetaDeg: Float = 90F,
        var apertureDeg: Float = 70F,
        var Rmin: Float = 0.4F,
        var Rmax: Float = 0.98F,
        var phi: Float = 0F,
        var scale: Float = 1F): Parcelable {
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

    companion object CREATOR : Parcelable.Creator<DewarpProcessParameters> {
        override fun createFromParcel(parcel: Parcel): DewarpProcessParameters {
            return DewarpProcessParameters(parcel)
        }

        override fun newArray(size: Int): Array<DewarpProcessParameters?> {
            return arrayOfNulls(size)
        }
    }

    enum class DewarpAngle(val thetaDeg: Float) {
        FRONT(270F),
        BACK(90F),
        LEFT(0F),
        RIGHT(180F)
    }
}

data class DewarpParameters(
        var inputParameters: DewarpInputParameters = DewarpInputParameters(),
        var processParameters: DewarpProcessParameters = DewarpProcessParameters()
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readParcelable(DewarpInputParameters::class.java.classLoader),
            parcel.readParcelable(DewarpProcessParameters::class.java.classLoader))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(inputParameters, flags)
        parcel.writeParcelable(processParameters, flags)
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
