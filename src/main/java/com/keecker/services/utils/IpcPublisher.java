/**
 * Copyright (C) 2016 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Contributors: Thomas Gallagher, Cyril Lugan
 */
package com.keecker.services.utils;

import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/** @hide
 * Sends messages to remote subscribers via their binder interface IIpcSubscriber.
 * Methods of IpcPublisher are thread safe.
 */
public class IpcPublisher<T extends Parcelable> {
    private static final String TAG = IpcPublisher.class.getSimpleName();

    /*
     * IIpcSubscriber cannot be compared to check if they refer to the same remote binder.
     * We have to do this comparison with the associated IBinder
     */
    private final ConcurrentHashMap<IBinder, IIpcSubscriber> mSubscribers = new ConcurrentHashMap<>();
    private final int mSignature;
    private final String mClassName;

    public void add(IIpcSubscriber subscriberInterface) {
        mSubscribers.put(subscriberInterface.asBinder(), subscriberInterface);
    }

    public void remove(IIpcSubscriber subscriberInterface) {
        mSubscribers.remove(subscriberInterface.asBinder());
    }

    public int getSubscribersCount() { return mSubscribers.size(); }

    synchronized public void publish(T object) {
        if (object == null) {
            Log.d(TAG, "Attempt to publish a null object");
            return;
        }
        int objSignature = IpcMessage.getClassSignature(object.getClass());
        if (objSignature != mSignature) {
            Log.e(TAG, String.format("Type mismatch, tried to publish %s on channel %s",
                    object.getClass().getName(), mClassName));
            return;
        }
        IpcMessage msg = new IpcMessage(object, mSignature);
        Iterator<Map.Entry<IBinder, IIpcSubscriber>> i = mSubscribers.entrySet().iterator();
        while (i.hasNext()) {
            try {
                i.next().getValue().onNewMessage(msg);
            } catch (DeadObjectException e) {
                // This subscriber is dead, remove it
                Log.e(TAG, "Dead process", e);
                i.remove();
            } catch (RemoteException e) {
                Log.e(TAG, "Remote exception : ", e);
            }
        }
        msg.recycle();
    }

    public IpcPublisher(Class<T> messageClass) {
        mSignature = IpcMessage.getClassSignature(messageClass);
        mClassName = messageClass.getName();
    }

    // Helper to publish something to the given subscriber
    public static <T extends Parcelable>
    void publish(IIpcSubscriber subscriber, Class<T> messageClass, T object) throws RemoteException {
        int signature = IpcMessage.getClassSignature(messageClass);
        IpcMessage msg = new IpcMessage(object, signature);
        try {
            if (subscriber != null) {
                subscriber.onNewMessage(msg);
            } else {
                Log.w(TAG, "Subscriber is null, could not call onNewMessage()");
            }
        } finally {
            msg.recycle();
        }
    }
}
