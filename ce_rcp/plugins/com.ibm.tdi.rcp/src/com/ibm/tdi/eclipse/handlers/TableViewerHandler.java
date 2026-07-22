/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;

public abstract class TableViewerHandler extends AbstractHandler implements IExecutableExtension, SelectionListener {
	/**
	 * 
	 */
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String commandId;
	private Table table;

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		this.commandId = config.getAttribute("commandId");
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		super.setEnabled(evaluationContext);
		if (evaluationContext instanceof IEvaluationContext) {
			IEvaluationContext ec = (IEvaluationContext) evaluationContext;
			Object focusControl = ec.getVariable("activeFocusControl");
			if (focusControl instanceof Table) {
				addSelectionHandler((Table) focusControl);
				return;
			}
		}
		addSelectionHandler(null);
	}

	protected void addSelectionHandler(Table focusControl) {
		if (table != null) {
			if (focusControl != null && !focusControl.isDisposed() && table == focusControl) {
				// We have already set this handler
				return;
			}

			if (!table.isDisposed()) {
				table.removeSelectionListener(this);
			}
			table = null;
		}
		if (focusControl != null && !focusControl.isDisposed()) {
			table = (Table) focusControl;
			if (table != null && !table.isDisposed()) {
				table.addSelectionListener(this);
			}
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	public void widgetSelected(SelectionEvent e) {
		fireHandlerChanged(new HandlerEvent(TableViewerHandler.this, true, false));
	}

	@Override
	public boolean isEnabled() {
		boolean enabled = false;
		if (table != null && !table.isDisposed()) {
			enabled = table.getSelectionCount() > 0;
		}
		return enabled;
	}

	/**
	 * @return the commandId
	 */
	protected String getCommandId() {
		return commandId;
	}

	/**
	 * @return the table
	 */
	protected Table getTable() {
		return table;
	}
}
