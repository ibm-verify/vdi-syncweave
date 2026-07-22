/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.handlers.configsettings;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.actions.ActionFactory;

import com.ibm.tdi.eclipse.actions.DeleteViewerItemAction;
import com.ibm.tdi.eclipse.handlers.TableViewerHandler;

/**
 * A Handler for DELETE keystrokes in table viewers in the Config Settings
 * panel.
 */
public class DeleteTableViewerContentsHandler extends TableViewerHandler {

	/**
	 * {@inheritDoc}
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (ActionFactory.DELETE.getCommandId().equals(getCommandId())) {
			Table table = getTable();
			if (table != null && !table.isDisposed()) {
				Object action = table.getData("com.ibm.tdi.action");
				if (action instanceof DeleteViewerItemAction) {
					((DeleteViewerItemAction) action).run();
				}
			}
		}
		return null;
	}

}
