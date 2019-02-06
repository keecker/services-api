package com.keecker.services.interfaces

import android.os.IInterface
import java.util.*

/**
 * Wraps a Service mock to test clients
 */
class MockedKeeckerServiceConnection<ServiceInterface:IInterface>(
        var dummyService : ServiceInterface
) : PersistentServiceConnection<ServiceInterface>{

    private var bound = false
    private val onNewServiceCallacks = LinkedList<(ServiceInterface) -> Unit>()

    override suspend fun <T> execute(lambda: (ServiceInterface) -> T): T? {
        if (!bound) {
            bound = true
            pretendANewServiceConnection()
        }
        return lambda.invoke(dummyService)
    }

    override fun onServiceConnected(lambda: (ServiceInterface) -> Unit) {
        onNewServiceCallacks.add(lambda)
    }

    fun pretendANewServiceConnection(newDummyService: ServiceInterface? = null) {
        if (newDummyService != null) dummyService = newDummyService
        for (callback in onNewServiceCallacks) {
            callback.invoke(dummyService)
        }
    }

    override fun unbind() {
        bound = false
    }
}