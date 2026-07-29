/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim.policy;

import java.io.IOException;

/**
 * Representation of password policy request and response messages.
 */
public interface PolicyServiceMessage {
	/**
	 * Typesafe enum type defintion for valid password policy request
	 * operations.
	 */
	public static class ServiceOp {
		private ServiceOp() {
			// no op
		}

		/** Validate password operation type */
		public static final ServiceOp VALIDATE_PASSWORD = new ServiceOp();
		/** Synchronize password operation type */
		public static final ServiceOp SYNC_PASSWORD = new ServiceOp();
	}

	/**
	 * Get the network message data representation.
	 * 
	 * @return The message data, or <code>null</code> if not set.
	 */
	String getMessageData() throws IOException;

	/**
	 * Get the operation type.
	 * 
	 * @return The operation type instance. Will be one of the references
	 *         defined on {@link PolicyServiceMessage.ServiceOp} .
	 */
	ServiceOp getOperation();
}
