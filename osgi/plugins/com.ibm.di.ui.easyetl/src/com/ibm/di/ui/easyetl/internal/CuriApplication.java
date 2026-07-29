/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.easyetl.internal;

import org.glassfish.jersey.server.ResourceConfig;

import com.ibm.di.ui.curi.CuriHandler;
import com.ibm.di.ui.curi.CustomMedia2JaxbJSONProvider;

public class CuriApplication extends ResourceConfig {

    @SuppressWarnings("unused")
    private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    public CuriApplication() {

        // Lifecycle / bootstrap
        register(AppInitializer.class);

        // Resources
        register(CuriHandler.class);

        // Providers
        register(CustomMedia2JaxbJSONProvider.class);
    }
}
