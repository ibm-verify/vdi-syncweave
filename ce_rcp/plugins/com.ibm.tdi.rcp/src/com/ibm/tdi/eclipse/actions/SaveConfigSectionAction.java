/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import java.io.File;
import java.io.FileOutputStream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.SaveAsDialog;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;

public class SaveConfigSectionAction extends Action implements DragSourceListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private BaseConfiguration config;
	private File file;
	
	public SaveConfigSectionAction(BaseConfiguration config) {
		super();
		this.config = config;
	}

	@Override
	public void run() {
		SaveAsDialog dlg = new SaveAsDialog(Display.getDefault().getActiveShell());
		if(dlg.open() == Window.OK) {
			MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Save As", "Not implemented yet");
		}
	}

	@Override
	public String getText() {
		return "Save As...";
	}

	public void dragFinished(DragSourceEvent event) {
		if(file != null) {
			file.delete();
			file = null;
		}
	}

	public void dragSetData(DragSourceEvent event) {
		StructuredSelection sel = new StructuredSelection(config);
		LocalSelectionTransfer.getTransfer().setSelection(sel);
//		event.data = sel;
		if(file == null) {
			try {
				String prefix = config.getShortName();
				BaseConfiguration parent = config.getParent();
				while(prefix == null && parent != null) {
					prefix = parent.getShortName();
					parent = parent.getParent();
				}
				if(prefix == null)
					prefix = "Copy";
				
				file = File.createTempFile(prefix, ".hooks");
				TDIConfigurationFile cfg = new TDIConfigurationFile();
				BaseConfiguration bc = (BaseConfiguration) config.getClone();
				cfg.setDefaultConfigObject(prefix, bc);
				cfg.commitChanges(new FileOutputStream(file), false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		event.data = new String[]{file.getAbsolutePath()};
		event.doit = true;
	}

	public void dragStart(DragSourceEvent event) {
	}
}
