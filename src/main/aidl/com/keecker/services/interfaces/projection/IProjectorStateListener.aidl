package com.keecker.services.interfaces.projection;

import com.keecker.services.interfaces.projection.ProjectorState;

oneway interface IProjectorStateListener {
    void onUpdate(in ProjectorState state);
}