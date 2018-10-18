/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.utils.sharedmemory;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.keecker.services.utils.test.sharedmemory.IFruitPublisherService;

/**
 * A publisher of fruits
 */
public class FruitPublisherService extends Service {

    public static final int NUM_BUFFERS = 10;

    private SharedMemory<SharedFruit> mSharedMemory;
    private SharedMemoryPublisher<SharedFruit> mFruitPublisher;

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedMemory = new SharedMemory<>(SharedFruit.class, NUM_BUFFERS, 1000);
        mFruitPublisher = new SharedMemoryPublisher<>(SharedFruit.class);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IFruitPublisherService.Stub() {
            @Override
            public void subscribeToFruit(ISharedMemorySubscriber subscriber) throws RemoteException {
                mFruitPublisher.add(subscriber);
            }

            @Override
            public void unsubscribeToFruit(ISharedMemorySubscriber subscriber) throws RemoteException {
                mFruitPublisher.remove(subscriber);
            }

            @Override
            public void publishFruit(String name) throws RemoteException {
                SharedFruit fruit = mSharedMemory.dequeueBuffer();
                fruit.setName(name);
                mFruitPublisher.publish(fruit, new SharedMemoryPublisher.Delegate<SharedFruit>() {
                    @Override
                    public void onAllClientsDone(SharedFruit buffer) {
                        mSharedMemory.enqueueBuffer(buffer);
                    }
                });
            }

            @Override
            public boolean dequeueBuffer() {
                return mSharedMemory.dequeueBuffer() != null;
            }
        };
    }
}
