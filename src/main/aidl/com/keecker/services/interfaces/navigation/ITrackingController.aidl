package com.keecker.services.interfaces.navigation;

import android.graphics.RectF;
import com.keecker.services.interfaces.embedded.camera.Frame;

/** @hide */
interface ITrackingController {
    void prepareReset();
    void cancelReset();
    void reset(in RectF box, in Frame frame);
    void stoptracking();
}
