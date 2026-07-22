/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server.validate.taddm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.ibm.di.cdm.core.CDMAttributesValidator;
import com.ibm.di.cdm.core.MetaData;
import com.ibm.di.cdm.core.NamingRule;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.osgi.ConnectorDelegate;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.validate.ValidationException;

/**
 * Validate the output map of the TADDM Connector to verify mapped attributes
 * against CDM naming rules.
 */
public class TADDMValidator extends CDMAttributesValidator {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The MetaData definition.
	 */
	private static MetaData metaData = null;

	/**
	 * Class type whose naming roles will be validate.
	 */
	private static String classType = null;

	/**
	 * Flag shows if a Connector is in IdML mode.
	 */
	private static boolean idmlMode = false;

	/**
	 * {@inheritDoc}
	 */
	protected List<NamingRule> getUnsatisfiedNamingRules(BaseConfiguration config) throws ValidationException {
		List<NamingRule> namingRules = new ArrayList<NamingRule>();
		if (!(config instanceof ConnectorConfig)) {
			return namingRules;
		}
		ConnectorConfig connConfig = (ConnectorConfig) config;

		String currentClassType = (String) connConfig.getConnectionConfig().getParameter("classType");
		if (currentClassType == null || currentClassType.trim().length() == 0) {
			throw new ValidationException(ResourceHash.getHash("miserver").getString("TADDM.CONN.NO.CLASS.TYPE"));
		}
		boolean currentIdMLMode = Boolean.parseBoolean((String) connConfig.getConnectionConfig().getParameter("idmlMode"));
		try {
			ConnectorInterface connector = null;
			if (cacheNeedsUpdate(currentClassType, currentIdMLMode)) {
				connector = SystemFunctions.loadConnector(connConfig);
				updateCache(connector, currentClassType, currentIdMLMode);
			}
			if (metaData != null) {
				Set<String> attributes = getEnabledAttributes(connConfig.getAttributeMap());
				namingRules.addAll(metaData.getUnsatisfiedNamingRules(currentClassType, attributes));
			}
			if (connector != null) {
				connector.terminate();
			}
		} catch (Exception e) {
			throw new ValidationException(e);
		}
		return namingRules;
	}

	/**
	 * Update cached meta data.
	 * 
	 * @param connector
	 *            the connector used for cache update.
	 * @param currentClassType
	 *            current set Class Type
	 * @param currentIdMLMode
	 *            current set IdML mode
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void updateCache(ConnectorInterface connector, String currentClassType, boolean currentIdMLMode) throws Exception {
		classType = currentClassType;
		idmlMode = currentIdMLMode;
		if (connector != null && connector instanceof ConnectorDelegate) {
			connector = ((ConnectorDelegate) connector).getDelegate();
		}

		if (connector != null) {
			connector.initialize(null);
			metaData = (MetaData) connector.getClass().getMethod("getMetaData", (Class[]) null).invoke(connector, (Object[]) null);
		}
	}

	/**
	 * Checks if cached data must be updated.
	 * 
	 * @param currentClassType
	 *            the class type from configuration.
	 * @param currentIdMLMode
	 *            status of IdML mode.
	 * @return <code>true</code> if cache needs update, otherwise
	 *         <code>false</code>.
	 */
	private boolean cacheNeedsUpdate(String currentClassType, boolean currentIdMLMode) {
		return metaData == null || classType == null || !classType.equals(currentClassType) || idmlMode != currentIdMLMode;
	}

}