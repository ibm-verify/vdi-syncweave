/*
 * Copyright IBM Corp. 2025
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
import org.eclipse.swt.widgets.List;

public abstract class ListViewerHandler extends AbstractHandler implements IExecutableExtension, SelectionListener {

	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The Command ID handled by this class.
	 */
	private String commandId;

	/**
	 * The List associated with this Handler.
	 */
	private List list;

	/**
	 * {@inheritDoc}
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		commandId = config.getAttribute("commandId");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnabled(Object evaluationContext) {
		super.setEnabled(evaluationContext);
		if (evaluationContext instanceof IEvaluationContext) {
			IEvaluationContext ec = (IEvaluationContext) evaluationContext;
			Object focusControl = ec.getVariable("activeFocusControl");
			if (focusControl instanceof List) {
				addSelectionHandler((List) focusControl);
				return;
			}
		}
		addSelectionHandler(null);
	}

	protected void addSelectionHandler(List focusControl) {
		if (list != null) {
			if (focusControl != null && !focusControl.isDisposed() && list == focusControl) {
				// We have already set this handler
				return;
			}

			if (!list.isDisposed()) {
				list.removeSelectionListener(this);
			}
			list = null;
		}
		if (focusControl != null && !focusControl.isDisposed()) {
			list = (List) focusControl;
			if (list != null && !list.isDisposed()) {
				list.addSelectionListener(this);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/**
	 * {@inheritDoc}
	 */
	public void widgetSelected(SelectionEvent e) {
		fireHandlerChanged(new HandlerEvent(ListViewerHandler.this, true, false));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled() {
		boolean enabled = false;
		if (list != null && !list.isDisposed()) {
			enabled = list.getSelectionCount() > 0;
		}
		return enabled;
	}

	/**
	 * Returns the command ID.
	 * 
	 * @return the commandId.
	 */
	protected String getCommandId() {
		return commandId;
	}

	/**
	 * Return the associated List.
	 * 
	 * @return the list.
	 */
	protected List getList() {
		return list;
	}

}
