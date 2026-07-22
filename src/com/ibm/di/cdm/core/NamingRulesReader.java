/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.core;

import java.util.List;

/**
 * The base class for all Naming Rule readers.
 * 
 */
public abstract class NamingRulesReader {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Returns all naming rules for the provided class.
	 * 
	 * @param cdmClassType
	 *            the CDM class type.
	 * @return a {@link List} of {@link NamingRule}s.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public abstract List<NamingRule> getAllNamingRules(String cdmClassType) throws Exception;

	/**
	 * Gets the fully qualified name of the provided class type.
	 * 
	 * @param cdmClassName
	 *            the CDM class type.
	 * @return the fully qualified name.
	 */
	protected abstract String getFullyQualifiedName(String cdmClassName);

	/**
	 * Gets the short version of the provided CDM class type.
	 * 
	 * @param cdmClassType
	 *            the CDM class type.
	 * @return the short name.
	 */
	protected String getShortName(String cdmClassType) {
		String newName = cdmClassType;
		int index = cdmClassType.lastIndexOf(".");
		if (index >= 0) {
			newName = cdmClassType.substring(index + 1);
		}
		return newName;
	}

}