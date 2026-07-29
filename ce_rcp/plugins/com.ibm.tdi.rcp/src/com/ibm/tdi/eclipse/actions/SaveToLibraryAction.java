/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class SaveToLibraryAction extends BaseAction {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public SaveToLibraryAction() {
	}

	public void run(IAction action) {
		IProject project = Utils.getProjectFor((BaseConfiguration) getFirstSelection());
		String folder = Utils.getFolderForConfig((BaseConfiguration) getFirstSelection());
		try {
			Utils.createFileFromConfig(project, folder, (BaseConfiguration) getFirstSelection(), getShell());
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		if(getFirstSelection() != null) {
			IProject project = Utils.getProjectFor((BaseConfiguration) getFirstSelection());
			if(project == null)
				action.setEnabled(false);
			if(Utils.getFolderForConfig((BaseConfiguration) getFirstSelection()) == null) {
				action.setEnabled(false);
			}
		}
	}

}
