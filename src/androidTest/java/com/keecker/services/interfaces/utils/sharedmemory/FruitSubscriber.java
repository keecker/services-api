/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.interfaces.utils.sharedmemory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FruitSubscriber extends SharedMemorySubscriber<SharedFruit> {

    private static final String TAG = FruitSubscriber.class.getSimpleName();

    String fruitName;
    int msgCount = 0;
    private CountDownLatch latch = new CountDownLatch(1);
    boolean behaveWellWithBuffers = true;
    boolean blockOnReceive = false;

    public FruitSubscriber(Class<SharedFruit> msgClass, String subscriberName) {
        super(msgClass, subscriberName);
    }

    @Override
    public void onNewMessage(SharedFruit buffer) {
        if (blockOnReceive) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                // Make sure the outer loop is notified! Very important
                Thread.currentThread().interrupt();
            }
        }
        msgCount++;
        fruitName = buffer.getName();
        if (behaveWellWithBuffers) {
            buffer.recycle();
        }
        latch.countDown();
    }

    boolean waitForMessage(long timeoutMs) throws InterruptedException {
        boolean didGoToZero = latch.await(timeoutMs, TimeUnit.MILLISECONDS);
        // Reinit for next message
        latch = new CountDownLatch(1);
        return didGoToZero;
    }
}
