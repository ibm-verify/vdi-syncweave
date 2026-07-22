/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This Exception class can be used when a minor Exception has occured. In most
 * cases it would be ignored. It accepts a list of Exceptions as sources, and
 * will display them all when printed.
 */
public class IgnorableException extends Exception {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * List of Throwables, contained by this one.
	 */
	private List<Throwable> exceptions;

	/**
	 * Constructor.
	 */
	public IgnorableException() {
		super();
		exceptions = new ArrayList<Throwable>();
	}

	/**
	 * Constructor.
	 * 
	 * @param ex
	 *            exception's cause.
	 */
	public IgnorableException(Throwable ex) {
		super(ex);
		exceptions = new ArrayList<Throwable>();
		exceptions.add(ex);
	}

	/**
	 * Constructor.
	 * 
	 * @param exs
	 *            list of occurred exceptions.
	 */
	public IgnorableException(List<Throwable> exs) {
		super();
		exceptions = exs;
	}

	/**
	 * Outputs a printable representation of the all causes stored in this
	 * Exception.
	 */
	@Override
	public void printStackTrace() {
		printStackTrace(System.err);
	}

	/**
	 * Outputs a printable representation of the all causes stored in this
	 * Exception.
	 * 
	 * @param err
	 *            The stream to write the walkback on.
	 */
	@Override
	public void printStackTrace(PrintStream err) {
		super.printStackTrace(err);
		err.println();
		err.println("Contained exceptions:");
		for (Throwable ex : exceptions) {
			ex.printStackTrace(err);
		}
	}

	/**
	 * Outputs a printable representation of the all causes stored in this
	 * Exception.
	 * 
	 * @param err
	 *            The writer to write the walkback on.
	 */
	@Override
	public void printStackTrace(PrintWriter err) {
		super.printStackTrace(err);
		err.println();
		err.println("Contained exceptions:");
		for (Throwable ex : exceptions) {
			ex.printStackTrace(err);
		}
	}

	/**
	 * Adds a Throwable to the list of contained ones.
	 * 
	 * @param t
	 *            a Throwable to add to the managed list.
	 */
	public void addThrowable(Throwable t) {
		exceptions.add(t);
	}

	/**
	 * Returns the list of managed Exceptions.
	 * 
	 * @return the list of managed Exceptions.
	 */
	public List<Throwable> getThrowables() {
		return exceptions;
	}

	/**
	 * Determines if this exception contains any Throwables.
	 * 
	 * @return wheter the list of managed Throwables is Empty or not.
	 */
	public boolean isEmpty() {
		return exceptions.isEmpty();
	}

	/**
	 * Answers the extra information message which was provided when the
	 * throwable was created. The messages of the contained Throwables will also
	 * be listed. If no message was provided for any of the Throwables,
	 * <b>null</b> will be logged for it.
	 * 
	 * @return String The receiver's message.
	 */
	@Override
	public String getMessage() {
		StringBuilder compoundMessage = new StringBuilder(super.getMessage());

		compoundMessage.append(", Contained exceptions:");
		for (Throwable t : exceptions) {
			compoundMessage.append(" ");
			compoundMessage.append(t.getMessage());
		}
		return compoundMessage.toString();
	}

}
