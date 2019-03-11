/*
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.embedded.camera.interfaces;

import android.os.ParcelFileDescriptor;

import com.keecker.services.interfaces.embedded.camera.Frame;

import com.keecker.embedded.camera.interfaces.CameraDisconnectionReason;

oneway interface ICameraServiceCallback {
    /**
     * Called when a new video frame is available.
     *
     * This is where most work should go: calling {@link CameraBufferMemory#mmap(int fd, int size)}, to get frame data, doing what you want with it and releasing the data with {@link CameraBufferMemory#munmap(byte[] buffer, int size}.
     * @param fd The parceled file descriptor pointing to the image data
     * @param frame The meta information about the image
     */
    void onNewFrame(in ParcelFileDescriptor fd, in Frame frame);

    /**
     * Called when camera get disconnected.
     * This can appear when client close the camera explicitly, but also if another client ask for
     * the camera.
     */
    void onDisconnected(in CameraDisconnectionReason reason);
}

