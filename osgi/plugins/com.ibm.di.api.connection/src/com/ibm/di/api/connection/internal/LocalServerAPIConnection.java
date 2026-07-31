/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.connection.internal;

import static com.ibm.di.api.connection.internal.ServerAPIConnectionService.L10N;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.EventListener;

import org.slf4j.LoggerFactory;

import com.ibm.di.api.DIException;
import com.ibm.di.api.connection.IServerAPIConnection;
import com.ibm.di.api.connection.internal.proxy.ApiAdapter;
import com.ibm.di.api.connection.internal.proxy.ConverterFactory;
import com.ibm.di.api.connection.internal.proxy.ApiAdapter.InstanceType;
import com.ibm.di.api.connection.internal.proxy.impl.ConverterFactoryImpl;
import com.ibm.di.api.connection.internal.track.RemoteReferenceTracker;
import com.ibm.di.api.remote.RemoteListener;
import com.ibm.di.api.remote.SessionFactory;

/**
 * This class is representing a connection to the Local TDI Server. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class LocalServerAPIConnection implements IServerAPIConnection {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private SessionFactory sf;
	private ConverterFactory cf;
	private RemoteReferenceTracker rrt;

	/**
	 * Calls the constructor ServerApiConnection(null, 0, true)
	 * 
	 * @throws DIException
	 *             if unable to obtain an instance of SessionFactory
	 */
	public LocalServerAPIConnection() throws DIException {
	}

	// used for unit testing purposes only
	protected LocalServerAPIConnection(Object sfInstance) {
		if (sfInstance instanceof com.ibm.di.api.local.SessionFactory) {
			rrt = new RemoteReferenceTracker();
			cf = new ConverterFactoryImpl(rrt);
			sf = (SessionFactory) createRemoteProxyForLocalInstance(new Class[] { SessionFactory.class }, sfInstance);
		} else if (sfInstance instanceof SessionFactory) {
			this.sf = (SessionFactory) sfInstance;
		}
	}

	/**
	 * Wraps a localInstance with a Proxy representing a remote interface.
	 * 
	 * @param localInstance
	 * @return the remote proxy of the local instance.
	 */
	protected Object createRemoteProxyForLocalInstance(Class<?>[] types, Object localInstance) {
		return Proxy.newProxyInstance(LocalServerAPIConnection.class.getClassLoader(), types, new ApiAdapter(localInstance,
				InstanceType.LOCAL, cf));
	}

	/**
	 * Checks whether the provided exception contains a known source exception.
	 * If it does the known exception is unwrapped and thrown instead.
	 * 
	 * @param e
	 *            the {@link InvocationTargetException} which might wrap a known
	 *            exception
	 * @throws DIException
	 *             which is the cause for the {@link InvocationTargetException}.
	 * @throws RemoteException
	 *             if an error occurs while communicating with the remote
	 *             server.
	 */
	private static void isKnownException(InvocationTargetException e) throws DIException, RemoteException {
		if (e.getCause() instanceof DIException) {
			throw (DIException) e.getCause();
		} else if (e.getCause() instanceof RemoteException) {
			throw (RemoteException) e.getCause();
		} else if (e.getCause() instanceof RuntimeException) {
			throw (RuntimeException) e.getCause();
		}
	}

	/**
	 * @return the contained SessionFactory for this instance.
	 * @throws NotBoundException
	 * @throws RemoteException
	 * @throws DIException
	 */
	public synchronized SessionFactory getSessionFactory() throws DIException {
		if (sf == null) {
			rrt = new RemoteReferenceTracker();
			cf = new ConverterFactoryImpl(rrt);
			sf = findSessionFactory();
		}
		return sf;
	}

	/**
	 * Obtains a {@link SessionFactory} to the Local Server API.
	 * 
	 * @return an instance to the SessionFactory object or null if one is not
	 *         available.
	 * @throws DIException
	 *             if unable to obtain an instance of SessionFactory
	 */
	private SessionFactory findSessionFactory() throws DIException {
		try {
			Class<?> apiEngineClass = Class.forName("com.ibm.di.api.APIEngine");
			Method getLocalSFMethod = apiEngineClass.getMethod("getLocalSessionFactory", new Class<?>[0]);
			Object sfInstance = getLocalSFMethod.invoke(null, new Object[0]);
			return (SessionFactory) createRemoteProxyForLocalInstance(new Class[] { SessionFactory.class }, sfInstance);
		} catch (ClassNotFoundException e) {
			return null;
		} catch (SecurityException e) {
			return null;
		} catch (NoSuchMethodException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (InvocationTargetException e) {
			try {
				isKnownException(e);
			} catch (RemoteException e1) {
				// this is impossible.
				e1.printStackTrace();
			}
		} catch (NoClassDefFoundError err) {
			LoggerFactory.getLogger(LocalServerAPIConnection.class).warn(L10N.getString("SERVER.API.CONNECTION.MISSING.LOCAL.API"));
		}
		return null;
	}

	/**
	 * This method prepares the user's implementation of the
	 * {@link EventListener} to be accessed by the remote server.
	 * 
	 * @param listener
	 *            the listener to export
	 * @param useSSL
	 *            specifies whether SSL should be used
	 * @param aUseCustomProperties
	 *            specifies whether to use the custom SSL properties and those
	 *            defined by the JVM.
	 * @return the exported listener
	 * @throws DIException
	 *             if an error occurs while exporting the listener
	 * @throws RemoteException
	 *             if an error occurs while communicating with the remote
	 *             server.
	 */
	public <L extends RemoteListener> L export(L listener, boolean useSSL, boolean useCustomProperties) throws DIException,
			RemoteException {
		// don't do anything ApiAdapter will handle it.
		return listener;
	}

	public synchronized void close() {
		if (sf != null) {
			sf = null;
			rrt.dispose();
			rrt = null;
		}
	}
}
