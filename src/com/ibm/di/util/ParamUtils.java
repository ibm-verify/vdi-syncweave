/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.util.HashMap;
import java.util.Map;
import com.ibm.di.server.ResourceHash;

/**
 * Utility class for work with command-line parameters.
 * 
 * @since 7.0
 */
public class ParamUtils {

	private static final String PROPERTIES_FILE = "miserver";

	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Retrieves the value of a required parameter.
	 * 
	 * @param params collection of parameters
	 * @param paramName parameter to retrieve
	 * @return the value of the parameter
	 * @throws Exception if the parameter is not found in the given collection
	 */
	public static String getRequiredParam(Map params, String paramName)
			throws Exception {

		String value = (String) params.get(paramName);

		if (value == null) {
			throw new Exception(resHash.getString(
					"PARAM.UTILS.MISSING.REQUIRED.PARAMETER", paramName));
		}

		return value;
	}

	/**
	 * Retrieves the value of an optional parameter.
	 * If the parameter is not set, a default value is returned.
	 * 
	 * @param params collection of parameters
	 * @param paramName parameter to retrieve
	 * @param defaultValue default value
	 * @return the value of the parameter or the default, if the parameter is not set
	 */
	public static String getOptionalParam(Map params, String paramName,
			String defaultValue) {

		String value = (String) params.get(paramName);

		if (value == null) {
			value = defaultValue;
		}

		return value;
	}

	/**
	 * Builds a collection of parameters (name-value) from a given command-line.
	 * If a command-line token starts with "-" it is considered to be a parameter name,
	 * otherwise it is deemed a parameter value.
	 * It is allowed to have parameters without a value - in this case an empty string is used
	 * to represent the value.
	 * 
	 * An example for a valid command-line is: "-flagA -param1 value1 -param2 value2 -flagB".
	 * 
	 * @param args command-line arguments
	 * @return parameter name to parameter value mapping
	 * @throws Exception if a value without corresponding parameter name is encountered
	 */
	public static Map parseCommandLine(String[] args) throws Exception {

		Map params = new HashMap();

		int i = 0;
		while (i < args.length) {

			if (!args[i].startsWith("-")) {
				throw new Exception(resHash.getString(
						"PARAM.UTILS.UNEXPECTED.CMDLINE.TOKEN", args[i]));
			}

			String paramName = args[i].substring(1); // skip the "-" at the beginning

			// read the value for this param
			String value = "";
			if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
				value = args[i + 1];
				++i; // the value is already read, so skip it
			}

			params.put(paramName, value);

			++i; //get to the next param name
		}

		return params;

	}

	/**
	 * Retrieves the value of a required system property.
	 * 
	 * @param prop
	 *            system property name
	 * @return the system property value
	 * @throws Exception
	 *             if the property is not set
	 */
	public static String getRequiredProperty(String prop) throws Exception {

		String value = System.getProperty(prop);

		if (value == null) {
			throw new Exception(resHash.getString(
					"PARAM.UTILS.MISSING.REQUIRED.SYSTEM.PROPERTY", prop));
		}

		return value;
	}

}
