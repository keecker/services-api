package com.keecker.services.navigation.interfaces;

import android.os.Parcel;
import android.os.Parcelable;

import com.keecker.services.utils.geometry.Quaternion;
import com.keecker.services.utils.geometry.Transform;
import com.keecker.services.utils.geometry.Vector3;

import java.util.Arrays;

/**
 * Created by xavier on 12/02/18.
 */
public class DetectionResult implements Parcelable{
    public final Transform transform;
    public final double confidence;
    public final double timestamp;

    protected DetectionResult(Parcel in) {
        transform = in.readParcelable(Transform.class.getClassLoader());
        confidence = in.readDouble();
        timestamp = in.readDouble();
    }

    public static final Creator<DetectionResult> CREATOR = new Creator<DetectionResult>() {
        @Override
        public DetectionResult createFromParcel(Parcel in) {
            return new DetectionResult(in);
        }

        @Override
        public DetectionResult[] newArray(int size) {
            return new DetectionResult[size];
        }
    };

    @Override
    public String toString() {
        return "DetectionResult{" +
                "transform=" + transform +
                ", confidence=" + confidence +
                ", timestamp=" + timestamp +
                '}';
    }

    public DetectionResult(float result[], double confidence, double timestamp) {
        this.transform = new Transform(
            new Vector3(result[0], result[1], result[2]),
            Quaternion.fromTheta(result[3])
        );
        this.confidence = confidence;
        this.timestamp = timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(transform, flags);
        dest.writeDouble(confidence);
        dest.writeDouble(timestamp);
    }
}
