/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.listener;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.ServletContext;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.AssemblyLineListener;
import com.ibm.di.api.bind.ConfigFileListener;
import com.ibm.di.api.bind.DIEventListener;
import com.ibm.di.api.bind.LogListener;
import com.ibm.di.api.bind.PollChannel;
import com.ibm.di.api.bind.PushChannel;
import com.ibm.di.api.connection.IServerAPIConnection;
import com.ibm.di.api.remote.ConfigurationFileListener;
import com.ibm.di.api.remote.RemoteListener;
import com.ibm.di.api.rest.internal.AppConstants;

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
public class ListenerFactory {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final IServerAPIConnection serverApi;
	private boolean customProps;
	private boolean useSSL;
	private final ServletContext sctx;

	public ListenerFactory(IServerAPIConnection serverApi, ServletContext sctx) {
		this.serverApi = serverApi;
		this.sctx = sctx;
		this.useSSL = Boolean.getBoolean(APIEngine.PROP_API_REMOTE_SSL_ON);
		this.customProps = Boolean.getBoolean("api.client.ssl.custom.properties.on");
	}

	/**
	 * Creates a new {@link com.ibm.di.api.remote.LogListener} based on the
	 * represented object. Before registering this instance with the Server API
	 * you need to export it through the {@link #export(RemoteListener)} method.
	 * 
	 * @param contextId
	 *            the string uniquely identifying the context for which a new
	 *            instance is being created.
	 * @param listenerId
	 *            the identifier of the listener unique in the scope of the
	 *            context for which a new instance is being created.
	 * @param l
	 *            the representation of the listener
	 * @return the log listener.
	 * @throws DIException
	 * @throws RemoteException
	 */
	public com.ibm.di.api.remote.LogListener newInstance(LogListener l) throws DIException, RemoteException {
		try {
			com.ibm.di.api.remote.LogListener inst = null;
			if (l.getChannel() instanceof PushChannel) {
				inst = new LogHttpForwarder((PushChannel) l.getChannel());
			} else if (l.getChannel() instanceof PollChannel) {
				inst = new LogQueueProducer((PollChannel) l.getChannel(), sctx);
			} else {
				throw new IllegalArgumentException(AppConstants.L10N.getString("REST.API.LISTENER.NO.CHANNEL"));
			}

			return inst;
		} catch (MalformedURLException e) {
			throw new DIException(e.getMessage());
		}
	}

	/**
	 * Creates a new {@link com.ibm.di.api.remote.AssemblyLineListener} based on
	 * the represented object. Before registering this instance with the Server
	 * API you need to export it through the {@link #export(RemoteListener)}
	 * method.
	 * 
	 * @param l
	 *            the representation of the listener
	 * @return the al listener.
	 * @throws DIException
	 * @throws RemoteException
	 */
	public com.ibm.di.api.remote.AssemblyLineListener newInstance(AssemblyLineListener l) throws DIException, RemoteException {
		try {
			com.ibm.di.api.remote.AssemblyLineListener inst = null;
			if (l.getChannel() instanceof PushChannel) {
				inst = new ALHttpForwarder((PushChannel) l.getChannel());
			} else if (l.getChannel() instanceof PollChannel) {
				inst = new ALQueueProducer((PollChannel) l.getChannel(), sctx);
			} else {
				throw new IllegalArgumentException(AppConstants.L10N.getString("REST.API.LISTENER.NO.CHANNEL"));
			}
			return inst;
		} catch (MalformedURLException e) {
			throw new DIException(e.getMessage());
		}
	}

	/**
	 * Creates a new {@link com.ibm.di.api.remote.DIEventListener} based on the
	 * represented object. Before registering this instance with the Server API
	 * you need to export it through the {@link #export(RemoteListener)} method.
	 * 
	 * @param l
	 *            the representation of the listener
	 * @return the event listener.
	 * @throws DIException
	 * @throws RemoteException
	 */
	public com.ibm.di.api.remote.DIEventListener newInstance(DIEventListener l) throws DIException, RemoteException {
		try {
			com.ibm.di.api.remote.DIEventListener inst = null;
			if (l.getChannel() instanceof PushChannel) {
				inst = new DIEventHttpForwarder((PushChannel) l.getChannel());
			} else if (l.getChannel() instanceof PollChannel) {
				inst = new DIEventQueueProducer((PollChannel) l.getChannel(), sctx);
			} else {
				throw new IllegalArgumentException(AppConstants.L10N.getString("REST.API.LISTENER.NO.CHANNEL"));
			}
			return inst;
		} catch (MalformedURLException e) {
			throw new DIException(e.getMessage());
		}
	}

	/**
	 * Creates a new {@link } based on the represented object. Before registering
	 * this instance with the Server API you need to export it through the
	 * {@link #export(RemoteListener)} method.
	 * 
	 * @param l
	 *            the representation of the listener
	 * @return the event listener.
	 * @throws DIException
	 * @throws RemoteException
	 */
	public ConfigurationFileListener newInstance(ConfigFileListener l) throws DIException, RemoteException {
		try {
			ConfigurationFileListener inst = null;
			if (l.getChannel() instanceof PushChannel) {
				inst = new ConfigFileEventHttpForwarder((PushChannel) l.getChannel());
			} else if (l.getChannel() instanceof PollChannel) {
				inst = new ConfigFileEventQueueProducer((PollChannel) l.getChannel(), sctx);
			} else {
				throw new IllegalArgumentException(AppConstants.L10N.getString("REST.API.LISTENER.NO.CHANNEL"));
			}
			return inst;
		} catch (MalformedURLException e) {
			throw new DIException(e.getMessage());
		}
	}

	public <L extends RemoteListener> L export(L listener) throws RemoteException, DIException {
		return serverApi.export(listener, useSSL, customProps);
	}
}
