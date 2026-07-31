/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.Messages;

public class AddSchemaItemAction extends Action implements IInputValidator {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private SchemaConfig sc;
	private Shell shell;
	public AddSchemaItemAction(Shell shell, SchemaConfig sc) {
		super();
		this.sc = sc;
		this.shell = shell;
		setText(Messages.getString("AddSchemaItemAction.1"));
		setToolTipText(Messages.getString("AddSchemaItemAction.2"));
	}

	public void run() {
		String newName = null;
		InputDialog id = new InputDialog(shell,
				Messages.getString("AddSchemaItemAction.3"),
				Messages.getString("AddSchemaItemAction.4"),
				"", this);
		if(id.open() == Window.OK)
			newName = id.getValue();
		if(newName != null && newName.trim().length() > 0) {
			newName = newName.trim();
			try {
				sc.newItem(newName);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e);
			}
		}
	}

	public String isValid(String newText) {
		if (newText == null)
			return null;
		newText = newText.trim();
		if(sc.getItem(newText)!= null)
			return Messages.getMessage("AddSchemaItemAction.5", newText);	
		else
			return null;
	}

}
