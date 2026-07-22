/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchCondition;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.widget.BranchConditionWidget;
import com.ibm.tdi.eclipse.wizards.ConnectorConfigWizard;

public class AddConditionPage extends WizardPage {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private BranchConditionWidget widget = null;
	private BranchingConfig branch = null;
	private Composite comp;
	
	public AddConditionPage(String pageName) {
		super(pageName, Messages.getString("BranchingConfig.title"), Activator.getImageDescriptor("Branch"));
		setDescription(Messages.getString("AddConditionPage.description"));
	}

	public void createControl(Composite parent) {
		BaseConfiguration bc = ((ConnectorConfigWizard) getWizard()).getConfigObject();
		comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new FillLayout());
		if(bc instanceof BranchingConfig)
			setConfiguration((BranchingConfig)bc);
		parent.layout(true);
		setControl(comp);
	}
	
	public void setConfiguration(BranchingConfig bc) {
		if (bc == branch || bc == null)
			return;
		if (widget != null)
			widget.dispose();
		branch = bc;
		BaseConfiguration loc = ((ConnectorConfigWizard) getWizard()).getLocation();
		if(loc != null)
			branch.setParent(loc);
		widget = new BranchConditionWidget(branch, comp, SWT.NONE);

		comp.layout(true);
	}
	
}
