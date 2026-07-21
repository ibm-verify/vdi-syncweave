/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.remote.RemoteListener;
import com.ibm.di.server.ResourceHash;

/**
 * This class is used to adapt from Remote to Local listener interfaces. This
 * adapter expects that the methods don't receive Remote references but only
 * serializable objects. Each method is a callback method that is not expected
 * to return a value. <br>
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class RemoteListenerAdapter implements InvocationHandler {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	private final RemoteListener remoteListener;

	private <R extends RemoteListener> RemoteListenerAdapter(R remoteListener) {
		this.remoteListener = remoteListener;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		// listener is registered in a list or map... since the remote listeners
		// are being pushed through rmi we need to use them as identifiers. Make
		// sure hashCode and equals methods are overridden correctly.
		if ("hashCode".equals(method.getName())) {
			return remoteListener.hashCode();
		} else if ("equals".equals(method.getName())) {
			if (args.length > 0 && args[0] != null && Proxy.isProxyClass(args[0].getClass())) {
				InvocationHandler ih = Proxy.getInvocationHandler(args[0]);
				if (ih instanceof RemoteListenerAdapter) {
					return ((RemoteListenerAdapter) ih).remoteListener.equals(remoteListener);
				}
			}
			return false;
		}

		// find the callback method
		Method remoteListenerMethod = remoteListener.getClass().getMethod(method.getName(), method.getParameterTypes());

		try {
			// invoke the method with the same arguments
			return remoteListenerMethod.invoke(remoteListener, args);
		} catch (InvocationTargetException e) {
			APIEngine.logWarn(sResHash.getString("SERVER.API.REMOTEEXCEPTION.ON.MESSAGELOGGED", e.getCause()));
			if (e.getCause() instanceof RemoteException) {
				checkForConnectionFailure((RemoteException) e.getCause());
			}
		}
		return null;
	}

	/**
	 * Determine whether the specified remote exception signals a connection
	 * failure.
	 * 
	 * @param ex
	 *            RMI exception.
	 * @throws RuntimeException
	 *             If the remote exception is a connection failure.
	 */
	private static final void checkForConnectionFailure(RemoteException ex) throws RuntimeException {
		if (ex instanceof java.rmi.ConnectException || ex instanceof java.rmi.ConnectIOException) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Creates a new adapter instance for a {@link RemoteListener}
	 * 
	 * @param <L>
	 *            The type of the remote lister
	 * @param <T>
	 *            the type of the local listener to adapt
	 * @param fromRemoteListener
	 *            the remote listener instance to adapt
	 * @param toClass
	 *            the local listener class
	 * @return a proxy instance that implements the specified local listener
	 *         class and delegates each method invocation to the specified
	 *         remote listener instance
	 */
	@SuppressWarnings("unchecked")
	public static final <L extends RemoteListener, T extends EventListener> T create(L fromRemoteListener, Class<T> toClass) {
		return (T) Proxy.newProxyInstance(toClass.getClassLoader(), new Class<?>[] { toClass }, new RemoteListenerAdapter(
				fromRemoteListener));
	}

	/**
	 * Creates a new adapter instance for a {@link RemoteListener}. Unlike the
	 * {@link #create(RemoteListener, Class)} method this one tries to
	 * automatically find the corresponding local listener for which an adapter
	 * will be created.
	 * 
	 * @param <L>
	 *            The type of the remote lister
	 * @param fromRemoteListener
	 *            the remote listener instance to adapt
	 * @return a proxy instance that implements the resolved local listener
	 *         class and delegates each method invocation to the specified
	 *         remote listener instance
	 */
	public static final EventListener create(RemoteListener fromRemoteListener, Class<? extends RemoteListener>[] interfaces)
			throws ClassNotFoundException {
		Class<?>[] ifaces = null;

		if (interfaces.length == 1) {
			ifaces = new Class<?>[1];
			ifaces[0] = getLocalClass(interfaces[0]);
			if (ifaces[0] == null) {
				throw new IllegalArgumentException(fromRemoteListener.getClass().getName());
			}
		} else {
			List<Class<?>> list = new ArrayList<Class<?>>(interfaces.length);
			for (Class<? extends RemoteListener> item : interfaces) {
				Class<?> local = getLocalClass(item);
				if (local != null) {
					list.add(local);
				}
			}
			ifaces = list.toArray(new Class[list.size()]);
		}

		return (EventListener) Proxy.newProxyInstance(fromRemoteListener.getClass().getClassLoader(), ifaces,
				new RemoteListenerAdapter(fromRemoteListener));
	}

	private static Class<?> getLocalClass(Class<? extends RemoteListener> cls) {
		try {
			return Class.forName("com.ibm.di.api.local." + cls.getSimpleName());
		} catch (ClassNotFoundException e) {
			return null; // ignore
		} catch (NoClassDefFoundError e) {
			return null; // ignore
		}
	}
}
