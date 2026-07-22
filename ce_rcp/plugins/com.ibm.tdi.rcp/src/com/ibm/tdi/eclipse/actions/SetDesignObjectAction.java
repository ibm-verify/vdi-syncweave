/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.SchemaEditor;

public class SetDesignObjectAction extends Action {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private BaseConfiguration config;
	private IProject project;
	private ArrayList<IFile> files;
	
	public SetDesignObjectAction(BaseConfiguration config) {
		super();
		this.config = config;
		project = Utils.getProjectFor(config);
		setText(Messages.getString("DiscoverSchemaWidget.design"));
	}

	@Override
	public boolean isEnabled() {
		files = SchemaEditor.getDesignSchemaFiles(project);
		return files != null;
	}

	@Override
	public void run() {
		if (files == null)
			return;
		
		Shell shell = Display.getCurrent().getActiveShell();
		try {
			
			ListDialog dlg = new ListDialog(shell) {
				@Override
				protected void createButtonsForButtonBar(Composite parent) {
					super.createButtonsForButtonBar(parent);
					createButton(parent, SWT.DEL, Messages.getString("general.delete.label"), false);
				}

				@Override
				protected void buttonPressed(int buttonId) {
					if(buttonId == SWT.DEL) {
						setReturnCode(SWT.DEL);
						close();
					} else {
						super.buttonPressed(buttonId);
					}
				}
				
			};
			dlg.setTitle(Messages.getString("DiscoverSchemaWidget.design"));
			dlg.setMessage(Messages.getString("DiscoverSchemaWidget.design.tooltip"));
			dlg.setContentProvider(new ArrayContentProvider());
			dlg.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return ((IFile)element).getName();
				}
			});
			
			String selectedSchema = SchemaEditor.getDesignSchemaName(config);
			dlg.setInput(files.toArray());
			for(IFile file : files) {
				if(file.getName().equals(selectedSchema))
					dlg.setInitialSelections(new Object[]{file});
			}
			
			int result = dlg.open();
			if(result == Dialog.OK) {
				selectedSchema = ((IFile)dlg.getResult()[0]).getName();
				SchemaEditor.setDesignSchemaName(config, selectedSchema);
			} else if (result == SWT.DEL) {
				SchemaEditor.setDesignSchemaName(config, null);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
