/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.util.List;

import com.ibm.di.config.interfaces.BaseConfiguration;

/**
 * Performs some kind of validation over a {@link BaseConfiguration}. As a
 * result a list of validation issues is returned.
 */
public interface Validator {

	/**
	 * The severity level for error.
	 */
	public static final int VALIDATION_ERROR = 2;

	/**
	 * The severity level for information.
	 */
	public static final int VALIDATION_INFO = 0;

	/**
	 * Perform validation.
	 * 
	 * @return list of validation issues.
	 * @throws ValidationException
	 *             if an error occurs.
	 */
	public List<ValidationIssue> validate() throws ValidationException;

	/**
	 * Initialize validator.
	 * 
	 * @param config
	 *            that contains information for validation.
	 */
	public void initialize(BaseConfiguration config);

	/**
	 * Terminate validator.
	 */
	public void terminate();

}