/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.widget.DiscoverSchemaWidget;
import com.ibm.tdi.eclipse.wizards.ConnectorConfigWizard;

public class DiscoverSchemaPage extends WizardPage implements ConnectorPage {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private DiscoverSchemaWidget ds;

	public DiscoverSchemaPage(String pageName) {
		super(pageName, Messages.getString("ConnectorSchema.toolbar.Discover"), null);
		setDescription(Messages.getMessage("DiscoverSchemaPage.description", null));
	}

	public void createControl(Composite parent) {
		ConnectorConfig cc = (ConnectorConfig) ((ConnectorConfigWizard) getWizard()).getConnectorConfig();
		ds = new DiscoverSchemaWidget(parent, SWT.CHECK, cc, null);
		setControl(ds);
		parent.layout(true, true);
	}

	public void setConfiguration(ConnectorConfig cc) {
		ds.setEditingConfig(cc);
	}

	public void createAttributeMaps() {
		ds.createAttributeMaps();
	}
	
}
