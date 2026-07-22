/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;

import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.wizards.PublishALWizard;

public class PublishALAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public PublishALAction() {
		super();
	}

	public void run(IAction action) {
		try {
			PublishALWizard wiz = new PublishALWizard((IFile) getFirstSelection());
			WizardDialog dlg = new WizardDialog(getShell(), wiz);
			dlg.open();
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
	}

}
