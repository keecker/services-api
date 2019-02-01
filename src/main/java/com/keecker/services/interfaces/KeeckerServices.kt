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
 * Created by Cyril Lugan on 2018-12-06.
 */

package com.keecker.services.interfaces

import android.annotation.SuppressLint
import android.content.Context
import com.keecker.services.interfaces.navigation.PerceptionClient
import com.keecker.services.interfaces.projection.ProjectorClient

/*
 * Android will warn us about storing an arbitrary Context in a static field. If it were
 * an activity Context for instance, this would create an infinite reference to that activity,
 * causing a leak.
 *
 * To prevent this we store the application context which is tied to the life cycle of the whole
 * application. The warning still has to be disabled manually.
 */
@SuppressLint("StaticFieldLeak")

/**
 * Main entry point to get Keecker Service Clients
 */
object KeeckerServices {
    lateinit var applicationContext : Context

    private val projectorClient: ProjectorClient by lazy {
        ProjectorClient(
                KeeckerServiceConnection(applicationContext, ProjectorClient.bindingInfo),
                apiClient)
    }

    private val perceptionClient: PerceptionClient by lazy {
        PerceptionClient(
                KeeckerServiceConnection(applicationContext, PerceptionClient.mvtPerceptionBindingInfo),
                KeeckerServiceConnection(applicationContext, PerceptionClient.perceptionBindingInfo),
                apiClient)
    }

    private val apiClient : ApiClient by lazy {
        ApiClient(
                KeeckerServiceConnection(applicationContext, ApiClient.bindingInfo),
                applicationContext)
    }

    @JvmStatic
    fun getProjectorClient(context: Context) : ProjectorClient {
        initApplicationContext(context)
        return projectorClient
    }

    @JvmStatic
    fun getPerceptionClient(context: Context) : PerceptionClient {
        initApplicationContext(context)
        return perceptionClient
    }

    private fun initApplicationContext(context: Context) {
        if (!::applicationContext.isInitialized) {
            applicationContext = context.applicationContext
        }
    }

    suspend fun getVersion(context: Context) : String? {
        initApplicationContext(context)
        return apiClient.getServicesVersion()
    }
}