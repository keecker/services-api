/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.embedded.zcam.interfaces;

import com.keecker.embedded.zcam.interfaces.ZcamMode;

import com.keecker.services.interfaces.utils.sharedmemory.ISharedMemorySubscriber;

interface IZcamService {
    /** @hide
     * Sets the zcam mode
     * @param mode - a {@link ZcamMode}
     * @throws RemoteException
     */
    void setMode(in ZcamMode mode);

    /**
     * Register a subscriber to receive point clouds {@link DepthBufferInfo}
     * @param subscriber - your subscriber
     * @throws RemoteException
     */
    void register(in ISharedMemorySubscriber subscriber);

    /**
     * Unregister a {@link ISharedMemorySubscriber}
     * @param subscriber
     * @throws RemoteException
     */
    void unregister(in ISharedMemorySubscriber subscriber);

    /** @hide
      * Type not used for now, may be used to select the logged data in the future
      * Filename is the file to log to.
      */
    void logDataTo(in String contentUrl, in String type);
}
