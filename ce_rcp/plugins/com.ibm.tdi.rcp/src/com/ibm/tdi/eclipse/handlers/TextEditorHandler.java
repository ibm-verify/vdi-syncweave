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
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;

import com.ibm.tdi.eclipse.widget.SimpleTextEditor;
import com.ibm.tdi.eclipse.widget.TDIExpressionEditor;

public class TextEditorHandler extends AbstractHandler implements IExecutableExtension, ISelectionChangedListener,
		IDocumentListener {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final int STE_FIND_REPLACE = -99;

	public static final String TDI_TEXT_WIDGET = "com.ibm.tdi.text.widget";
	private static final String EDIT_REDO = "org.eclipse.ui.edit.redo";
	private static final String EDIT_UNDO = "org.eclipse.ui.edit.undo";
	private static final String EDIT_SELECT_ALL = "org.eclipse.ui.edit.selectAll";
	private static final String EDIT_DELETE = "org.eclipse.ui.edit.delete";
	private static final String EDIT_PASTE = "org.eclipse.ui.edit.paste";
	private static final String EDIT_COPY = "org.eclipse.ui.edit.copy";
	private static final String EDIT_CUT = "org.eclipse.ui.edit.cut";
	private static final String EDIT_FIND = "org.eclipse.ui.edit.findReplace";

	private static final String[] EDIT_COMMANDS = new String[] { EDIT_COPY, EDIT_CUT, EDIT_DELETE, EDIT_PASTE, EDIT_REDO,
			EDIT_SELECT_ALL, EDIT_UNDO, EDIT_FIND };

	private static final int[] SOURCEVIEWER_COMMANDS = new int[] { SourceViewer.COPY, SourceViewer.CUT, SourceViewer.DELETE,
			SourceViewer.PASTE, SourceViewer.REDO, SourceViewer.SELECT_ALL, SourceViewer.UNDO, STE_FIND_REPLACE };

	private String commandId;
	private int sourceViewerCommand = -1;
	private SimpleTextEditor textEditor;
	private TDIExpressionEditor expressionEditor;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (expressionEditor != null) {
			if (expressionEditor.isDisposed())
				return null;
			if (sourceViewerCommand == STE_FIND_REPLACE)
				expressionEditor.findReplace();
			else
				expressionEditor.getSourceViewer().doOperation(sourceViewerCommand);
			return null;
		}
		if (textEditor == null || textEditor.isDisposed())
			return null;

		if (sourceViewerCommand == STE_FIND_REPLACE)
			textEditor.findReplace();
		else
			textEditor.getSourceViewer().doOperation(sourceViewerCommand);
		return null;
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		this.commandId = config.getAttribute("commandId");
		for (int i = 0; i < EDIT_COMMANDS.length; i++) {
			if (EDIT_COMMANDS[i].equals(commandId)) {
				sourceViewerCommand = SOURCEVIEWER_COMMANDS[i];
			}
		}
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		super.setEnabled(evaluationContext);
		if (evaluationContext instanceof IEvaluationContext) {
			IEvaluationContext ec = (IEvaluationContext) evaluationContext;
			Object focusControl = ec.getVariable("activeFocusControl");
			if (focusControl instanceof StyledText) {
				addSelectionHandler((StyledText) focusControl);
				return;
			}
		}
		addSelectionHandler(null);
	}

	protected void addSelectionHandler(StyledText focusControl) {
		if (textEditor != null) {
			if (focusControl != null && !focusControl.isDisposed() &&
					textEditor == focusControl.getData(TDI_TEXT_WIDGET)) {
				// We have already set this handler
				return;
			}

			if (!textEditor.isDisposed()) {
				textEditor.getSourceViewer().removeSelectionChangedListener(this);
				textEditor.removeDocumentListener(this);
			}
			textEditor = null;
		}
		if (expressionEditor != null) {
			if (focusControl != null && !focusControl.isDisposed() &&
					expressionEditor == focusControl.getData(TDI_TEXT_WIDGET)) {
				// We have already set this handler
				return;
			}

			if (!expressionEditor.isDisposed()) {
				expressionEditor.getSourceViewer().removeSelectionChangedListener(this);
			}
			expressionEditor = null;
		}
		if (focusControl != null && !focusControl.isDisposed()) {
			Object o = focusControl.getData(TDI_TEXT_WIDGET);
			if (o instanceof SimpleTextEditor) {
				textEditor = (SimpleTextEditor) o;
				if (!textEditor.isDisposed()) {
					textEditor.getSourceViewer().addSelectionChangedListener(this);
					textEditor.addDocumentListener(this);
				}
			}
			if (o instanceof TDIExpressionEditor) {
				expressionEditor = (TDIExpressionEditor) o;
				if (!expressionEditor.isDisposed()) {
					expressionEditor.getSourceViewer().addSelectionChangedListener(this);
				}
			}
		}
	}

	public void selectionChanged(SelectionChangedEvent event) {
		if (textEditor != null && !textEditor.isDisposed()) {
			fireHandlerChanged(new HandlerEvent(TextEditorHandler.this, true, false));
		}
		if (expressionEditor != null && !expressionEditor.isDisposed()) {
			fireHandlerChanged(new HandlerEvent(TextEditorHandler.this, true, false));
		}
	}

	public void documentAboutToBeChanged(DocumentEvent event) {
	}

	public void documentChanged(DocumentEvent event) {
		if (textEditor != null && !textEditor.isDisposed()) {
			fireHandlerChanged(new HandlerEvent(TextEditorHandler.this, true, false));
		}
	}

	@Override
	public boolean isEnabled() {
		boolean enabled = false;

		if (textEditor != null && !textEditor.isDisposed()) {
			if (sourceViewerCommand == -1)
				enabled = false;
			else if (sourceViewerCommand == STE_FIND_REPLACE)
				enabled = true;
			else
				enabled = textEditor.getSourceViewer().canDoOperation(sourceViewerCommand);
		}
		if (expressionEditor != null && !expressionEditor.isDisposed()) {
			if (sourceViewerCommand == -1)
				enabled = false;
			else if (sourceViewerCommand == STE_FIND_REPLACE)
				enabled = true;
			else
				enabled = expressionEditor.getSourceViewer().canDoOperation(sourceViewerCommand);
		}

		return enabled;
	}

	public int getSourceViewerCommand() {
		return sourceViewerCommand;
	}

	public String getCommandId() {
		return commandId;
	}

}
