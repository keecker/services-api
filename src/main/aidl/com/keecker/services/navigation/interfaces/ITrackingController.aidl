package com.keecker.services.navigation.interfaces;

import com.keecker.services.navigation.interfaces.DetectionResult;

/** @hide */
interface ITrackingController {
    void reset(in DetectionResult box);
    void stoptracking();
}
