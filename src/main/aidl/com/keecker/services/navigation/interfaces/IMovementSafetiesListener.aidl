package com.keecker.services.navigation.interfaces;

import com.keecker.hardware.robot.interfaces.MovementSafetiesStatus;

oneway interface IMovementSafetiesListener {
    void onNewStatus(in MovementSafetiesStatus status);
}