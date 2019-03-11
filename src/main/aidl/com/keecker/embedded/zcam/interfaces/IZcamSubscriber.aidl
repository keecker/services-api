/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.embedded.zcam.interfaces;

import com.keecker.embedded.zcam.interfaces.DepthBufferInfo;

interface IZcamSubscriber {
    void onDepthDataBufferAvailable(in DepthBufferInfo bufferInfo);
    void onDroppedFrame();
}
