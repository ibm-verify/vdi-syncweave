/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.exceptions;

public interface ErrorListener {

	public boolean handleError(Object source, Exception error);

}
