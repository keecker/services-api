/*
 * Copyright (C) 2018 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.keecker.services.utils.deeplearning

import android.os.Parcel
import android.os.Parcelable

/**
 * Type of objects that can be detected
 */
enum class ModelID constructor (val type: String) : Parcelable {

    CHARGING_STATION("chargingStation"), COCO_SSD("cocoSsd");

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.name)
    }

    companion object CREATOR : Parcelable.Creator<ModelID> {
        override fun createFromParcel(parcel: Parcel): ModelID {
            return ModelID.valueOf(parcel.readString())
        }

        override fun newArray(size: Int): Array<ModelID?> {
            return arrayOfNulls(size)
        }
    }
}
