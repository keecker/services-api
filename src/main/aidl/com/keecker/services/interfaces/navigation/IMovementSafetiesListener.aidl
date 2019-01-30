package com.keecker.services.interfaces.navigation;

import com.keecker.services.interfaces.embedded.stm.MovementSafetiesStatus;

oneway interface IMovementSafetiesListener {
    void onNewStatus(in MovementSafetiesStatus status);
}