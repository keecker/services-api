// IProjectorController.aidl
package com.keecker.services.projection.interfaces;

import com.keecker.services.projection.interfaces.ProjectorState;
import com.keecker.services.projection.interfaces.AspectRatio;
import com.keecker.services.projection.interfaces.DisplayMode;
import com.keecker.services.projection.interfaces.DisplayPosition;

import com.keecker.services.utils.IIpcSubscriber;
import com.keecker.services.utils.ILowBatteryNotificationListener;

import android.os.ParcelFileDescriptor;

interface IProjectorService {
    //Focus
    /**
     * Set a manual focus for the projector
     * @param focus between 0 and 100
     * @return true if no error changing focus
     */
    boolean setFocus(int focus);

    /**
     * Get current focus value
     * @return focus between 0 and 100
     */
    int getFocus();

    /**
     * Switch on auto focus mode
     * @return true if no error changing focus
     */
    boolean startAutoFocus();

    /**
     * Switch off auto focus mode
     * @return true if no error changing focus
     */
    boolean stopAutoFocus();

    /**
     * Check if auto focus is ON
     * @return true if auto focus is on
     */
    boolean isAutoFocus();
    //Keystone

    /**
     * Switch on Auto Keystone mode
     * @return true if no error changing focus
     */
    boolean startAutoKeystone();

    /**
     * Switch off Auto Keystone mode
     * @return true if no error changing focus
     */
    boolean stopAutoKeystone();

    /**
     * Check if auto keystone is ON
     * @return true if auto keystone is on
     */
    boolean isAutoKeystone();
    //Orientation

    /**
     * Set orientation of the projector
     * @param orientation between 0 and 90
     * @return true if no error changing orientation
     */
    boolean setOrientation(int orientation);

    /**
     * Get orientation of the projector
     * @return orientation between 0 and 100
     */
    int getOrientation();
    //Feedback

    /**
     * Subscribe to projector state change events
     * @param subscriber
     */
    void subscribeToState(in IIpcSubscriber subscriber);

    /**
     * Unsubscribe a previously subscribed listener projector state change events
     * @param subscriber
     */
    void unsubscribeToState(in IIpcSubscriber subscriber);
    // HW methods go here
    boolean switchLedOn();
    boolean switchLedOff();
    boolean isLedOn();

    /**
     * Set a manual value for projector keystone
     * @param keystone value between -40 and 40
     * @return true if no error changing orientation
     */
    boolean setKeystone(int keystone);

    /**
     * Get keystone of the projector
     * @return keystone value between -40 and 40
     */
    int getKeystone();

    /**
     * Set zoom value for the projector
     * @param zoom between 0 and 100 (50 is no zoom)
     * @return true if no error changing orientation
     */
    boolean setZoom(int zoom);

    /**
     * Get current zoom value of the projector
     * @return zoom between 0 and 100 (50 is no zoom)
     */
    int getZoom();

    /**
     * Set brightness value for the projector
     * @param brightness between 0 and 100
     * @return true if no error changing orientation
     */
    boolean setBrightness(int brightness);

    /**
     * Get current brightness value of the projector
     * @return brightness between 0 and 100
     */
    int getBrightness();

    /**
     * Set contrast value for the projector
     * @param contrast between 0 and 100
     * @return true if no error changing orientation
     */
    boolean setContrast(int contrast);

    /**
     * Get current contrast value of the projector
     * @return contrast between 0 and 100
     */
    int getContrast();

    /**
     * Switch to a particular display mode
     * @param displayMode to switch to
     * @return true if no error changing orientation
     */
    boolean setDisplayMode(in DisplayMode displayMode);

    /**
     * Get current display mode
     * @return current displaymode
     */
    DisplayMode getDisplayMode();

    /**
     * Flip projected image
     * @param displayPosition that represents the particular flip to set
     * @return true if no error changing orientation
     */
    boolean setDisplayPosition(in DisplayPosition displayPosition);

    /**
     * Get current flip mode
     * @return DisplayPosition that represents the current flip mode
     */
    DisplayPosition getDisplayPosition();

    /**
     * Change current aspect ratio for projection
     * @param aspectRatio
     * @return true if no error changing orientation
     */
    boolean setAspectRatio(in AspectRatio aspectRatio);


    /**
     * Get the current aspect ratio for projection
     * @return the current aspect ratio
     */
    AspectRatio getAspectRatio();

    /**
     * Change complete projector state in one call.
     * @param params to set
     * @return true if no error changing orientation
     */
    boolean setState(in ProjectorState params);

    /**
     * Get full projector state in one call
     * @return current projector state
     */
    ProjectorState getState();

    /**
    * Get temperature of the projector
    * @return temperature in degrees Celsius
    */
    int getTemperature();

    /**
    *  Subscribe to low battery turn off projector Notification
    *  @param ILowBatteryNotificationListener to register
    */
    void registerToLowBatteryTurnOffProjListener(in ILowBatteryNotificationListener listener);

    /**
    *  Unsubscribe to low battery turn off projector Notification
    *  @param ILowBatteryNotificationListener to unregister
    */
    void unregisterToLowBatteryTurnOffProjListener(in ILowBatteryNotificationListener listener);
}