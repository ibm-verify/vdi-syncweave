/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.jface.action.IAction;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.editors.AssemblyLineEditor3;

public class InsertComponentAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public InsertComponentAction() {
	}

	public void run(IAction action) {
		InsertComponent ic = new InsertComponent(getShell(), (BaseConfiguration) getFirstSelection());
		ic.run();
		if(ic.getComponent() != null && getTargetPart() instanceof AssemblyLineEditor3) {
			((AssemblyLineEditor3)getTargetPart()).handleComponentInserted(ic.getComponent());
		}
	}

}
