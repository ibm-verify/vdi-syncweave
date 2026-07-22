/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards.pages;

import com.ibm.di.config.interfaces.ConnectorConfig;

public interface ConnectorPage {
	public void setConfiguration(ConnectorConfig cc);
}
