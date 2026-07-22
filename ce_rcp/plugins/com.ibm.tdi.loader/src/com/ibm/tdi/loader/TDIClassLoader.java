/*
 * Copyright IBM Corp. 2010, 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.ArrayList;

import org.eclipse.osgi.internal.loader.ModuleClassLoader;

import org.eclipse.osgi.framework.util.KeyedElement;
import org.eclipse.osgi.internal.hookregistry.ClassLoaderHook;
import org.eclipse.osgi.internal.loader.classpath.ClasspathEntry;
import org.eclipse.osgi.internal.loader.classpath.ClasspathManager;
import org.eclipse.osgi.storage.BundleInfo.Generation;

/**
 * This class is used to fall back on the TDI class loader if the eclipse class loaders cannot find a class/resource.
 */

public class TDIClassLoader extends ClassLoaderHook implements KeyedElement {

    private static final String TDI_HOME_DIR    = "TDI_HOME_DIR";
    private static final String TDI_LOADER_PATH = "com.ibm.di.loader.IDILoader.path";

    public static final String KEY      = TDIClassLoader.class.getName();
    public static final int    HASHCODE = KEY.hashCode();

    private ClassLoader tdiLoader;

    public TDIClassLoader() {

        /*
         * Create our TDI class loader which will be the 'fallback' loader if
         * the standard loader is unable to find the class/resource.
         */

        try {
            File idl = new File(getTDIHomeDir(), "IDILoader.jar");

            final URL idlURL = idl.toURI().toURL();

            URLClassLoader ucl = AccessController.doPrivileged(
                                new PrivilegedAction<URLClassLoader>() {
                public URLClassLoader run() {
                    return new URLClassLoader(new URL[] { idlURL });
                }
            });

            Class<?> cls = ucl.loadClass("com.ibm.di.loader.ServerLauncher");

            tdiLoader = (ClassLoader)cls.getMethod("initClassLoader", 
                            (Class<?>[]) null).invoke(null, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean addClassPathEntry(
                ArrayList<ClasspathEntry> cpEntries, 
                String                    cp, 
                ClasspathManager          hostmanager, 
                Generation                sourceGeneration) {
        /*
         * Don't do anything if the classpath is "."
         */

        if (cp.equals(".")) {
            return false;
        }

        /*
         * We have a special case where the IDILoader.jar file is actually
         * present in the TDI home directory, even though MANIFEST.MF
         * specifies the 'jars' directory.  If we move the IDILoader.jar file
         * other things break and so it is easier to massage the class path
         * here.
         */

        if (cp.equals("jars/IDILoader.jar")) {
            cp = "IDILoader.jar";
        }

        /*
         * Check to see if the class path is relative to the TDI home.  If
         * it is we add in the classpath entry.
         */

        String absCp  = new String(getTDIHomeDir() + "/" + cp);
        File   cpFile = new File(absCp);

        if (cpFile.exists()) {
            ClasspathEntry entry = hostmanager.getExternalClassPath(
                                            absCp, sourceGeneration);
            cpEntries.add(entry);

            return true;
        } 

        return false;
    }

    @Override
    public String findLocalLibrary(Generation generation, String libName) {
        try {
            /*
             * Work out the absolute file name for the library.
             */

            String path = getTDIHomeDir() + "/libs/" + libName;

            if ((System.getProperty("os.name").indexOf("Windows") != -1)
					&& (!path.endsWith(".dll"))) {
                path += ".dll";
            }

            /*
             * See if the file exists.
             */

            File file = new File(path);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
			
            /*
             * Check to see whether the library is local to the solution
             * directory. The Solution directory is ALWAYS the current 
             * directory. There is no other sensible way to retrieve this 
             * directory. Multiple CE instances may run at the same time with 
             * different solution directories, and since ibmditk does a 
             * change-directory to the soldir we can assume this is also the 
             * correct place.
             */

            String solutionDirectory = new File(".").getAbsolutePath();

            path = solutionDirectory + "/libs/" + libName;

            if ((System.getProperty("os.name").indexOf("Windows") != -1)
					&& (!path.endsWith(".dll"))) {
                path += ".dll";
            }
	
            /*
             * See if the file exists.
             */

            file = new File(path);
            if (file.exists()) {
                return file.getAbsolutePath();
            }

        } catch (Exception ignore) {
            return null;
        }

        return null;
    }

    @Override
    public String postFindLibrary(String name, ModuleClassLoader classLoader) {
        return findLocalLibrary(null, name);
    }

    @Override
    public Class<?> preFindClass(String name, ModuleClassLoader classLoader) 
                throws ClassNotFoundException {

        /*
         * It is important that the com.ibm.di.connector object is loaded by
         * the TDI class loader, otherwise we cannot cast a connector object
         * to the base class.  Java uses the class name and class loader to
         * identify a class.
         */

        try {
            if (name.startsWith("com.ibm.")) {
                return tdiLoader.loadClass(name);
            }

        } catch (ClassNotFoundException e) {
        }

        return null;
    }

    @Override
    public Class<?> postFindClass(String name, ModuleClassLoader classLoader) 
                throws ClassNotFoundException {
        return tdiLoader.loadClass(name);
    }

    @Override
    public URL postFindResource(String name, ModuleClassLoader classLoader) 
                throws FileNotFoundException {
        return tdiLoader.getResource(name);
    }

    public boolean compare(KeyedElement other) {
        return other.getKey() == KEY;
    }

    public Object getKey() {
        return KEY;
    }

    public int getKeyHashCode() {
        return HASHCODE;
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

