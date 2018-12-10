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
 * Created by Cyril Lugan <cyril@keecker.com> on 2018-12-06.
 */

package com.keecker.services.interfaces

import android.annotation.SuppressLint
import android.content.Context
import com.keecker.services.projection.interfaces.ProjectorClient

/**
 * Holds a static reference to an application context, which should not cause a leak.
 */
@SuppressLint("StaticFieldLeak")
object KeeckerServices {
    lateinit var applicationContext : Context

    private val projectorClient: ProjectorClient by lazy {
        ProjectorClient(KeeckerServiceConnection(applicationContext, ProjectorClient.bindingInfo), apiClient)
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

    private fun initApplicationContext(context: Context) {
        if (!::applicationContext.isInitialized) {
            applicationContext = context.applicationContext
        }
    }

    suspend fun getVersion(context: Context) : String? {
        initApplicationContext(context)
        return apiClient.getVersion()
    }
}