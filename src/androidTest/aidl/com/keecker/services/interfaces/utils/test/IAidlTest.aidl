// IAidlTest.aidl
package com.keecker.services.interfaces.utils.test;

// Declare any non-default types here with import statements
import com.keecker.services.interfaces.utils.test.OneWayListener;

// AIDL interface unsed for KeeckerServiceConnection Android tests
interface IAidlTest {

    // Gives the pid of the process running that Service
    int getProcessId();

    // Exits the process when called
    void crash();

    // Registers/Unregisters a subscriber to receive messages
    void subscribeToMessages(in OneWayListener clientListener);
    void unsubscribeToMessages(in OneWayListener clientListener);

    // Sends the given message to the subscribed clients
    void publishMessage(in String msg);
    void publishMessagesDelayed(int count, long delayMs);
}
