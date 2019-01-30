/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.interfaces.utils.sharedmemory;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SharedMemory<T extends SharedMemoryBuffer> {

    private static final String TAG = SharedMemory.class.getSimpleName();

    private final int mBufferSize;
    private final List<T> mBuffers = new ArrayList<>();

    public SharedMemory(Class<T> clazz, int numBuffers, final int bufferSize) {
        if (numBuffers <= 0 || bufferSize <= 0) {
            throw new IllegalArgumentException(String.format(
                    Locale.ENGLISH, "Wrong arguments numBuffers %d bufferSize %d", numBuffers, bufferSize));
        }
        mBufferSize = bufferSize;
        for (int i = 0; i < numBuffers; i++) {
            // Create numBuffers objects of type clazz to wrap the fd
            try {
                Constructor<T> constructor = clazz.getConstructor(int.class);
                T buffer = constructor.newInstance(bufferSize);
                mBuffers.add(buffer);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                    InvocationTargetException e) {
                throw new RuntimeException(String.format(
                        "Failed to invoke %s(int) constructor, did you create one?",
                        clazz.getSimpleName()));
            }
        }
        Log.d(TAG, String.format("Created SharedMemory size: %d x %d bytes", numBuffers, bufferSize));
    }

    public synchronized void destroy() {
        Log.d(TAG, "Destroying " + this.toString());
        for (SharedMemoryBuffer buffer : mBuffers) {
            buffer.close();
        }
        mBuffers.clear();
    }

    public synchronized void enqueueBuffer(T buffer) {
        buffer.setIsAvailableForDequeue(true);
    }

    synchronized void enqueueBuffer(final long bufferId) {
        for (T buffer : mBuffers) {
            if (buffer.getBufferId() == bufferId) {
                enqueueBuffer(buffer);
                break;
            }
        }
    }

    public synchronized T dequeueBuffer() {
        // Return the first Buffer available for dequeue
        for (T buffer : mBuffers) {
            if (buffer.isAvailableForDequeue()) {
                buffer.setIsAvailableForDequeue(false);
                return buffer;
            }
        }
        return null;
    }

    /**
     * Dequeues a buffer and returns it's file descriptor
     * @return fd of the dequeued buffer or -1 if none is available
     */
    synchronized int dequeueBufferFd() {
        // Return the first Buffer available for dequeue
        for (T buffer : mBuffers) {
            if (buffer.isAvailableForDequeue()) {
                buffer.setIsAvailableForDequeue(false);
                return buffer.getFd();
            }
        }
        return -1;
    }

    /**
     * Returns the buffer with the given buffer id, or null if not found
     * @param bufferId
     * @return
     */
    public synchronized T getBufferFromId(long bufferId) {
        for (T buffer : mBuffers) {
            if (buffer.getBufferId() == bufferId) {
                return buffer;
            }
        }
        return null;
    }

    /**
     * Returns the buffer id for a given file descriptor
     * @param fd - the file descriptor to look for
     * @return the buffer id, or -1 if not found
     */
    synchronized long getBufferIdFromFd(int fd) {
        for (T buffer : mBuffers) {
            if (buffer.getFd() == fd) {
                return buffer.getBufferId();
            }
        }
        return -1;
    }

    int getNumBuffers() {
        return mBuffers.size();
    }

    int getBufferSize() {
        return mBufferSize;
    }
}
