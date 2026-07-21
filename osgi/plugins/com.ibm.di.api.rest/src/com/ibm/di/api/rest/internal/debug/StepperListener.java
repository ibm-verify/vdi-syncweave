/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.debug;

public interface StepperListener {
	
	public void handleEvent(StepperEvent event);

}
