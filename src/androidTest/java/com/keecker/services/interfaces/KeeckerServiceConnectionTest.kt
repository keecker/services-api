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
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Created by Cyril Lugan on 2018-11-28.
 */

package com.keecker.services.interfaces

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.keecker.services.interfaces.projection.IProjectorService
import com.keecker.services.interfaces.test.ITypicalService
import com.keecker.services.interfaces.test.TickListener
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import java.lang.IllegalStateException

/**
 * Tests Android IPC communications, handled by the [KeeckerServiceConnection] helper.
 */
@RunWith(AndroidJUnit4::class)
class KeeckerServiceConnectionTest {

   /**
    * In most of the cases, when binding to a Keecker Service, it runs in another process.
    * It is also located in another app, requiring to use an intent filter.
    */
    private val outerProcessBindingInfo = object : ServiceBindingInfo<ITypicalService> {
        override fun getIntent(): Intent {
            val intent = Intent("com.keecker.services.interfaces.test.BIND_TYPICAL_SERVICE")
            intent.component = ComponentName(
                   "com.keecker.services.interfaces.test",
                    "com.keecker.services.interfaces.TypicalServiceInAnotherProcess")
            return intent
        }

        override fun toInterface(binder: IBinder): ITypicalService {
            return ITypicalService.Stub.asInterface(binder)
        }
    }

    /**
     * Binding
     */
    private val innerProcessBindingInfo = object : ServiceBindingInfo<ITypicalService> {
        override fun getIntent(): Intent {
            return Intent(context, TypicalServiceInSameProcess::class.java)
        }

        override fun toInterface(binder: IBinder): ITypicalService {
            return ITypicalService.Stub.asInterface(binder)
        }
    }

    // We are expected to exceed this, but some tests are pretty long
    @get:Rule
    val globalTimeout = Timeout.seconds(120)

    lateinit var context : Context

    @Before
    fun before() {
        context = InstrumentationRegistry.getContext().applicationContext
    }

    @Test
    fun bindsWithAServiceInTheSameProcess() = runBlocking<Unit> {
        val connection = KeeckerServiceConnection(context, innerProcessBindingInfo)
        val pid = connection.execute { service -> service.getProcessId() }
        assertEquals("Service should run in the same process",
                android.os.Process.myPid(), pid)
    }

    @Test
    fun bindsWithAServiceInInAnotherProcess() = runBlocking<Unit> {
        val connection = KeeckerServiceConnection(context, outerProcessBindingInfo)
        val pid = connection.execute { service -> service.getProcessId() }
        assertNotEquals("Service should run in another process",
                android.os.Process.myPid(), pid)
        connection.unbind()
    }

    @Test
    fun lazilyBindsWhenExecutingSomething() = runBlocking<Unit> {
        val connection = KeeckerServiceConnection(context, outerProcessBindingInfo)
        // Count how many times are we notified about this new service instance
        var servicesInstances = 0
        connection.onServiceConnected { servicesInstances++ }
        // We are still not bound
        assertEquals(0, servicesInstances)
        // Asks something to the service, actually binding to it
        connection.execute { service -> service.getProcessId() }
        // As it is the first time this KeeckerServiceConnection interacts with this service,
        // we should have been notified once
        assertEquals(1, servicesInstances)
    }

    // TODO this actually blocks, check why
    @Ignore
    @Test
    fun isNotStuckByABlockingCall() = runBlocking<Unit> {
        val connection = KeeckerServiceConnection(context, outerProcessBindingInfo)
        // Count how many times are we notified about this new service instance
        val job = async { connection.execute { service -> service.freeze() }}
        delay(1000)
        // Should not timeout even if the preceeding call blocks
        connection.execute { service -> service.processId }
        job.cancel()
    }

    @Test
    fun notifiesAboutNewServiceInstanceAfterACrash() = runBlocking<Unit> {
        val connection = KeeckerServiceConnection(context, outerProcessBindingInfo)
        // Count how many times are we notified about new service instances
        var servicesInstances = 0
        connection.onServiceConnected { servicesInstances++ }
        // Asks the service to crash
        connection.execute { service -> service.crash() }
        // We actually bound to it
        val trials = servicesInstances
        // When asking for something else
        connection.execute { service -> service.getProcessId() }
        // When should have connected to a new instance
        assertEquals(trials + 1, servicesInstances)
    }

    @Test
    fun returnsNullIfTheRemoteServiceKeepsCrashing() = runBlocking<Unit> {
        val connection = KeeckerServiceConnection(context, outerProcessBindingInfo)
        // We will never be able to get something back from this call,
        // the connection should give up at some point.
        assertNull(connection.execute { service -> service.crash() })
    }

    @Test
    fun retriesACrashingCallOnce() = runBlocking<Unit> {
        val connection = KeeckerServiceConnection(context, outerProcessBindingInfo)
        // Count how many times are we notified about new service instances
        var trials = 0
        connection.onServiceConnected { trials++ }
        // Asks the service to crash
        connection.execute { service -> service.crash() }
        // We actually bound to it
        //assertEquals(1, servicesInstances)
        // When should have been bound to two services instance before giving up
        assertEquals(2, trials)
    }

    @Test
    fun aCrashingCallDoesNotDisruptTheNext() = runBlocking<Unit> {
        val connection = KeeckerServiceConnection(context, outerProcessBindingInfo)
        // We will never be able to get something back from this call,
        // the connection should give up at some point.
        assertNull(connection.execute { service -> service.crash() })
        // Whatever happened with the previous binder, the next call is expected to succeed
        assertNotNull( connection.execute { it.getProcessId() })
    }

    @Test(expected = IllegalStateException::class)
    fun blockingTheMainThreadRaisesAnException() = runBlocking<Unit> {
        val connection = KeeckerServiceConnection(context, outerProcessBindingInfo)
        GlobalScope.async(Dispatchers.Main) { // launch coroutine in the main thread
            connection.execute { it.processId }
        }.await()
    }

    // Use a callback interface to get data from the service without polling
    @Test
    fun clientCanSubscribeWithAnAidlListener() = runBlocking<Unit> {
        // Setup a channel that will be used to get messages
        val incommingMessages = Channel<Int>()
        val listener = object : TickListener.Stub() {
            override fun onNewSecond(msg: Int) {
                incommingMessages.sendBlocking(msg)
            }
        }
        val connection = KeeckerServiceConnection(context, outerProcessBindingInfo)
        // Tells the service that we want to get notified about messages
        connection.execute { it.subscribeToTicks(listener) }
        // Fail with a timeout if we don't receive another one
        incommingMessages.receive()
        // When no longer needing to be notified, you should unsubscribe
        connection.execute { it.unsubscribeToTicks(listener) }
        // Empty the list
        while (!incommingMessages.isEmpty) { incommingMessages.receive() }
        // We should no longer receive messages
        delay(5000)
        assertTrue(incommingMessages.isEmpty)
    }

    // When a client is no longer bound to a service, callback interfaces used to subscribe
    // to messages are still active. If this is not desirable, the client should unsubscribe
    // when unbinding.
    @Test
    fun clientSubscriberReceivesMessagesWhenUnboundingConnection() = runBlocking<Unit> {
        // Starts the service, so it keeps running when disconnecting
        context.startService(outerProcessBindingInfo.getIntent())
        // Setup a channel that will be used to get messages
        val incommingMessages = Channel<Int>()
        val listener = object : TickListener.Stub() {
            override fun onNewSecond(msg: Int) {
                incommingMessages.sendBlocking(msg)
            }
        }
        val connection = KeeckerServiceConnection(context, outerProcessBindingInfo)
        connection.execute { it.subscribeToTicks(listener) }
        // Even when unbound, subscribers continues to receive messages. The service
        // is still bound to the TickListener interface.
        connection.unbind()
        // Empty the list
        while (!incommingMessages.isEmpty) { incommingMessages.receive() }
        // Fail with a timeout if we don't receive another one
        incommingMessages.receive()
    }

    // Use PersistentServiceConnection onServiceConnected() to resubscribe automatically
    @Test
    fun clientsCanResubscribeAutomatically() = runBlocking<Unit> {
        // Starts the service, so it keeps running when disconnecting
        context.startService(outerProcessBindingInfo.getIntent())
        // Setup a channel that will be used to get messages
        val incommingMessages = Channel<Int>()
        val listener = object : TickListener.Stub() {
            override fun onNewSecond(msg: Int) {
                incommingMessages.sendBlocking(msg)
            }
        }
        val connection = KeeckerServiceConnection(context, outerProcessBindingInfo)
        // A client typically uses the new instance callback to resubscribe automatically
        connection.onServiceConnected( {it.subscribeToTicks(listener)})
        connection.execute { it.subscribeToTicks(listener) }
        connection.execute { it.crashAfterTheCall() }
        // Wait for the service to crash
        delay(5000)
        // Empty the received ticks
        while (!incommingMessages.isEmpty) { incommingMessages.receive() }
        // Fail with a timeout if we don't receive another one
        incommingMessages.receive()
    }

    /*
     * Corner cases that may not be properly handled
     */

    // This happens when not on Keecker
    // TODO(cyril) Check id there is a way to handle this better
    @Ignore
    @Test
    fun wrongBindingComponentSilentlyFails() = runBlocking<Unit> {
        val badBindingIntentInfo = object : ServiceBindingInfo<ITypicalService> {
            override fun getIntent(): Intent {
                val intent = outerProcessBindingInfo.getIntent()
                intent.component = ComponentName(
                   "com.keecker.services.interfaces.test",
                    "com.keecker.services.interfaces.utils.KeeckerServiceConnectionTest\$UnexistingService")
                return intent
            }
            override fun toInterface(binder: IBinder): ITypicalService {
                return outerProcessBindingInfo.toInterface(binder)
            }
        }
        // Lower the default binding timeout to pass this test faster
        val connection = KeeckerServiceConnection(
                context, badBindingIntentInfo, bindingTimeoutMs = 10000)
        assertNull(connection.execute { it.getProcessId() })
    }

    @Ignore
    @Test
    fun wrongBindingInterfaceSilentlyFails() = runBlocking<Unit> {
        val badBindingIntentInfo = object : ServiceBindingInfo<IProjectorService> {
            override fun getIntent(): Intent {
                return outerProcessBindingInfo.getIntent()
            }
            override fun toInterface(binder: IBinder): IProjectorService {
                return IProjectorService.Stub.asInterface(binder)
            }
        }
        val connection = KeeckerServiceConnection(context, badBindingIntentInfo)
        connection.execute { it.getTemperature() }
    }

    // TODO(cyril) Crash on bind, with retries

    // TODO(cyril) SecurityException When not allowed to bind / calling a private method
}