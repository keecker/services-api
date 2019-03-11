package com.keecker.embedded.stm.interfaces;

import android.os.ParcelFileDescriptor;

import com.keecker.services.interfaces.utils.IIpcSubscriber;

interface IHwRobotService {
    /** @hide */
    void subscribeToProjTilt(in IIpcSubscriber subscriber);

    /** @hide */
    void unsubscribeToProjTilt(in IIpcSubscriber subscriber);

    /** @hide */
    void subscribeToProjFocus(in IIpcSubscriber subscriber);

    /** @hide */
    void unsubscribeToProjFocus(in IIpcSubscriber subscriber);

    /**
     * Subscribe to Velocity @{@link Twist2dStamped} update events
     * @param listener
     *
     * WARNING: This is available in Beta and may be subject to change
     */
    void subscribeToVelocity(in IIpcSubscriber subscriber);

    /**
     * Unsubscribe a previously subscribed listener to Velocity @{@link Twist2dStamped} update event
     * @param listener
     *
     * WARNING: This is available in Beta and may be subject to change
     */
    void unsubscribeToVelocity(in IIpcSubscriber subscriber);

    /** @hide */
    void subscribeToMagneticField(in IIpcSubscriber subscriber);

    /** @hide */
    void unsubscribeToMagneticField(in IIpcSubscriber subscriber);

    /** @hide */
    void subscribeToSafeties(in IIpcSubscriber subscriber);

    /** @hide */
    void unsubscribeToSafeties(in IIpcSubscriber subscriber);

    /** @hide */
    void subscribeToFanSpeeds(in IIpcSubscriber subscriber);

    /** @hide */
    void unsubscribeToFanSpeeds(in IIpcSubscriber subscriber);

    /** @hide */
    void toggleSafeMode(boolean state);

    /**
     * @hide
     * Ignore some safeties during a short time
     * @param safeties_mask
     * @param duration duration in tenth of ms
     * Mask list :
     * typedef enum {
           IR_SENSOR_LEFT_0,
           IR_SENSOR_LEFT_1,
           IR_SENSOR_LEFT_2,
           IR_SENSOR_CLIFF_REAR_MIDDLE,
           IR_SENSOR_CLIFF_REAR_RIGHT,
           IR_SENSOR_CLIFF_REAR_LEFT,
           IR_SENSOR_RIGHT_0,
           IR_SENSOR_RIGHT_1,
           IR_SENSOR_RIGHT_2,
           IR_SENSOR_CLIFF_FRONT_MIDDLE,
           IR_SENSOR_CLIFF_FRONT_LEFT,
           IR_SENSOR_CLIFF_FRONT_RIGHT,
           LEFT_MOTOR_CURRENT,
           RIGHT_MOTOR_CURRENT,
           SHOCK_SAFETY_SENSOR,
           ANGULAR_INCONSISTENCY_SAFETY_SENSOR,
           NB_OF_SAFETY_SENSORS
       } SafetySensorId;
     */
    void ignoreSafeties(int safeties_mask,byte duration);

    /** @hide */
    void setVelocityCommand(boolean do_control, double linear, double angular);

    /** @hide */
    int getProjectorOrientation();

    /** @hide */
    boolean setProjectorOrientation(int orientation);

    /** @hide */
    int getProjectorFocus();

    /** @hide */
    boolean setProjectorFocus(int focus);

    /** @hide */
    boolean isGyroCalibrated();

    /** @hide */
    void setGyroSensitivity(float sensitivity);

    // Private / Debug

    /** @hide
     *  Direct mapping between speed command and motors PWM (bybasses PID control)
     */
    void setPwmMovementControl(boolean enable);

    /** @hide
     *  "ctrl": Saves control information in the given csv file.
     *  "safeties": Saves IR, cliffs and other safeties in the given csv file
     *  Stop logging if filename is empty
     */
    void logDataTo(in String contentUrl, in String type);

    /** @hide */
    void setPids(
            double p_motor    , double i_motor    , double d_motor    ,
            double p_angular  , double i_angular  , double d_angular  ,
            double p_ang_speed, double i_ang_speed, double d_ang_speed,
            double p_lin_speed, double i_lin_speed, double d_lin_speed,
            double ang_fil_rat, double lin_fil_rat);


    /** @hide dutyCycle between 0 and 100 */
    boolean setFansDutyCycle(int projectorFanDutyCycle, int rearFanDutyCycle);

    /** @hide speed between 0 and 100 */
    boolean setProjectorFanDutyCycle(int speed);

    /** @hide speed between 0 and 100 */
    boolean setRearFanDutyCycle(int speed);
}
