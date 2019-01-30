package com.keecker.services.interfaces.utils.test.sharedmemory;

import com.keecker.services.interfaces.utils.sharedmemory.ISharedMemorySubscriber;

interface IFruitPublisherService {
    // Interface used by the subscriber to subscribe
    void subscribeToFruit(in ISharedMemorySubscriber subscriber);
    void unsubscribeToFruit(in ISharedMemorySubscriber subscriber);

    // Interface used by the unit test
    void publishFruit(in String name);
    boolean dequeueBuffer();
}
