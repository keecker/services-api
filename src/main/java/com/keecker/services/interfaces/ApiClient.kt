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
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.keecker.services.interfaces.Constants.LOG_TAG
import java.util.*

enum class Feature(val permissions : List<String>) {
    PROJECTOR_ACCESS_STATE(listOf("com.keecker.permission.PROJECTION"))
}

enum class FeatureAvailabilty {AVAILABLE, NOT_AVAILABLE, NOT_ALLOWED}

interface ApiChecker {
    suspend fun getServicesVersion() : String?
    fun isRunningOnKeecker() : Boolean
    suspend fun isFeatureAvailable(feature: Feature) : FeatureAvailabilty
}

class ApiClient(val connection: PersistentServiceConnection<IApiService>, val context: Context) : ApiChecker {

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
            features.clear()
            servicesVersion = null
        }
    }

    override fun isRunningOnKeecker(): Boolean {
        return Build.BRAND == "Keecker"
    }

    val features = HashMap<Feature, FeatureAvailabilty>()
    var servicesVersion: String? = null

    suspend fun checkApi(): Bundle? {
        val clientInfos = Bundle()
        clientInfos.putString(INFO_VERSION, BuildConfig.VERSION_NAME)
        val servicesInfos = connection.execute { it.checkApi(clientInfos) }
        connection.unbind()
        if (servicesInfos != null) {
            servicesVersion = servicesInfos.getString(INFO_VERSION)
            val featuresStr = servicesInfos.getStringArrayList(INFO_FEATURES)
            for (feature in featuresStr) {
                try {
                    features.put(Feature.valueOf(feature), FeatureAvailabilty.NOT_ALLOWED)
                } catch (e: IllegalArgumentException) {
                    // This feature is not known to this sdk version.
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

    override suspend fun isFeatureAvailable(feature: Feature) : FeatureAvailabilty {
        // Fetch Api infos if not already done
        if (servicesVersion == null) checkApi()
        val availability = features.get(feature) ?: FeatureAvailabilty.NOT_AVAILABLE
        if (availability != FeatureAvailabilty.NOT_ALLOWED) {
            return availability
        } else {
            for (permission in feature.permissions) {
                val res = context.checkCallingOrSelfPermission(permission)
                if (res != PackageManager.PERMISSION_GRANTED) {
                    return FeatureAvailabilty.NOT_ALLOWED
                }
            }
            features.put(feature, FeatureAvailabilty.AVAILABLE)
            return FeatureAvailabilty.AVAILABLE
        }
    }
}