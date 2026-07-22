/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import javax.naming.Name;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;

/**
 * Describes the configuration of an item from a schema
 */
public class SchemaItemConfigImpl extends BaseConfigurationImpl implements SchemaItemConfig {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 5168801947811376566L;

	private ContainerConfig childSchemaList;

	public SchemaItemConfigImpl() {
		super();
	}

	public SchemaItemConfigImpl(Object config) {
		super(config);
	}

	/**
	 * Gets the attributeName attribute of the SchemaItemConfig object
	 * 
	 * @return The name of the attribute item
	 */
	public String getAttributeName() {
		return getStringParameter(InternalSchema.SCHEMA_NAME);
	}

	/**
	 * Sets the attributeName attribute of the SchemaItemConfig object
	 * 
	 * @param name
	 *            The new attributeName value
	 */
	public void setAttributeName(String name) {
		setStringParameter(InternalSchema.SCHEMA_NAME, name);
	}

	/**
	 * Gets the java class used internally for the value
	 * 
	 * @return The java class name
	 */
	public String getJavaClass() {
		return getStringParameter(InternalSchema.SCHEMA_INTERNAL_SYNTAX);
	}

	/**
	 * Sets the java class name of the SchemaItemConfig object
	 * 
	 * @param className
	 *            The new java class value
	 */
	public void setJavaClass(String className) {
		setStringParameter(InternalSchema.SCHEMA_INTERNAL_SYNTAX, className);
	}

	/**
	 * Gets the externalSyntax attribute of the SchemaItemConfig object
	 * 
	 * @return The externalSyntax value
	 */
	public String getExternalSyntax() {
		return getStringParameter(InternalSchema.SCHEMA_EXTERNAL_SYNTAX);
	}

	/**
	 * Sets the externalSyntax attribute of the SchemaItemConfig object
	 * 
	 * @param syntax
	 *            The new externalSyntax value
	 */
	public void setExternalSyntax(String syntax) {
		setStringParameter(InternalSchema.SCHEMA_EXTERNAL_SYNTAX, syntax);
	}

	/**
	 * Gets the presence flag of the SchemaItemConfig object. The presence flag
	 * indicates to the user whether it is required or optional.
	 * 
	 * @return The presence value
	 */
	public String getPresenceFlag() {
		return getStringParameter(InternalSchema.SCHEMA_PRESENCE);
	}

	/**
	 * Sets the presence flag of the SchemaItemConfig object
	 * 
	 * @param presence
	 *            The new presence value
	 */
	public void setPresenceFlag(String presence) {
		setStringParameter(InternalSchema.SCHEMA_PRESENCE, presence);
	}

	/**
	 * Returns the sample value
	 */
	public Object getSample() {
		return getParameter(InternalSchema.SCHEMA_SAMPLE);
	}

	/**
	 * Sets the sample value
	 */
	public void setSample(Object sample) {
		setParameter(InternalSchema.SCHEMA_SAMPLE, sample);
	}

	/**
	 * Return self clone
	 */
	public Object getClone() throws Exception {
		SchemaItemConfig sic = new SchemaItemConfigImpl(deepClone(null));
		sic.setName(getName());
		sic.init();
		sic.setMetamergeConfig(getMetamergeConfig());
		sic.setupInheritanceChain();
		sic.setModTS(getModTS());
		return sic;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.base.BaseConfigurationImpl#removeFromParent()
	 */
	public boolean detachFromParent() {
		if (getParent() instanceof ContainerConfig)
			((ContainerConfig) getParent()).removeConfig(this);
		else
			((SchemaConfig) getParent()).removeItem(getShortName());
		return true;
	}

	public boolean reattachToParent(int position) {
		if (getParent() instanceof ContainerConfig)
			((ContainerConfig) getParent()).addConfig(this);
		else
			((SchemaConfig) getParent()).setItem(getName(), this);
		return true;
	}

	public boolean isRequired() {
		if (getMinOccurrences() > 0)
			return true;
		else
			return PRESENCE_REQUIRED.equalsIgnoreCase(getPresenceFlag());
	}

	public ContainerConfig getChildSchemaList() {
		if (childSchemaList == null) {
			childSchemaList = new ContainerConfigImpl();
			childSchemaList.setParent(this);
			try {
				childSchemaList.init();
			} catch (Exception e) {
			}
		}

		return childSchemaList;
	}

	public int getMaxOccurrences() {
		return getIntegerParameter(InternalSchema.SCHEMA_OCCURS_MAX, 1);
	}

	public int getMinOccurrences() {
		int defval = 0;
		if (!hasParameter(InternalSchema.SCHEMA_OCCURS_MIN))
			defval = PRESENCE_REQUIRED.equals(getPresenceFlag()) ? 1 : 0;
		return getIntegerParameter(InternalSchema.SCHEMA_OCCURS_MIN, defval);
	}

	public void setMaxOccurrences(int max) {
		setIntegerParameter(InternalSchema.SCHEMA_OCCURS_MAX, max);
	}

	public void setMinOccurrences(int min) {
		setIntegerParameter(InternalSchema.SCHEMA_OCCURS_MIN, min);
	}

	public boolean isProperty() {
		return getBooleanParameter(InternalSchema.SCHEMA_PROPERTY, false);
	}

	public void setProperty(boolean property) {
		setBooleanParameter(InternalSchema.SCHEMA_PROPERTY, property);
	}

	public Name getName() {
		Name n = super.getName();
		if (n != null && n.size() > 1)
			return n;
		BaseConfiguration b = this;
		try {
			SchemaName name = new SchemaName();
			while (b != null && !(b instanceof SchemaConfig)) {
				if (b instanceof SchemaItemConfig) {
					String s = ((SchemaItemConfig) b).getAttributeName();
					if (s == null)
						return n;
					name.add(0, s);
				}
				b = b.getParent();
			}
			return name;
		} catch (Exception e) {
			return n;
		}
	}

	public boolean isRepeatable() {
		return getMaxOccurrences() > 1 || getMaxOccurrences() == -1;
	}

	public boolean isLeaf() {
		return getChildSchemaList().size() == 0;
	}

	public boolean getEnabled() {
		return getBooleanParameter(InternalSchema.ENABLED, true);
	}
}
