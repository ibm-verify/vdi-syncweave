/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.loader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.osgi.internal.hookregistry.HookConfigurator;
import org.eclipse.osgi.internal.hookregistry.HookRegistry;
import org.eclipse.osgi.internal.hookregistry.ActivatorHookFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * This class is an OSGI Adaptor hook that provides access to all of TDI's jar
 * files. 
 * 
 * In eclipse/configuration/config.ini (as an example): # TDI class loader
 * osgi.framework.extensions=com.ibm.tdi.loader
 * osgi.hook.configurators.include=com.ibm.tdi.loader.TDIClassLoaderHook
 * 
 * In addition, the "com.ibm.tdi.loader" plugin must reside in the same
 * directory as the "org.eclipse.osgi" plugin.
 */

public class TDIClassLoaderHook implements HookConfigurator {

    @Override
    public void addHooks(HookRegistry hookRegistry) {
        hookRegistry.addClassLoaderHook(new TDIClassLoader());
        hookRegistry.addActivatorHookFactory(new TDIActivationHookFactory());
    }

    public class TDIActivationHookFactory implements ActivatorHookFactory {
        @Override
	public BundleActivator createActivator() {
            return new TDIBundleActivator();
        }
    }

    public class TDIBundleActivator implements BundleActivator {
        private static final String TDI_HOME_DIR    = "TDI_HOME_DIR";
        private static final String TDI_LOADER_PATH = "com.ibm.di.loader.IDILoader.path";

        @Override
        public void start(BundleContext context) {
            File pluginsDir = new File(getTDIHomeDir(), "osgi/plugins");
            if (pluginsDir.isDirectory()) {
                for (File plugin : pluginsDir.listFiles()) {
                    try {
                        if (!plugin.getName().startsWith("com.ibm."))
                        {
                            continue;
                        }

                        URL url = plugin.toURI().toURL();
                        context.installBundle(url.toString());

                    } catch (MalformedURLException e) {
                        continue;
                    } catch (BundleException e) {
                        continue;
                    }
                }
            }
        }

        @Override
        public void stop(BundleContext context) {
        }

        private String getTDIHomeDir() {
            String str = System.getProperty(TDI_HOME_DIR);

            if (str == null || str.length() == 0) {
                str = System.getProperty(TDI_LOADER_PATH);
            }

            if (str == null || str.length() == 0) {
                str = System.getenv(TDI_HOME_DIR);
            }

            return str;
        }
    }
}

