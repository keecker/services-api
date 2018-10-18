/*
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.utils.ipc;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.keecker.services.utils.ServiceTestRule;
import com.keecker.services.utils.test.IOneWayService;
import com.keecker.services.utils.test.OneWayListener;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Test and compare various ipc communication strategies
 *
 * Oneway message are async and are based on a transaction queue of 1Mo max per for one subscriber for all his binded services.
 * This means some message might be dropped if the transaction queue is full.
 *
 * Here is a list of the different factor which, combine or not, might lead to a full transaction queue:
 *      - If the publisher send too many messages in a short time, which might lead to msg being send faster than read
 *      - If the messages are too big as each transaction will take more place in the queue
 *      - If a subscriber is binded to too many services as his queue is shared for all binded services
 *      - If a subscriber don't process a message faster than the publisher publish messages
 *
 * Notice that message are dropped per subcriber. If one subscriber is too slow in reading messages, other subscriber will still get all events.
 *
 * Dropped transaction can be observed in the logcat, they look like:
 * 11-21 13:15:05.045 16503-16518/com.keecker.services.utils.test:ipc_pub E/JavaBinder: !!! FAILED BINDER TRANSACTION !!!  (parcel size = 148)
 *
 * These tests are ignored as this is only used as a POC and not testing any production code, remove the annotation to run the tests.
 */
@RunWith(AndroidJUnit4.class)
@Ignore
public class IpcCommunication {

    @Rule public final ServiceTestRule mServiceRule = new ServiceTestRule();

    public IOneWayService getBinder() throws TimeoutException {
        final Context context = InstrumentationRegistry.getTargetContext();
        final Intent intent = new Intent(context, OneWayService.class);
        final IBinder iBinder = mServiceRule.bindService(intent);
        return IOneWayService.Stub.asInterface(iBinder);
    }

    /**
     * Assert that oneway message aren't easily drop.
     * We send 4k simple message (a simple string) without any delay between messages and assert none are dropped.
     * This works fine for 4k and can work for a higher number but will still be dependant of the proc load when the test is run.
     */
    @Test
    public void testOneWayListener() throws Exception {
        final IOneWayService publisherService = getBinder();
        final int expected = 4_000;
        final TestOneWayListener listener = new TestOneWayListener(expected);
        publisherService.subscribeToOneWayListener(listener);
        publisherService.floodOneWayListener(expected, 0);
        final boolean await = listener.mCountDownLatch.await(5L, TimeUnit.SECONDS);
        final long count = listener.mCountDownLatch.getCount();
        assertEquals("Missed " + count + " messages", 0, count);
        assertTrue(await);
    }

    /**
     * Assert that oneway message are dropped at some point if there is absolutely no time between each messages
     * We send 10k simple message (a simple string) without any delay between messages thus filling the transaction queue.
     */
    @Test
    public void testOneWayListenerFailWithTooManyMessage() throws Exception {
        final IOneWayService publisherService = getBinder();
        final int expected = 10_000;
        final TestOneWayListener listener = new TestOneWayListener(expected);
        publisherService.subscribeToOneWayListener(listener);
        publisherService.floodOneWayListener(expected, 0);
        final boolean await = listener.mCountDownLatch.await(5L, TimeUnit.SECONDS);
        Log.v("IpcCommunication", "Missed %d messages" + listener.mCountDownLatch.getCount());
        assertFalse(await);
    }

    /**
     * Assert that oneway message aren't dropped if there is a reasonable time between each messages.
     * We send 10k simple message (a simple string) without any delay between messages thus filling the transaction queue.
     *
     * Notice that while this test pass, it's only sending simple data (one small string).
     * Sending bigger messages won't work without a larger delay between each messages.
     * While using synchronous listener might seem like a good idea, it will probably result in longer delay between each
     * messages as ipc call are consuming.
     */
    @Test
    public void testOneWayListenerWithDelay() throws Exception {
        final IOneWayService publisherService = getBinder();
        final int expected = 10_000;
        final long delay = 10;
        final TestOneWayListener listener = new TestOneWayListener(expected);
        publisherService.subscribeToOneWayListener(listener);
        publisherService.floodOneWayListener(expected, delay);
        final boolean await = listener.mCountDownLatch.await(expected*delay + 10000, TimeUnit.MILLISECONDS);
        final long count = listener.mCountDownLatch.getCount();
        assertEquals("Missed " + count + " messages", 0, count);
        assertTrue(await);
    }

    private static class TestOneWayListener extends OneWayListener.Stub {

        public final CountDownLatch mCountDownLatch;

        public TestOneWayListener(int expected) {
            mCountDownLatch = new CountDownLatch(expected);
        }

        @Override public void onNewMessage(String number) throws RemoteException {
            mCountDownLatch.countDown();
        }
    }

    public static class OneWayService extends Service {
        private OneWayListener mOneWayListener;
        final IBinder mBinder = new IOneWayService.Stub() {

            @Override
            public void subscribeToOneWayListener(OneWayListener listener) throws RemoteException {
                mOneWayListener = listener;
            }

            @Override public void floodOneWayListener(final int count, final long delay) throws RemoteException {
                final OneWayListener listener = mOneWayListener;
                new Thread(new Runnable() {
                    @Override public void run() {
                        for (int i = 0; i < count; ++i) {
                            try {
                                listener.onNewMessage(String.valueOf(i));
                            } catch (RemoteException ignored) {}
                            if (delay > 0) {
                                SystemClock.sleep(delay);
                            }
                        }
                    }
                }).start();
            }
        };

        @Override public IBinder onBind(Intent intent) {
            Log.v("IpcCommunication", "onBind");
            return mBinder;
        }
    }

}
