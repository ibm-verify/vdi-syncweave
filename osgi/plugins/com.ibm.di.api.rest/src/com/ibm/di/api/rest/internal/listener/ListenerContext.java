/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.listener;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import com.ibm.di.web.common.atom.AtomText;
import com.ibm.di.web.common.atom.AtomCategory;
import com.ibm.di.web.common.atom.AtomLink;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.Listener;
import com.ibm.di.api.remote.RemoteListener;
import com.ibm.di.api.rest.internal.registry.ListenerRegistry.ListenerRegistration;

/**
 * Defines a common interface for working with Server API listeners. Instances
 * know how to convert from a POJO object describing the listener to an actual
 * RemoteListener and vice versa. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public interface ListenerContext<L extends Listener> {

	/**
	 * @return a map of the listener ids and the actual objects representing a
	 *         listener.
	 */
	public Map<String, L> list() throws DIException, RemoteException;

	/**
	 * Creates a new listener by using the specified listener representation
	 * object.
	 * 
	 * @param listener
	 *            the listener representation
	 * @return the id under which this listener can further be looked up.
	 * @throws RemoteException
	 */
	public String create(L listener) throws DIException, RemoteException;

	/**
	 * Deletes the listener under the specified id.
	 * 
	 * @param lId
	 *            the id of the listener
	 * @throws DIException
	 * @throws RemoteException
	 */
	public void delete(String lId) throws RemoteException, DIException;

	/**
	 * Obtains a listener representation for the specified id
	 * 
	 * @param lId
	 *            the id of the listener
	 * @return the listener representation object
	 */
	public L get(String lId) throws DIException, RemoteException;

	/**
	 * @return the categories describing the type of the listener referred to
	 *         using its identifier
	 * @throws DIException
	 * @throws RemoteException
	 */
	public List<AtomCategory> getCategories(String listenerId) throws RemoteException, DIException;

	/**
	 * Return the links describing the capabilities of the listener referred to
	 * by the <code>listenerId</code>
	 * 
	 * @param baseUri
	 *            the base uri of the server, used to expand the links with
	 * @param listenerId
	 *            the listener identifier unique in this context
	 * @return a list of links
	 * @throws DIException
	 * @throws RemoteException
	 */
	public List<AtomLink> getLinks(URI baseUri, String listenerId) throws RemoteException, DIException;

	/**
	 * Used to plug in a custom attaching/detaching mechanism into the Listener
	 * creation/deletion routines, that replaces the actual Server API
	 * registration.
	 * 
	 * @param <L>
	 *            the type of listener is being attached.
	 */
	public static interface ListenerAttacher<R extends RemoteListener> {

		public void attachListener(R l) throws RemoteException, DIException;

		public void detachListener(ListenerRegistration<R, ? extends Listener> r) throws RemoteException, DIException;
	}
}
