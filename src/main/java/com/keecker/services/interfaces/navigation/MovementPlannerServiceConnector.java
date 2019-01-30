/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.interfaces.navigation;

import android.content.Context;

import com.keecker.services.interfaces.utils.KeeckerServiceConnection;

public class MovementPlannerServiceConnector extends KeeckerServiceConnection<IMovementPlannerService> {

    private static final String PACKAGE_NAME = "com.keecker.services.interfaces";
    private static final String ACTION_BIND_MOVEMENT_PLANNER =
            "com.keecker.services.interfaces.navigation.ACTION_BIND_MOVEMENT_PLANNER";

    /**
     * Default constructor. Package visibility as it's supposed to be provided by dagger.
     * @param appContext The Application context.
     */
    public MovementPlannerServiceConnector(Context appContext) {
        super(IMovementPlannerService.class, appContext, ACTION_BIND_MOVEMENT_PLANNER, PACKAGE_NAME);
    }

}
