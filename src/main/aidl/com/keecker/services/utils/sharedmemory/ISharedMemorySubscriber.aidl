package com.keecker.services.utils.sharedmemory;

import com.keecker.services.utils.sharedmemory.SharedMemoryBuffer;
import com.keecker.services.utils.sharedmemory.ISharedBufferCallbacks;
import com.keecker.services.utils.IpcMessage;

interface ISharedMemorySubscriber {
    void onNewMessage(in IpcMessage msg, in ISharedBufferCallbacks bufferEventsCallback);
}