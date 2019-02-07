package com.keecker.services.interfaces

import android.os.Bundle
import android.os.IBinder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class ApiClientTest {

    @Test
    fun `tells a feature is not available when not available on the services side`() = runBlocking<Unit> {
        val apiClient = mockApiClient(
                mockApiService(availableFeatures = listOf()),
                availableFeatures = mapOf("PROJECTOR_ACCESS_STATE" to setOf())

        )
        assertEquals(FeatureAvailabilty.NOT_AVAILABLE,
                apiClient.isFeatureAvailable("PROJECTOR_ACCESS_STATE"))
    }

    @Test
    fun `tells a feature is not available when not available on the client side`() = runBlocking<Unit> {
        val apiClient = mockApiClient(
                mockApiService(availableFeatures = listOf("PROJECTOR_ACCESS_STATE")),
                availableFeatures = mapOf()

        )
        assertEquals(FeatureAvailabilty.NOT_AVAILABLE,
                apiClient.isFeatureAvailable("PROJECTOR_ACCESS_STATE"))
    }

    @Test
    fun `tells a feature is available when its need no permission`() = runBlocking<Unit> {
        val apiClient = mockApiClient(
                mockApiService(availableFeatures = listOf("PROJECTOR_ACCESS_STATE")),
                availableFeatures = mapOf("PROJECTOR_ACCESS_STATE" to setOf())

        )
        assertEquals(FeatureAvailabilty.AVAILABLE,
                apiClient.isFeatureAvailable("PROJECTOR_ACCESS_STATE"))
    }

    @Test
    fun `tells a feature is not available when it lacks a permission`() = runBlocking<Unit> {
        val apiClient = mockApiClient(
                mockApiService(availableFeatures = listOf("PROJECTOR_ACCESS_STATE")),
                availableFeatures = mapOf("PROJECTOR_ACCESS_STATE" to
                        setOf("com.keecker.permission.PROJECTION")),
                grantedPremissions = setOf()

        )
        assertEquals(FeatureAvailabilty.NOT_ALLOWED,
                apiClient.isFeatureAvailable("PROJECTOR_ACCESS_STATE"))
    }

    @Test
    fun `tells a feature is available when it has the permission`() = runBlocking<Unit> {
        val apiClient = mockApiClient(
                mockApiService(availableFeatures = listOf("PROJECTOR_ACCESS_STATE")),
                availableFeatures = mapOf("PROJECTOR_ACCESS_STATE" to
                        setOf("com.keecker.permission.PROJECTION")),
                grantedPremissions = setOf("com.keecker.permission.PROJECTION")

        )
        assertEquals(FeatureAvailabilty.AVAILABLE,
                apiClient.isFeatureAvailable("PROJECTOR_ACCESS_STATE"))
    }

    /*
     * Mocking boiler plate
     */

    fun mockApiService(
        servicesVersion: String = "1.12.0",
        availableFeatures: List<String> = listOf()
    ) : IApiService {
        return object : IApiService {
            override fun checkApi(clientInfo: Bundle?): Bundle {
                val bundle = mock(Bundle::class.java)
                `when`(bundle.getString(ApiClient.INFO_VERSION)).thenReturn(servicesVersion)
                `when`(bundle.getStringArrayList(ApiClient.INFO_FEATURES)).thenReturn(
                        ArrayList(availableFeatures))
                return bundle
            }

            override fun asBinder(): IBinder? {
                return null
            }
        }
    }

    fun mockApiClient(
            apiService : IApiService? = null,
            apiServiceConnection: PersistentServiceConnection<IApiService>? = null,
            availableFeatures: Map<String, Set<String>>,
            grantedPremissions: Set<String> = setOf()) : ApiClient {
        val permissionChecker = { perm: String -> grantedPremissions.contains(perm) }
        return ApiClient(
                apiServiceConnection ?: MockedKeeckerServiceConnection(
                        apiService ?: mockApiService()),
                availableFeatures,
                permissionChecker)
    }
}