/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.extensions.TDIExtension;

public class TDIAction extends Action implements TDIExtension {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private ArrayList<BaseConfiguration> targetConfigurationObjects;
	private BaseConfiguration editingConfiguration;
	private Object source;

	/* (non-Javadoc)
	 * @see com.ibm.tdi.eclipse.extensions.TDIExtension#setTargetConfigurationObjects(java.util.ArrayList)
	 */
	public void setTargetConfigurationObjects(ArrayList<BaseConfiguration> targetConfigurationObjects) {
		this.targetConfigurationObjects = targetConfigurationObjects;
	}

	/* (non-Javadoc)
	 * @see com.ibm.tdi.eclipse.extensions.TDIExtension#setTargetConfigurationObject(com.ibm.di.config.interfaces.BaseConfiguration)
	 */
	public void setTargetConfigurationObject(BaseConfiguration configuration) {
		this.targetConfigurationObjects = new ArrayList<BaseConfiguration>();
		targetConfigurationObjects.add(configuration);
	}

	/**
	 * Returns the target configuration objects.
	 * @see com.ibm.tdi.eclipse.extensions.TDIExtension#setTargetConfigurationObjects(java.util.ArrayList)
	 * @return the target configuration objects.
	 */
	public ArrayList<BaseConfiguration> getTargetConfigurationObjects() {
		return targetConfigurationObjects;
	}

	/**
	 * Returns a single {@link BaseConfiguration} object or null if the list of target
	 * config objects contains zero or more than one object.
	 * @return The BaseConfiguration object
	 */
	public BaseConfiguration getSingleTargetSelection() {
		if(targetConfigurationObjects != null && targetConfigurationObjects.size() == 1)
			return targetConfigurationObjects.get(0);
		else
			return null;
	}

	/**
	 * Returns the config object used by the IEditor. This is the top-level config object
	 * such as AssemblyLineConfig, ConnectorConfig etc.
	 * @return The editor's configuration object (or null if action isn't associated with an editor)
	 */
	public BaseConfiguration getEditingConfiguration() {
		return editingConfiguration;
	}

	/* (non-Javadoc)
	 * @see com.ibm.tdi.eclipse.extensions.TDIExtension#setEditingConfiguration(com.ibm.di.config.interfaces.BaseConfiguration)
	 */
	public void setEditingConfiguration(BaseConfiguration editingConfiguration) {
		this.editingConfiguration = editingConfiguration;
	}

	/* (non-Javadoc)
	 * @see com.ibm.tdi.eclipse.extensions.TDIExtension#setSource(java.lang.Object)
	 */
	public void setSource(Object source) {
		this.source = source;
	}

	/**
	 * Returns the "source" which is typically the object that owns/creates the extension point.
	 * @return The source
	 */
	public Object getSource() {
		return source;
	}

}
