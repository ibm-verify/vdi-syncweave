/*
 * Copyright IBM Corp. 2025
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
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.widget.LinkCriteriaWidget;
import com.ibm.tdi.eclipse.wizards.ConnectorConfigWizard;

public class LinkCriteriaPage extends WizardPage implements ConnectorPage {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private LinkCriteriaWidget widget;
	private Composite container;

	public LinkCriteriaPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	public LinkCriteriaPage(String pageName) {
		super(pageName, Messages.getString("ConnectorUI.LinkCriteria.label"), Activator.getImageDescriptor("Link")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());
		ConnectorConfig cc = (ConnectorConfig) ((ConnectorConfigWizard)getWizard()).getConnectorConfig();
		if(cc != null) {
			widget = new LinkCriteriaWidget(cc.getLinkCriteria(), container, SWT.NULL);
		}
		setControl(container);
	}

	public void setConfiguration(ConnectorConfig cc) {
		if(widget != null)
			widget.dispose();

		widget = new LinkCriteriaWidget(cc.getLinkCriteria(), container, SWT.NULL);
		container.getParent().layout(true, true);
	}

}
