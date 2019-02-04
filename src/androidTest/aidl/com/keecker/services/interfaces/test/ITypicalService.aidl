// IAidlTest.aidl
package com.keecker.services.interfaces.test;

// Declare any non-default types here with import statements
import com.keecker.services.interfaces.test.TickListener;

// AIDL interface unsed for KeeckerServiceConnection Android tests
interface ITypicalService {

    // Gives the pid of the process running that Service
    int getProcessId();

    // Exits the process when called
    void crash();

    // Registers/Unregisters a listener to receive ticks
    void subscribeToTicks(in TickListener listener);
    void unsubscribeToTicks(in TickListener listener);
}
