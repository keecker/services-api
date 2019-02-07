package com.keecker.services.interfaces

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import android.os.RemoteException
import com.keecker.services.interfaces.test.ITypicalService
import com.keecker.services.interfaces.test.TickListener
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class TypicalServiceBinder : ITypicalService.Stub() {

    init {
        thread {
            var count = 0
            while (true) {
                sleep(1000)
                count += 1
                publishTick(count)
            }
        }.start()
    }

    private val mSubscribers = RemoteCallbackList<TickListener>()

    override fun getProcessId(): Int {
        return android.os.Process.myPid()
    }

    override fun crash() {
        System.exit(42)
    }

    override fun crashAfterTheCall() {
        thread {
            sleep(1000)
            System.exit(42)
        }.start()
    }

    override fun freeze() {
        while (true) {}
    }

    override fun subscribeToTicks(listener: TickListener) {
        mSubscribers.register(listener)
    }

    override fun unsubscribeToTicks(listener: TickListener) {
        mSubscribers.unregister(listener)
    }

    fun publishTick(count: Int) {
        val n = mSubscribers.beginBroadcast()
        for (i in 0 until n) {
            try {
                mSubscribers.getBroadcastItem(i).onNewSecond(count)
            } catch (e: RemoteException) {
                // This subscriber is dead
            }

        }
        mSubscribers.finishBroadcast()
    }
}

class TypicalServiceInSameProcess : Service() {

    private val mBinder = TypicalServiceBinder()

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }
}

class TypicalServiceInAnotherProcess : Service() {

    private val mBinder = TypicalServiceBinder()

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }
}
