// IAidlTest.aidl
package com.keecker.services.utils.test;

// Declare any non-default types here with import statements
import com.keecker.services.utils.test.OneWayListener;

interface IAidlTest {

    int getProcessId();

    void crash();

    void subscribeToMessages(in OneWayListener clientListener);
    void unsubscribeToMessages(in OneWayListener clientListener);
    void publishMessage(in String msg);
    void publishMessagesDelayed(int count, long delayMs);
}
