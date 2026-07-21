/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.*;
/**
 * Used by the old Connector Schema Implementation.
 * @deprecated
 */
public class ConnectorSchemaItemConfigImpl extends BaseConfigurationImpl
		implements ConnectorSchemaItemConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -1665598194757295769L;

	public ConnectorSchemaItemConfigImpl() {
		super();
	}

	public ConnectorSchemaItemConfigImpl(Object config) {
		super(config);
	}

	public String getAttributeName() {
		return getStringParameter(InternalSchema.SCHEMA_NAME);
	}

	public void setAttributeName(String name) {
		setParameter(InternalSchema.SCHEMA_NAME, name);
	}

	public String getInternalSyntax() {
		return getStringParameter(InternalSchema.SCHEMA_INTERNAL_SYNTAX);
	}

	public void setInternalSyntax(String syntax) {
		setParameter(InternalSchema.SCHEMA_INTERNAL_SYNTAX, syntax);
	}

	public String getExternalSyntax() {
		return getStringParameter(InternalSchema.SCHEMA_EXTERNAL_SYNTAX);
	}

	public void setExternalSyntax(String syntax) {
		setParameter(InternalSchema.SCHEMA_EXTERNAL_SYNTAX, syntax);
	}

	public Object getSample() {
		return getParameter(InternalSchema.SCHEMA_SAMPLE);
	}

	public void setSample(Object sample) {
		setParameter(InternalSchema.SCHEMA_SAMPLE, sample);
	}

	public boolean getExcluded() {
		return getBooleanParameter(InternalSchema.SCHEMA_EXCLUDED, false);
	}

	public void setExcluded(boolean excluded) {
		setBooleanParameter(InternalSchema.SCHEMA_EXCLUDED, excluded);
	}

	public boolean getInputRequired() {
		return getBooleanParameter(InternalSchema.SCHEMA_INPUT_REQUIRED, false);
	}

	public void setInputRequired(boolean required) {
		setBooleanParameter(InternalSchema.SCHEMA_INPUT_REQUIRED, required);
	}

	public boolean getOutputRequired() {
		return getBooleanParameter(InternalSchema.SCHEMA_OUTPUT_REQUIRED, false);
	}

	public void setOutputRequired(boolean required) {
		setBooleanParameter(InternalSchema.SCHEMA_OUTPUT_REQUIRED, required);
	}

	public Object getDefaultValue() {
		return getParameter(InternalSchema.SCHEMA_DEFAULT_VALUE);
	}

	public void setDefaultValue(Object value) {
		setParameter(InternalSchema.SCHEMA_DEFAULT_VALUE, value);
	}
}
