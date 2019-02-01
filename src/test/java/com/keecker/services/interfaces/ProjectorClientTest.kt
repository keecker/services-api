package com.keecker.services.interfaces

import android.os.IBinder
import com.keecker.services.interfaces.projection.*
import com.keecker.services.interfaces.utils.IIpcSubscriber
import com.keecker.services.interfaces.utils.ILowBatteryNotificationListener
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.lang.AssertionError

class ProjectorClientTest {

    @Test
    fun `allows to poll the projector state`() = runBlocking<Unit> {
        // Counts getState() calls to the Service
        var getStateCalls = 0
        val client = makeClient(service = object : MockedProjectorService() {
            override fun getState(): ProjectorState {
                getStateCalls += 1
                return ProjectorState.defaultState
            }
        })
        val currentState = client.getState()
        assertNotNull(currentState)
        assertEquals(1, getStateCalls)
    }

    @Test
    fun `allows to set the projector state`() = runBlocking<Unit> {
        // Counts setState() calls to the Service
        var setStateCalls = 0
        val client = makeClient(service = object : MockedProjectorService() {
            override fun setState(params: ProjectorState?): Boolean {
                setStateCalls += 1
                return true
            }
        })
        // All the parameters are set by calling the Service setState once,
        // by only setting the parameters to change
        client.setState(ProjectorState(focus = 10, orientation = 45))
        assertEquals(1, setStateCalls)
    }

    @Test
    fun `allows to subscribe to projector state events`() = runBlocking<Unit> {
        // Counts subscription calls to the Service
        var subscribeCalls = 0
        var unsubscribeCalls = 0
        val client = makeClient(service = object : MockedProjectorService() {
            override fun subscribeToState(subscriber: IIpcSubscriber?) {
                subscribeCalls += 1
            }

            override fun unsubscribeToState(subscriber: IIpcSubscriber?) {
                unsubscribeCalls += 1
            }
        })
        val listener = object : IProjectorStateListener.Stub() {
            override fun onUpdate(state: ProjectorState?) {}
        }
        client.subscribeToState(listener)
        assertEquals(1, subscribeCalls)

        client.unsubscribeToState(listener)
        assertEquals(1, unsubscribeCalls)
    }

    @Test
    fun `automatically resubscribes to projector state changes on Service reconnect`() = runBlocking<Unit> {
        // Counts subscription calls to the Service
        var subscribeCalls = 0
        val service = object : MockedProjectorService() {
            override fun subscribeToState(subscriber: IIpcSubscriber?) {
                subscribeCalls += 1
            }
        }
        val connection = MockedKeeckerServiceConnection<IProjectorService>(service)
        val client = makeClient(connection = connection)
        // The client subscribes to state changes by giving a listener
        val listener = object : IProjectorStateListener.Stub() {
            override fun onUpdate(state: ProjectorState?) {}
        }
        client.subscribeToState(listener)
        assertEquals(1, subscribeCalls)

        // We are still subscribed, if the service have been updated / has crashed,
        // a new subscription call is made
        connection.newServiceInstance()
        assertEquals(2, subscribeCalls)

        // We are no longer subscribed, nothing happens when reconnecting
        client.unsubscribeToState(listener)
        connection.newServiceInstance()
        assertEquals(2, subscribeCalls)
    }

    @Test
    fun `requires the PROJECTOR_ACCESS_STATE feature`() = runBlocking {
        val apiChecker = object : MockedApiChecker() {
            override suspend fun isFeatureAvailable(feature: Feature): FeatureAvailabilty {
                return FeatureAvailabilty.NOT_AVAILABLE
            }
        }
        val client = makeClient(apiChecker = apiChecker)
        assertFalse(client.isApiAccessible())
    }

    @Test
    fun `returns null when using the client if API is not available`() = runBlocking {
        val apiChecker = object : MockedApiChecker() {
            override suspend fun isFeatureAvailable(feature: Feature): FeatureAvailabilty {
                return FeatureAvailabilty.NOT_AVAILABLE
            }
        }
        val client = makeClient(apiChecker = apiChecker)
        assertNull(client.getState())
    }

    /*
     * Mocking boiler plate
     */

    fun makeClient(
            service: IProjectorService? = null,
            connection: PersistentServiceConnection<IProjectorService>? = null,
            apiChecker: ApiChecker? = null
    ) : ProjectorCoroutineClient {
        return ProjectorClient(
                connection ?: MockedKeeckerServiceConnection(
                        service ?: MockedProjectorService()),
                apiChecker ?: MockedApiChecker())
    }

    open class MockedApiChecker : ApiChecker {
        override suspend fun getServicesVersion(): String? {
            return "1.12.0"
        }

        override fun isRunningOnKeecker(): Boolean {
            return true
        }

        override suspend fun isFeatureAvailable(feature: Feature): FeatureAvailabilty {
            return FeatureAvailabilty.AVAILABLE
        }
    }

    open class MockedProjectorService : IProjectorService {

        override fun setState(params: ProjectorState?): Boolean {
            return true
        }

        override fun getState(): ProjectorState {
            return ProjectorState.defaultState
        }

        override fun subscribeToState(subscriber: IIpcSubscriber?) {}


        override fun unsubscribeToState(subscriber: IIpcSubscriber?) {}

        override fun asBinder(): IBinder? {
            return null
        }

        // All the other methods are no longer used or not exposed

        override fun setFocus(focus: Int): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun startAutoFocus(): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun stopAutoFocus(): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun startAutoKeystone(): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun stopAutoKeystone(): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun setOrientation(orientation: Int): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun switchLedOn(): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun switchLedOff(): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun setKeystone(keystone: Int): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun setZoom(zoom: Int): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun setBrightness(brightness: Int): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun setContrast(contrast: Int): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun getContrast(): Int {
            throw AssertionError("Not exposed")
        }

        override fun setDisplayPosition(displayPosition: DisplayPosition?): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun getDisplayPosition(): DisplayPosition {
            throw AssertionError("Not exposed")
        }

        override fun setAspectRatio(aspectRatio: AspectRatio?): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun getAspectRatio(): AspectRatio {
            throw AssertionError("Not exposed")
        }

        override fun getTemperature(): Int {
            throw AssertionError("Not exposed")
        }

        override fun getFocus(): Int {
            throw AssertionError("Not exposed")
        }

        override fun isAutoFocus(): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun isAutoKeystone(): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun getOrientation(): Int {
            throw AssertionError("Not exposed")
        }

        override fun isLedOn(): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun getKeystone(): Int {
            throw AssertionError("Not exposed")
        }

        override fun getZoom(): Int {
            throw AssertionError("Not exposed")
        }

        override fun getBrightness(): Int {
            throw AssertionError("Not exposed")
        }

        override fun setDisplayMode(displayMode: DisplayMode?): Boolean {
            throw AssertionError("Not exposed")
        }

        override fun getDisplayMode(): DisplayMode {
            throw AssertionError("Not exposed")
        }

        override fun registerToLowBatteryTurnOffProjListener(listener: ILowBatteryNotificationListener?) {
            throw AssertionError("Not exposed")
        }

        override fun unregisterToLowBatteryTurnOffProjListener(listener: ILowBatteryNotificationListener?) {
            throw AssertionError("Not exposed")
        }
    }
}