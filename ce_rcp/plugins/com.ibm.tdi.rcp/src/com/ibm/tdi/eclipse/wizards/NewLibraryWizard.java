/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ibm.tdi.eclipse.Messages;

public class NewLibraryWizard extends Wizard {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public Text name;
	public Text clazz;
	private String libName;
	private String libClass;


	public NewLibraryWizard() {
		super();
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(new NameAndLibPage("1")); //$NON-NLS-1$
	}

	@Override
	public boolean canFinish() {
		return getLibClass().length() > 0 && getLibName().length() > 0;
	}
	
	public String getLibName() {
		return (libName == null ? "" : libName); //$NON-NLS-1$
	}
	
	public String getLibClass() {
		return (libClass == null ? "" : libClass); //$NON-NLS-1$
	}

	
	private class NameAndLibPage extends WizardPage {

		public NameAndLibPage(String pageName) {
			super(pageName);
			setTitle(Messages.getString("JavaLibrariesWidget.1")); //$NON-NLS-1$
			setDescription(Messages.getString("JavaLibrariesWidget.description")); //$NON-NLS-1$
		}
		
		public void createControl(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			c.setLayout(new GridLayout(2,false));
			
			// script object name
			new Label(c, SWT.LEFT).setText(Messages.getString("JavaLibrariesWidget.3")); //$NON-NLS-1$
			name = new Text(c, SWT.BORDER);
			name.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			
			// java class name
			new Label(c, SWT.LEFT).setText(Messages.getString("JavaLibrariesWidget.4")); //$NON-NLS-1$
			clazz = new Text(c, SWT.BORDER);
			clazz.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			setControl(c);
			
			ModifyListener ml = new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					libName = name.getText();
					libClass = clazz.getText();
					getContainer().updateButtons();
				}
			};
			name.addModifyListener(ml);
			clazz.addModifyListener(ml);
		}
		
	}

}
