package com.keecker.services.navigation.interfaces;

import com.keecker.embedded.stm.interfaces.MovementSafetiesStatus;

oneway interface IMovementSafetiesListener {
    void onNewStatus(in MovementSafetiesStatus status);
}