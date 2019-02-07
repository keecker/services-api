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
 * Created by Cyril Lugan on 2018-12-05.
 */

package com.keecker.services.interfaces

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.keecker.services.interfaces.Constants.LOG_TAG
import kotlin.collections.HashMap

enum class FeatureAvailabilty {AVAILABLE, NOT_AVAILABLE, NOT_ALLOWED}

interface ApiChecker {
    suspend fun getServicesVersion() : String?
    fun isRunningOnKeecker() : Boolean
    suspend fun isFeatureAvailable(feature: String) : FeatureAvailabilty
}

class ApiClient(
        private val connection: PersistentServiceConnection<IApiService>,
        private val supportedFeatures: Map<String, Set<String>>,
        private val isPermissionGranted: (String) -> Boolean
) : ApiChecker {

    companion object {
        val bindingInfo = object : ServiceBindingInfo<IApiService> {
            override fun getIntent(): Intent {
                val intent = Intent("com.keecker.services.interfaces.ACTION_BIND_API_SERVICE")
                intent.component = ComponentName(
                        "com.keecker.services",
                        "com.keecker.services.interfaces.ApiService")
                return intent
            }

            override fun toInterface(binder: IBinder): IApiService {
                return IApiService.Stub.asInterface(binder)
            }
        }

        const val INFO_VERSION = "version"
        const val INFO_FEATURES = "features"
    }

    init {
        connection.onServiceConnected {
            // Invalidate cached info on reconnect, in case services were updated
            availableFeatures.clear()
            servicesVersion = null
        }
    }

    override fun isRunningOnKeecker(): Boolean {
        return Build.BRAND == "Keecker"
    }

    val availableFeatures = HashMap<String, FeatureAvailabilty>()
    var servicesVersion: String? = null

    suspend fun checkApi(): Bundle? {
        val clientInfos = Bundle()
        clientInfos.putString(INFO_VERSION, BuildConfig.VERSION_NAME)
        val servicesInfos = connection.execute { it.checkApi(clientInfos) }
        // Stay bound to get notified it the KeeckerServices are updated
        if (servicesInfos != null) {
            servicesVersion = servicesInfos.getString(INFO_VERSION)
            val servicesFeatures = servicesInfos.getStringArrayList(INFO_FEATURES)
            if (servicesFeatures != null) {
                for (feature in servicesFeatures) {
                    if (supportedFeatures.containsKey(feature)) {
                        availableFeatures.put(feature, FeatureAvailabilty.NOT_ALLOWED)
                    }
                }
            }
        } else {
            Log.e(LOG_TAG, "Unable to retrieve Keecker Services API version")
        }
        return servicesInfos
    }

    override suspend fun getServicesVersion(): String? {
        if (servicesVersion == null) checkApi()
        return servicesVersion
    }

    override suspend fun isFeatureAvailable(feature: String) : FeatureAvailabilty {
        // Fetch Api infos if not already done
        if (servicesVersion == null) checkApi()
        val availability = availableFeatures.get(feature) ?: FeatureAvailabilty.NOT_AVAILABLE
        if (availability != FeatureAvailabilty.NOT_ALLOWED) {
            return availability
        } else {
            val permissions = supportedFeatures.get(feature) ?: return FeatureAvailabilty.NOT_AVAILABLE
            for (permission in permissions) {
                if (!isPermissionGranted.invoke(permission)) {
                    return FeatureAvailabilty.NOT_ALLOWED
                }
            }
            availableFeatures.put(feature, FeatureAvailabilty.AVAILABLE)
            return FeatureAvailabilty.AVAILABLE
        }
    }
}