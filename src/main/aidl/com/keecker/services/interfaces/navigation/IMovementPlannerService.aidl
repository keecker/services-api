package com.keecker.services.interfaces.navigation;

import com.keecker.services.interfaces.utils.IIpcSubscriber;
import com.keecker.services.interfaces.common.utils.map.Pose;
import com.keecker.services.interfaces.navigation.SafeModeState;

/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
interface IMovementPlannerService {
    // Set manual speeds, values should be between -1 and 1 and will be clamped
    void setManualCommand(boolean doControl, double linear, double angular);

    // The given speeds are a maximum in m/s and radian/s (NaN for default)
    // Feedback can be received via a GoToRelative message subscriber (null if not needed)
    void goToRelative(double x, double y, double theta,
                      double linear_speed, double angular_speed, in IIpcSubscriber sub);

    void setGlobalGoal(in Pose goal, in IIpcSubscriber sub);

    void subscribeToPath(in IIpcSubscriber sub);
    void unsubscribeToPath(in IIpcSubscriber sub);

    void subscribeToSafeModeState(in IIpcSubscriber sub);
    void unsubscribeToSafeModeState(in IIpcSubscriber sub);

    void alignToWall(in IIpcSubscriber sub);

    // Keecker will turn for the given angle in radians at the given speed.
    // The given speed is a maximum in radian/s (NaN for default)
    // Feedback can be received via a GoToRelative message subscriber (null if not needed)
    void turn(double angle, double speed, in IIpcSubscriber sub);

    // Allow to disable obstacles avoidance, should only be used for debuging
    void disableAvoidance(boolean disable);

    // Charging station
    void goToChargingStation(in IIpcSubscriber sub);
    void goToGivenChargingStation(in Pose goal, in IIpcSubscriber sub);
    boolean isReturningToChargingStation();

    // Stops Keecker immediately
    void stopRobot();

    // Toggles Safe Mode
    void toggleSafeMode(in SafeModeState state);

    /** @hide
      * Type not used for now, may be used to select the logged data in the future
      * Filename is the file to log to.
      */
    void logDataTo(in String contentUrl, in String type);
}
