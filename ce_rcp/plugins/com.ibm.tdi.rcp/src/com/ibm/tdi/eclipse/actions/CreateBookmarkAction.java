/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class CreateBookmarkAction extends BaseAction {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public CreateBookmarkAction() {
	}

	public void run(IAction action) {
		BaseConfiguration b = (BaseConfiguration) getFirstSelection();
		try {
			InputDialog id = new InputDialog(getShell(), 
					Messages.getString("action.label.0"), Messages.getString("CreateBookMarkAction.prompt"),  //$NON-NLS-1$ //$NON-NLS-2$
					b.getShortName(), null);
			if(id.open() != Window.OK)
				return;
			
			IFile file = ((TDIConfigurationFile)b.getMetamergeConfig()).getFile();
			for (Object o:getSelectionItems()) {
				IMarker bm = file.createMarker(IMarker.BOOKMARK);
				bm.setAttribute(IMarker.LOCATION, ((BaseConfiguration)o).getPath());
				bm.setAttribute(IMarker.MESSAGE, id.getValue());
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
	}

}
