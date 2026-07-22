/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.extensions;

import java.util.ArrayList;

import org.eclipse.ui.IEditorSite;

import com.ibm.di.config.interfaces.BaseConfiguration;

/**
 * This interface is used by TDI's extension points to invoke a handler for a contributed
 * extension object.
 */
public interface TDIExtension {
	
	/**
	 * This method is invoked before the run() method is invoked on an IAction/TDIAction object.
	 * The target configuration objects is a list of config objects selected for this command.
	 * 
	 * If the configuration object is null then the editing configuration should be used.
	 * 
	 * @param configuration
	 */
	public void setTargetConfigurationObjects(ArrayList<BaseConfiguration> configuration);
	
	/**
	 * This method calls setTargetConfigurationObjects(ArrayList<BaseConfiguration> configuration).
	 * 
	 * @param configuration
	 */
	public void setTargetConfigurationObject(BaseConfiguration configuration);
	
	/**
	 * This is method is invoked once to set the configuration object of the IEditor.
	 * 
	 * @param configuration
	 */
	public void setEditingConfiguration(BaseConfiguration configuration);
	
	/**
	 * Invoked before the run method is invoked on the action object. The source is the
	 * typically the object that owns the extension point (e.g. toolbar, popup menu).  
	 */
	public void setSource(Object source);
	
}
