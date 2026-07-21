/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import com.ibm.di.function.SystemFunctions;

/**
 * A utility class for working with resources. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class ResourceLocator {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Finds a resource URL by its name. The lookup is made by using
	 * {@link #getResourceFromClassLoaderChain(String)} method. If the name does
	 * not start with a leading slash and the resource was not found the first
	 * time another try will be made by prefixing the name with a forward slash.
	 * 
	 * If the resource exists and an URL is obtained it is then resolved using
	 * the {@link #resolveLocalURL(URL)} method.
	 * 
	 * @param resourceName
	 *            the name to look for.
	 * @return the URL of the resource.
	 */
	public static URL getResourceURL(String resourceName) {
		URL url = getResourceFromClassLoaderChain(resourceName);

		if (url == null && !resourceName.startsWith("/")) {
			// when inside OSGi context the resource should be an absolute URI
			url = getResourceFromClassLoaderChain("/" + resourceName);
		}

		if (url != null && !"file".equals(url.getProtocol()) && !"jar".equals(url.getProtocol())
				&& !"zip".equals(url.getProtocol())) {
			url = resolveLocalURL(url);
		}

		return url;
	}

	/**
	 * This method looks for a resource using the ClassLoaders chain. The first
	 * classloader that is asked is the context loader, then the loader that has
	 * loaded the {@link ResourceLocator} class and finally the system loader.
	 * 
	 * @param resource
	 *            the name of the resource to look for.
	 * @return the url to the resource or <code>null</code> if not found. The
	 *         first classloader responding with non-null value is the winner
	 *         and its response is returned.
	 */
	public static URL getResourceFromClassLoaderChain(String resource) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL result = null;
		if (cl != null) {
			result = cl.getResource(resource);
		}

		if (result == null && cl != ResourceLocator.class.getClassLoader()) {
			cl = ResourceLocator.class.getClassLoader();
			result = cl.getResource(resource);
		}

		if (result == null) {
			cl = ClassLoader.getSystemClassLoader();
			result = cl.getResource(resource);
		}

		return result;
	}

	/**
	 * If we are inside an OSGi context the URL will not be a file but a
	 * bundleresource. Use this method to convert that URL to a file. If the url
	 * is already a File or the OSGi context is not present this method will not
	 * perform anything and the provided URL will be returned. The access to the
	 * eclipse code is done through reflection so it is safe to include this
	 * code in non-osgi context.
	 * 
	 * @param resource
	 *            the URL to convert
	 * @return a connection to the jar file containing the passes resource.
	 * @throws IOException
	 *             if there is a problem to connect to the jar file.
	 */
	public static URL resolveLocalURL(URL resource) {
		if (resource != null) {
			try {
				// convert an eclipse resource URL to a normal URL using
				// org.eclipse.core.runtime api
				Class<?> fileLocatorClass = Class.forName("org.eclipse.core.runtime.FileLocator");
				Method getUrlMethod = fileLocatorClass.getMethod("resolve", new Class[] { URL.class });
				resource = (URL) getUrlMethod.invoke(null, new Object[] { resource });
			} catch (ClassNotFoundException cnfe) {
				SystemFunctions.doNothing(); // ignore
			} catch (NoClassDefFoundError e) {
				SystemFunctions.doNothing(); // ignore
			} catch (SecurityException e) {
				SystemFunctions.doNothing(); // ignore
			} catch (NoSuchMethodException e) {
				SystemFunctions.doNothing(); // ignore
			} catch (IllegalArgumentException e) {
				SystemFunctions.doNothing(); // ignore
			} catch (IllegalAccessException e) {
				SystemFunctions.doNothing(); // ignore
			} catch (InvocationTargetException e) {
				SystemFunctions.doNothing(); // ignore
			}
		}
		return resource;
	}
}
