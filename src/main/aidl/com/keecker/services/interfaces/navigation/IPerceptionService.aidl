/*
 * Copyright (C) 2017 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <p>
 * Contributors: Emeric Colombe
 */
package com.keecker.services.interfaces.navigation;

import android.graphics.RectF;
import com.keecker.services.interfaces.common.utils.map.Pose;
import com.keecker.services.interfaces.embedded.camera.Frame;
import com.keecker.services.interfaces.embedded.camera.VideoFormat;
import com.keecker.services.interfaces.navigation.IChargingStationListener;
import com.keecker.services.interfaces.navigation.ITrackedPoseListener;
import com.keecker.services.interfaces.navigation.ITrackingController;
import com.keecker.services.interfaces.navigation.IWallSegmentationListener;
import com.keecker.services.interfaces.navigation.DetectionResult;
import com.keecker.services.interfaces.utils.deeplearning.ModelID;


/** @hide */
interface IPerceptionService {
    void subscribeToWallSegmentation(IWallSegmentationListener sub);
    void unsubscribeToWallSegmentation(IWallSegmentationListener sub);

    Pose detectChargingStation();
    List<DetectionResult> detect(in VideoFormat cameraType, in ModelID type);

    ITrackingController startTracking(in RectF box, in Frame frame, in ITrackedPoseListener objectPositionListener);

    void subscribeToChargingStationDetector(IChargingStationListener sub);
    void unsubscribeToChargingStationDetector(IChargingStationListener sub);
}
