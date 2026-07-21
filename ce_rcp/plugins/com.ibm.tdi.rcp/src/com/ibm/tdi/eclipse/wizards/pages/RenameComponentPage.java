/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards.pages;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.tdi.eclipse.Messages;

public class RenameComponentPage extends WizardPage {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IInputValidator validator;
	private Label status;
	private Button refactor;
	protected String newNameStr;
	private boolean willRefactor = true;

	public RenameComponentPage(String pageName, IInputValidator validator, IResource resource) {
		super(pageName);
		this.validator = validator;
		this.newNameStr = resource.getName().substring(0, resource.getName().lastIndexOf("."));
		setTitle(Messages.getString("general.rename.tooltip"));
		setDescription(newNameStr);
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new RowLayout(SWT.VERTICAL));
		Label l = new Label(c, SWT.LEFT);
		l.setText(Messages.getString("RenameWorkAttributeItem.1"));
		final Text newName = new Text(c, SWT.BORDER | SWT.SINGLE);
		newName.setText(newNameStr);
		newName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				newNameStr = newName.getText();
				String str;
				if(validator != null) {
					str = validator.isValid(newNameStr);
					if(str == null) {
						status.setText("");
					} else {
						status.setText(str);
					}
					setPageComplete(str == null);
				} else {
					setPageComplete(newNameStr != null && newNameStr.length() > 0);
				}
			}
		});
		newName.setLayoutData(new RowData(200, SWT.DEFAULT));

		refactor = new Button(c, SWT.CHECK);
		refactor.setText(Messages.getString("rename.update.refs"));
		refactor.setSelection(true);
		refactor.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setPageComplete(isPageComplete());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		refactor.setEnabled(willRefactor);
		
		status = new Label(c, SWT.LEFT);
		status.setLayoutData(new RowData(400, SWT.DEFAULT));
		setControl(c);
	}
	
	public static boolean canRefactor(IResource res) {
		String folder = TDIConfigurationFile.getFolderForExtension(res.getFileExtension());
		if(MetamergeConfig.DEFAULT_CONNECTOR_FOLDER.equals(folder) ||
				MetamergeConfig.DEFAULT_FUNCTION_FOLDER.equals(folder) ||
				MetamergeConfig.DEFAULT_PARSER_FOLDER.equals(folder) ||
				MetamergeConfig.DEFAULT_ATTRIBUTEMAP_FOLDER.equals(folder) ||
				MetamergeConfig.DEFAULT_SCRIPT_FOLDER.equals(folder))
			return true;
		return false;
	}
	
	public void setWillRefactor(boolean willRefactor) {
		this.willRefactor = willRefactor;
	}

	public boolean willRefactor() {
		if(refactor != null && refactor.isEnabled())
			return refactor.getSelection();
		else
			return false;
	}

	public String getNewFilename() {
		return newNameStr;
	}

}
