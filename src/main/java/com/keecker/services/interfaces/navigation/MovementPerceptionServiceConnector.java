package com.keecker.services.interfaces.navigation;

import android.content.Context;

import com.keecker.services.interfaces.utils.KeeckerServiceConnection;
/*
 * Created by Thomas Gallagher on 4/18/17.
 */

/**
 * @deprecated  As of release 0.2.0, replaced by {@link PerceptionClient}
 */
@Deprecated
public class MovementPerceptionServiceConnector extends KeeckerServiceConnection<IMovementPerceptionService> {

    private static final String PACKAGE_NAME = "com.keecker.services";
    private static final String ACTION_BIND_MOVEMENT_PERCEPTION =
            "com.keecker.services.navigation.ACTION_BIND_MOVEMENT_PERCEPTION";

    /**
     * Default constructor. Package visibility as it's supposed to be provided by dagger.
     * @param appContext The Application context.
     */
    public MovementPerceptionServiceConnector(Context appContext) {
        super(IMovementPerceptionService.class, appContext,  ACTION_BIND_MOVEMENT_PERCEPTION, PACKAGE_NAME);
    }

}
