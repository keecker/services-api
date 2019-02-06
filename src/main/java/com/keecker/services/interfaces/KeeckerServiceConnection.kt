/*
 * Copyright (C) 2018 KEECKER SAS (www.keecker.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Cyril Lugan on 2018-11-20.
 */

package com.keecker.services.interfaces

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.IInterface
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * To bind to a [android.app.Service] we need to provide:
 *
 * - An Intent, used by Android to resolve the Service it wants to bind to
 * - An AIDL Interface, implemented and exposed by the Service.
 *
 * @param <ServiceInterface> AIDL interface exposed by the Service
 */
interface ServiceBindingInfo<ServiceInterface : IInterface> {

    /**
     * @return an Intent used by Android to resolve the service we want to bind to
     */
    fun getIntent() : Intent

    /**
     * Converts the given [binder] to a [ServiceInterface] implementation.
     * Typically implemented as:
     * ```
     * return ServiceInterface.Stub.asInterface(binder)
     * ```
     *
     * @param binder returned by Android when binding to a Service
     * @return AIDL [ServiceInterface] implementation
     */
    fun toInterface(binder: IBinder) : ServiceInterface
}

/**
 * A Service connection that will attempt to connect forever.
 * Wraps
 *
 * We do not let the user to get a binder because exception can be hard to catch on every call
 */
interface PersistentServiceConnection<ServiceInterface: IInterface> {

    suspend fun <T> execute(lambda: (ServiceInterface) -> T) : T?

    /**
     * Unbind, will rebind when calling getBinder
     */
    fun unbind()

    /**
     * Called each time the service gets a new binder
     * Indicates this is the first time that this connection talks to this particular
     * service instance. It happens when you start the connection, and may happen again
     * if the remote process dies or the app gets updated.
     */
    fun onServiceConnected(lambda: (ServiceInterface) -> Unit)
}

/**
 * Notifies about [KeeckerServiceConnection] internal exceptions.
 */
interface RemoteExceptionListener {
    fun onRemoteException(e: Throwable)
}

/**
 * A [PersistentServiceConnection] wrapping AIDL calls to handle potential
 * IPC errors automatically.
 *
 * @param <ServiceInterface> AIDL interface exposed by the service you want to bind to.
 *
 * @param context Android context used to bind and rebind. It must be valid during the whole
 *                bind duration. In most of the cases you should give the Application Context.
 *
 * @param bindingInfo Information needed to bind to the service and use its AIDL interface.
 *
 * @param exceptionListener Notifies about internal exceptions, swallowed when retrying the call.
 *
 * @param bindingTimeoutMs Kills the current [EphemeralServiceConnection] if it did not try to
 *                         reconnect during this period of time (milliseconds). When a service
 *                         crashes multiple times, Android may wait a bit before attempting to
 *                         rebind. This can be more than 10 seconds.
 */
class KeeckerServiceConnection<ServiceInterface : IInterface>(
        val context: Context,
        val bindingInfo: ServiceBindingInfo<ServiceInterface>,
        val exceptionListener : RemoteExceptionListener? = null,
        val bindingTimeoutMs : Long = 60000) :
        PersistentServiceConnection<ServiceInterface> {

    /**
     * Listeners to be notified when binding to a new service instance.
     */
    private val onServiceConnectedCallacks = LinkedList<(ServiceInterface) -> Unit>()

    /**
     * - Wraps an AIDL call and retries it once if it failed.
     * - Lazily binds before executing the AIDL call.
     *
     * @return the result of the AIDL call, null if it failed two times
     */
    override suspend fun <T> execute(lambda: (ServiceInterface) -> T) : T? {
        Log.d("CONN", "execute")
        for (trial in 1 .. 2) {
            val binder = withTimeoutOrNull(bindingTimeoutMs) { connection.getBinder() }
            if (binder == null) {
                // we had a timeout, the connection is most likely dead,
                // rebind to make android try again
                connection.unbind()
                continue
            }
            // Do not block the coroutine with a blocking IO call
            Log.d("CONN", "Calling lambda before IO")
            return withContext(Dispatchers.IO) { execute(binder, lambda) } ?: continue
        }
        return null
    }

    private fun <T> execute(binder: ServiceInterface, lambda: (ServiceInterface) -> T) : T? {
        try {
            Log.d("CONN", "Just before invoke")
            return lambda.invoke(binder)
        } catch (e: Throwable) {
            Log.d("CONN", "Got an exception")
            // Catch all for now because AIDL exception handling can be tricky,
            // In addition to RemoteException, some but not all exceptions thrown by the
            // server will be propagated.
            // TODO(cyril) Document and test those cases
            exceptionListener?.onRemoteException(e)
        }
        return null
    }

    /**
     * - Notifies when binding to a new Service instance. May it be because it is the first time
     *   we connect to it, because it crashed or because we rebounded to the same instance.
     * - Called before exectuting any AIDL call that is triggered the bind
     * - Typically used to get the service to its expected state, like resubscribing to some
     *   data after a crash.
     */
    override fun onServiceConnected(lambda: (ServiceInterface) -> Unit) {
        onServiceConnectedCallacks.add(lambda)
    }

    override fun unbind() {
        connection.unbind()
    }

    private val connection = object : ServiceConnection {

        val bound = AtomicBoolean(false)

        fun bindIfNeeded() {
            synchronized(bound) {
                if (!bound.get()) {
                    bound.set(true)
                    context.bindService(bindingInfo.getIntent(), this, Context.BIND_AUTO_CREATE)
                }
            }
        }

        fun unbind() {
            synchronized(bound) {
                if (bound.get()) {
                    bound.set(false)
                    context.unbindService(this)
                }
            }
        }

        /**
         * Used to wait for [onServiceConnected] callbacks,
         * which may never happen if something went wrong when binding.
         */
        private val onConnectedEvents = LinkedBlockingQueue<ServiceInterface>()

        val binderMutex = Mutex()
        private var binderCached : ServiceInterface? = null

        /**
         * Returns a binder, waiting for the [onServiceConnected] callback if not already received.
         * This suspending function may never return if something went wrong when binding, it
         * is intended to be used with a timeout.
         *
         * @return a binder if the connection is alive, null if the connection is dead
         */
        suspend fun getBinder() : ServiceInterface {
            if (Looper.getMainLooper().thread == Thread.currentThread()) {
                // Waiting for a binder in the main thread would never return since the
                // onServiceConnected method is also called in the main thread.
                throw IllegalStateException("This method cannot be called from Main Thread.")
            }
            bindIfNeeded()
            binderMutex.withLock {
                val binder = binderCached
                return if (binder?.asBinder()?.isBinderAlive == true) {
                    binder
                } else {
                    val newBinder = withContext(Dispatchers.IO) {
                        onConnectedEvents.take()
                    }
                    binderCached = newBinder
                    newBinder
                }
            }
        }

        /**
         * Called when a connection to the Service has been established, with
         * the [IBinder] of the communication channel to the
         * Service.
         *
         * **Note:** If the system has started to bind your
         * client app to a service, it's possible that your app will never receive
         * this callback. Your app won't receive a callback if there's an issue with
         * the service, such as the service crashing while being created.
         *
         * @param name The concrete component name of the service that has
         * been connected.
         *
         * @param binder The IBinder of the Service's communication channel,
         * which you can now make calls on.
         */
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Log.d("CONN", "onServiceConnected")
            if (binder != null) {
                val service = bindingInfo.toInterface(binder)
                for (callback in onServiceConnectedCallacks) {
                    execute(service, callback)
                }
                onConnectedEvents.put(service)
            } else {
                onNullBinding()
            }
        }

        /**
         * Called when a connection to the Service has been lost.  This typically
         * happens when the process hosting the service has crashed or been killed.
         * This does *not* remove the ServiceConnection itself -- this
         * binding to the service will remain active, and you will receive a call
         * to [onServiceConnected] when the Service is next running.
         *
         * @param name The concrete component name of the service whose
         * connection has been lost.
         */
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("CONN", "onServiceDisconnected")
        }

        /**
         * *Note:* Available in API 26, called manually for now.
         *
         * This connection will not be usable, we have no use case of this with the Keecker Services.
         */
        private fun onNullBinding() {
            throw java.lang.IllegalStateException("Null binding unexpected on Keecker Services")
        }

        /**
         * *Note:* Since API 26. Not sure if it really gets called.
         *
         * Called when the binding to this connection is dead.  This means the
         * interface will never receive another connection.  The application will
         * need to unbind and rebind the connection to activate it again.  This may
         * happen, for example, if the application hosting the service it is bound to
         * has been updated.
         *
         * @param name The concrete component name of the service whose
         * connection is dead.
         */
        override fun onBindingDied(name: ComponentName?) {
            Log.d("CONN", "onBindingDied")
            unbind()
        }
    }
}