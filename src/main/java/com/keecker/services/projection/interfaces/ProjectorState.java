package com.keecker.services.projection.interfaces;

import android.os.Parcel;
import android.os.Parcelable;

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
public class ProjectorState implements Parcelable {

    public final Integer orientation;
    public final Integer focus;
    public final Boolean autoFocus;
    public final Boolean autoKeystone;
    public final Boolean disabled;
    public final Boolean powerOn;
    public final Boolean ledOn;
    public final Integer keystone;
    public final Integer zoom;
    public final Integer brightness;
    public final Integer contrast;
    public final DisplayMode displayMode;
    public final DisplayPosition displayPosition;
    public final AspectRatio aspectRatio;

    public ProjectorState(
            Integer orientation,
            Integer focus,
            Boolean autoFocus,
            Boolean autoKeystone,
            Boolean disabled,
            Boolean powerOn,
            Boolean ledOn,
            Integer keystone,
            Integer zoom,
            Integer brightness,
            Integer contrast,
            DisplayMode displayMode,
            DisplayPosition displayPosition,
            AspectRatio aspectRatio) {
        this.orientation = orientation;
        this.focus = focus;
        this.autoFocus = autoFocus;
        this.autoKeystone = autoKeystone;
        this.disabled = disabled;
        this.powerOn = powerOn;
        this.ledOn = ledOn;
        this.keystone = keystone;
        this.zoom = zoom;
        this.brightness = brightness;
        this.contrast = contrast;
        this.displayMode = displayMode;
        this.displayPosition = displayPosition;
        this.aspectRatio = aspectRatio;
    }

    static void writeIntegerOrNull(Integer i, Parcel out) {
        if (i != null) {
            out.writeInt(1);
            out.writeInt(i);
        } else {
            out.writeInt(0);
        }
    }

    static void writeBooleanOrNull(Boolean b, Parcel out) {
        if (b != null) {
            out.writeInt(1);
            out.writeInt((b) ? 1 : 0);
        } else {
            out.writeInt(0);
        }
    }

    static void writeParcelableOrNull(Parcelable p, Parcel out, int flags) {
        if (p != null) {
            out.writeInt(0);
            out.writeParcelable(p, flags);
        } else {
            out.writeInt(0);
        }
    }

    static Integer readIntegerOrNull(Parcel in) {
        if (in.readInt() == 1) {
            return in.readInt();
        } else {
            return null;
        }
    }

    static Boolean readBooleanOrNull(Parcel in) {
        if (in.readInt() == 1) {
            return in.readInt() == 1;
        } else {
            return null;
        }
    }

    static <T extends Parcelable> T readParcelableOrNull(Parcel in, ClassLoader loader) {
        if (in.readInt() == 1) {
            return in.readParcelable(loader);
        } else {
            return null;
        }
    }


    public static final Parcelable.Creator<ProjectorState> CREATOR =
                new Parcelable.Creator<ProjectorState>() {

            @Override public ProjectorState createFromParcel(Parcel in) {
                return new ProjectorState(
                    readIntegerOrNull(in),
                    readIntegerOrNull(in),
                    readBooleanOrNull(in),
                    readBooleanOrNull(in),
                    readBooleanOrNull(in),
                    readBooleanOrNull(in),
                    readBooleanOrNull(in),
                    readIntegerOrNull(in),
                    readIntegerOrNull(in),
                    readIntegerOrNull(in),
                    readIntegerOrNull(in),
                    (DisplayMode) readParcelableOrNull(in, DisplayMode.class.getClassLoader()),
                    (DisplayPosition) readParcelableOrNull(in, DisplayPosition.class.getClassLoader()),
                    (AspectRatio) readParcelableOrNull(in, AspectRatio.class.getClassLoader()));
        }

            @Override public ProjectorState[] newArray(int size) {
                return new ProjectorState[size];
            }
    };

    public static ProjectorState defaultState = new ProjectorState(
                10,
                50,
                false,
                false,
                false,
                true,
                true,
                0,
                50,
                50,
                50,
                DisplayMode.USER,
                DisplayPosition.FRONT_TABLE,
                AspectRatio.R16_9
        );

    @Override public int describeContents() { return 0; }

    @Override public void writeToParcel(Parcel dest, int flags) {
        writeIntegerOrNull(orientation, dest);
        writeIntegerOrNull(focus, dest);
        writeBooleanOrNull(autoFocus, dest);
        writeBooleanOrNull(autoKeystone, dest);
        writeBooleanOrNull(disabled, dest);
        writeBooleanOrNull(powerOn, dest);
        writeBooleanOrNull(ledOn, dest);
        writeIntegerOrNull(keystone, dest);
        writeIntegerOrNull(zoom, dest);
        writeIntegerOrNull(brightness, dest);
        writeIntegerOrNull(contrast, dest);
        writeParcelableOrNull(displayMode, dest, flags);
        writeParcelableOrNull(displayPosition, dest, flags);
        writeParcelableOrNull(aspectRatio, dest, flags);
    }
}
