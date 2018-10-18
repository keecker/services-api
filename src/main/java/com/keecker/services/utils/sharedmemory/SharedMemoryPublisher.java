/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.utils.sharedmemory;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.LongSparseArray;

import com.keecker.services.utils.IpcMessage;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 *
 * @param <T>
 */
public class SharedMemoryPublisher<T extends SharedMemoryBuffer> {

    private static final String TAG = SharedMemoryPublisher.class.getSimpleName();

    public interface Delegate<T> {
        void onAllClientsDone(T buffer);
    }

    private final ExecutorService mLatchesThreadpool;
    private final LongSparseArray<CountDownLatch> mLatches;
    private final ConcurrentHashMap<IBinder, ISharedMemorySubscriber> mSubscribers = new ConcurrentHashMap<>();
    private final int mSignature;
    private final String mClassName;

    public SharedMemoryPublisher(Class<T> msgClass) {
        // Threadpool that will wait for clients to tell us they're done with the buffers
        mLatchesThreadpool = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "BufferDoneWaiter");
            }
        });
        // Latches to check that all clients are done with one ipcMessage
        mLatches = new LongSparseArray<>();
        // Class signature
        mSignature = IpcMessage.getClassSignature(msgClass);
        mClassName = msgClass.getSimpleName();
    }

    public synchronized void add(ISharedMemorySubscriber sub) {
        mSubscribers.put(sub.asBinder(), sub);
    }

    public synchronized void remove(ISharedMemorySubscriber sub) {
        mSubscribers.remove(sub.asBinder());
    }

    public synchronized void publish(final T object, final Delegate<T> delegate) {
        if (object == null) {
            Log.d(TAG, "Attempt to publish a null object");
            return;
        }
        // Check we're publishing the correct type
        int objSignature = IpcMessage.getClassSignature(object.getClass());
        if (objSignature != mSignature) {
            Log.e(TAG, String.format("Type mismatch, tried to publish %s on channel %s",
                    object.getClass().getName(), mClassName));
            return;
        }
        // Create the countdown latch
        final CountDownLatch latch = new CountDownLatch(getSubscribersCount());
        mLatches.put(object.getBufferId(), latch);
        // Start the watcher thread that will wait on the latch
        mLatchesThreadpool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await(10000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage(), e);
                } finally {
                    // All subscribers are done or timed out, notify our parent
                    delegate.onAllClientsDone(object);
                }
            }
        });
        // Notify our clients
        IpcMessage msg = new IpcMessage(object, mSignature);
        Iterator<Map.Entry<IBinder, ISharedMemorySubscriber>> i = mSubscribers.entrySet().iterator();
        while (i.hasNext()) {
            try {
                i.next().getValue().onNewMessage(msg, new ISharedBufferCallbacks.Stub() {
                    @Override
                    public void doneWithBuffer() throws RemoteException {
                        latch.countDown();
                    }
                });
            } catch (RemoteException e) {
                // This subscriber is dead, remove it
                i.remove();
                // Won't need the ipcMessage anymore as it's dead
                latch.countDown();
            }
        }
    }

    public int getSubscribersCount() {
        return mSubscribers.size();
    }
}
