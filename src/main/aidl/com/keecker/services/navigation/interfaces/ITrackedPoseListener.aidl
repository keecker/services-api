package com.keecker.services.navigation.interfaces;

import com.keecker.common.utils.map.Pose;
import com.keecker.embedded.camera.interfaces.Frame;

/** @hide */
oneway interface ITrackedPoseListener {
    void onNewBoxTracked(in RectF box, in Frame frame);
    void onNewPositionTracked(in Pose position);
    void onTrackingLost();
}
