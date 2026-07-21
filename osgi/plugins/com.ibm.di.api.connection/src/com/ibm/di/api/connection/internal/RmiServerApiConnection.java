/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.connection.internal;

import static com.ibm.di.api.connection.internal.ServerAPIConnectionService.L10N;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import com.ibm.di.api.DIException;
import com.ibm.di.api.connection.IServerAPIConnection;
import com.ibm.di.api.remote.RemoteListener;
import com.ibm.di.api.remote.SessionFactory;

/**
 * Represents a connection to the Remote API Server. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class RmiServerApiConnection implements IServerAPIConnection {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private SessionFactory sf;
	private final String host;
	private final int port;

	/**
	 * Calls the constructor ServerApiConnection(host, port, false)
	 * 
	 * @throws DIException
	 *             if unable to obtain an instance of SessionFactory
	 */
	public RmiServerApiConnection(String host, int port) throws DIException {
		if (host != null && host.trim().length() > 0 && port > 0) {
			// keep the settings so we can obtain a session factory later.
			this.host = host;
			this.port = port;
		} else {
			throw new IllegalArgumentException(L10N.getString("SERVER.API.CONNECTION.IVALID.HOST.OR.PORT", new Object[] { host,
					Integer.toString(port) }));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.api.connection.IServerAPIConnection#getSessionFactory()
	 */
	public synchronized SessionFactory getSessionFactory() throws RemoteException, NotBoundException {
		if (sf == null) {
			try {
				sf = (SessionFactory) Naming.lookup("rmi://" + host + ":" + port + "/SessionFactory");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return sf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.api.connection.IServerAPIConnection#export(com.ibm.di.api.
	 * remote.RemoteListener, boolean, boolean)
	 */
	@SuppressWarnings("unchecked")
	public <L extends RemoteListener> L export(L listener, boolean useSSL, boolean useCustomProperties) throws DIException,
			RemoteException {
		L result = null;
		if (listener.getClass().getPackage().getName().equals("com.ibm.di.api.remote.impl")
				&& listener.getClass().getCanonicalName().endsWith("Base")) {
			// one of ours after exporting... no need to reexport.
			result = listener;
		} else {
			// prepare the remote objects
			Set<Class<? extends RemoteListener>> ifaces = getRemoteListenerInterfaces(listener.getClass());
			// only the first interface is exported for now not all of them.
			Class<? extends RemoteListener> iface = ifaces.iterator().next();

			String baseClassName = "com.ibm.di.api.remote.impl." + iface.getSimpleName() + "Base";
			String createInstanceMethodName = "createInstance";
			try {
				Class<?> baseClass = Class.forName(baseClassName);
				Method createInstanceMethod = baseClass.getMethod(createInstanceMethodName, new Class[] { listener.getClass(),
						boolean.class, boolean.class });
				result = (L) createInstanceMethod.invoke(null, listener, useSSL, useCustomProperties);
			} catch (ClassNotFoundException e) {
				throw new RemoteException(e.getLocalizedMessage(), e);
			} catch (SecurityException e) {
				throw new RemoteException(e.getLocalizedMessage(), e);
			} catch (NoSuchMethodException e) {
				throw new RemoteException(e.getLocalizedMessage(), e);
			} catch (IllegalArgumentException e) {
				throw new RemoteException(e.getLocalizedMessage(), e);
			} catch (IllegalAccessException e) {
				throw new RemoteException(e.getLocalizedMessage(), e);
			} catch (InvocationTargetException e) {
				if (e.getCause() instanceof DIException) {
					throw (DIException) e.getCause();
				} else if (e.getCause() instanceof RemoteException) {
					throw (RemoteException) e.getCause();
				} else if (e.getCause() instanceof RuntimeException) {
					throw (RuntimeException) e.getCause();
				}
			}
		}
		return result;
	}

	private static Set<Class<? extends RemoteListener>> getRemoteListenerInterfaces(Class<?> listener) {
		Set<Class<? extends RemoteListener>> set = new HashSet<Class<? extends RemoteListener>>();
		getRemoteListenerInterfaces(listener, set);
		return set;
	}

	@SuppressWarnings("unchecked")
	private static void getRemoteListenerInterfaces(Class<?> listener, Set<Class<? extends RemoteListener>> set) {
		if (listener == null || listener == Object.class) {
			return;
		}

		Class<?>[] interfaces = listener.getInterfaces();
		if (interfaces != null) {
			for (Class<?> iface : interfaces) {
				if (RemoteListener.class.isAssignableFrom(iface)) {
					set.add((Class<? extends RemoteListener>) iface);
				}
			}
		}
		getRemoteListenerInterfaces(listener.getSuperclass(), set);
	}
}
