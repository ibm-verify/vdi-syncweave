/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import java.util.Date;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.DIException;
import com.ibm.di.api.jmx.JMXAgent;
import com.ibm.di.api.local.AssemblyLine;
import com.ibm.di.api.local.AssemblyLineHandler;
import com.ibm.di.config.interfaces.ExternalPropertiesConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.PoolDefConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.TaskCallBlock;

/**
 * 
 * ConfigInstance class implements public methods exposed through JMX layer.
 * 
 */
public class ConfigInstance extends BaseAdmin implements ConfigInstanceMBean {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Type of the MBean.
	 */
	public static final String MBEAN_TYPE = "ConfigInstance";

	/**
	 * com.ibm.di.api.local.ConfigInstance
	 */
	private com.ibm.di.api.local.ConfigInstance mConfigInstance = null;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Class constructor.
	 * 
	 * @param aConfigInstance
	 *            com.ibm.di.api.local.ConfigInstance
	 * @throws DIException
	 */
	public ConfigInstance(com.ibm.di.api.local.ConfigInstance aConfigInstance)
			throws DIException {
		mConfigInstance = aConfigInstance;
	}

	// MBean interface

	/**
	 * {@inheritDoc}
	 */
	public String getType() {
		return MBEAN_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() throws DIException {
		return formatForObjectName(getConfigId());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConfigId() throws DIException {
		// everyone is allowed to execute this method

		return mConfigInstance.getConfigId();
	}

	/**
	 * {@inheritDoc}
	 */
	public MetamergeConfig getConfiguration() throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanReadConfig(userId,
						configId)) {
			throw new AuthorizationException();
		}

		return mConfigInstance.getConfiguration();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setConfiguration(MetamergeConfig aConfiguration)
			throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mConfigInstance.setConfiguration(aConfiguration);
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Deprecated
	public void saveConfiguration() throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mConfigInstance.saveConfiguration();
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Deprecated
	public void saveConfiguration(Boolean aEncrypt) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mConfigInstance.saveConfiguration(aEncrypt.booleanValue());
	}

	/**
	 * {@inheritDoc}
	 */
	public ExternalPropertiesConfig getExternalProperties() throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanReadConfig(userId,
						configId)) {
			throw new AuthorizationException();
		}

		return mConfigInstance.getExternalProperties();
	}

	/**
	 * {@inheritDoc}
	 */
	public ExternalPropertiesConfig getExternalProperties(String aKey)
			throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanReadConfig(userId,
						configId)) {
			throw new AuthorizationException();
		}

		return mConfigInstance.getExternalProperties(aKey);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getExternalPropertiesKeys() throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanReadConfig(userId,
						configId)) {
			throw new AuthorizationException();
		}

		return mConfigInstance.getExternalPropertiesKeys();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public void setExternalProperties(ExternalPropertiesConfig aExPropConfig)
			throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mConfigInstance.setExternalProperties(aExPropConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public void setExternalProperties(String aKey,
			ExternalPropertiesConfig aExPropConfig) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mConfigInstance.setExternalProperties(aKey, aExPropConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public void saveExternalProperties() throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mConfigInstance.saveExternalProperties();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public String[] getAssemblyLinesNames() throws DIException {
		// deprecated call
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !(JMXAgent.getSecRegistry().userCanReadConfig(userId,
						configId) || JMXAgent.getSecRegistry()
						.userCanExecuteConfigALs(userId, configId))) {
			throw new AuthorizationException();
		}
		return mConfigInstance.getAssemblyLinesNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getAssemblyLineNames() throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !(JMXAgent.getSecRegistry().userCanReadConfig(userId,
						configId) || JMXAgent.getSecRegistry()
						.userCanExecuteConfigALs(userId, configId))) {
			throw new AuthorizationException();
		}

		return mConfigInstance.getAssemblyLineNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public SchemaConfig getAssemblyLineInputParameters(String aAssemblyLineName)
			throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !(JMXAgent.getSecRegistry().userCanReadConfig(userId,
						configId) || JMXAgent.getSecRegistry()
						.userCanExecuteAL(userId, configId, aAssemblyLineName))) {
			throw new AuthorizationException();
		}

		return mConfigInstance
				.getAssemblyLineInputParameters(aAssemblyLineName);
	}

	/**
	 * {@inheritDoc}
	 */
	public SchemaConfig getAssemblyLineOutputParameters(String aAssemblyLineName)
			throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !(JMXAgent.getSecRegistry().userCanReadConfig(userId,
						configId) || JMXAgent.getSecRegistry()
						.userCanExecuteAL(userId, configId, aAssemblyLineName))) {
			throw new AuthorizationException();
		}

		return mConfigInstance
				.getAssemblyLineOutputParameters(aAssemblyLineName);
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName[] getAssemblyLines() throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !(JMXAgent.getSecRegistry().userCanReadConfig(userId,
						configId) || JMXAgent.getSecRegistry()
						.userCanExecuteConfigALs(userId, configId))) {
			throw new AuthorizationException();
		}

		AssemblyLine[] assemblyLines = mConfigInstance.getAssemblyLines();
		ObjectName[] oNames = new ObjectName[assemblyLines.length];
		for (int i = 0; i < assemblyLines.length; i++) {
			oNames[i] = com.ibm.di.api.jmx.mbeans.AssemblyLine.genObjectName(
					assemblyLines[i].getName(), assemblyLines[i]
							.getUniqueCode());
		}
		return oNames;
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName startAssemblyLine(String aAssemblyLineName)
			throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, aAssemblyLineName)) {
			throw new AuthorizationException();
		}

		AssemblyLine assemblyLine = mConfigInstance
				.startAssemblyLine(aAssemblyLineName);
		return com.ibm.di.api.jmx.mbeans.AssemblyLine.genObjectName(
				assemblyLine.getName(), assemblyLine.getUniqueCode());
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName startAssemblyLine(String aAssemblyLineName, Boolean aSync)
			throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, aAssemblyLineName)) {
			throw new AuthorizationException();
		}

		AssemblyLine assemblyLine = mConfigInstance.startAssemblyLine(
				aAssemblyLineName, aSync.booleanValue());
		return com.ibm.di.api.jmx.mbeans.AssemblyLine.genObjectName(
				assemblyLine.getName(), assemblyLine.getUniqueCode());
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName startAssemblyLine(String aAssemblyLineName,
			Entry aInputData) throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, aAssemblyLineName)) {
			throw new AuthorizationException();
		}

		AssemblyLine assemblyLine = mConfigInstance.startAssemblyLine(
				aAssemblyLineName, aInputData);
		return com.ibm.di.api.jmx.mbeans.AssemblyLine.genObjectName(
				assemblyLine.getName(), assemblyLine.getUniqueCode());
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName startAssemblyLine(String aAssemblyLineName,
			Entry aInputData, Boolean aSync) throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, aAssemblyLineName)) {
			throw new AuthorizationException();
		}

		AssemblyLine assemblyLine = mConfigInstance.startAssemblyLine(
				aAssemblyLineName, aInputData, aSync.booleanValue());
		return com.ibm.di.api.jmx.mbeans.AssemblyLine.genObjectName(
				assemblyLine.getName(), assemblyLine.getUniqueCode());
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName startAssemblyLine(String aAssemblyLineName,
			Entry aInputData, AssemblyLineListener aListener, Boolean aGetLogs)
			throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, aAssemblyLineName)) {
			throw new AuthorizationException();
		}

		AssemblyLineListenerAdapter listener = new AssemblyLineListenerAdapter(
				aListener);
		AssemblyLine assemblyLine = mConfigInstance.startAssemblyLine(
				aAssemblyLineName, aInputData, listener, aGetLogs
						.booleanValue());
		return com.ibm.di.api.jmx.mbeans.AssemblyLine.genObjectName(
				assemblyLine.getName(), assemblyLine.getUniqueCode());
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName startAssemblyLine(String aAssemblyLineName,
			Entry aInputData, AssemblyLineListener aListener, Boolean aGetLogs,
			Boolean aSync) throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, aAssemblyLineName)) {
			throw new AuthorizationException();
		}

		AssemblyLineListenerAdapter listener = new AssemblyLineListenerAdapter(
				aListener);
		AssemblyLine assemblyLine = mConfigInstance.startAssemblyLine(
				aAssemblyLineName, aInputData, listener, aGetLogs
						.booleanValue(), aSync.booleanValue());
		return com.ibm.di.api.jmx.mbeans.AssemblyLine.genObjectName(
				assemblyLine.getName(), assemblyLine.getUniqueCode());
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName startAssemblyLine(String aAssemblyLineName,
			Entry aInputData, AssemblyLineListener aListener, Boolean aGetLogs,
			Boolean aSync, Boolean aGetEntryOnEachCycle) throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, aAssemblyLineName)) {
			throw new AuthorizationException();
		}

		AssemblyLineListenerAdapter listener = new AssemblyLineListenerAdapter(
				aListener);
		AssemblyLine assemblyLine = mConfigInstance.startAssemblyLine(
				aAssemblyLineName, aInputData, listener, aGetLogs
						.booleanValue(), aSync.booleanValue(),
				aGetEntryOnEachCycle.booleanValue());
		return com.ibm.di.api.jmx.mbeans.AssemblyLine.genObjectName(
				assemblyLine.getName(), assemblyLine.getUniqueCode());
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName startAssemblyLineManual(String aAssemblyLineName,
			Entry aInputData) throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, aAssemblyLineName)) {
			throw new AuthorizationException();
		}

		AssemblyLineHandler assemblyLineHandler = mConfigInstance
				.startAssemblyLineManual(aAssemblyLineName, aInputData);
		com.ibm.di.api.jmx.mbeans.AssemblyLineHandler alh = new com.ibm.di.api.jmx.mbeans.AssemblyLineHandler(
				assemblyLineHandler);
		ObjectName objectName = null;
		try {
			objectName = JMXAgent.registerMBean(alh);
		} catch (JMException e) {
			APIEngine
					.logErrorAndThrowException(
							sResHash
									.getString("SEVER.API.COULD.NOT.REGISTER.ASSEMBLYLINE.HANDLER.MBEAN"),
							e);
		}
		return objectName;
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName startAssemblyLine(String aAssemblyLineName,
			TaskCallBlock aTcb) throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, aAssemblyLineName)) {
			throw new AuthorizationException();
		}

		AssemblyLine assemblyLine = mConfigInstance.startAssemblyLine(
				aAssemblyLineName, aTcb);
		return com.ibm.di.api.jmx.mbeans.AssemblyLine.genObjectName(
				assemblyLine.getName(), assemblyLine.getUniqueCode());
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName startAssemblyLine(String aAssemblyLineName,
			TaskCallBlock aTcb, Boolean aSync) throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, aAssemblyLineName)) {
			throw new AuthorizationException();
		}

		AssemblyLine assemblyLine = mConfigInstance.startAssemblyLine(
				aAssemblyLineName, aTcb, aSync.booleanValue());
		return com.ibm.di.api.jmx.mbeans.AssemblyLine.genObjectName(
				assemblyLine.getName(), assemblyLine.getUniqueCode());
	}

	/**
	 * {@inheritDoc}
	 */
	public void reload() throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mConfigInstance.reload();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mConfigInstance.stop();
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getALLogFileNames(String aALName) throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, aALName)) {
			throw new AuthorizationException();
		}

		return mConfigInstance.getALLogFileNames(aALName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLastLogFileName(String aALName) throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, aALName)) {
			throw new AuthorizationException();
		}

		return mConfigInstance.getALLastLogFileName(aALName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLog(String aALName, String aLogFileName)
			throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, aALName)) {
			throw new AuthorizationException();
		}

		return mConfigInstance.getALLog(aALName, aLogFileName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLogLastChunk(String aALName, String aLogFileName,
			Integer aKilobytes) throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						configId, aALName)) {
			throw new AuthorizationException();
		}

		return mConfigInstance.getALLogLastChunk(aALName, aLogFileName,
				aKilobytes.intValue());
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getInstanceBootTime() throws DIException {
		return mConfigInstance.getInstanceBootTime();
	}

	/**
	 * Formats the object name to look nice.
	 * 
	 * @param aRawName
	 *            not formatted name.
	 * @return formatted name
	 */
	private static String formatForObjectName(String aRawName) {
		if (aRawName == null) {
			return null;
		}

		StringBuffer buffer = new StringBuffer(aRawName);
		int i = 0;
		while (i < buffer.length()) {
			switch (buffer.charAt(i)) {
			case ':':
			case ',':
			case '=':
			case '*':
			case '?':
				buffer.deleteCharAt(i);
				break;

			default:
				i++;
				break;
			}
		}
		return buffer.toString();
	}

	/**
	 * Generates object name for specified assembly line handler.
	 * 
	 * @param aConfigInstanceName
	 *            the name of the config instance.
	 * @return the generated object name
	 * 
	 * @throws DIException
	 *             if error occurs while creating AssemblyLineHandler JMX object
	 *             name.
	 */
	public static ObjectName genObjectName(String aConfigInstanceName)
			throws DIException {
		String id = formatForObjectName(aConfigInstanceName);
		String keyProperties = "type=" + MBEAN_TYPE + ",id=" + id;
		ObjectName objectName = null;
		try {
			objectName = new ObjectName(JMXAgent.MBEAN_SERVER_DOMAIN + ":"
					+ keyProperties);
		} catch (MalformedObjectNameException e) {
			APIEngine
					.logErrorAndThrowException(
							sResHash
									.getString("SEVER.API.COULD.NOT.CREATE.CONFIGINSTANCE.JMX.OBJECT.NAME"),
							e);
		}
		return objectName;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGlobalUniqueID() throws DIException {
		return mConfigInstance.getGlobalUniqueID();
	}

	// Connector Pool calls

	/**
	 * {@inheritDoc}
	 */
	public String[] getConnectorPoolNames() throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !(JMXAgent.getSecRegistry().userCanExecuteConfigALs(userId,
						configId) || JMXAgent.getSecRegistry()
						.userCanReadConfig(userId, configId))) {
			throw new AuthorizationException();
		}

		return mConfigInstance.getConnectorPoolNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getConnectorPoolSize(String aConnectorPoolName)
			throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteConfigALs(userId,
						configId)) {
			throw new AuthorizationException();
		}

		return mConfigInstance.getConnectorPoolSize(aConnectorPoolName);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getConnectorPoolFreeNum(String aConnectorPoolName)
			throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteConfigALs(userId,
						configId)) {
			throw new AuthorizationException();
		}

		return mConfigInstance.getConnectorPoolFreeNum(aConnectorPoolName);
	}

	/**
	 * {@inheritDoc}
	 */
	public PoolDefConfig getConnectorPoolConfig(String aConnectorPoolName)
			throws DIException {
		String userId = getCurrentUserId();
		String configId = mConfigInstance.getConfigId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanReadConfig(userId,
						configId)) {
			throw new AuthorizationException();
		}

		return mConfigInstance.getConnectorPoolConfig(aConnectorPoolName);
	}

	/**
	 * {@inheritDoc}
	 */
	public int purgeConnectorPool(String aConnectorPoolName) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mConfigInstance.purgeConnectorPool(aConnectorPoolName);
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName getTDIProperties() throws Exception {
		String userId = getCurrentUserId();
		if (userId != null && !(JMXAgent.getSecRegistry().userIsAdmin(userId))) {
			throw new AuthorizationException();
		}

		com.ibm.di.config.interfaces.TDIProperties local_tdip = mConfigInstance
				.getConfiguration().getTDIProperties();
		ObjectName oName = com.ibm.di.api.jmx.mbeans.TDIProperties
				.genObjectName(local_tdip.toString());
		return oName;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConfigPath() {
		// everyone is allowed to execute this method
		return mConfigInstance.getConfigPath();
	}

}
