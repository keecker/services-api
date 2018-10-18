package com.keecker.services.utils.test.sharedmemory;

import com.keecker.services.utils.sharedmemory.ISharedMemorySubscriber;

interface IFruitPublisherService {
    // Interface used by the subscriber to subscribe
    void subscribeToFruit(in ISharedMemorySubscriber subscriber);
    void unsubscribeToFruit(in ISharedMemorySubscriber subscriber);

    // Interface used by the unit test
    void publishFruit(in String name);
    boolean dequeueBuffer();
}