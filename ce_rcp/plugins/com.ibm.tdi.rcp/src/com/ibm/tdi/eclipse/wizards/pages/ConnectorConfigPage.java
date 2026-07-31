/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards.pages;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.widget.RawConnectorWidget;
import com.ibm.tdi.eclipse.wizards.ConnectorConfigWizard;

public class ConnectorConfigPage extends WizardPage implements ConnectorPage {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private RawConnectorWidget widget;
	private Composite container;
	private ConnectorConfig cc;

	public ConnectorConfigPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	public ConnectorConfigPage(String pageName) {
		super(pageName, Messages.getString("PropertyStore.RawConnector"), Activator.getImageDescriptor("Connector"));
	}

	public void createControl(Composite parent) {
		this.container = new Composite(parent, SWT.NULL);
		this.container.setLayout(new FillLayout());
		cc = (ConnectorConfig) ((ConnectorConfigWizard) getWizard()).getConnectorConfig();
		if(cc instanceof FunctionConfig) {
			widget = new RawConnectorWidget(container, SWT.NONE, ((FunctionConfig)cc).getFunctionConfig(), true);
		} else if (cc != null) {
			widget = new RawConnectorWidget(container, SWT.NONE, cc.getConnectionConfig(), true);
		}
		setControl(container);
	}

	public void setConfiguration(ConnectorConfig cc) {
		if(widget != null) {
			widget.dispose();
			widget = null;
		}
		this.cc = cc;
		if(cc instanceof FunctionConfig)
			widget = new RawConnectorWidget(container, SWT.NONE, ((FunctionConfig)cc).getFunctionConfig(), false);
		else if(!(cc instanceof ALMappingConfig))
			widget = new RawConnectorWidget(container, SWT.NONE, cc.getConnectionConfig(), false);
		if (widget != null) {
			container.getParent().layout();
			container.layout();
		}
	}

	@Override
	public void dispose() {
		if(widget != null)
			widget.dispose();
		super.dispose();
	}

	// Override this since getNextPage will sometimes return DISCOVER page and create that one
	// before the others that would appear depending on mode, apparently a problem for the wizard
	@Override
	public boolean canFlipToNextPage() {
		if (widget!= null && widget.isLogConnector())
			return false;
//		return Utils.hasParserRequirements(cc) || Utils.hasLinkRequirements(cc);
		return Utils.hasParserRequirements(cc);
	}
}
