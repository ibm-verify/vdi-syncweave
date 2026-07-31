/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.debug;

public interface StepperListener {
	
	public void handleEvent(StepperEvent event);

}
