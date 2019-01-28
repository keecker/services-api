/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Contributors: Thomas Gallagher
 */
package com.keecker.services.interfaces.utils.sharedmemory;

import java.nio.ByteBuffer;

/** @hide */
public class NativeByteBuffer {

    static {
        System.loadLibrary("sharedmemory");
    }

    /**
     * Get a ByteBuffer out of a file descriptor
     * @param fd
     * @param size
     * @return a ByteBuffer or null on error
     */
    private static native ByteBuffer fdToByteBuffer(int fd, int size);
    /**
     * Returns 0 on success, -1 on failure
     * @param buffer
     * @return
     */
    public static native int munmapByteBuffer(ByteBuffer buffer, int size);

    /**
     * Copy a java byte array to a native file descriptor, assuming there is enough space of course
     * @param bytes
     * @param fd
     */
    public static native void copyByteArrayToFd(byte[] bytes, int fd);

    public static ByteBuffer adopt(final int fd, final int size) {
        return fdToByteBuffer(fd, size);
    }
}
