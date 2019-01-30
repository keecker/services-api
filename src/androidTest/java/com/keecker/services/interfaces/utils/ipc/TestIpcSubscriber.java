/*
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.interfaces.utils.ipc;

import android.os.Parcelable;

import com.keecker.services.interfaces.utils.IpcSubscriber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

class TestIpcSubscriber<T extends Parcelable> extends IpcSubscriber<T> {

    private final AtomicReference<CountDownLatch> mLatchRef = new AtomicReference<>();
    private final List<T> mObjects = Collections.synchronizedList(new ArrayList<T>());

    public TestIpcSubscriber(Class<T> messageClass, int countdown) {
        super(messageClass, countdown);
        resetCountdown(countdown);
    }

    public TestIpcSubscriber(Class<T> messageClass) {
        this(messageClass, 1);
    }

    @Override public void onNewMessage(T msg) {
        mObjects.add(msg);
        mLatchRef.get().countDown();
    }

    public void resetCountdown(int count) {
        mLatchRef.set(new CountDownLatch(count));
    }

    public boolean waitForElements() throws InterruptedException {
        return waitForElements(100);
    }

    public boolean waitForElements(long timeoutMs) throws InterruptedException {
        return mLatchRef.get().await(timeoutMs, TimeUnit.MILLISECONDS);
    }

    public int size() {
        return mObjects.size();
    }
}
