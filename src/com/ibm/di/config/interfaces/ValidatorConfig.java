/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * A validator configuration that can be provided in the form of the Component.
 */
public interface ValidatorConfig extends BaseConfiguration {

	/**
	 * Validator class parameter name.
	 */
	public static final String VALIDATOR_CLASS = "class";

	/**
	 * Validator type parameter name.
	 */
	public static final String VALIDATOR_TYPE = "type";

	/**
	 * Set the validator class in Base Configuration.
	 * 
	 * @param clazz
	 *            to be set.
	 */
	public void setValidatorClass(String clazz);

	/**
	 * Get the validator class from Base Configuration.
	 * 
	 * @return validator class.
	 */
	public String getValidatorClass();

	/**
	 * Set the validator type in Base Configuration.
	 * 
	 * @param type
	 *            to be set.
	 */
	public void setType(String type);

	/**
	 * Get the validator type from Base Configuration.
	 * 
	 * @return validator type.
	 */
	public String getType();

}