/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ide.IDE;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.wizards.NewIncludeWizard;

public class NewNamespaceAction extends BaseAction {

	public NewNamespaceAction() {
	}

	public void run(IAction action) {
		NewIncludeWizard wiz = new NewIncludeWizard();
		WizardDialog dlg = null;
		dlg = new WizardDialog(getShell(), wiz);
		if(dlg.open() == Window.OK) {
			try {
				IFolder folder = (IFolder)getFirstSelection();
				NamespaceConfig nc = wiz.getNamespaceConfig();
				IFile file = folder.getFile(nc.getShortName() + "." + TDIConfigurationFile.XT_NAMESPACE);
				TDIConfigurationFile cfg = new TDIConfigurationFile(file);
				cfg.setDefaultConfigObject(nc.getShortName(), nc);
				cfg.commitVersion(true);
				IDE.openEditor(getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
			}
		}
	}

}
