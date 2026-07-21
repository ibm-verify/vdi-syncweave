/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.builders;

import org.eclipse.core.resources.IMarker;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.Utils;

public abstract class ComponentValidator {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	/**
	 * Used when a component references an attribute not defined in the component's schema
	 */
	public final static String SCHEMA_NOT_DEFINED = "schema.not.defined";
	
	/**
	 * Used when a component references an attribute not defined in the work entry
	 */
	public final static String WORK_NOT_DEFINED = "work.not.defined";
	
	/**
	 * Used when a property isn't found in any of the property stores 
	 */
	public final static String PROPERTY_NOT_DEFINED = "property.not.defined";
	
	/**
	 * Used when an AL has more than one server mode connector
	 */
	public final static String MULTIPLE_SERVERS = "multiple.server.mode";
	
	/**
	 * Used for script syntax errors
	 */
	public final static String SCRIPT_SYNTAX_ERROR = "script.syntax.error";

	public IMarker addProblem(int severity, String problem, BaseConfiguration config, String message) {
		return Utils.logProblem(severity, problem, config, message);
	}

	/**
	 * Perform a validation of the provided configuration.
	 * 
	 * @param configuration
	 * @return true if no conditions were found (false)
	 * @throws Exception
	 */
	public abstract boolean validate(BaseConfiguration configuration) throws Exception;
	
}
