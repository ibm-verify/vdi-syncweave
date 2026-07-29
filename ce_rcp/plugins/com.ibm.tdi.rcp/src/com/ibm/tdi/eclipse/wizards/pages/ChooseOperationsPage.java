/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards.pages;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.wizards.ConnectorConfigWizard;

public class ChooseOperationsPage extends WizardPage {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public ChooseOperationsPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	public ChooseOperationsPage(String pageName) {
		super(pageName, "Choose Operations", Activator.getImageDescriptor("Schema"));
	}

	public void createControl(Composite parent) {
		ConnectorConfig cc = (ConnectorConfig) ((ConnectorConfigWizard)getWizard()).getConnectorConfig();
		Composite p = new Composite(parent, SWT.NULL);
		p.setLayout(new GridLayout());
		
		Composite tools = new Composite(p, SWT.NULL);
		tools.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		
		TableViewer table = new TableViewer(p, SWT.NULL);
		table.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				if(inputElement instanceof ConnectorConfig) {
					ConnectorConfig cc = (ConnectorConfig) inputElement;
					if(cc.getMode().equals(ConnectorConfig.ADDONLY_MODE))
						return new String[]{"Add"};
					else if(cc.getMode().equals(ConnectorConfig.UPDATE_MODE))
						return new String[]{"Lookup", "Add", "Modify"};
					else if(cc.getMode().equals(ConnectorConfig.DELETE_MODE))
						return new String[]{"Delete"};
					else if(cc.getMode().equals(ConnectorConfig.LOOKUP_MODE))
						return new String[]{"Lookup"};
					else if(cc.getMode().equals(ConnectorConfig.ITERATOR_MODE))
						return new String[]{"Get Next"};
					else if(cc.getMode().equals(ConnectorConfig.DELTA_MODE))
						return new String[]{"Add", "Modify", "Delete"};
					else
						return new String[]{cc.getMode()};
				} else {
					return new String[]{"Standard Operation"};
				}
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		table.setInput(cc);
		table.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		setControl(p);
	}

}
