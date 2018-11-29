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
 * Created by Cyril Lugan <cyril@keecker.com> on 2018-11-20.
 */

@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.keecker.services.interfaces

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.IInterface
import android.os.Looper
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.withTimeoutOrNull
import java.util.*

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
 * Wraps an Android [android.content.ServiceConnection] to be used in coroutines.
 *
 * Like an Android ServiceConnection, it may attempt to bind again if the remote service crashed,
 * but it won't do it if dead or unbound.
 */
class EphemeralServiceConnection(val context: Context, val intent: Intent) :
        android.content.ServiceConnection {

    /**
     * A task that will be completed on the [onServiceConnected] callback,
     * which may never happen if something went wrong when binding.
     */
    private var bindingTask = CompletableDeferred<IBinder>()

    init {
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    /**
     * Called when a connection to the Service has been established, with
     * the [android.os.IBinder] of the communication channel to the
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
     * @param service The IBinder of the Service's communication channel,
     * which you can now make calls on.
     */
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (service != null) {
            bindingTask.complete(service)
        } else {
            onNullBinding()
        }
    }

    /**
     * Returns a binder, waiting for the [onServiceConnected] callback if not already received.
     * This suspending function can block forever if something went wrong when binding, it
     * is intended to be used with a timeout.
     *
     * @return a binder if the connection is alive, null if the connection is dead
     */
    suspend fun getBinder() : IBinder? {
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            throw IllegalStateException("This method cannot be called from Main Thread.")
        }
        return bindingTask.await()
    }

    /**
     * Called when a connection to the Service has been lost.  This typically
     * happens when the process hosting the service has crashed or been killed.
     * This does *not* remove the ServiceConnection itself -- this
     * binding to the service will remain active, and you will receive a call
     * to {@link #onServiceConnected} when the Service is next running.
     *
     * @param name The concrete component name of the service whose
     * connection has been lost.
     */
    override fun onServiceDisconnected(name: ComponentName?) {
        // TODO(cyril) check if a race condition is possible here
        // We are expecting another onServiceConnected
        bindingTask = CompletableDeferred()
    }

    /**
     * *Note:* Available in API 26, called manually for now.
     *
     * This connection will not be usable, we have no use case of this with the Keecker Services.
     */
    private fun onNullBinding() {
        unbind()
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
        unbind()
    }

    /**
     * Kills that connection, it will never attempt to bind again.
     */
    fun unbind() {
        context.unbindService(this)
    }
}

/**
 * A Service connection that will attempt to connect forever.
 * Wraps
 *
 * We do not let the user to get a binder because exception can be hard to catch on every call
 */
interface PersistentServiceConnection<ServiceInterface: IInterface> {

    // It porefered to execute your tuff here as it catches exceptions
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
    fun onNewServiceInstance(lambda: suspend (ServiceInterface) -> Unit)
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
     * Last used binder interface, null if it failed the last call
     */
    private var cachedBinderAsInterface : ServiceInterface? = null

    /**
     * Listeners to be notified when binding to a new service instance.
     */
    private val onNewServiceCallacks = LinkedList<suspend (ServiceInterface) -> Unit>()

    /**
     * - Wraps an AIDL call and retries it once if it failed.
     * - Lazily binds before executing the AIDL call.
     *
     * @return the result of the AIDL call, null if it failed two times
     */
    override suspend fun <T> execute(lambda: (ServiceInterface) -> T) : T? {
        for (i in 1 .. 2) {
            if (cachedBinderAsInterface?.asBinder()?.isBinderAlive != true) {
                cachedBinderAsInterface = getNewBinderAsInterface()
            }
            val binderAsInterface = cachedBinderAsInterface ?: continue

            try {
                // TODO(cyril) What happens if this blocks the coroutine?
                return lambda.invoke(binderAsInterface)
            } catch (e: Throwable) {
                // Catch all because AIDL exception handling can be tricky,
                // In addition to RemoteException, some but not all exceptions thrown by the
                // server will be propagated.
                // TODO(cyril) Document and test those cases
                cachedBinderAsInterface = null
                exceptionListener?.onRemoteException(e)
            }
        }
        return null
    }

    /**
     * Currently used Service Connection, alive or null
     */
    private var connection: EphemeralServiceConnection? = null

    /**
     * Get a binder from the current Service connection or create a new one.
     */
    private suspend fun getNewBinderAsInterface() : ServiceInterface? {
        for (i in 1 .. 2) {
            // Creates a new ephemeral connection if not initialized or dead
            if (connection == null) {
                connection = EphemeralServiceConnection(context, bindingInfo.getIntent())
            }
            val binder = withTimeoutOrNull(bindingTimeoutMs) { connection?.getBinder() }
            if (binder == null) {
                // Ephemeral connection is dead, it will not attempt to bind anymore.
                // Create a new one.
                unbind()
            } else {
                val binderAsInterface = bindingInfo.toInterface(binder)
                for (callback in onNewServiceCallacks) {
                    callback.invoke(binderAsInterface)
                }
                return binderAsInterface
            }
        }
        return null
    }

    /**
     * - Notifies when binding to a new Service instance. May it be because it is the first time
     *   we connect to it, or because it crashed.
     * - Does not notify when unbinding then rebinding to the same Service instance
     * - Called before exectuting any AIDL call that is triggered the bind
     * - Typically used to get the service to its expected state, like resubscribing to some
     *   data after a crash.
     */
    override fun onNewServiceInstance(lambda: suspend (ServiceInterface) -> Unit) {
        onNewServiceCallacks.add(lambda)
    }

    /**
     * Unbind to the service.
     */
    override fun unbind() {
        connection?.unbind()
        connection = null
    }
}