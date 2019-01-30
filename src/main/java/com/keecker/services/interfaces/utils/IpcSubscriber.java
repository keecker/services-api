/**
 * Copyright (C) 2016 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Contributors: Cyril Lugan
 */
package com.keecker.services.interfaces.utils;

import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Implements the AIDL interface used remotely by publishers. One thread is started for each
 * subscriber to dispatch the messages. When a publisher sends a message, it is received and queued
 * to be handled in the thread. If messages are not handled fast enough, the queue will discard
 * oldest messages.
 *
 * The user can choose to provide an IpcMessageHandler or directly override the
 * onNewMessage method.
 */

public class IpcSubscriber<T extends Parcelable>
        extends IIpcSubscriber.Stub
        implements IpcMessageHandler<T> {

    private static final String TAG = IpcSubscriber.class.getSimpleName();

    private final IpcHandlerThread<IpcMessage> mHandlerThread;
    private final Parcelable.Creator<T> mParcelableCreator;

    public IpcSubscriber(final Class<T> messageClass, int queueSize) {
        Field f;
        try {
            f = messageClass.getField("CREATOR");
        } catch (NoSuchFieldException e) {
            throw new UnsupportedOperationException("Parcelables are expected to have a CREATOR field");
        }
        try {
            mParcelableCreator = (Parcelable.Creator) f.get(null);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException("Unable to get parcelable CREATOR");
        }
        mHandlerThread = new IpcHandlerThread<>(IpcMessage.class, queueSize,
                new IpcHandlerThread.Delegate<IpcMessage>() {
            @Override
            public void onNewMessage(IpcMessage msg) {
                if (msg.getSignature() == IpcMessage.getClassSignature(messageClass)) {
                    IpcSubscriber.this.onNewMessage(msg.getPayload(mParcelableCreator));
                } else {
                    Log.e(TAG, "Message not dispatched, class mismatch");
                }
                msg.recycle();
            }

            @Override
            public void onMessageDiscarded(IpcMessage msg) {
                msg.recycle();
            }

            @Override
            public void onThreadExit(Queue<IpcMessage> remainingMsg) {
                for (IpcMessage msg : remainingMsg) {
                    msg.recycle();
                }
            }
        }, null);
        mHandlerThread.start();
        sActiveSubscribers.add(this);
    }

    public IpcSubscriber(Class<T> messageClass) {
        this(messageClass, 10);
    }

    @Override
    final public void onNewMessage(IpcMessage msg) {
        if (!msg.hasBeenContructedWithAParcel()) {
            // If the message have been published from the same process, it may be recycled
            // before being handled by the handler thread. To avoid this, we queue a copy of the
            // message.
            msg = msg.makeWithAParcel();
        }
        mHandlerThread.queue(msg);
    }

    @Override
    public void onNewMessage(T msg) {
        // Can be overrided instead of constructing with a IpcMessageHandler
    }

    public void stop() throws InterruptedException {
        mHandlerThread.interrupt();
        mHandlerThread.join();
        sActiveSubscribers.remove(this);
    }

    public int getStackedMessagesCount() {
        return mHandlerThread.getQueueSize();
    }

    private final static CopyOnWriteArrayList<IpcSubscriber> sActiveSubscribers = new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<IpcSubscriber> getActiveSubscribers() {
            return sActiveSubscribers;
    }
}
