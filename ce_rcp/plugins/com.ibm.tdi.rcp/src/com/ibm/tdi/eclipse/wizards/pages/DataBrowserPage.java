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
import com.ibm.tdi.eclipse.databrowser.DataBrowser;

public class DataBrowserPage extends WizardPage implements ConnectorPage {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Composite container;
	private DataBrowser widget;

	public DataBrowserPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	public DataBrowserPage(String pageName) {
		super(pageName, "Data Browser", Activator.getImageDescriptor("Parser")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());
		container.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		setMessage(Messages.getString("DataBrowser.description"));
		setControl(container);
	}

	public void setConfiguration(ConnectorConfig cc) {
		if(widget != null) {
			widget.dispose();
		}
		widget = DataBrowser.getInstance(container, cc);
		container.getParent().layout(true, true);
	}

}
