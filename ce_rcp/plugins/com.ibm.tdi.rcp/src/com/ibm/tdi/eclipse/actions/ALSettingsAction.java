/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.dialogs.GenericFormDialog;
import com.ibm.tdi.eclipse.editors.AssemblyLineEditor2;
import com.ibm.tdi.eclipse.editors.AssemblyLineEditor3;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class ALSettingsAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IWorkbenchPart part;
	private AssemblyLineConfig target;

	public ALSettingsAction() {
		super();
	}

	public ALSettingsAction(IWorkbenchPart part, AssemblyLineConfig target) {
		super();
		this.part = part;
		this.target = target;
	}

	public void run(IAction action) {
		if(target != null && part != null) {
			GenericFormDialog dlg = new GenericFormDialog(part.getSite().getShell(),
				"AL Settings", target.getSettings());  //$NON-NLS-1$
			dlg.setTitle(Messages.getString("assemblyline.tabs.settings.tooltip")); //$NON-NLS-1$
			dlg.setNoCancel();
			dlg.open();
		} else if (getFirstSelection() instanceof IFile) {
			openALEditor((IFile)getFirstSelection(), action.getActionDefinitionId().equals("com.ibm.tdi.rcp.allogsettings.action"));
		}
	}
	
	private void openALEditor(IFile file, boolean logsettings) {
		try {
			IEditorPart editor = IDE.openEditor(getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
			if(editor instanceof AssemblyLineEditor3) {
				if(logsettings)
					((AssemblyLineEditor3)editor).showLogSettingsDialog();
				else
					((AssemblyLineEditor3)editor).showALSettingsDialog();
			}
		} catch (PartInitException e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
	}

}
