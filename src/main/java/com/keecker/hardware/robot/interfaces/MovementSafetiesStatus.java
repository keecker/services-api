/*
 * Copyright (C) 2018 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Created by Cyril Lugan <cyril@keecker.com> on 2018-10-16
 */
package com.keecker.hardware.robot.interfaces;

import android.os.Parcel;
import android.os.Parcelable;

public class MovementSafetiesStatus implements Parcelable {

    public static final int NB_OF_PROXIMITY_SENSORS = 12;

    // Will be initialized to 0 according to language spec
    private double[] timestamps = new double[NB_OF_PROXIMITY_SENSORS];
    /**
     * Proximity sensors range in mm
     * int because shorts cannot be parceled
     */
    private int[] ranges = new int[NB_OF_PROXIMITY_SENSORS];

    private int errors = 0;
    private int triggers = 0;

    public MovementSafetiesStatus() {}

    public static final Creator<MovementSafetiesStatus> CREATOR =
            new Creator<MovementSafetiesStatus>() {
        public MovementSafetiesStatus createFromParcel(Parcel in) {
            return new MovementSafetiesStatus(in);
        }

        public MovementSafetiesStatus[] newArray(int size) {
            return new MovementSafetiesStatus[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public MovementSafetiesStatus(Parcel in) {
        in.readIntArray(ranges);
        in.readDoubleArray(timestamps);
        errors = in.readInt();
        triggers = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeIntArray(ranges);
        out.writeDoubleArray(timestamps);
        out.writeInt(errors);
        out.writeInt(triggers);
    }

    public MovementSafetiesStatus setProximityRange(int range, double stamp, int sensorId) {
        if (sensorId >= 0 && sensorId < ranges.length) {
            this.ranges[sensorId] = range;
            this.timestamps[sensorId] = stamp;
        }
        return this;
    }

    public MovementSafetiesStatus setErrors(int errors) {
        this.errors = errors;
        return this;
    }

    public MovementSafetiesStatus setTriggers(int triggers) {
        this.triggers = triggers;
        return this;
    }

    public boolean isSafetyTriggered(int movement_safeties_mask) {
        return (triggers & movement_safeties_mask) != 0;
    }

    public int[] getRangesInMm() {
        return ranges;
    }

    public double[] getRangesInM() {
        double[] rangesM = new double[ranges.length];
        for(int i = 0; i < ranges.length; i++) {
            rangesM[i] = ranges[i] / 1000.0;
        }
        return rangesM;
    }

    public double[] getRangesTimestamps() {
        return timestamps;
    }

    public boolean[] getRangesErrors() {
        boolean[] errors = new boolean[ranges.length];
        for(int i = 0; i < ranges.length; i++) {
            errors[i] = (this.errors & (1 << i)) != 0;
        }
        return errors;
    }

    public int getTriggersMask() {
        return triggers;
    }

    public int getErrorsMask() {
        return errors;
    }
}
