/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// JobStatus.java
//
//
//
package com.ibm.di.server;

/**
 * This class marks the moment when an object of its is created. Also monitors
 * for the end time an exceptions that may occur.
 * 
 */
public class JobStatus {

	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The name attribute of the JobStatus object.
	 */
	public String name;

	/**
	 * The thread attribute of the JobStatus object.
	 */
	public Object thread;

	/**
	 * The thread name attribute of the JobStatus object.
	 */
	public String threadName;

	/**
	 * The start time of the JobStatus object (in milliseconds).
	 */
	public long start;

	/**
	 * The end time of the JobStatus object (in milliseconds).
	 */
	public long end;

	/**
	 * The exception attribute of the JobStatus object (in milliseconds).
	 */
	public Exception exception;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            a name for the object
	 * @param thread
	 *            a thread object
	 */
	public JobStatus(String name, Object thread) {
		this.name = name;
		this.thread = thread;
		start = System.currentTimeMillis();
		exception = null;
	}

	/**
	 * Ends the JobStatus.
	 */
	public void end() {
		this.end = System.currentTimeMillis();
	}

	/**
	 * Returns the ending status of the job.
	 * 
	 * @return 'OK' if no exception has occured, otherwise the string of the
	 *         exception is returned
	 */
	public String endStatus() {
		if (exception != null)
			return exception.toString();
		else
			return "OK";
	}
}
