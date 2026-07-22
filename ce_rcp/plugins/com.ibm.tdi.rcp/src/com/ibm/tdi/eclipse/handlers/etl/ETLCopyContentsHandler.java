/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.handlers.etl;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.actions.ActionFactory;

import com.ibm.tdi.eclipse.actions.CopyTableContentsAction;
import com.ibm.tdi.eclipse.handlers.TableViewerHandler;

public class ETLCopyContentsHandler extends TableViewerHandler {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.tdi.eclipse.handlers.TableViewerHandler#execute(org.eclipse.core
	 * .commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (ActionFactory.COPY.getCommandId().equals(getCommandId())) {
			Table table = getTable();
			if (table != null && !table.isDisposed()) {
				CopyTableContentsAction cp = new CopyTableContentsAction(table);
				cp.run();
			}
		}

		return null;
	}
}
