package com.keecker.services.utils.ipc;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.keecker.services.utils.IpcMessage;
import com.keecker.services.utils.IpcSubscriberRule;
import com.keecker.services.utils.test.IPublisherService;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
@RunWith(AndroidJUnit4.class)
public class IpcSameProcess {

    @Rule public final IpcSubscriberRule mServiceRule = new IpcSubscriberRule();

    public IPublisherService getBinder() throws TimeoutException {
        final Context context = InstrumentationRegistry.getTargetContext();
        final Intent intent = new Intent(context, InnerProcessPublisherService.class);
        final IBinder iBinder = mServiceRule.bindService(intent);
        return IPublisherService.Stub.asInterface(iBinder);
    }

    /**
     * Tests what happens if the publisher and the subscriber are in the same process.
     */
    @Test
    public void testMessageHadlingInTheSameProcess() throws Exception {
        // The user gives a payload to the publisher
        PublisherService.Weather weather = new PublisherService.Weather(5, 10);
        // The publisher wraps it in an IpcMessage
        IpcMessage msg = new IpcMessage(weather, IpcMessage.getClassSignature(weather.getClass()));
        // If the subscriber is in the same process than the publisher, it will copy the message
        IpcMessage msgForLocalSub = msg.makeWithAParcel();
        // The subscriber will enqueue the message to be processed by a thread,
        // which unparcels the message
        PublisherService.Weather receivedWeather =
                msgForLocalSub.getPayload(PublisherService.Weather.CREATOR);

        assertEquals(weather.rain, receivedWeather.rain);
        assertEquals(weather.sun, receivedWeather.sun);
    }

    @Test
    public void testSubscriberReceivesMessages() throws Exception {

        final IPublisherService publisherService = getBinder();
        final TestIpcSubscriber<PublisherService.Weather> subscriber = new TestIpcSubscriber<PublisherService.Weather>(PublisherService.Weather.class) {
            @Override
            public void onNewMessage(PublisherService.Weather msg) {
                super.onNewMessage(msg);
                assertEquals(10, msg.sun);
                assertEquals(5, msg.rain);
            }
        };

        // Check subscriber get the correct message
        publisherService.subscribeToWeather(subscriber);
        publisherService.publishWeather(10, 5);
        assertTrue(subscriber.waitForElements());
    }

    public static class InnerProcessPublisherService extends PublisherService {}
}
