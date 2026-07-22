/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model;

import java.rmi.RemoteException;
import java.util.Collection;

import com.ibm.di.tp.server.model.config.InstanceData;
import com.ibm.di.tp.server.model.config.PropertySheetDefinition;
import com.ibm.di.tp.server.model.exception.SCMPException;

/**
 * This class represents a Touchpoint Type as defined by the SCMP/CaaS
 * specification. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public interface TouchpointType {

	/**
	 * Creates a {@link TouchpointInstance} instance using the specified
	 * configuration.
	 * 
	 * @param instId
	 *            the id of the instance to set on the new instance
	 * @param gcfg
	 *            the instance configuration of this instance
	 * @return the new instance objects
	 * @throws SCMPException
	 */
	public TouchpointInstance createInstance(String instId, TouchpointRole role, InstanceData cfg) throws SCMPException;

	/**
	 * @return the list of instances for this particular type.
	 */
	public Collection<TouchpointInstance> getInstances() throws SCMPException;

	/**
	 * Disposes the instance.
	 * 
	 * @param the
	 *            instId of the instance object
	 * @throws DIException
	 * @throws RemoteException
	 */
	public void disposeInstance(String instId) throws SCMPException;

	/**
	 * @return the raw id of this type. This is generated based on the system
	 *         dependent type. Note this id has no limitation to what characters
	 *         it might contain.
	 */
	public String getId();

	/**
	 * @return a {@link Collection} containing all the roles this
	 *         {@link TouchpointType} supports.
	 */
	public Collection<TouchpointRole> getSupportedRoles() throws SCMPException;

	/**
	 * @return <code>true</code> if this {@link TouchpointType} has a
	 *         {@link PropertySheetDefinition} describing the properties an user
	 *         should provide when creating a {@link TouchpointInstance},
	 *         <code>false</code> otherwise.
	 */
	public boolean hasPropertySheetDefinition();

	/**
	 * Returns the {@link PropertySheetDefinition} for this
	 * {@link TouchpointType}. If {@link #hasPropertySheetDefinition()} returns
	 * <code>false</code> this method will return <code>null</code>.
	 * 
	 * @return the {@link PropertySheetDefinition} or <code>null</code>
	 * @throws Exception
	 */
	public PropertySheetDefinition getPropertySheetDefinition() throws SCMPException;

}
