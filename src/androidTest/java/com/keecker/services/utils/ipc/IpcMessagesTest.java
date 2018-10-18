package com.keecker.services.utils.ipc;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.keecker.services.utils.IpcSubscriber;
import com.keecker.services.utils.IpcSubscriberRule;
import com.keecker.services.utils.test.IPublisherService;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class IpcMessagesTest {

    @Rule public final IpcSubscriberRule mServiceRule = new IpcSubscriberRule();

    public IPublisherService getBinder() throws TimeoutException {
        final Context context = InstrumentationRegistry.getTargetContext();
        final Intent intent = new Intent(context, PublisherService.class);
        final IBinder iBinder = mServiceRule.bindService(intent);
        return IPublisherService.Stub.asInterface(iBinder);
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

    @Test
    public void testMultipleSubscribersCanReceiveMessages() throws Exception {

        final IPublisherService publisherService = getBinder();
        final TestIpcSubscriber<PublisherService.Weather> subscriber1 = new TestIpcSubscriber<>(PublisherService.Weather.class);
        final TestIpcSubscriber<PublisherService.Weather> subscriber2 = new TestIpcSubscriber<>(PublisherService.Weather.class);
        publisherService.subscribeToForecast(subscriber1);
        publisherService.subscribeToForecast(subscriber2);

        // Check the two subscriber get the message
        publisherService.publishForecast();
        assertTrue(subscriber1.waitForElements());
        assertTrue(subscriber2.waitForElements());
    }

    @Test
    public void testSubscriberGetOnlyTheirMessages() throws Exception {

        final IPublisherService publisherService = getBinder();
        final TestIpcSubscriber<PublisherService.Weather> weatherSubscriber = new TestIpcSubscriber<>(PublisherService.Weather.class);
        final TestIpcSubscriber<PublisherService.Weather> forecastSubscriber = new TestIpcSubscriber<>(PublisherService.Weather.class);
        publisherService.subscribeToWeather(weatherSubscriber);
        publisherService.subscribeToForecast(forecastSubscriber);

        // Check only weatherSubscriber gets Weather messages
        publisherService.publishWeather(1, 2);
        assertTrue(weatherSubscriber.waitForElements());
        assertFalse(forecastSubscriber.waitForElements());

        weatherSubscriber.resetCountdown(1);
        forecastSubscriber.resetCountdown(1);

        // Check only forecastSubscriber gets forecast messages
        publisherService.publishForecast();
        assertFalse(weatherSubscriber.waitForElements());
        assertTrue(forecastSubscriber.waitForElements());
    }

    @Test
    public void testSubscriberCanUnsubscribe() throws Exception {

        final IPublisherService publisherService = getBinder();
        final TestIpcSubscriber<PublisherService.Weather> subscriber = new TestIpcSubscriber<>(PublisherService.Weather.class);
        publisherService.subscribeToForecast(subscriber);

        // Check subscriber gets the message
        publisherService.publishForecast();
        assertTrue(subscriber.waitForElements());

        // Un-subscribe
        publisherService.unsubscribeToForecast(subscriber);
        subscriber.resetCountdown(1);

        // Check subscriber gets no messages
        publisherService.publishForecast();
        assertFalse(subscriber.waitForElements());
    }

    @Test
    public void testSubscriberDoesNotReceiveMessagesTwiceWhenSubscribedTwice() throws Exception {

        final IPublisherService publisherService = getBinder();
        // Init with countdown to 2
        final TestIpcSubscriber<PublisherService.Weather> subscriber = new TestIpcSubscriber<>(PublisherService.Weather.class, 2);
        publisherService.subscribeToForecast(subscriber);
        publisherService.subscribeToForecast(subscriber);

        publisherService.publishForecast();

        // Check we only got one message
        assertFalse(subscriber.waitForElements());
        assertEquals(1, subscriber.size());
    }

    @Test
    public void testSubscribersRemainActiveWhenOutOfScope() throws Exception {

        final IPublisherService publisherService = getBinder();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        {
            final IpcSubscriber<PublisherService.Weather> subscriber = new IpcSubscriber<PublisherService.Weather>(PublisherService.Weather.class) {
                @Override
                public void onNewMessage(PublisherService.Weather msg) {
                    countDownLatch.countDown();
                }
            };
            publisherService.subscribeToForecast(subscriber);
        }

        publisherService.publishForecast();
        assertTrue(countDownLatch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testAStopedSubscriberDoesNotReceiveMessages() throws Exception {

        final IPublisherService publisherService = getBinder();
        final TestIpcSubscriber<PublisherService.Weather> subscriber = new TestIpcSubscriber<>(PublisherService.Weather.class);
        publisherService.subscribeToForecast(subscriber);

        // Stop subscriber
        subscriber.stop();

        // Check no messages are received
        publisherService.publishForecast();
        assertFalse(subscriber.waitForElements());
    }

    @Test
    public void testAStopedSubscriberDoesNotAppearInActiveSubscribers() throws Exception {

        final IpcSubscriber<PublisherService.Weather> subscriber = new IpcSubscriber<>(PublisherService.Weather.class);
        assertEquals(1, IpcSubscriber.getActiveSubscribers().size());
        subscriber.stop();
        assertEquals(0, IpcSubscriber.getActiveSubscribers().size());
    }

    @Test
    public void testSubscribersCanBeFloodedWithoutMissingMessages() throws Exception {

        final IPublisherService publisherService = getBinder();
        // Init with countdown to 1000
        final TestIpcSubscriber<PublisherService.Weather> subscriber = new TestIpcSubscriber<>(PublisherService.Weather.class, 1000);
        publisherService.subscribeToForecast(subscriber);

        publisherService.floodForecast(1000);
        subscriber.waitForElements(200);
        assertEquals(1000, subscriber.size());
    }

    @Test
    public void testSubscribersDoNotReceiveWrongMessageTypes() throws Exception {

        final IPublisherService publisherService = getBinder();
        final TestIpcSubscriber<PublisherService.Weather> subscriber = new TestIpcSubscriber<>(PublisherService.Weather.class);
        publisherService.subscribeToAlerts(subscriber);

        publisherService.publishAlert("Alert!");
        assertFalse(subscriber.waitForElements());
    }

}
