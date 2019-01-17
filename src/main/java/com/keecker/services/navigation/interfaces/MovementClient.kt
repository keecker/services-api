/*
 * Copyright (C) 2019 KEECKER SAS (www.keecker.com)
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
 * Created by Cyril Lugan on 2019-01-17.
 */

package com.keecker.services.navigation.interfaces

import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import com.keecker.services.interfaces.ApiChecker
import com.keecker.services.interfaces.PersistentServiceConnection
import com.keecker.services.interfaces.ServiceBindingInfo
import com.keecker.services.utils.CompletableFutureCompat
import com.keecker.services.utils.IpcSubscriber
import com.keecker.services.utils.asCompletableFuture
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

interface MovementCoroutineClient {

    /**
     * @param x distance in meters on the x axis (front)
     * @param y distance in meters on the y axis (right)
     * @return A boolean indicating if the go to succeeded
     */
    suspend fun goToRelative(x: Double, y: Double, theta: Double): Boolean
}

interface MovementAsyncClient {
    fun goToRelativeAsync(x: Double, y: Double, th: Double) : CompletableFutureCompat<Boolean>
}

class MovementClient(
        private val mvtPlannerConnection: PersistentServiceConnection<IMovementPlannerService>,
        private val apiChecker: ApiChecker
        ) :
        MovementCoroutineClient,
        MovementAsyncClient {

    companion object {
        val mvtPlannerBindingInfo = object : ServiceBindingInfo<IMovementPlannerService> {
            override fun getIntent(): Intent {
                val intent = Intent("com.keecker.services.navigation.ACTION_BIND_MOVEMENT_PLANNER")
                intent.component = ComponentName(
                        "com.keecker.services",
                        "com.keecker.services.navigation.MovementPlannerService")
                return intent
            }

            override fun toInterface(binder: IBinder): IMovementPlannerService {
                return IMovementPlannerService.Stub.asInterface(binder)
            }
        }
    }

    override suspend fun goToRelative(x: Double, y: Double, theta: Double): Boolean {
        val deffered = CompletableDeferred<Boolean>()
        val subscriber = object : IpcSubscriber<RelativeGoToStatus>(RelativeGoToStatus::class.java!!, 10) {
            override fun onNewMessage(msg: RelativeGoToStatus) {
                deffered.complete(msg.success)
            }
        }
        mvtPlannerConnection.execute { it.goToRelative(x, y, theta, Double.NaN, Double.NaN, subscriber) }
        return deffered.await()
    }

    override fun goToRelativeAsync(x: Double, y: Double, th: Double) : CompletableFutureCompat<Boolean> {
        return GlobalScope.async {
            goToRelative(x, y, th)
        }.asCompletableFuture()
    }
 }
