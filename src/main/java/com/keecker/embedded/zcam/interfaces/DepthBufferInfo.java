/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.embedded.zcam.interfaces;

import android.os.Parcel;

import com.keecker.services.interfaces.utils.sharedmemory.SharedMemoryBuffer;

/**
 * Represents a point cloud. To get the data:
 * <pre>
 *     ByteBuffer cloud = depthBufferInfo.getBytes();
 * </pre>
 * Each point in the point cloud is represented by the following native struct:
 * <pre>
 *    struct DepthPoint {
 *          float x;                 //!< X coordinate [meters]
 *          float y;                 //!< Y coordinate [meters]
 *          float z;                 //!< Z coordinate [meters]
 *          float noise;             //!< noise value [meters]
 *          uint16_t grayValue;      //!< 16-bit gray value
 *          uint8_t depthConfidence; //!< value 0 = good, other = bad
 *     };
 * </pre>
 * Be careful with unsigned values when using them! *
 */
public class DepthBufferInfo extends SharedMemoryBuffer {
    /**
     * Version of the point cloud
     */
    public int version;
    /**
     * Timestamp, in seconds
     */
    public double stamp;
    /**
     * Width of the point cloud in pixels
     */
    public int width;
    /**
     * Height of the point cloud in pixels
     */
    public int height;

//    /** @hide */
//    public DepthBufferInfo(ParcelFileDescriptor pfd, int version, double stamp, int width, int height,
//                           int size) {
//        super(pfd, size);
//        this.version = version;
//        this.stamp = stamp;
//        this.width = width;
//        this.height = height;
//    }

    /**
     * Do not remove, used by reflection
     * @param size
     */
    public DepthBufferInfo(int size) {
        super(size);
    }

    /** @hide */
    @Override
    public int describeContents() {
        return 0;
    }

    /** @hide */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.version);
        dest.writeDouble(this.stamp);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
    }

    /** @hide */
    protected DepthBufferInfo(Parcel in) {
        super(in);
        this.version = in.readInt();
        this.stamp = in.readDouble();
        this.width = in.readInt();
        this.height = in.readInt();
    }

    /** @hide */
    public static final Creator<DepthBufferInfo> CREATOR = new Creator<DepthBufferInfo>() {
        @Override
        public DepthBufferInfo createFromParcel(Parcel source) {
            return new DepthBufferInfo(source);
        }

        @Override
        public DepthBufferInfo[] newArray(int size) {
            return new DepthBufferInfo[size];
        }
    };
}
