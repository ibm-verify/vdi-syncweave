/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.wizards.pages.RefactorComponentPage;
import com.ibm.tdi.eclipse.wizards.pages.RenameComponentPage;

public class RenameComponentWizard extends Wizard {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IResource resource;
	private IInputValidator validator;
	private List<IFile> refactorList;

	private RenameComponentPage rename;

	private RefactorComponentPage refactor;

	public RenameComponentWizard(IResource resource, IInputValidator validator) {
		this.resource = resource;
		this.validator = validator;
	}
	
	public List<IFile> getRefactorList() {
		return refactorList;
	}

	@Override
	public boolean performFinish() {
		this.refactorList = ((RefactorComponentPage)getPage("refactor")).getRefactorItems();
		return true;
	}

	@Override
	public void addPages() {
		rename = new RenameComponentPage("rename", validator, resource);
		refactor = new RefactorComponentPage("refactor", resource);
		addPage(rename);
		addPage(refactor);
		if(!refactor.hasReferences())
			rename.setWillRefactor(false);
	}
	
	private boolean willRefactor() {
		if (rename.willRefactor()) {
			return refactor.hasReferences();
		}
		return false;
	}

	@Override
	public boolean canFinish() {
		if(!willRefactor())
			return super.canFinish();
		
		// make sure the user sees this page before finishing
		return ((IWizardContainer)getContainer()).getCurrentPage() == refactor;
	}

	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage p = super.getNextPage(page);
		if (p != null && p.getName().equals("refactor") && !willRefactor()) {
			return null;
		}
		return p;
	}
	
	@Override
	public String getWindowTitle() {
		return Messages.getString("action.label.1");
	}

	public String getNewFileName() {
		return rename.getNewFilename();
	}
}
