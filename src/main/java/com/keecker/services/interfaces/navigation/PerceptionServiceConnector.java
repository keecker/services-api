/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.interfaces.navigation;

import android.content.Context;
import com.keecker.services.interfaces.utils.KeeckerServiceConnection;

/** @hide */
public class PerceptionServiceConnector extends KeeckerServiceConnection<IPerceptionService> {

    private static final String PACKAGE_NAME = "com.keecker.services.interfaces";
    private static final String ACTION_BIND_PERCEPTION =
            "com.keecker.services.interfaces.navigation.ACTION_BIND_PERCEPTION";

    /**
     * Default constructor. Package visibility as it's supposed to be provided by dagger.
     * @param appContext The Application context.
     */
    public PerceptionServiceConnector(Context appContext) {
        super(IPerceptionService.class, appContext, ACTION_BIND_PERCEPTION, PACKAGE_NAME);
    }

}
