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
package com.keecker.services.interfaces.projection

import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.keecker.services.interfaces.*
import com.keecker.services.interfaces.Constants.LOG_TAG
import com.keecker.services.interfaces.utils.CompletableFutureCompat
import com.keecker.services.interfaces.utils.IpcSubscriber
import com.keecker.services.interfaces.utils.asCompletableFuture
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.util.*

/**
 * Projector client interface for Kotlin.
 */
interface ProjectorCoroutineClient {

    suspend fun isApiAccessible() : Boolean

    /**
     * @param state Projector state parameters to change, null for the others.
     */
    suspend fun setState(projectorState: ProjectorState): Boolean

    /**
     * @return All the projector state parameters.
     */
    suspend fun getState(): ProjectorState?

    /**
     * @param listener Get notified when any of the projector parameters changes.
     */
    suspend fun subscribeToState(subscriber: IProjectorStateListener)

    /**
     * @param listener Stop to get about changes.
     */
    suspend fun unsubscribeToState(subscriber: IProjectorStateListener)
}

/**
 * Projector client interface for Java.
 */
interface ProjectorAsyncClient {
    fun setStateAsync(projectorState: ProjectorState) : CompletableFutureCompat<Boolean>
    fun getStateAsync() : CompletableFutureCompat<ProjectorState?>

    fun subscribeToStateAsync(subscriber: IProjectorStateListener) : CompletableFutureCompat<Unit>
    fun unsubscribeToStateAsync(subscriber: IProjectorStateListener) : CompletableFutureCompat<Unit>
}

/**
 * Gives access to the Projector Service:
 * - settings
 * - position
 * - power
 *
 * @param connection Connection bound to the Projector Service.
 */
class ProjectorClient(private val connection: PersistentServiceConnection<IProjectorService>,
                      private val apiChecker: ApiChecker) :
        ProjectorCoroutineClient,
        ProjectorAsyncClient {

    init {
        connection.onServiceConnected {
            if (stateSubscribers.size > 0) {
                it.subscribeToState(stateSubscriber)
            }
        }
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

    override suspend fun isApiAccessible() : Boolean {
        return when (apiChecker.isFeatureAvailable("PROJECTOR_ACCESS_STATE")) {
            FeatureAvailabilty.AVAILABLE -> true
            FeatureAvailabilty.NOT_ALLOWED -> {
                Log.e(LOG_TAG,
                        "API call not allowed, a required permission have not been granted")
                false}
            FeatureAvailabilty.NOT_AVAILABLE -> {
                val clientVersion = BuildConfig.VERSION_NAME
                val servicesVersion = apiChecker.getServicesVersion() ?: "Unknown"
                Log.e(LOG_TAG, "Unsupported API call, " +
                    "Services version: $servicesVersion, client version: $clientVersion")
                false}
        }
    }

    private val stateSubscribers = HashSet<IProjectorStateListener>()

    // Legacy subscriber, will be replaced by IProjectorStateListener
    // TODO(cyril) move to listener
    private val stateSubscriber = object : IpcSubscriber<ProjectorState>(ProjectorState::class.java) {
        override fun onNewMessage(msg: ProjectorState?) {
            for (listener in stateSubscribers) {
                listener.onUpdate(msg)
            }
        }
    }

    override suspend fun subscribeToState(listener: IProjectorStateListener) {
        if (!isApiAccessible()) return
        if (stateSubscribers.size == 0) {
            connection.execute { it.subscribeToState(stateSubscriber) }
        }
        stateSubscribers.add(listener)
    }

    override suspend fun unsubscribeToState(listener: IProjectorStateListener) {
        if (!isApiAccessible()) return
        stateSubscribers.remove(listener)
        if (stateSubscribers.size == 0) {
            connection.execute { it.unsubscribeToState(stateSubscriber) }
        }
    }

    override suspend fun setState(state: ProjectorState): Boolean {
        if (!isApiAccessible()) return false
        return connection.execute { it.setState(state) } ?: false
    }

    override suspend fun getState(): ProjectorState? {
        if (!isApiAccessible()) return null
        return connection.execute { it.getState() }
    }

    override fun setStateAsync(projectorState: ProjectorState) : CompletableFutureCompat<Boolean> {
        return GlobalScope.async {
            setState(projectorState)
        }.asCompletableFuture()
    }

    override fun getStateAsync() : CompletableFutureCompat<ProjectorState?> {
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
            unsubscribeToState(subscriber)
        }.asCompletableFuture()
    }
}