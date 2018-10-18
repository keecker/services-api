package com.keecker.services.navigation.interfaces;

import com.keecker.services.utils.IIpcSubscriber;
import com.keecker.services.utils.sharedmemory.ISharedMemorySubscriber;
import com.keecker.services.navigation.interfaces.IMovementSafetiesListener;
import com.keecker.services.navigation.interfaces.IOdometryListener;

interface IMovementPerceptionService {
    void subscribeLocalMap(in ISharedMemorySubscriber sub);
    void unsubscribeLocalMap(in ISharedMemorySubscriber sub);
    void subscribeCompass(in IIpcSubscriber sub);
    void unsubscribeCompass(in IIpcSubscriber sub);
    // Notifications for when Keecker starts/stops moving
    void subscribeToMovementEvents(in IIpcSubscriber sub);
    void unsubscribeToMovementEvents(in IIpcSubscriber sub);
    void subscribeToKidnapping(in IIpcSubscriber sub);
    void unsubscribeToKidnapping(in IIpcSubscriber sub);
    boolean isMoving();
    /**
     * Ask for travelled distance since birth in meters
     * @return The value of the travelled distance
     */
    double getTravelledDistance();

    void subscribeToSafeties(in IMovementSafetiesListener listener);
    void unsubscribeToSafeties(in IMovementSafetiesListener listener);

    void subscribeToOdometry(in IOdometryListener listener);
    void unsubscribeToOdometry(in IOdometryListener listener);
}
