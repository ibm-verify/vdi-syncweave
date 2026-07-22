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

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.widget.ParserWidget;
import com.ibm.tdi.eclipse.wizards.ConnectorConfigWizard;

public class ParserConfigPage extends WizardPage implements ConnectorPage, MetamergeConfigChangeListener {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private ParserWidget widget;
	private Composite container;
	private ConnectorConfig config;

	public ParserConfigPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	public ParserConfigPage(String pageName) {
		super(pageName, Messages.getString("PropertyStore.Parser"), Activator.getImageDescriptor("Parser")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());
		container.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		ConnectorConfig cc = (ConnectorConfig) ((ConnectorConfigWizard)getWizard()).getConnectorConfig();
		if(cc != null) {
			widget = new ParserWidget(container, SWT.FILL, cc.getParserConfig());
		}
		setControl(container);
		container.layout(true,true);
	}

	public void setConfiguration(ConnectorConfig cc) {
		if(widget != null)
			widget.dispose();
		
		if(config != null)
			config.getParserConfig().removeListener(this);
		
		config = cc;
		config.getParserConfig().addListener(this);

		widget = new ParserWidget(container, SWT.FILL, cc.getParserConfig());
		container.getParent().layout(true, true);
	}

	public void configurationChanged(MetamergeConfigChange changeEvent) {
		if ("setInheritsFrom".equals(changeEvent.getUserObject())) {
			setConfiguration(config);
		}
	}

}
