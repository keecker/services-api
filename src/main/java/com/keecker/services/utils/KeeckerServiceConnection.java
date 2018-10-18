/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Helper for aidl communication.
 * @hide
 */
public class KeeckerServiceConnection<T> {
    private static final String TAG = KeeckerServiceConnection.class.getSimpleName();
    public static final String AS_INTERFACE = "asInterface";
    public static final String STUB_CLASS_SUFFIX = "$Stub";

    // Variables that handle the max number of service disconnects before refusing to bind
    private static final int MAX_SERVICE_DISCONNECTS = 5;
    private static final long SERVICE_DISCONNECTS_TIMEOUT = 1000;   // in milliseconds
    private boolean mAutoReconnect = false;
    private long mLastConnection;
    private int mFailedConnections = 0;
    private boolean mRefuseToBind = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private final Method mAsInterfaceMethod;
    private CopyOnWriteArrayList<OnServiceConnectedListener<T>> mOnServiceConnectedListeners = new CopyOnWriteArrayList<>();
    private AtomicReference<ServiceConnectionListener> mServiceConnectionListener = new AtomicReference<>();
    private AtomicReference<IBinder> mBinderRef = new AtomicReference<>();
    protected final Context mContext;
    private String mPackageName = "";
    private String mIntentAction = "";
    private Intent mExplicitIntent = null;
    private ConcurrentLinkedQueue<AsyncBinderListener<T>> mAsyncListeners = new ConcurrentLinkedQueue<>();

    /**
     * If true, we will automatically rebind when a disconnect occurs
     * @param autoReconnect
     */
    public void setAutoReconnect(boolean autoReconnect) {
        mAutoReconnect = autoReconnect;
    }

    public void setServiceConnectionListener(ServiceConnectionListener serviceConnectionListener) {
        this.mServiceConnectionListener.set(serviceConnectionListener);
    }

    /**
     * OnServiceConnectedListener is called each time we have a new connection to the remote binder
     * Use this system to register your inter-process listeners !
     * @param listener
     */
    public void addOnServiceConnectedListener(OnServiceConnectedListener<T> listener) {
        if (listener == null) {
            throw new NullPointerException("Listener cannot be null");
        }
        mOnServiceConnectedListeners.add(listener);
    }

    public void removeOnServiceConnectedListener(OnServiceConnectedListener<T> listener) {
        if (listener == null) {
            throw new NullPointerException("Listener cannot be null");
        }
        mOnServiceConnectedListeners.remove(listener);
    }

    public static class Builder<T> {
        private final Class<T> mClass;
        private final Context mContext;
        public String mPackageName = null;
        public String mIntentAction = null;
        public Intent mExplicitIntent = null;
        public ServiceConnectionListener mServiceConnectionListener = null;

        public Builder(final Class<T> binderClass, final Context context) {
            mClass = binderClass;
            mContext = context;
            mPackageName = context.getPackageName();
        }

        public Builder<T> packageName(final String packageName) {
            mPackageName = packageName;
            return this;
        }

        public Builder<T> intentAction(final String intentAction) {
            mIntentAction = intentAction;
            return this;
        }

        public Builder<T> explicitIntent(final Intent explicitIntent) {
            mExplicitIntent = explicitIntent;
            return this;
        }

        public Builder<T> serviceConnectionListener(final ServiceConnectionListener
                                                             serviceConnectionListener) {
            mServiceConnectionListener = serviceConnectionListener;
            return this;
        }

        public KeeckerServiceConnection<T> build() {
            return new KeeckerServiceConnection<>(mClass, mContext, mIntentAction, mPackageName,
                    mExplicitIntent, mServiceConnectionListener);
        }

    }

    protected KeeckerServiceConnection(Class<T> aClass, final Context context,
                                     final String intentAction, String packageName) {
        this(aClass, context, intentAction, packageName, null, null);
    }

    private KeeckerServiceConnection(Class<T> aClass, final Context context,
                                     final String intentAction, String packageName,
                                     final Intent explicit, ServiceConnectionListener serviceConnectionListener) {

        if (aClass == null) {
            throw new IllegalArgumentException("binderClass cannot be null...");
        }
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null...");
        }

        if ((explicit == null && (packageName == null && intentAction == null)) ||
                (explicit != null && (intentAction != null)) ||
                (explicit == null && (packageName == null || intentAction == null))) {
            throw new IllegalArgumentException("You must give either packageName & " +
                    "intentAction or explicitIntent");
        }

        mContext = context;
        mIntentAction = intentAction;
        mPackageName = packageName;
        mExplicitIntent = explicit;
        mServiceConnectionListener.set(serviceConnectionListener);

        final String className = aClass.getCanonicalName() + STUB_CLASS_SUFFIX;
        try {
            mAsInterfaceMethod = Class.forName(className).getMethod(AS_INTERFACE,
                    IBinder.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(className + " does not have static method " +
                    AS_INTERFACE, e);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(aClass + " does not have inner class called " +
                    STUB_CLASS_SUFFIX, e);
        }
    }

    /**
     * Simply start the service without binding
     */
    public synchronized void startService() {
        mContext.startService(getExplicitIntent());
    }

    /**
     * Bind synchronously to service that has a receiver for impIntent inside packageName.
     * WARNINGS: throw RuntimeException if called from main thread as otherwise it would deadlock.
     * @return Binder or null
     */
     public synchronized T getBinder() throws ServiceConnectionException {
         T binder = peekBinder();
         if (binder != null) {
             return binder;
         }

         if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
             throw new ServiceConnectionException("This method cannot be called from Main Thread...");
         }

         final CountDownLatch latch = new CountDownLatch(1);
         getBinder(new AsyncBinderListener<T>() {
             @Override public void onBindSuccessful(T binder) { latch.countDown(); }
             @Override public void onBindError() { latch.countDown(); }
         });

         try {
             // We do not need a timeout apparently android handle it for us in the bind process
             latch.await();
         } catch (InterruptedException e) {
             throw new ServiceConnectionException("Interrupted");
         }

         binder = peekBinder();
         if (binder != null) {
             return binder;
         } else {
             throw new ServiceConnectionException("Cannot bind this service...");
         }
     }

    /**
     * Get binder asynchronously
     * @param listener
     */
    public synchronized void getBinder(AsyncBinderListener<T> listener) {
        // TODO: Check if there is already a binding request and call onBindError if we have no reply from android
        if (mRefuseToBind) {
            Log.e(TAG, String.format("Too many failed connections, refusing to bind to %s",
                    getExplicitIntent()));
            listener.onBindError();
            return;
        }
        try {
            T binder = peekBinder();
            if (binder != null) {
                listener.onBindSuccessful(binder);
                return;
            }
        } catch (ServiceConnectionException e) {
            listener.onBindError();
            return;
        }
        mAsyncListeners.add(listener);
        Intent serviceIntent = getExplicitIntent();
        if (serviceIntent == null) {
            Log.e(TAG, "Unable to resolve implicit intent for " +
                    mPackageName + " - " + mIntentAction);
            mAsyncListeners.poll().onBindError();
            return;
        }
        boolean willBind = this.mContext.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        if (!willBind) {
            mAsyncListeners.poll().onBindError();
            mAsyncListeners = null;
        }
    }

    /**
     * Peek the binder
     * @return the binder if it's valid or null if not
     */
    public synchronized T peekBinder() throws ServiceConnectionException {
        IBinder binder = mBinderRef.get();
        if (binder != null && binder.isBinderAlive()) {
            return castBinder(binder);
        }
        return null;
    }

    public void unlinkToDeath(IBinder.DeathRecipient deathRecipient) {
        final IBinder binder = mBinderRef.get();
        if (binder == null) {
            return;
        }
        try {
            binder.unlinkToDeath(deathRecipient, 0);
        } catch (Exception ignored) {}
    }

    /**
     * Unbind this connection
     */
    public synchronized void unbind() {
        try {
            IBinder binder = mBinderRef.get();
            if (mContext != null && binder != null && binder.isBinderAlive()) {
                mContext.unbindService(mServiceConnection);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public synchronized void bind() {
        getBinder(new AsyncBinderListener<T>() {
            @Override
            public void onBindSuccessful(T binder) {

            }

            @Override
            public void onBindError() {
                Intent intent = getExplicitIntent();
                Log.e(TAG, String.format("Failed to bind to %s",
                        intent == null ? "null" : intent.toString()));
            }
        });
    }

    private T castBinder(final IBinder binder) throws ServiceConnectionException {
        try {
            return (T) mAsInterfaceMethod.invoke(null, binder);
        } catch (Exception e) {
            throw new ServiceConnectionException("Unable to cast binder ?!", e);
        }
    }

    private Intent createExplicitFromImplicitIntent() {
        // Retrieve all services that can match the given intent
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(new Intent(mIntentAction), 0);
        ResolveInfo serviceInfo = null;
        // Make sure only one match was found
        if (resolveInfos == null || resolveInfos.size() <= 0) {
            Log.e(TAG, "No services resolved for " + mPackageName);
            return null;
        }

        for (ResolveInfo info : resolveInfos) {
            if (info.serviceInfo.packageName.equals(mPackageName)) {
                serviceInfo = info;
                break;
            }
        }

        if (serviceInfo == null) {
            return null;
        }

        // Get component info and create ComponentName
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(mIntentAction);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLastConnection = SystemClock.elapsedRealtime();
            mBinderRef.set(service);
            // Notify all stacked listeners
            AsyncBinderListener<T> listener;
            while ((listener = mAsyncListeners.poll()) != null) {
                try {
                    T casted = castBinder(service);
                    listener.onBindSuccessful(casted);
                } catch (ServiceConnectionException e) {
                    Log.e(TAG, e.getMessage(), e);
                    listener.onBindError();
                }
            }
            for (OnServiceConnectedListener<T> onConnectedListener : mOnServiceConnectedListeners) {
                try {
                    final T binder = castBinder(service); // This would throw is meanwhile we lose the connection
                    onConnectedListener.onConnected(binder);
                } catch (ServiceConnectionException ignored) {}
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (!mRefuseToBind) {
                long now = SystemClock.elapsedRealtime();
                Log.d(TAG, String.format("onServiceDisconnected, dT = %d", now - mLastConnection));
                if (now - mLastConnection < SERVICE_DISCONNECTS_TIMEOUT) {
                    mFailedConnections++;
                } else {
                    mFailedConnections = 0;
                }
                if (mFailedConnections == MAX_SERVICE_DISCONNECTS) {
                    mRefuseToBind = true;
                }
            }
            if (mAutoReconnect) {
                // TODO: Maybe add delay if we fail on succession instead of just refusing to bind
                mHandler.post(new Runnable() {
                    @Override public void run() {
                        bind();
                    }
                });
            }
            mBinderRef.set(null);
            ServiceConnectionListener listener = mServiceConnectionListener.get();
            if (listener != null) {
                listener.onDisconnect();
            }
        }
    };

    private Intent getExplicitIntent() {
        if (mExplicitIntent == null) {
            mExplicitIntent = createExplicitFromImplicitIntent();
        }
        return mExplicitIntent;
    }

    public interface AsyncBinderListener<T> {
        void onBindSuccessful(T binder);
        void onBindError();
    }

    public interface OnServiceConnectedListener<T> {
        void onConnected(T binder);
    }

    public interface ServiceConnectionListener {
        void onDisconnect();
    }

    public static class ServiceConnectionException extends Exception {
        private ServiceConnectionException(final String detailMessage, final Throwable throwable) {
            super(detailMessage, throwable);
        }

        public ServiceConnectionException(final String s) {
            super(s);
        }
    }
}
