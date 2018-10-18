/*
 * Copyright (C) 2017 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <p>
 * Contributors: Emeric Colombe
 */
package com.keecker.services.navigation.interfaces;

import com.keecker.services.navigation.interfaces.IChargingStationListener;
import com.keecker.services.utils.IIpcSubscriber;
import com.keecker.common.utils.map.Pose;

/** @hide */
interface IPerceptionService {
    void subscribeToWallSegmentation(in IIpcSubscriber sub);
    void unsubscribeToWallSegmentation(in IIpcSubscriber sub);

    Pose detectChargingStation();
    void subscribeToChargingStationDetector(IChargingStationListener sub);
    void unsubscribeToChargingStationDetector(IChargingStationListener sub);
}
