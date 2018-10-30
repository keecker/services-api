package com.keecker.services.projection.interfaces

import android.os.Parcel
import android.os.Parcelable
/**
 * ProjectorParams is the class for the projector parameters.**
 *
 * List of the projector parameters :
 *
 * - A power state, projector ON or OFF
 * - A LED state, projector LED ON or OFF
 * - An angle the verin control the projector position
 * - A focusKnobPosition the focusKnobPosition control the image quality
 * - A keystone the projector keystone
 * - A zoom the image zoom
 * - A brightness the image brightness
 * - A contrast the image contrast
 * - A displayMode
 * - A displayPosition
 * - An aspectRatio
 *
 */
data class ProjectorState(
        val orientation: Int? = null,
        val focus: Int? = null,
        val autoFocus: Boolean? = null,
        val autoKeystone: Boolean? = null,
        val disabled: Boolean? = null,
        val powerOn: Boolean? = null,
        val ledOn: Boolean? = null,
        val keystone: Int? = null,
        val zoom: Int? = null,
        val brightness: Int? = null,
        val contrast: Int? = null,
        val displayMode: DisplayMode? = null,
        val displayPosition: DisplayPosition? = null,
        val aspectRatio: AspectRatio? = null
) : Parcelable {

    companion object CREATOR : Parcelable.Creator<ProjectorState> {

        override fun createFromParcel(parcel: Parcel): ProjectorState {
            return ProjectorState(
            readIntegerOrNull(parcel),
            readIntegerOrNull(parcel),
            readBooleanOrNull(parcel),
            readBooleanOrNull(parcel),
            readBooleanOrNull(parcel),
            readBooleanOrNull(parcel),
            readBooleanOrNull(parcel),
            readIntegerOrNull(parcel),
            readIntegerOrNull(parcel),
            readIntegerOrNull(parcel),
            readIntegerOrNull(parcel),
            readParcelableOrNull<DisplayMode>(parcel, DisplayMode::class.java.classLoader) as DisplayMode?,
            readParcelableOrNull<DisplayPosition>(parcel, DisplayPosition::class.java.classLoader) as DisplayPosition?,
            readParcelableOrNull<AspectRatio>(parcel, AspectRatio::class.java.classLoader) as AspectRatio?)

        }

        override fun newArray(size: Int): Array<ProjectorState?> {
            return arrayOfNulls(size)
        }

        internal fun writeIntegerOrNull(i: Int?, out: Parcel) {
            if (i != null) {
                out.writeInt(1)
                out.writeInt(i)
            } else {
                out.writeInt(0)
            }
        }

        internal fun writeBooleanOrNull(b: Boolean?, out: Parcel) {
            if (b != null) {
                out.writeInt(1)
                out.writeInt(if (b) 1 else 0)
            } else {
                out.writeInt(0)
            }
        }

        internal fun writeParcelableOrNull(p: Parcelable?, out: Parcel, flags: Int) {
            if (p != null) {
                out.writeInt(0)
                out.writeParcelable(p, flags)
            } else {
                out.writeInt(0)
            }
        }

        internal fun readIntegerOrNull(`in`: Parcel): Int? {
            return if (`in`.readInt() == 1) {
                `in`.readInt()
            } else {
                null
            }
        }

        internal fun readBooleanOrNull(`in`: Parcel): Boolean? {
            return if (`in`.readInt() == 1) {
                `in`.readInt() == 1
            } else {
                null
            }
        }

        internal fun <T : Parcelable> readParcelableOrNull(`in`: Parcel, loader: ClassLoader): T? {
            return if (`in`.readInt() == 1) {
                `in`.readParcelable(loader)
            } else {
                null
            }
        }

        @JvmStatic
        public val defaultState = ProjectorState(
            orientation = 10,
            focus = 50,
            autoFocus = false,
            autoKeystone = false,
            disabled = false,
            powerOn = true,
            ledOn = true,
            keystone = 0,
            zoom = 50,
            brightness = 50,
            contrast = 50,
            displayMode = DisplayMode.USER,
            displayPosition = DisplayPosition.FRONT_TABLE,
            aspectRatio = AspectRatio.R16_9)
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeIntegerOrNull(orientation, dest)
        writeIntegerOrNull(focus, dest)
        writeBooleanOrNull(autoFocus, dest)
        writeBooleanOrNull(autoKeystone, dest)
        writeBooleanOrNull(disabled, dest)
        writeBooleanOrNull(powerOn, dest)
        writeBooleanOrNull(ledOn, dest)
        writeIntegerOrNull(keystone, dest)
        writeIntegerOrNull(zoom, dest)
        writeIntegerOrNull(brightness, dest)
        writeIntegerOrNull(contrast, dest)
        writeParcelableOrNull(displayMode, dest, flags)
        writeParcelableOrNull(displayPosition, dest, flags)
        writeParcelableOrNull(aspectRatio, dest, flags)
    }
}
