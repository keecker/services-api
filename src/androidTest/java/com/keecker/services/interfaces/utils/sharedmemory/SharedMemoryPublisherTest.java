/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Contributors: Thomas Gallagher
 */
package com.keecker.services.interfaces.utils.sharedmemory;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.keecker.services.interfaces.utils.test.sharedmemory.IFruitPublisherService;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
@SuppressWarnings("unchecked")
public class SharedMemoryPublisherTest {

    private static final String TAG = SharedMemoryPublisherTest.class.getSimpleName();

    @Rule
    public final SharedMemServiceRule mServiceRule = new SharedMemServiceRule();

    private IFruitPublisherService getBinder() throws TimeoutException {
        final Context context = InstrumentationRegistry.getTargetContext();
        final Intent intent = new Intent(context, FruitPublisherService.class);
        final IBinder iBinder = mServiceRule.bindService(intent);
        return IFruitPublisherService.Stub.asInterface(iBinder);
    }

    private IFruitPublisherService mPublisherService;

    @Before
    public void before() throws TimeoutException {
        mPublisherService = getBinder();
    }

    @After
    public void after() throws TimeoutException {
        mServiceRule.shutdownService();
    }

    @Test
    public void testPublisherCanSendMessages() throws RemoteException, InterruptedException {
        FruitSubscriber subscriber = new FruitSubscriber(SharedFruit.class, "FruitSubscriber");
        mPublisherService.subscribeToFruit(subscriber);
        mPublisherService.publishFruit("BANANA");
        assertTrue(subscriber.waitForMessage(500));
        assertEquals("BANANA", subscriber.fruitName);
        assertEquals(1, subscriber.msgCount);
        mPublisherService.publishFruit("KIWI");
        assertTrue(subscriber.waitForMessage(500));
        assertEquals("KIWI", subscriber.fruitName);
        assertEquals(2, subscriber.msgCount);
    }

    @Test
    public void testAddingSameSubscriber() throws RemoteException, InterruptedException {
        // Add the same subscriber twice and check we only get notified once
        FruitSubscriber subscriber = new FruitSubscriber(SharedFruit.class, "FruitSubscriber");
        mPublisherService.subscribeToFruit(subscriber);
        mPublisherService.subscribeToFruit(subscriber);
        mPublisherService.publishFruit("BANANA");
        assertTrue(subscriber.waitForMessage(500));
        assertEquals("BANANA", subscriber.fruitName);
        assertEquals(1, subscriber.msgCount);
        mPublisherService.publishFruit("KIWI");
        assertTrue(subscriber.waitForMessage(500));
        assertEquals("KIWI", subscriber.fruitName);
        assertEquals(2, subscriber.msgCount);
    }

    @Test
    public void testMultipleSubscribers() throws RemoteException, InterruptedException {
        int numSubs = 100;
        List<FruitSubscriber> subscribers = new ArrayList<>();
        for (int i = 0; i < numSubs; i++) {
            FruitSubscriber sub = new FruitSubscriber(SharedFruit.class, "FruitSub: " + i);
            subscribers.add(sub);
            mPublisherService.subscribeToFruit(sub);
        }
        mPublisherService.publishFruit("APPLE");
        // Wait for all the messages
        for (FruitSubscriber sub : subscribers) {
            assertTrue(sub.waitForMessage(500));
            assertEquals("APPLE", sub.fruitName);
            assertEquals(1, sub.msgCount);
        }
    }

    @Test
    public void testCanUnsubscribe() throws RemoteException, InterruptedException {
        FruitSubscriber subscriber = new FruitSubscriber(SharedFruit.class, "FruitSubscriber");
        mPublisherService.subscribeToFruit(subscriber);
        mPublisherService.unsubscribeToFruit(subscriber);
        mPublisherService.publishFruit("BANANA");
        assertFalse(subscriber.waitForMessage(500));
    }

    @Test
    public void testWellBehavedSubscribersDontExhaustSharedMemory() throws RemoteException, InterruptedException {
        int numSubs = 10;
        List<FruitSubscriber> subscribers = new ArrayList<>();
        for (int i = 0; i < numSubs; i++) {
            FruitSubscriber sub = new FruitSubscriber(SharedFruit.class, "FruitSub: " + i);
            subscribers.add(sub);
            mPublisherService.subscribeToFruit(sub);
        }
        // Publish 30 messages at 10Hz
        publishAtRate(30, 100, "WATERMELON");
        // Wait for the last message
        for (FruitSubscriber sub : subscribers) {
            assertTrue(sub.waitForMessage(500));
            assertEquals(30, sub.msgCount);
        }
        // Now we should still be able to dequeue all the buffers
        for (int i = 0; i < FruitPublisherService.NUM_BUFFERS; i++) {
            assertTrue(mPublisherService.dequeueBuffer());
        }
    }

    @Test
    public void testNaughtySubscribersExhaustSharedMemory() throws RemoteException, InterruptedException {
        int numSubs = FruitPublisherService.NUM_BUFFERS;
        List<FruitSubscriber> subscribers = new ArrayList<>();
        for (int i = 0; i < numSubs; i++) {
            FruitSubscriber sub = new FruitSubscriber(SharedFruit.class, "FruitSub: " + i);
            // These subscribers wont recycle their buffers
            sub.behaveWellWithBuffers = false;
            subscribers.add(sub);
            mPublisherService.subscribeToFruit(sub);
        }
        // Publish NUM_BUFFERS messages at 10Hz
        publishAtRate(FruitPublisherService.NUM_BUFFERS, 100, "TOMATO");
        // Wait for the last message
        for (FruitSubscriber sub : subscribers) {
            assertTrue(sub.waitForMessage(500));
            assertEquals(FruitPublisherService.NUM_BUFFERS, sub.msgCount);
        }
        // There should be no buffer left
        assertFalse(mPublisherService.dequeueBuffer());
    }

    @Test
    public void testBlockingSubscriberDontBlockEveryone() throws RemoteException, InterruptedException {
        int numSubs = 10;
        List<FruitSubscriber> subscribers = new ArrayList<>();
        for (int i = 0; i < numSubs; i++) {
            FruitSubscriber sub = new FruitSubscriber(SharedFruit.class, "FruitSub: " + i);
            if (i == 0) {
                sub.blockOnReceive = true;
            }
            subscribers.add(sub);
            mPublisherService.subscribeToFruit(sub);
        }
        // Publish 10 messages at 10Hz
        publishAtRate(10, 100, "ORANGE");
        for (int i = 0; i < numSubs; i++) {
            Log.d(TAG, String.valueOf(i));
            FruitSubscriber sub = subscribers.get(i);
            if (i == 0) {
                assertFalse(sub.waitForMessage(500));
                continue;
            }
            // Wait for the last message and check the msg count
            assertTrue(sub.waitForMessage(500));
            assertEquals(10, sub.msgCount);
        }
    }

    @Test
    public void testSubscribersCanBeStopped() throws RemoteException, InterruptedException {
        FruitSubscriber subscriber = new FruitSubscriber(SharedFruit.class, "FruitSubscriber");
        mPublisherService.subscribeToFruit(subscriber);
        subscriber.stop();
        mPublisherService.publishFruit("LEMON");
        // Should not receive message
        assertFalse(subscriber.waitForMessage(500));
    }

    private void publishAtRate(int numBuffers, int period, String fruitName) throws RemoteException, InterruptedException {
        for (int i = 0; i < numBuffers; i++) {
            mPublisherService.publishFruit(fruitName);
            Thread.sleep(period);
        }
    }
}


