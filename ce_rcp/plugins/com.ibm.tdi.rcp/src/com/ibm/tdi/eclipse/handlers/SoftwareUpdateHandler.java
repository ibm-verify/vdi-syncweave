/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.ibm.tdi.eclipse.Messages;

public class SoftwareUpdateHandler extends AbstractHandler {

	public SoftwareUpdateHandler() {
		setBaseEnabled(true);
	}
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		MessageDialog.openInformation(
				Display.getCurrent().getActiveShell(),
				Messages.getString("miadmin.frametitle"),
				Messages.getString("SoftwareUpdate.disabled")
		);
		return null;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isHandled() {
		return true;
	}

}
