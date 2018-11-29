/* Copyright (C) 2018 KEECKER SAS (www.keecker.com)
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
 */
@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")
package com.keecker.services.projection.interfaces

import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import com.keecker.services.interfaces.PersistentServiceConnection
import com.keecker.services.interfaces.ServiceBindingInfo
import com.keecker.services.utils.CompletableFutureCompat
import com.keecker.services.utils.IpcSubscriber
import com.keecker.services.utils.asCompletableFuture
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import java.util.*

interface ProjectorCoroutineClient {
    suspend fun setState(projectorState: ProjectorState): Boolean
    suspend fun getState(): ProjectorState

    suspend fun subscribeToState(subscriber: IProjectorStateListener): Unit
    suspend fun unsubscribeToState(subscriber: IProjectorStateListener): Unit
}

interface ProjectorAsyncClient {
    fun setStateAsync(projectorState: ProjectorState) : CompletableFutureCompat<Boolean>
    fun getStateAsync() : CompletableFutureCompat<ProjectorState>

    fun subscribeToStateAsync(subscriber: IProjectorStateListener) : CompletableFutureCompat<Unit>
    fun unsubscribeToStateAsync(subscriber: IProjectorStateListener) : CompletableFutureCompat<Unit>
}

class ProjectorClient(val connection: PersistentServiceConnection<IProjectorService>) :
        ProjectorCoroutineClient,
        ProjectorAsyncClient {

    init {
        connection.onNewServiceInstance { onReconnect() }
    }

    companion object {
        val bindingInfo = object : ServiceBindingInfo<IProjectorService> {
            override fun getIntent(): Intent {
                val intent = Intent("com.keecker.services.ACTION_BIND_KEECKER_PROJECTOR")
                intent.component = ComponentName(
                        "com.keecker.services",
                        "com.keecker.services.projection.ProjectorService")
                return intent
            }

            override fun toInterface(binder: IBinder): IProjectorService {
                return IProjectorService.Stub.asInterface(binder)
            }
        }
    }

    val stateSubscribers = HashSet<IProjectorStateListener>()

    // Legacy subscriber, will be replaced by IProjectorStateListener
    val stateSubscriber = object : IpcSubscriber<ProjectorState>(ProjectorState::class.java) {
        override fun onNewMessage(msg: ProjectorState?) {
            for (listener in stateSubscribers) {
                listener.onUpdate(msg)
            }
        }
    }

    override suspend fun subscribeToState(listener: IProjectorStateListener) {
        if (stateSubscribers.size == 0) {
            connection.execute { it.subscribeToState(stateSubscriber) }
        }
        stateSubscribers.add(listener)
    }

    override suspend fun unsubscribeToState(listener: IProjectorStateListener) {
        stateSubscribers.remove(listener)
        if (stateSubscribers.size == 0) {
            connection.execute { it.unsubscribeToState(stateSubscriber) }
        }
    }

    private suspend fun onReconnect() {
        if (stateSubscribers.size > 0) {
            connection.execute { it.subscribeToState(stateSubscriber) }
        }
    }

    override suspend fun setState(state: ProjectorState): Boolean {
        return connection.execute { it.setState(state) } ?: false
    }

    override suspend fun getState(): ProjectorState {
        while (true) {
            return connection.execute { it.getState() } ?: continue
        }
    }

    override fun setStateAsync(projectorState: ProjectorState) : CompletableFutureCompat<Boolean> {
        return GlobalScope.async {
            setState(projectorState)
        }.asCompletableFuture()
    }

    override fun getStateAsync() : CompletableFutureCompat<ProjectorState> {
        return GlobalScope.async {
            getState()
        }.asCompletableFuture()
    }

    override fun subscribeToStateAsync(subscriber: IProjectorStateListener): CompletableFutureCompat<Unit> {
        return GlobalScope.async {
            subscribeToState(subscriber)
        }.asCompletableFuture()
    }

    override fun unsubscribeToStateAsync(subscriber: IProjectorStateListener): CompletableFutureCompat<Unit> {
        return GlobalScope.async {
            subscribeToState(subscriber)
        }.asCompletableFuture()
    }

    /**
     * Temporarily unbind subscribers will be re subscribed on reconnect.
     */
    fun unbind() {
        connection.unbind()
    }
}