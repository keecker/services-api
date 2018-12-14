/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.navigation.interfaces;

import android.content.Context;

import com.keecker.services.utils.KeeckerServiceConnection;

/**
 * @deprecated  As of release 0.2.0, replaced by {@link NavigationClient}
 */
@Deprecated
public class MovementPlannerServiceConnector extends KeeckerServiceConnection<IMovementPlannerService> {

    private static final String PACKAGE_NAME = "com.keecker.services";
    private static final String ACTION_BIND_MOVEMENT_PLANNER =
            "com.keecker.services.navigation.ACTION_BIND_MOVEMENT_PLANNER";

    /**
     * Default constructor. Package visibility as it's supposed to be provided by dagger.
     * @param appContext The Application context.
     */
    public MovementPlannerServiceConnector(Context appContext) {
        super(IMovementPlannerService.class, appContext, ACTION_BIND_MOVEMENT_PLANNER, PACKAGE_NAME);
    }

}
