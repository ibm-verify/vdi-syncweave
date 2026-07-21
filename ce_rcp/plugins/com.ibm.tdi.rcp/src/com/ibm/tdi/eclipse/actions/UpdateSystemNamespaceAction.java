/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;

import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class UpdateSystemNamespaceAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public UpdateSystemNamespaceAction() {
	}

	public void run(IAction action) {
		for(Object obj : getSelectionItems()) {
			IFile file = (IFile) obj;
			try {
				Activator.getDefault().updateSystemNamespace(file);
			} catch (Exception e) {
				EclipseAppender.logerror(file.getName() + ": " + e.getMessage(), e, getShell());
			}
		}
	}

}
