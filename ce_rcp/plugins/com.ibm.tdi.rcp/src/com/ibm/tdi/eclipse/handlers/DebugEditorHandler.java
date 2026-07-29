/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.actions.ActionFactory;

import com.ibm.tdi.eclipse.stepper.StepperWatchList;

public class DebugEditorHandler extends AbstractHandler implements IExecutableExtension, SelectionListener {
	/**
	 * 
	 */
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String commandId;
	private Tree tree;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(ActionFactory.DELETE.getCommandId().equals(commandId)) {
			if(tree == null || tree.isDisposed())
				return null;
			
			Object widget = tree.getData("com.ibm.tdi.widget");
			if(widget instanceof StepperWatchList) {
				((StepperWatchList)widget).deleteSelectedItems();
			}
		}
		return null;
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		this.commandId = config.getAttribute("commandId");
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		super.setEnabled(evaluationContext);
		if (evaluationContext instanceof IEvaluationContext) {
			IEvaluationContext ec = (IEvaluationContext) evaluationContext;
			Object focusControl = ec.getVariable("activeFocusControl");
			if (focusControl instanceof Tree) {
				addSelectionHandler((Tree) focusControl);
				return;
			}
		}
		addSelectionHandler(null);
	}

	protected void addSelectionHandler(Tree focusControl) {
		if (tree != null) {
			if (focusControl != null && !focusControl.isDisposed() &&
					tree == focusControl) {
				// We have already set this handler
				return;
			}

			if (!tree.isDisposed()) {
				tree.removeSelectionListener(this);
			}
			tree = null;
		}
		if (focusControl != null && !focusControl.isDisposed()) {
			tree = (Tree) focusControl;
			if (tree != null && !tree.isDisposed()) {
				tree.addSelectionListener(this);
			}
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	public void widgetSelected(SelectionEvent e) {
		fireHandlerChanged(new HandlerEvent(DebugEditorHandler.this, true, false));
	}

	@Override
	public boolean isEnabled() {
		boolean enabled = false;
		if (tree != null && !tree.isDisposed()) {
				enabled = tree.getSelectionCount() > 0;
		}
		return enabled;
	}

}
