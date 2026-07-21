/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.model.def;

/**
 * The abstract definition for the data model. 
 * It provides common class properties. 
 * 
 * @author yavor.gologanov
 *
 */
public abstract class AbstractDefinition {

	private String displayPrefix = null;
	private String displayName = null;
	private boolean visible = true;
	
	/**
	 * Determines whether this definition is visible in the DI Entry. 
	 * 
	 * @return boolean 
	 * 				true if the definition is visible, false otherwise
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Makes this definition visible or hides depending on the value of parameter visible. 
	 * 
	 * @param visible 
	 * 				true if the definition is visible, false otherwise
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Returns a name for the DI Entry attribute that corresponds to this definition.
	 * 
	 * @return String 
	 * 				the DI Entry attribute name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets a corresponding DI Entry attribute.
	 * 
	 * @param displayName 
	 * 				the DI Entry attribute name
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}		
	
	/**
	 * Returns the namespace prefix of this definition, or null if it is unspecified.
	 * 
	 * @return String
	 */
	public String getDisplayPrefix() {
		return displayPrefix;
	}

	/**
	 * Sets the namespace prefix of this definition.
	 * 
	 * @param displayPrefix String
	 */
	public void setDisplayPrefix(String displayPrefix) {
		this.displayPrefix = displayPrefix;
	}	
	
}