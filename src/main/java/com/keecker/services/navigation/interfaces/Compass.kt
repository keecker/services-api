/*
 * Copyright (C) 2018 KEECKER SAS (www.keecker.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Created by Cyril Lugan on 2018-11-26.
 */

package com.keecker.services.navigation.interfaces

import android.os.Parcel
import android.os.Parcelable

data class Compass(val timestamp: Double,
    val northDirection: Double,
    val northConfidence: Double,
    val strength: Double,
    val calibrationX: Double,
    val calibrationY: Double): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(timestamp)
        parcel.writeDouble(northDirection)
        parcel.writeDouble(northConfidence)
        parcel.writeDouble(strength)
        parcel.writeDouble(calibrationX)
        parcel.writeDouble(calibrationY)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Compass> {
        override fun createFromParcel(parcel: Parcel): Compass {
            return Compass(parcel)
        }

        override fun newArray(size: Int): Array<Compass?> {
            return arrayOfNulls(size)
        }
    }
}