/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.DIException;
import com.ibm.di.api.jmx.JMXAgent;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.TaskStatistics;

/**
 * Represents an AssemblyLine instance.
 */
public class AssemblyLine extends BaseAdmin implements AssemblyLineMBean {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Type of the MBean.
	 */
	public static final String MBEAN_TYPE = "AssemblyLine";

	/**
	 * com.ibm.di.api.local.AssemblyLine
	 */
	private com.ibm.di.api.local.AssemblyLine mAssemblyLine = null;

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
	 * @param aAssemblyLine
	 *            assembly line to set.
	 * @throws DIException
	 *             DIException if an error occurs while retrieving the name of
	 *             the AssemblyLine.
	 */
	public AssemblyLine(com.ibm.di.api.local.AssemblyLine aAssemblyLine)
			throws DIException {
		mAssemblyLine = aAssemblyLine;
		mId = aAssemblyLine.getName() + "."
				+ Integer.toString(aAssemblyLine.getUniqueCode());
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
	 * 
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
	 * Returns ObjectName generated from the AssemblyLine's configuration ID,
	 * gotten from the AssemblyLine's configuration instance.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var jmxUrl = new javax.management.remote.JMXServiceURL(
	 * 		&quot;service:jmx:rmi://localhost/jndi/rmi://localhost:1099/jmxconnector&quot;);
	 * var jmxConnector = javax.management.remote.JMXConnectorFactory.connect(jmxUrl);
	 * var jmxMBeanServer = jmxConnector.getMBeanServerConnection();
	 * //for local access use:
	 * //MBeanServer jmxMBeanServer = com.ibm.di.api.jmx.JMXAgent.getMBeanServer();
	 * 
	 * var mBeans = jmxMBeanServer.queryMBeans(null, null).iterator();
	 * while (mBeans.hasNext()) {
	 * 	ALMBean = mBeans.next();
	 * 	if (ALMBean.getClassName().equals(&quot;com.ibm.di.api.jmx.mbeans.AssemblyLine&quot;)) {
	 * 		break;
	 * 	}
	 * }
	 * 
	 * if (ALMBean instanceof javax.management.ObjectInstance
	 * 		&amp;&amp; ALMBean.getClassName().equals(
	 * 				&quot;com.ibm.di.api.jmx.mbeans.AssemblyLine&quot;)) {
	 * 	// the method getConfigInstance() cannot be invoked with the invoke() method because it contains &quot;get&quot; in front.
	 * 	// Instead, for methods that have &quot;get&quot; in front, use getAttribute() and the name of the property
	 * 	// in this case getConfigInstance method transforms to ConfigInstance property
	 * 	var mConfigInstance = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;ConfigInstance&quot;);
	 * 	task.logmsg(&quot;Config Instance: &quot; + mConfigInstance);
	 * 
	 * 	var mGlobalUniqueID = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;GlobalUniqueID&quot;);
	 * 	task.logmsg(&quot;Global Unique ID: &quot; + mGlobalUniqueID);
	 * 
	 * 	var mName = jmxMBeanServer.getAttribute(ALMBean.getObjectName(), &quot;Name&quot;);
	 * 	task.logmsg(&quot;Name: &quot; + mName);
	 * 
	 * 	var mResult = jmxMBeanServer
	 * 			.getAttribute(ALMBean.getObjectName(), &quot;Result&quot;);
	 * 	task.logmsg(&quot;Result: &quot; + mResult);
	 * 
	 * 	var mStatistics = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;Statistics&quot;);
	 * 	task.logmsg(&quot;Statistics: &quot; + mStatistics);
	 * 
	 * 	var mUniqueCode = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;UniqueCode&quot;);
	 * 	task.logmsg(&quot;UniqueCode: &quot; + mUniqueCode);
	 * 
	 * 	var isActive = jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;isActive&quot;,
	 * 			null, null);
	 * 	if (isActive) {
	 * 		jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;stop&quot;, null, null);
	 * 		isActive = jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;isActive&quot;,
	 * 				null, null);
	 * 		if (!isActive) {
	 * 			task.logmsg(&quot;Assembly Line successfully stopped.&quot;);
	 * 		}
	 * 	}
	 * } else {
	 * 	task.logmsg(&quot;No Assembly Lines found&quot;);
	 * }
	 * </pre>
	 * 
	 * @return ObjectName object generated from the AssemblyLine's configuration
	 *         ID.
	 * @throws DIException
	 *             if an error occurs while generating the ObjectName.
	 */
	public ObjectName getConfigInstance() throws DIException {
		// everyone is allowed to execute this method

		return ConfigInstance.genObjectName(mAssemblyLine.getConfigInstance()
				.getConfigId());
	}

	/**
	 * Returns the name of the AssemblyLine.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var jmxUrl = new javax.management.remote.JMXServiceURL(
	 * 		&quot;service:jmx:rmi://localhost/jndi/rmi://localhost:1099/jmxconnector&quot;);
	 * var jmxConnector = javax.management.remote.JMXConnectorFactory.connect(jmxUrl);
	 * var jmxMBeanServer = jmxConnector.getMBeanServerConnection();
	 * //for local access use:
	 * //MBeanServer jmxMBeanServer = com.ibm.di.api.jmx.JMXAgent.getMBeanServer();
	 * 
	 * var mBeans = jmxMBeanServer.queryMBeans(null, null).iterator();
	 * while (mBeans.hasNext()) {
	 * 	ALMBean = mBeans.next();
	 * 	if (ALMBean.getClassName().equals(&quot;com.ibm.di.api.jmx.mbeans.AssemblyLine&quot;)) {
	 * 		break;
	 * 	}
	 * }
	 * 
	 * if (ALMBean instanceof javax.management.ObjectInstance
	 * 		&amp;&amp; ALMBean.getClassName().equals(
	 * 				&quot;com.ibm.di.api.jmx.mbeans.AssemblyLine&quot;)) {
	 * 	// the method getConfigInstance() cannot be invoked with the invoke() method because it contains &quot;get&quot; in front.
	 * 	// Instead, for methods that have &quot;get&quot; in front, use getAttribute() and the name of the property
	 * 	// in this case getConfigInstance method transforms to ConfigInstance property
	 * 	var mConfigInstance = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;ConfigInstance&quot;);
	 * 	task.logmsg(&quot;Config Instance: &quot; + mConfigInstance);
	 * 
	 * 	var mGlobalUniqueID = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;GlobalUniqueID&quot;);
	 * 	task.logmsg(&quot;Global Unique ID: &quot; + mGlobalUniqueID);
	 * 
	 * 	var mName = jmxMBeanServer.getAttribute(ALMBean.getObjectName(), &quot;Name&quot;);
	 * 	task.logmsg(&quot;Name: &quot; + mName);
	 * 
	 * 	var mResult = jmxMBeanServer
	 * 			.getAttribute(ALMBean.getObjectName(), &quot;Result&quot;);
	 * 	task.logmsg(&quot;Result: &quot; + mResult);
	 * 
	 * 	var mStatistics = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;Statistics&quot;);
	 * 	task.logmsg(&quot;Statistics: &quot; + mStatistics);
	 * 
	 * 	var mUniqueCode = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;UniqueCode&quot;);
	 * 	task.logmsg(&quot;UniqueCode: &quot; + mUniqueCode);
	 * 
	 * 	var isActive = jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;isActive&quot;,
	 * 			null, null);
	 * 	if (isActive) {
	 * 		jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;stop&quot;, null, null);
	 * 		isActive = jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;isActive&quot;,
	 * 				null, null);
	 * 		if (!isActive) {
	 * 			task.logmsg(&quot;Assembly Line successfully stopped.&quot;);
	 * 		}
	 * 	}
	 * } else {
	 * 	task.logmsg(&quot;No Assembly Lines found&quot;);
	 * }
	 * </pre>
	 * 
	 * @return String object representing the AssemblyLine's name.
	 * @throws DIException
	 *             if an error occurs while retrieving the name of the
	 *             AssemblyLine.
	 */
	public String getName() throws DIException {
		// everyone is allowed to execute this method

		return mAssemblyLine.getName();
	}

	/**
	 * Returns the unique code of the AssemblyLine.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var jmxUrl = new javax.management.remote.JMXServiceURL(
	 * 		&quot;service:jmx:rmi://localhost/jndi/rmi://localhost:1099/jmxconnector&quot;);
	 * var jmxConnector = javax.management.remote.JMXConnectorFactory.connect(jmxUrl);
	 * var jmxMBeanServer = jmxConnector.getMBeanServerConnection();
	 * //for local access use:
	 * //MBeanServer jmxMBeanServer = com.ibm.di.api.jmx.JMXAgent.getMBeanServer();
	 * 
	 * var mBeans = jmxMBeanServer.queryMBeans(null, null).iterator();
	 * while (mBeans.hasNext()) {
	 * 	ALMBean = mBeans.next();
	 * 	if (ALMBean.getClassName().equals(&quot;com.ibm.di.api.jmx.mbeans.AssemblyLine&quot;)) {
	 * 		break;
	 * 	}
	 * }
	 * 
	 * if (ALMBean instanceof javax.management.ObjectInstance
	 * 		&amp;&amp; ALMBean.getClassName().equals(
	 * 				&quot;com.ibm.di.api.jmx.mbeans.AssemblyLine&quot;)) {
	 * 	// the method getConfigInstance() cannot be invoked with the invoke() method because it contains &quot;get&quot; in front.
	 * 	// Instead, for methods that have &quot;get&quot; in front, use getAttribute() and the name of the property
	 * 	// in this case getConfigInstance method transforms to ConfigInstance property
	 * 	var mConfigInstance = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;ConfigInstance&quot;);
	 * 	task.logmsg(&quot;Config Instance: &quot; + mConfigInstance);
	 * 
	 * 	var mGlobalUniqueID = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;GlobalUniqueID&quot;);
	 * 	task.logmsg(&quot;Global Unique ID: &quot; + mGlobalUniqueID);
	 * 
	 * 	var mName = jmxMBeanServer.getAttribute(ALMBean.getObjectName(), &quot;Name&quot;);
	 * 	task.logmsg(&quot;Name: &quot; + mName);
	 * 
	 * 	var mResult = jmxMBeanServer
	 * 			.getAttribute(ALMBean.getObjectName(), &quot;Result&quot;);
	 * 	task.logmsg(&quot;Result: &quot; + mResult);
	 * 
	 * 	var mStatistics = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;Statistics&quot;);
	 * 	task.logmsg(&quot;Statistics: &quot; + mStatistics);
	 * 
	 * 	var mUniqueCode = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;UniqueCode&quot;);
	 * 	task.logmsg(&quot;UniqueCode: &quot; + mUniqueCode);
	 * 
	 * 	var isActive = jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;isActive&quot;,
	 * 			null, null);
	 * 	if (isActive) {
	 * 		jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;stop&quot;, null, null);
	 * 		isActive = jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;isActive&quot;,
	 * 				null, null);
	 * 		if (!isActive) {
	 * 			task.logmsg(&quot;Assembly Line successfully stopped.&quot;);
	 * 		}
	 * 	}
	 * } else {
	 * 	task.logmsg(&quot;No Assembly Lines found&quot;);
	 * }
	 * </pre>
	 * 
	 * @return Integer object representing the unique code of the AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while retrieving the unique code of the
	 *             AssemblyLine.
	 */
	public Integer getUniqueCode() throws DIException {
		// everyone is allowed to execute this method

		return Integer.valueOf(mAssemblyLine.getUniqueCode());
	}

	/**
	 * Returns configuration information about the AssemblyLine.
	 * 
	 * @return AssemblyLineConfig representing the configuration information of
	 *         the AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while retrieving the configuration
	 *             information of the AssemblyLine.
	 */
	public AssemblyLineConfig getConfig() throws DIException {
		String userId = getCurrentUserId();
		String configId = mAssemblyLine.getConfigInstance().getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanReadConfig(userId,
						configId)) {
			throw new AuthorizationException();
		}

		return mAssemblyLine.getConfig();
	}

	/**
	 * Gets the nullBehavior attribute of the AssemblyLine object
	 * 
	 * @return String object representing the nullBehavior attribute value or
	 *         null if no setting values are available for the AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while getting the nullBehavior attribute.
	 */
	public String getNullBehavior() throws DIException {
		// everyone is allowed to execute this method

		return mAssemblyLine.getNullBehavior();
	}

	/**
	 * Gets the nullBehaviorValue attribute of the AssemblyLine object.
	 * 
	 * @return String object representing the nullBehaviorValue attribute value
	 *         or null if no setting values are available for the AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while getting the nullBehaviorValue
	 *             attribute.
	 */
	public String getNullBehaviorValue() throws DIException {
		// everyone is allowed to execute this method

		return mAssemblyLine.getNullBehaviorValue();
	}

	/**
	 * This method returns the TaskStatistics object for this AssemblyLine.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var jmxUrl = new javax.management.remote.JMXServiceURL(
	 * 		&quot;service:jmx:rmi://localhost/jndi/rmi://localhost:1099/jmxconnector&quot;);
	 * var jmxConnector = javax.management.remote.JMXConnectorFactory.connect(jmxUrl);
	 * var jmxMBeanServer = jmxConnector.getMBeanServerConnection();
	 * //for local access use:
	 * //MBeanServer jmxMBeanServer = com.ibm.di.api.jmx.JMXAgent.getMBeanServer();
	 * 
	 * var mBeans = jmxMBeanServer.queryMBeans(null, null).iterator();
	 * while (mBeans.hasNext()) {
	 * 	ALMBean = mBeans.next();
	 * 	if (ALMBean.getClassName().equals(&quot;com.ibm.di.api.jmx.mbeans.AssemblyLine&quot;)) {
	 * 		break;
	 * 	}
	 * }
	 * 
	 * if (ALMBean instanceof javax.management.ObjectInstance
	 * 		&amp;&amp; ALMBean.getClassName().equals(
	 * 				&quot;com.ibm.di.api.jmx.mbeans.AssemblyLine&quot;)) {
	 * 	// the method getConfigInstance() cannot be invoked with the invoke() method because it contains &quot;get&quot; in front.
	 * 	// Instead, for methods that have &quot;get&quot; in front, use getAttribute() and the name of the property
	 * 	// in this case getConfigInstance method transforms to ConfigInstance property
	 * 	var mConfigInstance = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;ConfigInstance&quot;);
	 * 	task.logmsg(&quot;Config Instance: &quot; + mConfigInstance);
	 * 
	 * 	var mGlobalUniqueID = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;GlobalUniqueID&quot;);
	 * 	task.logmsg(&quot;Global Unique ID: &quot; + mGlobalUniqueID);
	 * 
	 * 	var mName = jmxMBeanServer.getAttribute(ALMBean.getObjectName(), &quot;Name&quot;);
	 * 	task.logmsg(&quot;Name: &quot; + mName);
	 * 
	 * 	var mResult = jmxMBeanServer
	 * 			.getAttribute(ALMBean.getObjectName(), &quot;Result&quot;);
	 * 	task.logmsg(&quot;Result: &quot; + mResult);
	 * 
	 * 	var mStatistics = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;Statistics&quot;);
	 * 	task.logmsg(&quot;Statistics: &quot; + mStatistics);
	 * 
	 * 	var mUniqueCode = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;UniqueCode&quot;);
	 * 	task.logmsg(&quot;UniqueCode: &quot; + mUniqueCode);
	 * 
	 * 	var isActive = jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;isActive&quot;,
	 * 			null, null);
	 * 	if (isActive) {
	 * 		jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;stop&quot;, null, null);
	 * 		isActive = jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;isActive&quot;,
	 * 				null, null);
	 * 		if (!isActive) {
	 * 			task.logmsg(&quot;Assembly Line successfully stopped.&quot;);
	 * 		}
	 * 	}
	 * } else {
	 * 	task.logmsg(&quot;No Assembly Lines found&quot;);
	 * }
	 * </pre>
	 * 
	 * @return The accumulated TaskStatistics object.
	 * @throws DIException
	 *             if an error occurs while getting the AssemblyLine statistics.
	 */
	public TaskStatistics getStatistics() throws DIException {
		// everyone is allowed to execute this method

		return mAssemblyLine.getStatistics();
	}

	/**
	 * Checks if the AssemblyLine is active.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var jmxUrl = new javax.management.remote.JMXServiceURL(
	 * 		&quot;service:jmx:rmi://localhost/jndi/rmi://localhost:1099/jmxconnector&quot;);
	 * var jmxConnector = javax.management.remote.JMXConnectorFactory.connect(jmxUrl);
	 * var jmxMBeanServer = jmxConnector.getMBeanServerConnection();
	 * //for local access use:
	 * //MBeanServer jmxMBeanServer = com.ibm.di.api.jmx.JMXAgent.getMBeanServer();
	 * 
	 * var mBeans = jmxMBeanServer.queryMBeans(null, null).iterator();
	 * while (mBeans.hasNext()) {
	 * 	ALMBean = mBeans.next();
	 * 	if (ALMBean.getClassName().equals(&quot;com.ibm.di.api.jmx.mbeans.AssemblyLine&quot;)) {
	 * 		break;
	 * 	}
	 * }
	 * 
	 * if (ALMBean instanceof javax.management.ObjectInstance
	 * 		&amp;&amp; ALMBean.getClassName().equals(
	 * 				&quot;com.ibm.di.api.jmx.mbeans.AssemblyLine&quot;)) {
	 * 	// the method getConfigInstance() cannot be invoked with the invoke() method because it contains &quot;get&quot; in front.
	 * 	// Instead, for methods that have &quot;get&quot; in front, use getAttribute() and the name of the property
	 * 	// in this case getConfigInstance method transforms to ConfigInstance property
	 * 	var mConfigInstance = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;ConfigInstance&quot;);
	 * 	task.logmsg(&quot;Config Instance: &quot; + mConfigInstance);
	 * 
	 * 	var mGlobalUniqueID = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;GlobalUniqueID&quot;);
	 * 	task.logmsg(&quot;Global Unique ID: &quot; + mGlobalUniqueID);
	 * 
	 * 	var mName = jmxMBeanServer.getAttribute(ALMBean.getObjectName(), &quot;Name&quot;);
	 * 	task.logmsg(&quot;Name: &quot; + mName);
	 * 
	 * 	var mResult = jmxMBeanServer
	 * 			.getAttribute(ALMBean.getObjectName(), &quot;Result&quot;);
	 * 	task.logmsg(&quot;Result: &quot; + mResult);
	 * 
	 * 	var mStatistics = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;Statistics&quot;);
	 * 	task.logmsg(&quot;Statistics: &quot; + mStatistics);
	 * 
	 * 	var mUniqueCode = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;UniqueCode&quot;);
	 * 	task.logmsg(&quot;UniqueCode: &quot; + mUniqueCode);
	 * 
	 * 	var isActive = jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;isActive&quot;,
	 * 			null, null);
	 * 	if (isActive) {
	 * 		jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;stop&quot;, null, null);
	 * 		isActive = jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;isActive&quot;,
	 * 				null, null);
	 * 		if (!isActive) {
	 * 			task.logmsg(&quot;Assembly Line successfully stopped.&quot;);
	 * 		}
	 * 	}
	 * } else {
	 * 	task.logmsg(&quot;No Assembly Lines found&quot;);
	 * }
	 * </pre>
	 * 
	 * @return true if the AssemblyLine's thread is alive, false otherwise.
	 * @throws DIException
	 *             if an error occurs while getting the AssemblyLine state.
	 */
	public Boolean isActive() throws DIException {
		// everyone is allowed to execute this method

		return Boolean.valueOf(mAssemblyLine.isActive());
	}

	/**
	 * This method returns the result entry object. This object is a copy of the
	 * working entry as it were when the AssemblyLine finished processing the
	 * connectors.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var jmxUrl = new javax.management.remote.JMXServiceURL(
	 * 		&quot;service:jmx:rmi://localhost/jndi/rmi://localhost:1099/jmxconnector&quot;);
	 * var jmxConnector = javax.management.remote.JMXConnectorFactory.connect(jmxUrl);
	 * var jmxMBeanServer = jmxConnector.getMBeanServerConnection();
	 * //for local access use:
	 * //MBeanServer jmxMBeanServer = com.ibm.di.api.jmx.JMXAgent.getMBeanServer();
	 * 
	 * var mBeans = jmxMBeanServer.queryMBeans(null, null).iterator();
	 * while (mBeans.hasNext()) {
	 * 	ALMBean = mBeans.next();
	 * 	if (ALMBean.getClassName().equals(&quot;com.ibm.di.api.jmx.mbeans.AssemblyLine&quot;)) {
	 * 		break;
	 * 	}
	 * }
	 * 
	 * if (ALMBean instanceof javax.management.ObjectInstance
	 * 		&amp;&amp; ALMBean.getClassName().equals(
	 * 				&quot;com.ibm.di.api.jmx.mbeans.AssemblyLine&quot;)) {
	 * 	// the method getConfigInstance() cannot be invoked with the invoke() method because it contains &quot;get&quot; in front.
	 * 	// Instead, for methods that have &quot;get&quot; in front, use getAttribute() and the name of the property
	 * 	// in this case getConfigInstance method transforms to ConfigInstance property
	 * 	var mConfigInstance = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;ConfigInstance&quot;);
	 * 	task.logmsg(&quot;Config Instance: &quot; + mConfigInstance);
	 * 
	 * 	var mGlobalUniqueID = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;GlobalUniqueID&quot;);
	 * 	task.logmsg(&quot;Global Unique ID: &quot; + mGlobalUniqueID);
	 * 
	 * 	var mName = jmxMBeanServer.getAttribute(ALMBean.getObjectName(), &quot;Name&quot;);
	 * 	task.logmsg(&quot;Name: &quot; + mName);
	 * 
	 * 	var mResult = jmxMBeanServer
	 * 			.getAttribute(ALMBean.getObjectName(), &quot;Result&quot;);
	 * 	task.logmsg(&quot;Result: &quot; + mResult);
	 * 
	 * 	var mStatistics = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;Statistics&quot;);
	 * 	task.logmsg(&quot;Statistics: &quot; + mStatistics);
	 * 
	 * 	var mUniqueCode = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;UniqueCode&quot;);
	 * 	task.logmsg(&quot;UniqueCode: &quot; + mUniqueCode);
	 * 
	 * 	var isActive = jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;isActive&quot;,
	 * 			null, null);
	 * 	if (isActive) {
	 * 		jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;stop&quot;, null, null);
	 * 		isActive = jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;isActive&quot;,
	 * 				null, null);
	 * 		if (!isActive) {
	 * 			task.logmsg(&quot;Assembly Line successfully stopped.&quot;);
	 * 		}
	 * 	}
	 * } else {
	 * 	task.logmsg(&quot;No Assembly Lines found&quot;);
	 * }
	 * </pre>
	 * 
	 * @return The last "work" entry object.
	 * @throws DIException
	 *             if an error occurs while getting the result Entry.
	 */
	public Entry getResult() throws DIException {
		// everyone is allowed to execute this method

		return mAssemblyLine.getResult();
	}

	/**
	 * Stops the execution of the AssemblyLine.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var jmxUrl = new javax.management.remote.JMXServiceURL(
	 * 		&quot;service:jmx:rmi://localhost/jndi/rmi://localhost:1099/jmxconnector&quot;);
	 * var jmxConnector = javax.management.remote.JMXConnectorFactory.connect(jmxUrl);
	 * var jmxMBeanServer = jmxConnector.getMBeanServerConnection();
	 * //for local access use:
	 * //MBeanServer jmxMBeanServer = com.ibm.di.api.jmx.JMXAgent.getMBeanServer();
	 * 
	 * var mBeans = jmxMBeanServer.queryMBeans(null, null).iterator();
	 * while (mBeans.hasNext()) {
	 * 	ALMBean = mBeans.next();
	 * 	if (ALMBean.getClassName().equals(&quot;com.ibm.di.api.jmx.mbeans.AssemblyLine&quot;)) {
	 * 		break;
	 * 	}
	 * }
	 * 
	 * if (ALMBean instanceof javax.management.ObjectInstance
	 * 		&amp;&amp; ALMBean.getClassName().equals(
	 * 				&quot;com.ibm.di.api.jmx.mbeans.AssemblyLine&quot;)) {
	 * 	// the method getConfigInstance() cannot be invoked with the invoke() method because it contains &quot;get&quot; in front.
	 * 	// Instead, for methods that have &quot;get&quot; in front, use getAttribute() and the name of the property
	 * 	// in this case getConfigInstance method transforms to ConfigInstance property
	 * 	var mConfigInstance = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;ConfigInstance&quot;);
	 * 	task.logmsg(&quot;Config Instance: &quot; + mConfigInstance);
	 * 
	 * 	var mGlobalUniqueID = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;GlobalUniqueID&quot;);
	 * 	task.logmsg(&quot;Global Unique ID: &quot; + mGlobalUniqueID);
	 * 
	 * 	var mName = jmxMBeanServer.getAttribute(ALMBean.getObjectName(), &quot;Name&quot;);
	 * 	task.logmsg(&quot;Name: &quot; + mName);
	 * 
	 * 	var mResult = jmxMBeanServer
	 * 			.getAttribute(ALMBean.getObjectName(), &quot;Result&quot;);
	 * 	task.logmsg(&quot;Result: &quot; + mResult);
	 * 
	 * 	var mStatistics = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;Statistics&quot;);
	 * 	task.logmsg(&quot;Statistics: &quot; + mStatistics);
	 * 
	 * 	var mUniqueCode = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;UniqueCode&quot;);
	 * 	task.logmsg(&quot;UniqueCode: &quot; + mUniqueCode);
	 * 
	 * 	var isActive = jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;isActive&quot;,
	 * 			null, null);
	 * 	if (isActive) {
	 * 		jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;stop&quot;, null, null);
	 * 		isActive = jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;isActive&quot;,
	 * 				null, null);
	 * 		if (!isActive) {
	 * 			task.logmsg(&quot;Assembly Line successfully stopped.&quot;);
	 * 		}
	 * 	}
	 * } else {
	 * 	task.logmsg(&quot;No Assembly Lines found&quot;);
	 * }
	 * </pre>
	 * 
	 * @throws DIException
	 *             if an error occurs while stopping the AssemblyLine.
	 */
	public void stop() throws DIException {
		String userId = getCurrentUserId();
		String configId = mAssemblyLine.getConfigInstance().getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, mAssemblyLine.getName())) {
			throw new AuthorizationException();
		}
		mAssemblyLine.stop();
	}

	/**
	 * Returns the fully-qualified path of the log file of the AssemblyLine.
	 * 
	 * @return the fully-qualified log file path.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine log file
	 *             path.
	 */
	public String getSystemLogFilePath() throws DIException {
		// everyone is allowed to execute this method

		return mAssemblyLine.getSystemLogFilePath();
	}

	/**
	 * Returns the name of the log file of the AssemblyLine (not prefixed by
	 * folders path).
	 * 
	 * @return the log file name.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine log file
	 *             name.
	 */
	public String getSystemLogFileName() throws DIException {
		// everyone is allowed to execute this method

		return mAssemblyLine.getSystemLogFileName();
	}

	/**
	 * Retrieves the current AssemblyLine's system log.
	 * 
	 * @return the log generated by the AssemblyLine so far.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine's log.
	 */
	public String getSystemLog() throws DIException {
		String userId = getCurrentUserId();
		String configId = mAssemblyLine.getConfigInstance().getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, mAssemblyLine.getName())) {
			throw new AuthorizationException();
		}

		return mAssemblyLine.getSystemLog();
	}

	/**
	 * Retrieves the last chunk from the current AssemblyLine's system log.
	 * 
	 * @param aLastKilobytes
	 *            specifies in kilobytes the size of the log's last chunk that
	 *            will be read.
	 * 
	 * @return The last chunk of the AssemblyLine's log, generated so far.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine's log.
	 */
	public String getSystemLogLastChunk(Integer aLastKilobytes)
			throws DIException {
		String userId = getCurrentUserId();
		String configId = mAssemblyLine.getConfigInstance().getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, mAssemblyLine.getName())) {
			throw new AuthorizationException();
		}

		return mAssemblyLine.getSystemLogLastChunk(aLastKilobytes.intValue());
	}

	/**
	 * Generates object name for specified assembly line.
	 * 
	 * @param aAssemblyLineName
	 *            the name of the assembly line.
	 * @param aUniqueCode
	 *            unique code used for building the AssemblyLine MBean id.
	 * @return the generated object name
	 * 
	 * @throws DIException
	 *             if error occurs while creating AssemblyLine JMX object name.
	 */
	public static ObjectName genObjectName(String aAssemblyLineName,
			int aUniqueCode) throws DIException {
		String id = aAssemblyLineName + "." + aUniqueCode;
		String keyProperties = "type=" + MBEAN_TYPE + ",id=" + id;
		ObjectName objectName = null;
		try {
			objectName = new ObjectName(JMXAgent.MBEAN_SERVER_DOMAIN + ":"
					+ keyProperties);
		} catch (MalformedObjectNameException e) {
			APIEngine
					.logErrorAndThrowException(
							sResHash
									.getString("SEVER.API.COULD.NOT.CREATE.ASSEMBLYLINE.JMX.OBJECT.NAME"),
							e);
		}
		return objectName;
	}

	/**
	 * Returns AssemblyLine GUID. The GUID is a string value that is unique for
	 * each component ever created by a particular TDI Server.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var jmxUrl = new javax.management.remote.JMXServiceURL(
	 * 		&quot;service:jmx:rmi://localhost/jndi/rmi://localhost:1099/jmxconnector&quot;);
	 * var jmxConnector = javax.management.remote.JMXConnectorFactory.connect(jmxUrl);
	 * var jmxMBeanServer = jmxConnector.getMBeanServerConnection();
	 * //for local access use:
	 * //MBeanServer jmxMBeanServer = com.ibm.di.api.jmx.JMXAgent.getMBeanServer();
	 * 
	 * var mBeans = jmxMBeanServer.queryMBeans(null, null).iterator();
	 * while (mBeans.hasNext()) {
	 * 	ALMBean = mBeans.next();
	 * 	if (ALMBean.getClassName().equals(&quot;com.ibm.di.api.jmx.mbeans.AssemblyLine&quot;)) {
	 * 		break;
	 * 	}
	 * }
	 * 
	 * if (ALMBean instanceof javax.management.ObjectInstance
	 * 		&amp;&amp; ALMBean.getClassName().equals(
	 * 				&quot;com.ibm.di.api.jmx.mbeans.AssemblyLine&quot;)) {
	 * 	// the method getConfigInstance() cannot be invoked with the invoke() method because it contains &quot;get&quot; in front.
	 * 	// Instead, for methods that have &quot;get&quot; in front, use getAttribute() and the name of the property
	 * 	// in this case getConfigInstance method transforms to ConfigInstance property
	 * 	var mConfigInstance = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;ConfigInstance&quot;);
	 * 	task.logmsg(&quot;Config Instance: &quot; + mConfigInstance);
	 * 
	 * 	var mGlobalUniqueID = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;GlobalUniqueID&quot;);
	 * 	task.logmsg(&quot;Global Unique ID: &quot; + mGlobalUniqueID);
	 * 
	 * 	var mName = jmxMBeanServer.getAttribute(ALMBean.getObjectName(), &quot;Name&quot;);
	 * 	task.logmsg(&quot;Name: &quot; + mName);
	 * 
	 * 	var mResult = jmxMBeanServer
	 * 			.getAttribute(ALMBean.getObjectName(), &quot;Result&quot;);
	 * 	task.logmsg(&quot;Result: &quot; + mResult);
	 * 
	 * 	var mStatistics = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;Statistics&quot;);
	 * 	task.logmsg(&quot;Statistics: &quot; + mStatistics);
	 * 
	 * 	var mUniqueCode = jmxMBeanServer.getAttribute(ALMBean.getObjectName(),
	 * 			&quot;UniqueCode&quot;);
	 * 	task.logmsg(&quot;UniqueCode: &quot; + mUniqueCode);
	 * 
	 * 	var isActive = jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;isActive&quot;,
	 * 			null, null);
	 * 	if (isActive) {
	 * 		jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;stop&quot;, null, null);
	 * 		isActive = jmxMBeanServer.invoke(ALMBean.getObjectName(), &quot;isActive&quot;,
	 * 				null, null);
	 * 		if (!isActive) {
	 * 			task.logmsg(&quot;Assembly Line successfully stopped.&quot;);
	 * 		}
	 * 	}
	 * } else {
	 * 	task.logmsg(&quot;No Assembly Lines found&quot;);
	 * }
	 * </pre>
	 * 
	 * @return The AssemblyLine GUID value.
	 * @throws DIException
	 *             if an error occurs while obtaining the GUID.
	 */
	public String getGlobalUniqueID() throws DIException {
		return mAssemblyLine.getGlobalUniqueID();
	}

	/**
	 * Check weather the AssemblyLine is simulating or not.
	 * 
	 * @return true if the AssemblyLine is simulating, false if it is not.
	 * @throws DIException
	 *             if an error occurs while obtaining the simulation state
	 */
	public boolean isSimulating() throws DIException {

		return mAssemblyLine.isSimulating();
	}

	/**
	 * Changes the way the AssemblyLine treats the target systems it is
	 * connecting/interacting to/with. Turning the simulation on will make the
	 * AssemblyLine use the SimulationConfig child of the AssemblyLineConfig in
	 * order to properly handle sensitive data.
	 * 
	 * @param simulate
	 *            true switches the simulation on, false switches it off
	 * 
	 * @throws DIException
	 *             if an error occurs while changing the simulation state
	 */
	public void setSimulating(boolean simulate) throws DIException {

		mAssemblyLine.setSimulating(simulate);
	}

	/**
	 * {@inheritDoc}
	 */
	public void attachDebugger(int port, String host, boolean onerror)
			throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}
		mAssemblyLine.attachDebugger(port, host, onerror);
	}

	/**
	 * {@inheritDoc}
	 */
	public void detachDebugger(Object msg) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}
		mAssemblyLine.detachDebugger(msg);
	}
}
