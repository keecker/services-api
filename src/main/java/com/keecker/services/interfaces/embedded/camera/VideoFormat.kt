/*
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.interfaces.embedded.camera;

import android.os.Parcel
import android.os.Parcelable

enum class CameraType {
    FRONT,
    TOP,
    DEWARPED;

    companion object {
        @JvmStatic
        fun getCameraType(type: Int): CameraType {
            return when(type) {
                0 -> CameraType.FRONT
                1 -> CameraType.TOP
                else -> CameraType.DEWARPED
            }
        }
    }

    fun getIndex(): Int {
        return when(this) {
            FRONT -> 0
            TOP -> 1
            else -> 2
        }
    }

    fun getDriverIndex(): Int {
        return when(this) {
            FRONT -> 0
            else -> 1
        }
    }
}

data class CamSize(val width: Int, val height: Int)

enum class ImageFormat {
    YUV420SP
}

data class Frame (val videoFormat: VideoFormat, val timestamp: Double):  Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readParcelable(VideoFormat::class.java.classLoader),
            parcel.readDouble())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(videoFormat, flags)
        parcel.writeDouble(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Frame> {
        override fun createFromParcel(parcel: Parcel): Frame {
            return Frame(parcel)
        }

        override fun newArray(size: Int): Array<Frame?> {
            return arrayOfNulls(size)
        }
    }
}

data class VideoFormat @JvmOverloads constructor(
        val cameraType: CameraType,
        val width: Int, val height: Int,
        val format: ImageFormat = ImageFormat.YUV420SP,
        val dewarpParameters: DewarpParameters? = null) : Parcelable {

    val camera = cameraType.getIndex()

    private constructor(type: Int, width: Int, height: Int, format: Int,
                        parameters: DewarpParameters? = null) : this(
            CameraType.getCameraType(type),
            width, height,
            ImageFormat.values()[format],
            parameters
            )

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readParcelable(DewarpParameters::class.java.classLoader))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(cameraType.getIndex())
        parcel.writeInt(width)
        parcel.writeInt(height)
        parcel.writeInt(format.ordinal)
        parcel.writeParcelable(dewarpParameters, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getPixelSize(): Int {
        //FIXME(Guillaume): do something great instead
        return (width*height*1.5).toInt()
    }

    fun getSize(): CamSize{
        return CamSize(width,height)
    }

    companion object CREATOR : Parcelable.Creator<VideoFormat> {
        override fun createFromParcel(parcel: Parcel): VideoFormat {
            return VideoFormat(parcel)
        }

        override fun newArray(size: Int): Array<VideoFormat?> {
            return arrayOfNulls(size)
        }
    }
}
