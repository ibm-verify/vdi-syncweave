/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;

/**
 * Contains all the parameters needed to load and instantiate a Connector.
 *
 */
public class RawConnectorConfigImpl extends BaseConfigurationImpl implements
		RawConnectorConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 8439049716964119460L;

	public RawConnectorConfigImpl() {
		super();
	}

	public RawConnectorConfigImpl(Object config) {
		super(config);
	}

	public String getJavaClass() {
		return getStringParameter(InternalSchema.CONNECTOR_CONNECTOR_JAVACLASS);
	}

	public void setJavaClass(String javaClass) {
		setStringParameter(InternalSchema.CONNECTOR_CONNECTOR_JAVACLASS,
				javaClass);
	}

	public int getParserOption() {
		String s = getStringParameter(InternalSchema.CONNECTOR_CONNECTOR_PARSEROPTION);
		if (s == null)
			return RawConnectorConfig.PARSER_OPTIONAL;
		if (s.equalsIgnoreCase("Required"))
			return RawConnectorConfig.PARSER_REQUIRED;
		if (s.equalsIgnoreCase("Useless"))
			return RawConnectorConfig.PARSER_USELESS;

		return RawConnectorConfig.PARSER_OPTIONAL;
	}

	/**
	 * We override this method to change the inherited object if we inherit from
	 * a connector.
	 */
	public void setInheritsFrom(BaseConfiguration inheritFrom) {
		if (inheritFrom instanceof ConnectorConfig)
			super.setInheritsFrom(((ConnectorConfig) inheritFrom)
					.getConnectionConfig());
		else
			super.setInheritsFrom(inheritFrom);
	}

	public void updateInheritsFrom(String ref) throws Exception {
		if (ref != null && ref.startsWith("@")) {
			super.setInheritsFromRef(ref);
			setParameter(InternalSchema.CONNECTOR_CONNECTOR_PARSEROPTION, "Useless");
			setJavaClass(ref);
			setupInheritanceChain();
		} else {
			removeParameter(InternalSchema.CONNECTOR_CONNECTOR_PARSEROPTION);
			removeParameter(InternalSchema.CONNECTOR_CONNECTOR_JAVACLASS);
			
			if (ref == null) {
				ref = "";
			}

			super.updateInheritsFrom(ref);
		}
	}
}
