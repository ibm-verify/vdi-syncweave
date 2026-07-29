/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.jar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.ibm.di.cdm.core.CDMAttributesValidator;
import com.ibm.di.cdm.core.MetaData;
import com.ibm.di.cdm.core.MetaDataFactory;
import com.ibm.di.cdm.core.NamingRule;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.validate.ValidationException;

/**
 * Validates the output map of a IT registry Connector using the naming rules
 * definitions from the CDM JAR file we ship with TDI.
 */
public class CDMComponentAttributesValidator extends CDMAttributesValidator {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "miserver";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static final ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * The MetaData definition.
	 */
	private MetaData metaData = null;

	/**
	 * Whether this Connector should use the IT registry CDM definitions of the
	 * JAR ones.
	 */
	private boolean useITregistry;

	/**
	 * {@inheritDoc}
	 */
	protected List<NamingRule> getUnsatisfiedNamingRules(BaseConfiguration config) throws ValidationException {
		BaseConfiguration parameterConfig = null;
		AttributeMapConfig attributesMapConfig = null;
		if (config instanceof ConnectorConfig) {
			attributesMapConfig = ((ConnectorConfig) config).getAttributeMap(false);
			if (config instanceof FunctionConfig) {
				parameterConfig = ((FunctionConfig) config).getFunctionConfig();
			} else {
				parameterConfig = ((ConnectorConfig) config).getConnectionConfig();
			}
		}

		List<NamingRule> namingRules = new ArrayList<NamingRule>();
		if (parameterConfig == null || attributesMapConfig == null) {
			return namingRules;
		}

		String currentClassType = (String) parameterConfig.getParameter("classType");
		if (currentClassType == null || currentClassType.trim().length() == 0) {
			throw new ValidationException(resHash.getString("CDM.NO.CLASS.TYPE"));
		}
		boolean useITregistryCdm = parameterConfig.getBooleanParameter("useITRegistryCdm", false);
		Set<String> attributes = getEnabledAttributes(attributesMapConfig);
		MetaData metadata = getMetaData(useITregistryCdm);
		try {
			namingRules.addAll(metadata.getUnsatisfiedNamingRules(currentClassType, attributes));
		} catch (Exception e) {
			throw new ValidationException(e.getMessage(), e);
		}
		return namingRules;
	}

	/**
	 * Returns the appropriate {@link MetaData}. <p><b>Note: </b>The meta data
	 * is cached and reused upon next calls unless the configuration of the
	 * Component has changed.</p>
	 * 
	 * @param useITregistryCdm
	 *            whether IT registry of Jar meta data should be used.
	 * @return {@link MetaData} instance.
	 */
	private MetaData getMetaData(boolean useITregistryCdm) {
		if (metaData == null || useITregistryCdm != useITregistry) {
			// reinitialize meta data
			if (!useITregistryCdm) {
				useITregistry = useITregistryCdm;
				metaData = MetaDataFactory.getJarMetaData();
			} else {
				throw new UnsupportedOperationException(resHash.getString("CDM.IT.REGISTRY.NOT.SUPPORTED.FOR.VALIDATION"));
			}
		}
		return metaData;
	}
}
