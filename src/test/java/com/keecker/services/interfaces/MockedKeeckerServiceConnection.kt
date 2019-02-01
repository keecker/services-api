package com.keecker.services.interfaces

import android.os.IInterface
import java.util.*

/**
 * Wraps a Service mock to test clients
 * TODO use the logic from the actual KeeckerServiceConnection
 */
class MockedKeeckerServiceConnection<ServiceInterface:IInterface>(
        val dummyService : ServiceInterface
) : PersistentServiceConnection<ServiceInterface>{

    private var bound = false
    private val onNewServiceCallacks = LinkedList<suspend (ServiceInterface) -> Unit>()

    override suspend fun <T> execute(lambda: (ServiceInterface) -> T): T? {
        if (!bound) {
            newServiceInstance()
            bound = true
        }
        return lambda.invoke(dummyService)
    }

    override fun onNewServiceInstance(lambda: suspend (ServiceInterface) -> Unit) {
        onNewServiceCallacks.add(lambda)
    }

    suspend fun newServiceInstance() {
        for (callback in onNewServiceCallacks) {
            callback.invoke(dummyService)
        }
    }

    override fun unbind() {
        bound = false
    }
}