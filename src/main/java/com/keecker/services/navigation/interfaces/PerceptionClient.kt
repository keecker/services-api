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
 * Created by Cyril Lugan on 2018-11-30.
 */
package com.keecker.services.navigation.interfaces

import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import com.keecker.services.interfaces.ApiChecker
import com.keecker.services.interfaces.PersistentServiceConnection
import com.keecker.services.interfaces.ServiceBindingInfo
import com.keecker.services.utils.CompletableFutureCompat
import com.keecker.services.utils.asCompletableFuture
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.util.*

interface PerceptionCoroutineClient {

    suspend fun subscribeToOdometry(subscriber: IOdometryListener)

    suspend fun unsubscribeToOdometry(subscriber: IOdometryListener)

    suspend fun subscribeToWallDetection(subscriber: IWallSegmentationListener)
    suspend fun unsubscribeToWallDetection(subscriber: IWallSegmentationListener)

    // TODO(cyril) check when does ot returns null
    suspend fun detectWall(): PlaneSegmentationResult?
}

interface PerceptionAsyncClient {
    fun subscribeToOdometryAsync(subscriber: IOdometryListener) : CompletableFutureCompat<Unit>
    fun unsubscribeToOdometryAsync(subscriber: IOdometryListener) : CompletableFutureCompat<Unit>

    fun subscribeToWallDetectionAsync(subscriber: IWallSegmentationListener):
            CompletableFutureCompat<Unit>
    fun unsubscribeToWallDetectionAsync(subscriber: IWallSegmentationListener):
            CompletableFutureCompat<Unit>

    fun detectWallAsync(): CompletableFutureCompat<PlaneSegmentationResult?>
}

class PerceptionClient(
        private val mvtPerceptionConnection: PersistentServiceConnection<IMovementPerceptionService>,
        private val perceptionConnection: PersistentServiceConnection<IPerceptionService>,
        private val apiChecker: ApiChecker
        ) :
        PerceptionCoroutineClient,
        PerceptionAsyncClient {

    init {
        mvtPerceptionConnection.onNewServiceInstance {
            for (subscriber in odometrySubscribers) {
                mvtPerceptionConnection.execute { it.unsubscribeToOdometry(subscriber) }
            }
        }
    }

    companion object {
        val mvtPerceptionBindingInfo = object : ServiceBindingInfo<IMovementPerceptionService> {
            override fun getIntent(): Intent {
                val intent = Intent("com.keecker.services.navigation.ACTION_BIND_MOVEMENT_PERCEPTION")
                intent.component = ComponentName(
                        "com.keecker.services",
                        "com.keecker.services.navigation.MovementPerceptionService")
                return intent
            }

            override fun toInterface(binder: IBinder): IMovementPerceptionService {
                return IMovementPerceptionService.Stub.asInterface(binder)
            }
        }
        val perceptionBindingInfo = object : ServiceBindingInfo<IPerceptionService> {
            override fun getIntent(): Intent {
                val intent = Intent("com.keecker.services.navigation.ACTION_BIND_PERCEPTION")
                intent.component = ComponentName(
                        "com.keecker.services",
                        "com.keecker.services.navigation.perception.PerceptionService")
                return intent
            }

            override fun toInterface(binder: IBinder): IPerceptionService {
                return IPerceptionService.Stub.asInterface(binder)
            }
        }
    }

    private val odometrySubscribers = HashSet<IOdometryListener>()

    override suspend fun subscribeToOdometry(listener: IOdometryListener) {
        mvtPerceptionConnection.execute { it.subscribeToOdometry(listener) }
        odometrySubscribers.add(listener)
    }

    override suspend fun unsubscribeToOdometry(listener: IOdometryListener) {
        odometrySubscribers.remove(listener)
        mvtPerceptionConnection.execute { it.unsubscribeToOdometry(listener) }
    }

    private val wallDetectionSubscribers = HashSet<IWallSegmentationListener>()

    override suspend fun subscribeToWallDetection(subscriber: IWallSegmentationListener) {
        perceptionConnection.execute { it.subscribeToWallSegmentation(subscriber) }
        wallDetectionSubscribers.add(subscriber)
    }

    override suspend fun unsubscribeToWallDetection(subscriber: IWallSegmentationListener) {
        wallDetectionSubscribers.remove(subscriber)
        perceptionConnection.execute { it.unsubscribeToWallSegmentation(subscriber) }
    }

    override suspend fun detectWall() : PlaneSegmentationResult? {
        val deferred = CompletableDeferred<PlaneSegmentationResult?>()
        val subscriber = object : IWallSegmentationListener.Stub() {
            override fun onWallSegmentation(segmentationResult: PlaneSegmentationResult?) {
                deferred.complete(segmentationResult)
            }
        }
        subscribeToWallDetection(subscriber)
        // TODO(cyril) use default connection timeout?
        deferred.await()
        unsubscribeToWallDetection(subscriber)
        return deferred.getCompleted()
    }

    override fun subscribeToOdometryAsync(subscriber: IOdometryListener):
            CompletableFutureCompat<Unit> {
        return GlobalScope.async {
            subscribeToOdometry(subscriber)
        }.asCompletableFuture()
    }

    override fun unsubscribeToOdometryAsync(subscriber: IOdometryListener):
            CompletableFutureCompat<Unit> {
        return GlobalScope.async {
            unsubscribeToOdometry(subscriber)
        }.asCompletableFuture()
    }

    override fun subscribeToWallDetectionAsync(subscriber: IWallSegmentationListener):
            CompletableFutureCompat<Unit> {
        return GlobalScope.async {
            subscribeToWallDetection(subscriber)
        }.asCompletableFuture()
    }

    override fun unsubscribeToWallDetectionAsync(subscriber: IWallSegmentationListener):
            CompletableFutureCompat<Unit> {
        return GlobalScope.async {
            unsubscribeToWallDetection(subscriber)
        }.asCompletableFuture()
    }

    override fun detectWallAsync(): CompletableFutureCompat<PlaneSegmentationResult?> {
        return GlobalScope.async {
            detectWall()
        }.asCompletableFuture()
    }
}