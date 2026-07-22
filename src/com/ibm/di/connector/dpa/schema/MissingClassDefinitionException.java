/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.schema;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class MissingClassDefinitionException extends RuntimeException {
	
	private static final long serialVersionUID = -3418768836110908583L;
	
	private String className = null;
	
	/**
	 * 
	 * @param className
	 */
	public MissingClassDefinitionException(String className) {
		this.className = className;
	}

	/**
	 * 
	 */
	public String getMessage() {
		return "Missing class definition: " + className;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getClassName() {
		return className;
	}
	
}
