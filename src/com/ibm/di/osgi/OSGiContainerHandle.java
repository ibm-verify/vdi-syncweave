/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.osgi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class OSGiContainerHandle {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static OSGiContainerHandle instance;

	private Class<?> bundleContextClass;

	private Object systemBundleContext;

	private ClassLoader frameworkClassLoader;

	private OSGiContainerHandle() {

		OSGiLauncher osgi = new OSGiLauncher();
		// Starting the framework will register a shutdown hook that will
		// stop the OSGi Framework when TDI jvm stops. No need for us to do
		// so.
		osgi.start();

		try {
			bundleContextClass = osgi.getFrameworkClassLoader().loadClass("org.osgi.framework.BundleContext");
			systemBundleContext = osgi.getSystemBundleContext();
			frameworkClassLoader = osgi.getFrameworkClassLoader();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private OSGiContainerHandle(Object systemBundleContext, Class<?> bundleContextClass, ClassLoader frameworkClassLoader) {
		this.systemBundleContext = systemBundleContext;
		this.bundleContextClass = bundleContextClass;
		this.frameworkClassLoader = frameworkClassLoader;
	}

	public static void setSystemBundleContext(Object systemBundleContext, Class<?> bundleContextClass) {
		synchronized (OSGiContainerHandle.class) {
			instance = new OSGiContainerHandle(systemBundleContext, bundleContextClass, bundleContextClass.getClassLoader());
		}
	}

	public static OSGiContainerHandle getHandle() {
		return getHandle(true);
	}

	public static OSGiContainerHandle getHandle(boolean autostart) {
		synchronized (OSGiContainerHandle.class) {
			if (instance == null && autostart) {
				instance = new OSGiContainerHandle();
			}
			return instance;
		}
	}

	public Object getServiceReference(String serviceClazz) throws Throwable {
		Object[] services = getServices(serviceClazz, null);
		return services == null || services.length == 0 ? null : services[1];
	}

	
	
	public Object[] getServiceReferences(String serviceClazz, String filter) throws Throwable {
		try {
			Method srMethod = bundleContextClass.getMethod("getServiceReferences", new Class[] { String.class, String.class });
			Object srs = srMethod.invoke(systemBundleContext, new Object[] { serviceClazz, filter });
			return (Object[]) srs;
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}

		return null;
	}

	public Object getService(String serviceClazz) throws Throwable {
		Object[] services = getServices(serviceClazz, null);
		return services == null || services.length == 0 ? null : services[1];
	}

	public Object[] getServices(String serviceClazz, String filter) throws Throwable {
		Object[] srs = getServiceReferences(serviceClazz, filter);
		if (srs != null && srs.length > 0) {
			try {
				Class<?> srClass = frameworkClassLoader.loadClass("org.osgi.framework.ServiceReference");
				Method sMethod = bundleContextClass.getMethod("getService", new Class[] { srClass });
				ArrayList<Object> services = new ArrayList<Object>(srs.length);

				for (Object sr : srs) {
					Object srvc = sMethod.invoke(systemBundleContext, sr);
					if (srvc != null) {
						services.add(srvc);
					}
				}
				return services.toArray();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		}

		return null;
	}

	/**
	 * @param string
	 * @throws Throwable
	 */
	public boolean startBundle(String symbolicName) throws Throwable {
		Object b = null;
		try {
			b = findBundle(systemBundleContext, bundleContextClass, symbolicName);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}

		if (b != null) {
			return startBundle(b);
		}

		return false;
	}

	public boolean startBundle(Object bundleObject) throws Throwable {
		Class<?> bundleClass = bundleObject.getClass();
		Method state = bundleClass.getMethod("getState", (Class[]) null);
		Method start = bundleClass.getMethod("start", (Class[]) null);
		Method headers = bundleClass.getMethod("getHeaders", (Class[]) null);

		Dictionary dict = (Dictionary) headers.invoke(bundleObject, (Object[]) null);

		if ((((Integer) state.invoke(bundleObject, (Object[]) null) &
		//
		(/* Bundle.ACTIVE */0x00000020 | /* Bundle.STARTING */0x00000008)) == 0 ||

		/*
		 * check lazy activation policy as if set the state will be STARTING but
		 * not active
		 */
		"lazy".equals(dict.get("Bundle-ActivationPolicy")))) {
			try {
				start.invoke(bundleObject, (Object[]) null);
				return true;
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		}
		return false;
	}

	public Object[] findBundles(String targetBundlesPrefix) throws IllegalArgumentException, SecurityException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		return findBundles(systemBundleContext, bundleContextClass, targetBundlesPrefix);
	}

	public static Object[] findBundles(Object anyBundleContext, Class<?> bundleContextClass, String targetBundlesPrefix)
			throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, ClassNotFoundException {
		Object[] bundles = (Object[]) bundleContextClass.getMethod("getBundles", (Class[]) null).invoke(anyBundleContext,
				(Object[]) null);

		Class<?> bundleClass = bundleContextClass.getClassLoader().loadClass("org.osgi.framework.Bundle");
		Method name = bundleClass.getMethod("getSymbolicName", (Class[]) null);

		List<Object> result = new LinkedList<Object>();

		for (Object bundle : bundles) {
			Object symbolicName = name.invoke(bundle, (Object[]) null);
			if (symbolicName != null && ((String) symbolicName).startsWith(targetBundlesPrefix)) {
				result.add(bundle);
			}
		}
		return result.toArray();
	}

	public static Object findBundle(Object anyBundleContext, Class<?> bundleContextClass, String targetBundleSymbolicName)
			throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, ClassNotFoundException {

		Object[] bundles = (Object[]) bundleContextClass.getMethod("getBundles", (Class[]) null).invoke(anyBundleContext,
				(Object[]) null);

		Class<?> bundleClass = bundleContextClass.getClassLoader().loadClass("org.osgi.framework.Bundle");
		Method name = bundleClass.getMethod("getSymbolicName", (Class[]) null);

		for (Object bundle : bundles) {
			Object symbolicName = name.invoke(bundle, (Object[]) null);
			if (symbolicName == null) {
				// Log bundle with null symbolic name for debugging
				try {
					Method location = bundle.getClass().getMethod("getLocation", (Class[]) null);
					String bundleLocation = (String) location.invoke(bundle, (Object[]) null);
					System.err.println("WARNING: Bundle at location '" + bundleLocation + "' has null Bundle-SymbolicName");
				} catch (Exception e) {
					System.err.println("WARNING: Found bundle with null Bundle-SymbolicName (unable to get location)");
				}
			} else if (symbolicName.equals(targetBundleSymbolicName)) {
				return bundle;
			}
		}
		return null;
	}
}
