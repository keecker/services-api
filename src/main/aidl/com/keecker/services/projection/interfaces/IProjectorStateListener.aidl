package com.keecker.services.projection.interfaces;

import com.keecker.services.projection.interfaces.ProjectorState;

oneway interface IProjectorStateListener {
    void onUpdate(in ProjectorState state);
}