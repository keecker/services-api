/*
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.embedded.camera.interfaces;

import com.keecker.services.interfaces.embedded.camera.VideoFormat;
import com.keecker.services.interfaces.embedded.camera.DewarpParameters;

import com.keecker.embedded.camera.interfaces.CalibrationParams;
import com.keecker.embedded.camera.interfaces.ICameraServiceCallback;

interface ICameraService {
    /**
      * Start a video preview at the required frameRate and video format.
      * @param cb Callback called on new frame
      * @param format {@link VideoFormat} to use (resolution, parameters, etc.)
      * @param frameRate Frame rate requested
      * @return True successfully start preview
      */
    boolean startPreview(ICameraServiceCallback cb, in VideoFormat format, in int frameRate);
    /**
      * Start video preview without launching LED animation
      * @param clientName Name of the client binding to this service
      * @see #startPreview(ICameraServiceCallback cb, in VideoFormat format, in int frameRate)
      *
      * @hide
      */
    boolean startPreviewSilently(ICameraServiceCallback cb, in VideoFormat format, in int frameRate, in String clientName);
    void stopPreview(ICameraServiceCallback cb, in VideoFormat format);

    boolean takeAPicture(ICameraServiceCallback cb, in  VideoFormat format);
    /**
      * Take a picture without launching LED animation
      *
      * @hide
      */
    boolean takeAPictureSilently(ICameraServiceCallback cb, in  VideoFormat format, in String clientName);
    void releaseTakeAPicture(ICameraServiceCallback cb, in VideoFormat format);

    void resetDewarpPosition();
    void setDewarpPosition(in DewarpParameters dewarpParameters);
    void scale(in float dScale);
    void scroll(in int dTheta,in float dPhi);

    CalibrationParams getFishEyeCalibrationParams();

    void registerToPreview(ICameraServiceCallback cb);
    void unregisterToPreview(ICameraServiceCallback cb);
}
