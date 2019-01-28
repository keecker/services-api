/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.interfaces.utils.sharedmemory;

public interface SharedMemoryMessageHandler<T extends SharedMemoryBuffer & IRecyclable> {
    void onNewMessage(T msg) throws InterruptedException;
}
