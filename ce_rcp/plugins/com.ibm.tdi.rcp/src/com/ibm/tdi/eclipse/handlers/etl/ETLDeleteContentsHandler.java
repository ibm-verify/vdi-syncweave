/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.handlers.etl;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.undo.DeleteResourcesOperation;

import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.actions.BaseAction;
import com.ibm.tdi.eclipse.handlers.TableViewerHandler;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class ETLDeleteContentsHandler extends TableViewerHandler {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (ActionFactory.DELETE.getCommandId().equals(getCommandId())) {
			Table table = getTable();
			if (table != null && !table.isDisposed()) {
				deleteContents(table);
			}
		}
		return null;
	}

	public void deleteContents(final Table t) {

		BaseAction del = new BaseAction() {
			public void run(IAction action) {
				if (t.getSelectionCount() > 0) {
					IResource[] projs = new IResource[t.getSelectionCount()];

					for (int i = 0; i < projs.length; i++) {
						projs[i] = (IResource) t.getSelection()[i].getData();
					}

					if (MessageDialog.openConfirm(getShell(), Messages.getString("general.delete.label"), Messages
							.getString("general.delete.tooltip"))) {
						IUndoContext undo = getWorkbench().getOperationSupport().getUndoContext();
						DeleteResourcesOperation dro = new DeleteResourcesOperation(projs, Messages
								.getString("general.delete.label"), true);
						dro.addContext(undo);
						try {
							OperationHistoryFactory.getOperationHistory().execute(dro, null, null);
						} catch (ExecutionException e) {
							EclipseAppender.logerror(e.toString(), e, getShell());
						}
					}
				}
			}
		};

		del.run(null);
	}
}
