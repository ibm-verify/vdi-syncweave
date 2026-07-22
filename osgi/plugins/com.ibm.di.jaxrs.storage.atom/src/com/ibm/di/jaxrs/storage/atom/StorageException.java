/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.jaxrs.storage.atom;

/**
 * Exception type for the storage package.
 * 
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class StorageException extends RuntimeException {
	
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	
	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = -2225687000791779763L;

	/**
	 * @param message Exception message.
	 */
	public StorageException(String message) {
		super(message);
	}
	
	/**
	 * @param message Exception message.
	 * @param cause Exception cause.
	 */
	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * @param cause Exception cause.
	 */
	public StorageException(Throwable cause) {
		super(cause);
	}
}
