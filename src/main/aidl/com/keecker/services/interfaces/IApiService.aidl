package com.keecker.services.interfaces;

import android.os.Bundle;

interface IApiService {

    /**
     * Checks which features provided by the installed Keecker Services
     * can be used by the client, given its version.
     *
     * @param clientInfo Bundle containing the following key value pairs:
     *         "version" with String value, version name of the client library
     *
     * @return Bundle containing the following key value pairs:
     *         "version" with String value, version of the Keecker Services
     *         "features" with a StringArrayList containing feature names
     *              "PROJECTOR_ACCESS_STATE"
     */

    Bundle checkApi(in Bundle clientInfo);
}
