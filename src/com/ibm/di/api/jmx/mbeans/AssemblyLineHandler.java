/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.DIException;
import com.ibm.di.api.jmx.JMXAgent;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * 
 * This class implements several methods to handle an AssemblyLine.
 * 
 */
public class AssemblyLineHandler extends BaseAdmin implements
		AssemblyLineHandlerMBean {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Type of the MBean.
	 */
	public static final String MBEAN_TYPE = "AssemblyLineHandler";

	/**
	 * com.ibm.di.api.local.AssemblyLineHandler
	 */
	private com.ibm.di.api.local.AssemblyLineHandler mALHandler = null;

	/**
	 * ID.
	 */
	private String mId = null;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Class constructor
	 * 
	 * @param aALHandler
	 *            com.ibm.di.api.local.AssemblyLineHandler
	 * @throws DIException
	 */
	public AssemblyLineHandler(
			com.ibm.di.api.local.AssemblyLineHandler aALHandler)
			throws DIException {
		mALHandler = aALHandler;
		mId = aALHandler.getAssemblyLine().getName()
				+ "."
				+ Integer
						.toString(aALHandler.getAssemblyLine().getUniqueCode());
	}

	// MBean interface

	/**
	 * Reads attribute "Type".
	 * <p>
	 * <code>getType()</code> and <code>getId()</code> are used in a common
	 * schema for object names for all MBeans in the management package. The key
	 * properties part of the object name of each MBean is defined as
	 * <code>"type=" + getType() + ",id=" + getId()</code>, for example
	 * "type=AssemblyLine,id=Hello".
	 * 
	 * @return the type of this MBean.
	 */
	public String getType() {
		return MBEAN_TYPE;
	}

	/**
	 * Reads attribute "Id". The "Id" value should be different for different
	 * MBeans of the same type.
	 * <p>
	 * <code>getType()</code> and <code>getId()</code> are used in a common
	 * schema for object names for all MBeans in the management package. The key
	 * properties part of the object name of each MBean is defined as
	 * <code>"type=" + getType() + ",id=" + getId()</code>, for example
	 * "type=AssemblyLine,id=Hello".
	 * 
	 * @return the Id of this MBean.
	 */
	public String getId() {
		return mId;
	}

	/**
	 * Returns an ObjectName generated from the AssemblyLine's name and
	 * AssemblyLine's unique code.
	 * 
	 * @return the ObjectName of the AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while getting the AssemblyLine's
	 *             ObjectName.
	 */
	public ObjectName getAssemblyLine() throws DIException {
		checkIfUserCanExecuteAL();

		return com.ibm.di.api.jmx.mbeans.AssemblyLine.genObjectName(mALHandler
				.getAssemblyLine().getName(), mALHandler.getAssemblyLine()
				.getUniqueCode());
	}

	/**
	 * Executes an AssemblyLine cycle. If an Entry is provided then it becomes
	 * the work entry.
	 * 
	 * @param aEntry
	 *            the work entry to use, or null to use an empty work
	 *            entry/iterator entry.
	 * @param aProcessTCB
	 *            if true, the AL's call/return attribute maps are applied to
	 *            the provided entry and returned entry.
	 * @return the work entry at the end of the cycle.
	 * @throws DIException
	 *             if an error occurs while executing the AssemblyLine.
	 */
	public Entry executeCycle(Entry aEntry, Boolean aProcessTCB)
			throws DIException {
		checkIfUserCanExecuteAL();

		return mALHandler.executeCycle(aEntry, aProcessTCB.booleanValue());
	}

	/**
	 * Executes an AssemblyLine cycle. If an Entry is provided then it becomes
	 * the work entry.
	 * 
	 * @param aEntry
	 *            the work entry to use, or null to use an empty work
	 *            entry/iterator entry.
	 * @return the work entry at the end of the cycle.
	 * @throws DIException
	 *             if an error occurs while executing the AssemblyLine.
	 */
	public Entry executeCycle(Entry aEntry) throws DIException {
		checkIfUserCanExecuteAL();

		return mALHandler.executeCycle(aEntry);
	}

	/**
	 * Executes an AssemblyLine cycle with null work Entry attribute.
	 * 
	 * @return the work entry at the end of the cycle
	 * @throws DIException
	 *             if an error occurs while executing the AssemblyLine.
	 */
	public Entry executeCycle() throws DIException {
		checkIfUserCanExecuteAL();

		return mALHandler.executeCycle();
	}

	/**
	 * Closes the Assembly LineHandler MBean.
	 * 
	 * @throws DIException
	 *             if error occurs while closing the MBean.
	 */
	public void close() throws DIException {
		checkIfUserCanExecuteAL();

		mALHandler.close();
		try {
			JMXAgent.unregisterMBean(new ObjectName(
					JMXAgent.MBEAN_SERVER_DOMAIN + ":" + getKeyPropertyList()));
		} catch (JMException e) {
			APIEngine
					.logError(sResHash
							.getString(
									"SEVER.API.COULD.NOT.UNREGISTER.ASSEMBLYLINE.HANDLER.MBEAN",
									e.toString()));
		}
	}

	/**
	 * Generates object name for specified assembly line handler.
	 * 
	 * @param aName
	 *            the name of the assembly line handler.
	 * @param aUniqueCode
	 *            unique code used for building the AssemblyLineHandler MBean
	 *            id.
	 * @return the generated object name
	 * 
	 * @throws DIException
	 *             if error occurs while creating AssemblyLineHandler JMX object
	 *             name.
	 */
	public static ObjectName genObjectName(String aName, int aUniqueCode)
			throws DIException {
		String id = aName + "." + aUniqueCode;
		String keyProperties = "type=" + MBEAN_TYPE + ",id=" + id;
		ObjectName objectName = null;
		try {
			objectName = new ObjectName(JMXAgent.MBEAN_SERVER_DOMAIN + ":"
					+ keyProperties);
		} catch (MalformedObjectNameException e) {
			APIEngine
					.logErrorAndThrowException(
							sResHash
									.getString("SEVER.API.COULD.NOT.CREATE.ASSEMBLYLINE.HANDLER.JMX.OBJECT.NAME"),
							e);
		}
		return objectName;
	}

	/**
	 * Checks if the current user could execute the AssemblyLine associated with
	 * this AssemblyLineHandler.
	 * 
	 * @throws DIException
	 *             if Runtime or Security exception occurs.
	 */
	private void checkIfUserCanExecuteAL() throws DIException {
		String userId = getCurrentUserId();
		String configId = mALHandler.getAssemblyLine().getConfigInstance()
				.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, mALHandler.getAssemblyLine().getName())) {
			throw new AuthorizationException();
		}
	}

}
