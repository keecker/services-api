/*
 *  Copyright (C) 2018 KEECKER SAS (www.keecker.com)
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *
 *  Created by Emeric Colombe
 *
 */
package com.keecker.services.interfaces.navigation

import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import com.keecker.services.interfaces.embedded.camera.Frame

data class DetectionResult (
        var id : String,
        var title : String,
        var confidence : Float,
        var location : RectF,
        var frame: Frame? = null) : Parcelable {

    constructor(parcel: Parcel)
            : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readFloat(),
            parcel.readParcelable(RectF::class.java.classLoader),
            parcel.readParcelable(Frame::class.java.classLoader))

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.id)
        dest.writeString(this.title)
        dest.writeFloat(this.confidence)
        dest.writeParcelable(this.location, flags)
        dest.writeParcelable(this.frame, flags)
    }


    companion object CREATOR : Parcelable.Creator<DetectionResult> {
        override fun createFromParcel(parcel: Parcel): DetectionResult {
            return DetectionResult(parcel)
        }

        override fun newArray(size: Int): Array<DetectionResult?> {
            return arrayOfNulls(size)
        }
    }

}
