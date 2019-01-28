package com.keecker.services.interfaces.navigation;

import com.keecker.services.interfaces.common.utils.map.Pose;
import com.keecker.services.interfaces.embedded.camera.Frame;

/** @hide */
oneway interface ITrackedPoseListener {
    void onNewBoxTracked(in RectF box, in Frame frame);
    void onNewPositionTracked(in Pose position);
    void onTrackingLost();
}
