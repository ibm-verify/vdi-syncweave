/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.wizards.NullValueBehaviorWizard;

public class NullBehaviorAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public void run(IAction action) {
		BaseConfiguration first = (BaseConfiguration) getFirstSelection();
		NullValueBehaviorWizard wiz = new NullValueBehaviorWizard(first);
		WizardDialog dlg = new WizardDialog(getShell(), wiz);
		if (dlg.open() != Dialog.OK)
			return;
			
		for (Object o:getSelectionItems()) {
			if (o == first)
				continue;
			BaseConfiguration config = (BaseConfiguration) o;
			config.setNullBehavior(first.getNullBehavior());
			config.setNullBehaviorValue(first.getNullBehaviorValue());
			config.setNullDefinition(first.getNullDefinition());
			config.setNullDefinitionValue(first.getNullDefinitionValue());
		}
	}
}
