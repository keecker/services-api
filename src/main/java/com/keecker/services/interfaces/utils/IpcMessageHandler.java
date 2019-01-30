/**
 * Copyright (C) 2016 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Contributors: Thomas Gallagher, Cyril Lugan
 */
package com.keecker.services.interfaces.utils;

import android.os.Parcelable;

public interface IpcMessageHandler<T extends Parcelable> {
    void onNewMessage(T msg) throws InterruptedException;
}
