/*
 * Created by Thomas Gallagher on 4/18/17.
 */

package com.keecker.services.interfaces.navigation;

import android.content.Context;
import com.keecker.services.interfaces.utils.KeeckerServiceConnection;

public class MovementPerceptionServiceConnector extends KeeckerServiceConnection<IMovementPerceptionService> {
    private static final String PACKAGE_NAME = "com.keecker.services.interfaces";
    private static final String ACTION_BIND_MOVEMENT_PERCEPTION =
            "com.keecker.services.interfaces.navigation.ACTION_BIND_MOVEMENT_PERCEPTION";

    /**
     * Default constructor. Package visibility as it's supposed to be provided by dagger.
     * @param appContext The Application context.
     */
    public MovementPerceptionServiceConnector(Context appContext) {
        super(IMovementPerceptionService.class, appContext,  ACTION_BIND_MOVEMENT_PERCEPTION, PACKAGE_NAME);
    }

}
