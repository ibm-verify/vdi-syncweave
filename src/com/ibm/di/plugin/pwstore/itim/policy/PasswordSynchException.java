/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim.policy;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * <p>
 * Exception type thrown when password synchronization fails.
 * </p>
 * <p>
 * This class provides a method to retrieve the list of passwords that have not
 * been synchronized. This situation could occur when an error was detected
 * before all passowrds could be synchronized.
 * </p>
 * 
 */
public class PasswordSynchException extends Exception {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	private List unSynchedPass;

	/**
	 * 
	 */
	public PasswordSynchException() {
		super();
	}

	/**
	 * @param message
	 */
	public PasswordSynchException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PasswordSynchException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public PasswordSynchException(Throwable cause) {
		super(cause);
	}

	/**
	 * @return The list of unsynchronized passwords, or null if not set. This
	 *         list is unmodifiable.
	 */
	public List getUnSynchronizedPasswords() {
		return unSynchedPass;
	}

	/**
	 * Takes a view of the sub list of the source list starting at the iterator
	 * to the size of the source list. This method and class do not modify the
	 * source list. Any post method execution of the source list will invalidate
	 * this objects' stored view of the source list.
	 * 
	 * @param i
	 *            The first source element of the sub list
	 * @param src
	 *            The original list of passwords.
	 * 
	 * @throws IllegalArgumentException
	 *             if any of the parameters are null.
	 */
	public void initUnSynchronizedPasswords(ListIterator i, List src) {
		if (i == null || src == null) {
			throw new IllegalArgumentException();
		}

		int startIdx = i.previousIndex();
		if (startIdx < 0) {
			startIdx = 0;
		}

		List l = src.subList(startIdx, src.size());
		this.unSynchedPass = Collections.unmodifiableList(l);
	}

}
