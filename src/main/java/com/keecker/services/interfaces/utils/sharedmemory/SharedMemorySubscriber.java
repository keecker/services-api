/**
 * Copyright (C) 2016 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.interfaces.utils.sharedmemory;


import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import com.keecker.services.interfaces.utils.IpcHandlerThread;
import com.keecker.services.interfaces.utils.IpcMessage;

import java.lang.reflect.Field;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

public class SharedMemorySubscriber<T extends SharedMemoryBuffer>
        extends ISharedMemorySubscriber.Stub
        implements SharedMemoryMessageHandler<T> {

    private static final String TAG = SharedMemorySubscriber.class.getSimpleName();

    private final IpcHandlerThread<MsgContainer> mHandlerThread;
    private final Parcelable.Creator<T> mParcelableCreator;

    public SharedMemorySubscriber(final Class<T> msgClass, String subscriberName) {
        Field f;
        try {
            f = msgClass.getField("CREATOR");
        } catch (NoSuchFieldException e) {
            throw new UnsupportedOperationException("Parcelables are expected to have a CREATOR field");
        }
        try {
            mParcelableCreator = (Parcelable.Creator) f.get(null);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException("Unable to get parcelable CREATOR");
        }
        mHandlerThread = new IpcHandlerThread<>(MsgContainer.class, 1, new IpcHandlerThread.Delegate<MsgContainer>() {
            @Override
            public void onNewMessage(MsgContainer msg) {
                if (msg.getSignature() == IpcMessage.getClassSignature(msgClass)) {
                    T object = msg.ipcMessage.getPayload(mParcelableCreator);
                    object.setBufferCallbacks(msg.callback);
                    SharedMemorySubscriber.this.onNewMessage(object);
                    // Recycle the "Parcel"
                    msg.ipcMessage.recycle();
                } else {
                    Log.e(TAG, "Message not dispatched, class mismatch");
                }
            }

            @Override
            public void onMessageDiscarded(MsgContainer msg) {
                msg.ipcMessage.recycle();
                try {
                    msg.callback.doneWithBuffer();
                } catch (RemoteException e) {
                    // Ignored, nothing we can do here
                }
            }

            @Override
            public void onThreadExit(Queue<MsgContainer> remainingMsg) {
                for (MsgContainer msg : remainingMsg) {
                    msg.ipcMessage.recycle();
                    try {
                        msg.callback.doneWithBuffer();
                    } catch (RemoteException e) {
                        // Ignored, nothing we can do here
                    }
                }
            }
        }, subscriberName);
        mHandlerThread.start();
        sActiveSubscribers.add(this);
    }

    @Override
    public void onNewMessage(IpcMessage msg, ISharedBufferCallbacks bufferEventsCallback) throws RemoteException {
        // Coming from the other process, queue to handler thread
        mHandlerThread.queue(new MsgContainer(msg, bufferEventsCallback));
    }

    @Override
    public void onNewMessage(T buffer) {
        // This will be overloaded by clients
    }

    public void stop() throws InterruptedException {
        mHandlerThread.interrupt();
        mHandlerThread.join();
        sActiveSubscribers.remove(this);
    }

    private final static CopyOnWriteArrayList<SharedMemorySubscriber> sActiveSubscribers = new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<SharedMemorySubscriber> getActiveSubscribers() {
        return sActiveSubscribers;
    }
}
