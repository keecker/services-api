/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.interfaces.projection;

import android.content.Context;

import com.keecker.services.interfaces.utils.KeeckerServiceConnection;

/** @hide */
public class ProjectorServiceConnector extends KeeckerServiceConnection<IProjectorService>{

    private static final String PACKAGE_NAME = "com.keecker.services.interfaces";
    private static final String ACTION_BIND_PROJECTOR =
            "com.keecker.services.interfaces.ACTION_BIND_KEECKER_PROJECTOR";

    @Deprecated
    /** Once all service connections are managed by dagger this method should be removed */
    public static ProjectorServiceConnector getServiceConnection(final Context context) {
        return new ProjectorServiceConnector(context);
    }

    public ProjectorServiceConnector(Context context) {
        super(IProjectorService.class, context,
                ACTION_BIND_PROJECTOR,
                PACKAGE_NAME);
    }

}
