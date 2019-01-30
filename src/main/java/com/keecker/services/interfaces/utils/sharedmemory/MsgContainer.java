/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.interfaces.utils.sharedmemory;

import com.keecker.services.interfaces.utils.IpcMessage;

class MsgContainer {
    final IpcMessage ipcMessage;
    final ISharedBufferCallbacks callback;

    MsgContainer(IpcMessage ipcMessage, ISharedBufferCallbacks callback) {
        this.ipcMessage = ipcMessage;
        this.callback = callback;
    }

    int getSignature() {
        return ipcMessage.getSignature();
    }
}
