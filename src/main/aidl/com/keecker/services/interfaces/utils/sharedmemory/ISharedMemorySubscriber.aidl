package com.keecker.services.interfaces.utils.sharedmemory;

import com.keecker.services.interfaces.utils.sharedmemory.SharedMemoryBuffer;
import com.keecker.services.interfaces.utils.sharedmemory.ISharedBufferCallbacks;
import com.keecker.services.interfaces.utils.IpcMessage;

interface ISharedMemorySubscriber {
    void onNewMessage(in IpcMessage msg, in ISharedBufferCallbacks bufferEventsCallback);
}