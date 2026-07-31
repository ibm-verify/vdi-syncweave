/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.util.InheritanceUtil;

public class SelectWorkspaceInheritance extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private BaseConfiguration b = null;

	public SelectWorkspaceInheritance() {
	}

	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		Object[] sel = getSelectionItems();
		if (sel.length != 1)
			b = null;
		else
			b = InheritanceUtil.getConfigFromSelection(sel[0]);
		action.setEnabled(b != null);
	}
	
	public void run(IAction action) {
		if (b == null)
			return;
		InheritanceUtil.changeInheritance(b);
	}

}
