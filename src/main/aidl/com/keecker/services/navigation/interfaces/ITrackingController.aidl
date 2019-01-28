package com.keecker.services.navigation.interfaces;

import android.graphics.RectF;
import com.keecker.embedded.camera.interfaces.Frame;

/** @hide */
interface ITrackingController {
    void prepareReset();
    void cancelReset();
    void reset(in RectF box, in Frame frame);
    void stoptracking();
}
