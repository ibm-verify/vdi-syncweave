/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server.validate;

import com.ibm.di.config.interfaces.BaseConfiguration;

/**
 * The class represents issues related to design-time validation of some aspect
 * of a TDI Component. It is reminiscent of the Eclipse IMarker abstraction.
 */
public class ValidationIssue {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The level of the problem.
	 */
	private int severity = 0;

	/**
	 * The problem.
	 */
	private String problem = null;

	/**
	 * The configuration that has the problem.
	 */
	private BaseConfiguration config = null;

	/**
	 * The message that will be displayed.
	 */
	private String message = null;

	/**
	 * Constructor.
	 * 
	 * @param severity
	 *            the level of the problem.
	 * @param problem
	 *            the problem.
	 * @param config
	 *            that has the problem.
	 * @param message
	 *            that will be displayed.
	 */
	public ValidationIssue(int severity, String problem, BaseConfiguration config, String message) {
		super();
		this.severity = severity;
		this.problem = problem;
		this.config = config;
		this.message = message;
	}

	/**
	 * Return the level of the problem.
	 * 
	 * @return the severity
	 */
	public int getSeverity() {
		return severity;
	}

	/**
	 * Set level of the problem.
	 * 
	 * @param severity
	 *            the severity to be set
	 */
	public void setSeverity(int severity) {
		this.severity = severity;
	}

	/**
	 * Return the problem.
	 * 
	 * @return the problem.
	 */
	public String getProblem() {
		return problem;
	}

	/**
	 * Set the problem.
	 * 
	 * @param problem
	 *            the problem to be set
	 */
	public void setProblem(String problem) {
		this.problem = problem;
	}

	/**
	 * Return the configuration that has the problem.
	 * 
	 * @return the configuration.
	 */
	public BaseConfiguration getConfig() {
		return config;
	}

	/**
	 * Set configuration that has the problem.
	 * 
	 * @param config
	 *            the configuration to be set.
	 */
	public void setConfig(BaseConfiguration config) {
		this.config = config;
	}

	/**
	 * Return the message that will be displayed.
	 * 
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set message that will be displayed.
	 * 
	 * @param message
	 *            the message to be set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}