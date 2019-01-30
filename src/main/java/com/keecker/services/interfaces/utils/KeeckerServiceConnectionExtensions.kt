/*
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.keecker.services.interfaces.utils

import android.os.IBinder
import android.os.IInterface
import android.os.RemoteCallbackList
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

sealed class Result<Type, Error>
data class Success<Type, Error>(val value: Type): Result<Type, Error>()
data class Failure<Type, Error>(val error: Error): Result<Type, Error>()

/**
 * Wraps your lambda to catch potential IPC error.
 * If you don't use the error, prefer [silentExecute].
 * If you don't have a returned value, prefer [executeUnit].
 * @return A [Success] with the return value of the lambda if no error occurred or a [Failure]
 * with the ipc error.
 */
inline fun <Response, Binder> KeeckerServiceConnection<Binder>.execute(lambda: (Binder) -> Response): Result<Response, Throwable> {
    return try {
        Success(lambda(binder))
    } catch (e: Throwable) {
        Log.e(KeeckerServiceConnection.TAG, "IPC Error", e)
        Failure(e)
    }
}

/**
 * Wraps your lambda to catch potential IPC error.
 * If you want to access the ipc error, prefer [execute].
 * If you don't have a returned value, prefer [executeUnit].
 * @return The return type of the lambda or null if an error occurred.
 */
inline fun <Response, Binder> KeeckerServiceConnection<Binder>.silentExecute(lambda: (Binder) -> Response): Response? {
    return try {
        lambda(binder)
    } catch (e: Throwable) {
        Log.e(KeeckerServiceConnection.TAG, "IPC Error", e)
        null
    }
}

/**
 * Wraps your lambda to catch potential IPC error.
 * @return The return type of the lambda or null if an error occurred.
 */
inline fun <Binder> KeeckerServiceConnection<Binder>.executeUnit(lambda: (Binder) -> Unit): Throwable? {
    return try {
        lambda(binder)
        null
    } catch (e: Throwable) {
        Log.e(KeeckerServiceConnection.TAG, "IPC Error", e)
        e
    }
}

/**
 * Wraps [KeeckerServiceConnection] to add suspendable logic
 */
class SuspendableServiceConnection<Binder>(private val serviceConnection: KeeckerServiceConnection<Binder>) {

    /**
     * Binds to the service and return the binder using a Channel to suspend while waiting for the binder.
     * You should not use this directly and prefer using [execute], [silentExecute] and [executeUnit]
     */
    @Deprecated("Do not use directly, prefer execute or silentExecute")
    suspend fun bind(): Binder? {
        // TODO: Maybe add a timeout ?
        val binderChannel = Channel<Binder?>(1)
        serviceConnection.getBinder(object : KeeckerServiceConnection.AsyncBinderListener<Binder> {
            override fun onBindSuccessful(binder: Binder) {
                GlobalScope.launch { binderChannel.send(binder) }
            }

            override fun onBindError() {
                GlobalScope.launch { binderChannel.send(null) }
            }
        })
        return binderChannel.receive()
    }

    /**
     * Wraps your lambda to catch potential IPC error.
     * If you don't us the error, prefer [silentExecute].
     * If you don't have a returned value, prefer [executeUnit].
     * @return A [Success] with the return value of the lambda if no error occurred or a [Failure]
     * with the ipc error.
     */
    suspend inline fun <Response> execute(lambda: (Binder) -> Response): Result<Response, Throwable> {
        return try {
            @Suppress("DEPRECATION")
            val binder = bind() ?: return Failure(KeeckerServiceConnection.ServiceConnectionException("Service crashed"))
            Success(lambda(binder))
        } catch (e: Throwable) {
            Log.e(KeeckerServiceConnection.TAG, "IPC Error", e)
            Failure(e)
        }
    }

    /**
     * Wraps your lambda to catch potential IPC error.
     * If you want to access the ipc error, prefer [execute].
     * If you don't have a returned value, prefer [executeUnit].
     * @return The return type of the lambda or null if an error occurred.
     */
    suspend inline fun <Response> silentExecute(lambda: (Binder) -> Response): Response? {
        return try {
            @Suppress("DEPRECATION")
            val binder = bind() ?: return null
            lambda(binder)
        } catch (e: Throwable) {
            Log.e(KeeckerServiceConnection.TAG, "IPC Error", e)
            null
        }
    }

    /**
     * Wraps your lambda to catch potential IPC error.
     * @return The return type of the lambda or null if an error occurred.
     */
    suspend inline fun executeUnit(lambda: (Binder) -> Unit): Throwable? {
        return try {
            @Suppress("DEPRECATION")
            val binder = bind() ?: return KeeckerServiceConnection.ServiceConnectionException("Service crashed")
            lambda(binder)
            null
        } catch (e: Throwable) {
            Log.e(KeeckerServiceConnection.TAG, "IPC Error", e)
            e
        }
    }
}

val <Binder> KeeckerServiceConnection<Binder>.suspendable: SuspendableServiceConnection<Binder>
    get() = SuspendableServiceConnection(this)

/**
 * A Death recipient which offer the failure value to the given Channel.
 */
class ChannelDeathRecipient<Element>(private val completionChannel: Channel<Element>, private val failureValue: Element): IBinder.DeathRecipient {

    private var binder: IBinder? = null

    override fun binderDied() {
        GlobalScope.launch {
            try {
                completionChannel.send(failureValue)
                completionChannel.close()
            } catch (e: Exception) {
                Log.e(KeeckerServiceConnection.TAG, "IPC Error", e)
            }
        }
    }

    fun linkTo(binder: IBinder) {
        this.binder = binder
        binder.linkToDeath(this, 0)
    }

    fun unlink() {
        try {
            binder?.unlinkToDeath(this, 0)
        } catch (ignored: Exception) {}
    }
}

/**
 * A wrapper for RemoteCallbackList that can call a lambda when the first subscriber is registered
 * or when the last subscriber in the list is unregistered
 */
class ListenerList<E: IInterface>(private val firstSubscriber: (() -> Unit)? = null,
                                  private val lastUnsubscriber: (() -> Unit)? = null)
    : RemoteCallbackList<E>() {

    override fun register(callback: E): Boolean {
        if (super.register(callback)) {
            if (this.registeredCallbackCount == 1) {
                firstSubscriber?.invoke()
            }
            return true
        }
        return false
    }
    override fun unregister(callback: E) : Boolean {
        if (super.unregister(callback)) {
            if (this.registeredCallbackCount == 0) {
                lastUnsubscriber?.invoke()
            }
            return true
        }
        return  false
    }
    override fun onCallbackDied(callback: E) {
        if (this.registeredCallbackCount == 0) {
            lastUnsubscriber?.invoke()
        }
        super.onCallbackDied(callback)
    }
}
