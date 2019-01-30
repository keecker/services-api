package com.keecker.services.interfaces.navigation;

import android.os.Parcel;
import android.os.Parcelable;

import com.keecker.services.interfaces.utils.geometry.Quaternion;
import com.keecker.services.interfaces.utils.geometry.Transform;
import com.keecker.services.interfaces.utils.geometry.Vector3;

public class DetectionPose implements Parcelable{
    public final Transform transform;
    public final double confidence;
    public final double timestamp;

    protected DetectionPose(Parcel in) {
        transform = in.readParcelable(Transform.class.getClassLoader());
        confidence = in.readDouble();
        timestamp = in.readDouble();
    }

    public static final Creator<DetectionPose> CREATOR = new Creator<DetectionPose>() {
        @Override
        public DetectionPose createFromParcel(Parcel in) {
            return new DetectionPose(in);
        }

        @Override
        public DetectionPose[] newArray(int size) {
            return new DetectionPose[size];
        }
    };

    @Override
    public String toString() {
        return "DetectionPose{" +
                "transform=" + transform +
                ", confidence=" + confidence +
                ", timestamp=" + timestamp +
                '}';
    }

    public DetectionPose(float result[], double confidence, double timestamp) {
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
