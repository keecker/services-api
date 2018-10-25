/*
 * Copyright (C) 2017 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <p>
 * Contributors: Emeric Colombe
 */
package com.keecker.services.navigation.interfaces;

import com.keecker.common.utils.map.Pose;
import com.keecker.hardware.camera.interfaces.VideoFormat;
import com.keecker.services.navigation.interfaces.IChargingStationListener;
import com.keecker.services.navigation.interfaces.ITrackedPoseListener;
import com.keecker.services.navigation.interfaces.ITrackingController;
import com.keecker.services.navigation.interfaces.IWallSegmentationListener;
import com.keecker.services.navigation.interfaces.DetectionResult;
import com.keecker.services.utils.deeplearning.ModelID;


/** @hide */
interface IPerceptionService {
    void subscribeToWallSegmentation(IWallSegmentationListener sub);
    void unsubscribeToWallSegmentation(IWallSegmentationListener sub);

    Pose detectChargingStation();
    List<DetectionResult> detect(in VideoFormat cameraType, in ModelID type);

    ITrackingController startTracking(in DetectionResult objectToTrack, in ITrackedPoseListener objectPositionListener);

    void subscribeToChargingStationDetector(IChargingStationListener sub);
    void unsubscribeToChargingStationDetector(IChargingStationListener sub);
}
