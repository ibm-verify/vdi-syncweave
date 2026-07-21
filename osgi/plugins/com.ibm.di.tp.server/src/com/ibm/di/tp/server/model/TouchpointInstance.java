/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model;

import java.util.Collection;

import com.ibm.di.tp.server.model.config.DestinationData;
import com.ibm.di.tp.server.model.config.InstanceData;
import com.ibm.di.tp.server.model.config.StatusData;
import com.ibm.di.tp.server.model.exception.SCMPException;

/**
 * This class represents a Touchpoint Instance as defined by the SCMP/CaaS
 * specification. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public interface TouchpointInstance {

	/**
	 * @return an object representing the status of the instance at the moment
	 *         this method is called. Changing the data in the returned object
	 *         has no effect on this instance.
	 * @throws SCMPException
	 */
	public StatusData getStatus() throws SCMPException;

	/**
	 * @return the configuration data of this instance.
	 */
	public InstanceData getConfiguration() throws SCMPException;

	/**
	 * @param cfg
	 *            the new configuration data of this element.
	 * @throws Exception
	 */
	public void setConfiguration(InstanceData cfg) throws SCMPException;

	/**
	 * @return the raw id of this instance. This is provided when the instance
	 *         is created using
	 *         {@link TouchpointType#createInstance(String, org.w3c.dom.Element)}
	 *         method. Note this id has no limitation to what characters it
	 *         might contain.
	 */
	public String getId();

	/**
	 * @return the touchpoint type this instance belongs to.
	 */
	public TouchpointType getTouchpointType();

	/**
	 * Creates a {@link DestinationData}.
	 * 
	 * @param id
	 *            the id of the destination object
	 * @param cfg
	 *            the configuration data
	 * @return the newly created instance
	 * @throws Exception
	 */
	public TouchpointDestination createDestination(DestinationData cfg) throws SCMPException;

	/**
	 * Deletes the {@link DestinationData} corresponding to the specified id.
	 * This call silently succeeds even if the specified id does not exist.
	 * 
	 * @param dest
	 *            the {@link TouchpointDestination} to delete.
	 * @throws Exception
	 */
	public void deleteDestination(TouchpointDestination dest) throws SCMPException;

	/**
	 * @return a collection with the {@link TouchpointDestination}s. If no
	 *         objects have been created yet and the TP Instance role expects
	 *         the user to provide destinations an empty collection will be
	 *         returned. If the role does not support
	 *         {@link TouchpointDestination} a <code>null</code> will be
	 *         returned.
	 */
	public Collection<TouchpointDestination> getDestinations() throws SCMPException;

	/**
	 * @return the role this instance has.
	 */
	public TouchpointRole getRole();
}
