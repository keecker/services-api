/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Created by Thomas Gallagher on 24/11/15.
 */
package com.keecker.services.utils.sharedmemory;

import android.annotation.SuppressLint;
import android.os.MemoryFile;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base class for all buffers managed by {@link SharedMemory}.
 */
@SuppressLint("ParcelCreator")
public class SharedMemoryBuffer implements Parcelable, IRecyclable {

    private static final String TAG = SharedMemoryBuffer.class.getSimpleName();

    // Unique ID of this ipcMessage, among a SharedMemory object
    private final MemoryFile mMemoryFile;
    private final FileDescriptor mFd;
    private final long bufferId;
    private final ParcelFileDescriptor pfd;
    // Size in bytes
    private final int size;
    private final AtomicReference<Boolean> isAvailableForDequeue = new AtomicReference<>();
    private ByteBuffer mByteBuffer = null;
    private ISharedBufferCallbacks mSharedBufferCallbacks;

    protected SharedMemoryBuffer() {
        this.mMemoryFile = null;
        this.mFd = null;
        this.bufferId = -1;
        this.pfd = null;
        this.size = 0;
    }

    public SharedMemoryBuffer(int size) {
        try {
            this.mMemoryFile = new MemoryFile("", size);
            // Keep a ref on this to prevent closing
            this.mFd = getFd(mMemoryFile);
            if (this.mFd == null) {
                throw new RuntimeException("Failed to get FileDescriptor through reflection");
            }
            Constructor<ParcelFileDescriptor> constructor = ParcelFileDescriptor.class.getConstructor(FileDescriptor.class);
            this.pfd = constructor.newInstance(mFd);
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
        this.bufferId = mFd.hashCode();
        // ipcMessage is available for dequeue when built
        this.isAvailableForDequeue.set(true);
        this.size = size;
    }

    public final void close() {
        if (mMemoryFile != null) {
            mMemoryFile.close();
        }
        if (pfd != null) {
            try {
                pfd.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    public final ByteBuffer getBytes() {
        if (mByteBuffer != null) {
            return mByteBuffer;
        }
        mByteBuffer = NativeByteBuffer.adopt(pfd.getFd(), size);
        if (mByteBuffer == null) {
            throw new RuntimeException("mmap failed!");
        }
        return mByteBuffer;
    }

    public final int getSize() {
        return size;
    }

    public final ParcelFileDescriptor getPfd() {
        return pfd;
    }

    public boolean isAvailableForDequeue() {
        return isAvailableForDequeue.get();
    }

    public void setIsAvailableForDequeue(boolean isAvailableForDequeue) {
        this.isAvailableForDequeue.set(isAvailableForDequeue);
    }

    public int getFd() {
        return pfd.getFd();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.bufferId);
        dest.writeParcelable(this.pfd, flags);
        dest.writeInt(this.size);
    }

    protected SharedMemoryBuffer(Parcel in) {
        this.bufferId = in.readLong();
        this.pfd = in.readParcelable(ParcelFileDescriptor.class.getClassLoader());
        this.size = in.readInt();
        // Unused here
        this.mMemoryFile = null;
        this.mFd = null;
    }

    public long getBufferId() {
        return bufferId;
    }

    void setBufferCallbacks(ISharedBufferCallbacks callbacks) {
        mSharedBufferCallbacks = callbacks;
    }

    private static FileDescriptor getFd(MemoryFile memFile) {
        try {
            Method method = memFile.getClass().getMethod("getFileDescriptor");
            return (FileDescriptor) method.invoke(memFile);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String toString() {
        return "SharedMemoryBuffer{" +
                "bufferId=" + bufferId +
                ", size=" + size +
                '}';
    }

    /**
     * Closes the file descriptor on the side of the receiver of the buffer, you should not call this
     * method if you are in the same process as the publisher!
     */
    @Override
    public void recycle() {
        // Done with the buffer, close the fd on this side
        NativeByteBuffer.munmapByteBuffer(getBytes(), getSize());
        try {
            getPfd().close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        // Try to notify the SharedMemoryService if there's one
        if (mSharedBufferCallbacks == null) {
            return;
        }
        try {
            mSharedBufferCallbacks.doneWithBuffer();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
