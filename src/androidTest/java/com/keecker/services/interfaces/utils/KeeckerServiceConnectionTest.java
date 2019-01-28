package com.keecker.services.interfaces.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.keecker.services.interfaces.utils.test.IAidlTest;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNotEquals;

/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
@RunWith(AndroidJUnit4.class)
public class KeeckerServiceConnectionTest {

    private static final String TAG = KeeckerServiceConnectionTest.class.getSimpleName();

    private static final String AIDL_OUTER_PROCESS
            = "com.keecker.services.interfaces.utils.test.BIND_OUTER_PROCESS";
    private static final String AIDL_INEER_PROCESS
            = "com.keecker.services.interfaces.utils.test.BIND_INNER_PROCESS";
    private static final String AIDL_SLEEPY_PROCESS
            = "com.keecker.services.interfaces.utils.test.BIND_SLEEPY_PROCESS";
    private static final String AIDL_CRASHY_PROCESS
            = "com.keecker.services.interfaces.utils.test.BIND_CRASHY_PROCESS";

    private Context mContext;

    @Before public void before() {
        mContext = InstrumentationRegistry.getContext();
    }

    @Test(timeout = 5000l)
    public void bindAidlInnerProcess()
            throws KeeckerServiceConnection.ServiceConnectionException, RemoteException {
        final KeeckerServiceConnection<IAidlTest> serviceConnection =
                new KeeckerServiceConnection.Builder<>(IAidlTest.class, mContext)
                        .intentAction(AIDL_INEER_PROCESS)
                        .build();
        try {
            final IAidlTest binder = serviceConnection.getBinder();
            assertEquals("Service should run in same process",
                    android.os.Process.myPid(), binder.getProcessId());
            assertNotNull(binder);
        } finally {
            serviceConnection.unbind();
        }
    }

    @Test(timeout = 5000l)
    public void asyncBindToAidlInnerProcess() throws Exception {
        final KeeckerServiceConnection<IAidlTest> serviceConnection =
                new KeeckerServiceConnection.Builder<>(IAidlTest.class, mContext)
                        .intentAction(AIDL_INEER_PROCESS)
                        .build();
        try {
            final BindListener<IAidlTest> bindListener = new BindListener<>();
            serviceConnection.getBinder(bindListener);
            final IAidlTest binder = bindListener.awaitBinder(3, TimeUnit.SECONDS);
            assertEquals("Service should run in same process",
                    android.os.Process.myPid(), binder.getProcessId());
            assertNotNull(binder);
        } finally {
            serviceConnection.unbind();
        }
    }

    @Test(timeout = 5000l)
    public void bindAidlOuterProcess() throws Exception {
        final KeeckerServiceConnection<IAidlTest> serviceConnection =
                new KeeckerServiceConnection.Builder<>(IAidlTest.class, mContext)
                        .intentAction(AIDL_OUTER_PROCESS)
                        .build();
        try {
            final IAidlTest binder = serviceConnection.getBinder();
            assertNotEquals("Service should run in another process",
                    android.os.Process.myPid(), binder.getProcessId());
            assertNotNull(binder);
        } finally {
            serviceConnection.unbind();
        }
    }

    @Test(timeout = 10000l)
    public void rebindToAidlOuterProcess() throws Exception {
        final ConnectionListener connectionListener = new ConnectionListener();
        final KeeckerServiceConnection<IAidlTest> serviceConnection =
                new KeeckerServiceConnection.Builder<>(IAidlTest.class, mContext)
                        .intentAction(AIDL_OUTER_PROCESS)
                        .serviceConnectionListener(connectionListener)
                        .build();

        try {
            final BindListener<IAidlTest> bindListener = new BindListener<>();
            serviceConnection.getBinder(bindListener);
            final IAidlTest binder = bindListener.awaitBinder(3, TimeUnit.SECONDS);
            assertNotNull(binder);
            assertNotEquals("Service should run in another process",
                    android.os.Process.myPid(), binder.getProcessId());
            try {
                binder.crash();
            } catch (Throwable ignored) {}
            connectionListener.awaitDisconnect(5, TimeUnit.SECONDS);
        } finally {
            serviceConnection.unbind();
        }
    }

    @Test(timeout = 2000l)
    public void bindWithWrongIntent() throws Exception {
        final KeeckerServiceConnection<IAidlTest> serviceConnection =
                new KeeckerServiceConnection.Builder<>(IAidlTest.class, mContext)
                        .intentAction("BLARG")
                        .build();
        try {
            serviceConnection.getBinder();
            try {
                fail("WTF, we successfully binded to an incorrect intent");
            } finally {
                serviceConnection.unbind();
            }
        } catch (KeeckerServiceConnection.ServiceConnectionException ignored) {}
    }

    @Test(timeout = 2000l)
    public void asyncBindWithWrongIntent() throws InterruptedException {
        final KeeckerServiceConnection<IAidlTest> serviceConnection =
                new KeeckerServiceConnection.Builder<>(IAidlTest.class, mContext)
                        .intentAction("BLARG")
                        .build();
        try {
            final BindListener<IAidlTest> bindListener = new BindListener<>();
            serviceConnection.getBinder(bindListener);
            bindListener.awaitError(1, TimeUnit.SECONDS);
        } finally {
            serviceConnection.unbind();
        }
    }

    @Test(timeout = 1000l)
    public void buildWithWrongClass() throws KeeckerServiceConnection.ServiceConnectionException {
        try {
            new KeeckerServiceConnection.Builder<>(Object.class, mContext)
                    .intentAction("BLARG")
                    .build();
            fail("WTF, we successfully build while specifying a wrong class");
        } catch (IllegalArgumentException ignored) {}
    }

    @Test(timeout = 10000l)
    public void multipleBinders() throws InterruptedException {
        int numConns = 10;
        final CountDownLatch latch = new CountDownLatch(numConns);
        List<KeeckerServiceConnection<IAidlTest>> connections = new ArrayList<>(numConns);
        for (int i = 0; i < numConns; i++) {
            connections.add(new KeeckerServiceConnection.Builder<>(IAidlTest.class, mContext)
                    .explicitIntent(new Intent(mContext, AidlInnerProcessService.class))
                    .build());
        }
        try {
            for (KeeckerServiceConnection<IAidlTest> conn : connections) {
                conn.getBinder(new KeeckerServiceConnection.AsyncBinderListener<IAidlTest>() {
                    @Override
                    public void onBindSuccessful(IAidlTest binder) {
                        latch.countDown();
                    }

                    @Override
                    public void onBindError() {
                        fail("onBindError");
                    }
                });
            }
            latch.await();
        } finally {
            for (KeeckerServiceConnection<IAidlTest> conn : connections) {
                conn.unbind();
            }
        }
    }

    @Test
    public void testRapidAsyncBinds() throws InterruptedException {
        final KeeckerServiceConnection<IAidlTest> serviceConnection =
                new KeeckerServiceConnection.Builder<>(IAidlTest.class, mContext)
                        .intentAction(AIDL_SLEEPY_PROCESS)
                        .build();
        final int numBinds = 10;
        final CountDownLatch countdownLatch = new CountDownLatch(numBinds);
        for (int i = 0; i < numBinds; i++) {
            serviceConnection.getBinder(new KeeckerServiceConnection.AsyncBinderListener<IAidlTest>() {
                @Override
                public void onBindSuccessful(IAidlTest binder) {
                    countdownLatch.countDown();
                }

                @Override
                public void onBindError() {

                }
            });
        }
        countdownLatch.await(5, TimeUnit.SECONDS);
        assertEquals("Not all callbacks have been called!", 0, countdownLatch.getCount());
    }

    @Test
    public void bindToCrashyProcess() throws InterruptedException {
        final KeeckerServiceConnection<IAidlTest> serviceConnection =
                new KeeckerServiceConnection.Builder<>(IAidlTest.class, mContext)
                        .intentAction(AIDL_CRASHY_PROCESS)
                        .build();
        final int numBinds = 10;
        final CountDownLatch successLatch = new CountDownLatch(5);
        final CountDownLatch errorLatch = new CountDownLatch(5);
        for (int i = 0; i < numBinds; i++) {
            serviceConnection.getBinder(new KeeckerServiceConnection.AsyncBinderListener<IAidlTest>() {
                @Override
                public void onBindSuccessful(IAidlTest binder) {
                    successLatch.countDown();
                }

                @Override
                public void onBindError() {
                    errorLatch.countDown();
                }
            });
            Thread.sleep(1000);
        }
        successLatch.await(10, TimeUnit.SECONDS);
        assertEquals("Not all success callbacks have been called!", 0, successLatch.getCount());
        errorLatch.await(10, TimeUnit.SECONDS);
        assertEquals("Not all error callbacks have been called!", 0, errorLatch.getCount());
    }

    @Test
    public void testServiceConnectionListener() throws InterruptedException {
        final int numBinds = 5;
        final CountDownLatch disconnectsLatch = new CountDownLatch(numBinds);
        final KeeckerServiceConnection<IAidlTest> serviceConnection =
                new KeeckerServiceConnection.Builder<>(IAidlTest.class, mContext)
                        .intentAction(AIDL_CRASHY_PROCESS)
                        .serviceConnectionListener(new KeeckerServiceConnection.ServiceConnectionListener() {
                            @Override
                            public void onDisconnect() {
                                disconnectsLatch.countDown();
                            }
                        })
                        .build();
        final CountDownLatch successLatch = new CountDownLatch(numBinds);
        for (int i = 0; i < numBinds; i++) {
            serviceConnection.getBinder(new KeeckerServiceConnection.AsyncBinderListener<IAidlTest>() {
                @Override
                public void onBindSuccessful(IAidlTest binder) {
                    successLatch.countDown();
                }

                @Override
                public void onBindError() {
                    fail("onBindError shouldn't have been called");
                }
            });
            Thread.sleep(1000);
        }
        successLatch.await(10, TimeUnit.SECONDS);
        assertEquals("Not all success callbacks have been called!", 0, successLatch.getCount());
        disconnectsLatch.await(10, TimeUnit.SECONDS);
        assertEquals("Not all disconnect callbacks have been called!", 0, disconnectsLatch.getCount());

    }

    private static class ConnectionListener
            implements KeeckerServiceConnection.ServiceConnectionListener {

        private final LinkedBlockingQueue<Object> mEvents = new LinkedBlockingQueue<>();

        public void awaitDisconnect(long timeout, TimeUnit unit) throws InterruptedException {
            mEvents.poll(timeout, unit);
        }

        @Override public void onDisconnect() {
            try {
                mEvents.put(new Object());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class BindListener<B>
            implements KeeckerServiceConnection.AsyncBinderListener<B> {

        private static class BindEvent<B> {
            public final B binder;

            public BindEvent(@NonNull B binder) {
                this.binder = binder;
            }
        }

        private static class BindError {}

        private final LinkedBlockingQueue<Object> mEvents = new LinkedBlockingQueue<>();

        @SuppressWarnings("unchecked")
        public B awaitBinder(long timeout, TimeUnit unit) throws InterruptedException {
            final Object poll = mEvents.poll(timeout, unit);
            if (poll == null) {
                throw new AssertionFailedError("Reached timeout while waiting for binder");
            } else if (!(poll instanceof BindEvent)) {
                throw new AssertionFailedError(
                        "Received event : " + poll.getClass() + " instead of BindEvent");
            } else {
                return (B) ((BindEvent) poll).binder;
            }
        }

        public void awaitError(long timeout, TimeUnit unit) throws InterruptedException {
            final Object poll = mEvents.poll(timeout, unit);
            if (poll == null) {
                throw new AssertionFailedError("Reached timeout while waiting for binder");
            } else if (!(poll instanceof BindError)) {
                throw new AssertionFailedError(
                        "Received event : " + poll.getClass() + " instead of BindError");
            }
        }

        @Override
        public void onBindSuccessful(B binder) {
            try {
                mEvents.put(new BindEvent<>(binder));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void onBindError() {
            try {
                mEvents.put(new BindError());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class AidlBinder extends IAidlTest.Stub {

        @Override public int getProcessId() throws RemoteException {
            return android.os.Process.myPid();
        }

        @Override public void crash() throws RemoteException {
            System.exit(42);
        }
    }

    public static class AidlInnerProcessService extends Service {

        private final IAidlTest.Stub mBinder = new AidlBinder();

        @Nullable @Override public IBinder onBind(Intent intent) {
            return mBinder;
        }
    }

    public static class AidlOuterProcessService extends Service {

        private final IAidlTest.Stub mBinder = new AidlBinder();

        @Nullable @Override public IBinder onBind(Intent intent) {
            return mBinder;
        }
    }

    public static class SleepyProcessService extends Service {
        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            return new AidlBinder();
        }
    }

    public static class CrashyProcessService extends Service {

        @Override
        public void onCreate() {
            super.onCreate();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    System.exit(42);
                }
            }, 500);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return new AidlBinder();
        }
    }
}
