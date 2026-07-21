/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.handlers.configsettings;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.actions.ActionFactory;

import com.ibm.tdi.eclipse.actions.DeleteViewerItemAction;
import com.ibm.tdi.eclipse.handlers.ListViewerHandler;

/**
 * A Handler for DELETE keystrokes in table viewers in the Config Settings
 * panel.
 */
public class DeleteListViewerContentsHandler extends ListViewerHandler {

	/**
	 * {@inheritDoc}
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (ActionFactory.DELETE.getCommandId().equals(getCommandId())) {
			List list = getList();
			if (list != null && !list.isDisposed()) {
				Object action = list.getData("com.ibm.tdi.action");
				if (action instanceof DeleteViewerItemAction) {
					((DeleteViewerItemAction) action).run();
				}
			}
		}
		return null;
	}

}
