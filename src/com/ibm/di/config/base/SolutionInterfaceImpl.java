/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.ArrayList;
import java.util.List;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.ExposedProperty;
import com.ibm.di.config.interfaces.SolutionInterface;

/**
 * This class provides access to the Solution interface settings of a
 * configuration. Most of these elements are used to define external aspects of
 * a configuration such as which AssemblyLines and properties are
 * visible/editable by a user at runtime.
 * 
 */
public class SolutionInterfaceImpl extends ContainerConfigImpl implements
		SolutionInterface {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = 1L;

	private final static String EXP_AL = "ExposedAssemblyLines";

	private final static String EXP_PROP = "ExposedProperties";

	private ContainerConfig exposedAssemblyLines;

	private ContainerConfig exposedProperties;

	public SolutionInterfaceImpl() {
		super();
		init();
	}

	public SolutionInterfaceImpl(Object data) {
		super(data);
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.base.ContainerConfigImpl#init()
	 */
	public void init() {
		try {
			super.init();
		} catch (Exception ignore) {
		}

		exposedAssemblyLines = (ContainerConfig) getConfig(EXP_AL);
		if (exposedAssemblyLines == null) {
			exposedAssemblyLines = new ContainerConfigImpl();
			try {
				exposedAssemblyLines.setName(EXP_AL);
			} catch (Exception ignore) {
			}
			addConfig(exposedAssemblyLines);
		}

		exposedProperties = (ContainerConfig) getConfig(EXP_PROP);
		if (exposedProperties == null) {
			exposedProperties = new ContainerConfigImpl();
			try {
				exposedProperties.setName(EXP_PROP);
			} catch (Exception ignore) {
			}
			addConfig(exposedProperties);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.SolutionInterface#getExposedAssemblyLines()
	 */
	public ContainerConfig getExposedAssemblyLines() {
		return exposedAssemblyLines;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.SolutionInterface#addExposedAssemblyLine
	 * (java.lang.String)
	 */
	public BaseConfiguration addExposedAssemblyLine(String name)
			throws Exception {
		BaseConfiguration bc = getExposedAssemblyLines().getConfig(name);
		if (bc != null)
			return bc;

		bc = new BaseConfigurationImpl();
		bc.setName(name);
		exposedAssemblyLines.addConfig(bc);
		return bc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.SolutionInterface#getExposedProperty(java
	 * .lang.String, java.lang.String)
	 */
	public ExposedProperty getExposedProperty(String propertyName,
			String storeName) {
		String key = propertyName + (storeName == null ? "" : ":" + storeName);
		return (ExposedProperty) getExposedProperties().getConfig(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.SolutionInterface#addExposedProperty(java
	 * .lang.String, java.lang.String)
	 */
	public ExposedProperty addExposedProperty(String propertyName,
			String storeName) {
		String name = propertyName + (storeName == null ? "" : ":" + storeName);
		ExposedProperty ep = (ExposedProperty) getExposedProperties()
				.getConfig(name);
		if (ep == null) {
			ep = new ExposedPropertyImpl();
			ep.setStoreName(storeName);
			ep.setPropertyName(propertyName);
			getExposedProperties().addConfig(ep);
		}
		return ep;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.SolutionInterface#getExposedProperties()
	 */
	public ContainerConfig getExposedProperties() {
		return exposedProperties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.SolutionInterface#getHealthAssemblyLine()
	 */
	public String getHealthAssemblyLine() {
		return getStringParameter(InternalSchema.SI_EXP_HEALTH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.SolutionInterface#setHealthAssemblyLine(
	 * java.lang.String)
	 */
	public void setHealthAssemblyLine(String name) {
		if ("".equals(name))
			setStringParameter(InternalSchema.SI_EXP_HEALTH, null);
		else
			setStringParameter(InternalSchema.SI_EXP_HEALTH, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.SolutionInterface#getPropertyCategoryNames()
	 */
	public List<String> getPropertyCategoryNames() {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < getExposedProperties().size(); i++) {
			if (getExposedProperties().getConfig(i) instanceof ExposedProperty) {
				String cat = ((ExposedProperty) getExposedProperties()
						.getConfig(i)).getCategory();
				if (cat != null && !list.contains(cat))
					list.add(cat);
			}
		}
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.SolutionInterface#getPropertyStoreNames()
	 */
	public List<String> getPropertyStoreNames() {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < getExposedProperties().size(); i++) {
			if (getExposedProperties().getConfig(i) instanceof ExposedProperty) {
				String store = ((ExposedProperty) getExposedProperties()
						.getConfig(i)).getStoreName();
				if (store != null && !list.contains(store))
					list.add(store);
			}
		}
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.interfaces.SolutionInterface#getInstanceID()
	 */
	public String getInstanceID() {
		return getStringParameter(InternalSchema.SI_INSTANCE_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.SolutionInterface#setInstanceID(java.lang
	 * .String)
	 */
	public void setInstanceID(String id) {
		setStringParameter(InternalSchema.SI_INSTANCE_ID, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.SolutionInterface#getHealthPollInterval()
	 */
	public int getHealthPollInterval() {
		return getIntegerParameter(InternalSchema.SI_EXP_HEALTH_POLL, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.SolutionInterface#setHealthPollInterval(int)
	 */
	public void setHealthPollInterval(int seconds) {
		setIntegerParameter(InternalSchema.SI_EXP_HEALTH_POLL, seconds);
	}

	public Object getClone() throws Exception {
		SolutionInterfaceImpl si = new SolutionInterfaceImpl();

		for (int i = 0; i < exposedProperties.size(); i++)
			si.getExposedProperties()
					.addConfig(
							(ExposedProperty) exposedProperties.getConfig(i)
									.getClone());

		for (int i = 0; i < exposedAssemblyLines.size(); i++)
			si.addExposedAssemblyLine(exposedAssemblyLines.getConfig(i)
					.getShortName());

		si.setHealthAssemblyLine(getHealthAssemblyLine());
		si.setHealthPollInterval(getHealthPollInterval());
		si.setInstanceID(getInstanceID());
		si.setEnabled(getEnabled());

		si.setName(getName());
		si.setModTS(getModTS());
		return si;
	}
}
