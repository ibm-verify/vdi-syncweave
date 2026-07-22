/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.util;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;

import com.ibm.tdi.eclipse.log.EclipseAppender;

/**
 * This class listens for verify and modify events on the Text control and adds
 * an undoable operation for each change.
 * 
 */
public class UndoRedoSupport implements ModifyListener, VerifyListener {

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private StyledText text;
	private Text ctext;

	private String undoString = null;
	private String redoString = null;

	private String paramName;
	private IOperationHistory operationHistory;
	private IUndoContext undoContext;

	public UndoRedoSupport(StyledText text, String paramName, IOperationHistory operationHistory, IUndoContext undoContext) {
		this.text = text;
		this.paramName = paramName == null ? "" : paramName;
		this.operationHistory = operationHistory;
		this.undoContext = undoContext;
		verifyNotNull();

		// -- verify: to grab the string before it is modified
		// -- modify: to grab the string after it has been modified
		text.addVerifyListener(this);
		text.addModifyListener(this);
	}

	public UndoRedoSupport(Text text, String paramName, IOperationHistory operationHistory, IUndoContext undoContext) {
		this.ctext = text;
		this.paramName = paramName == null ? "" : paramName;
		this.operationHistory = operationHistory;
		this.undoContext = undoContext;
		verifyNotNull();
		
		// -- verify: to grab the string before it is modified
		// -- modify: to grab the string after it has been modified
		text.addVerifyListener(this);
		text.addModifyListener(this);
	}

	/**
	 * Make sure that operationHistory and undoContext are not null.
	 */
	private void verifyNotNull() {
		if (operationHistory != null && undoContext != null)
			return;
		IWorkbenchOperationSupport support = PlatformUI.getWorkbench().getOperationSupport();
		if (operationHistory == null)
			operationHistory = support.getOperationHistory();
		if (undoContext == null)
			undoContext = support.getUndoContext();
	}

	private String getText() {
		if (text != null)
			return text.getText();
		else
			return ctext.getText();
	}

	protected void setText(String str) {
		if (text != null) {
			text.removeVerifyListener(this);
			text.removeModifyListener(this);
		} else {
			ctext.removeVerifyListener(this);
			ctext.removeModifyListener(this);
		}
		if (text != null) {
			text.setText(str);
			text.setSelection(str.length(), str.length());
			text.addVerifyListener(this);
			text.addModifyListener(this);
		} else {
			ctext.setText(str);
			ctext.setSelection(str.length(), str.length());
			ctext.addVerifyListener(this);
			ctext.addModifyListener(this);
		}
	}

	public void verifyText(VerifyEvent e) {
		undoString = getText();
	}

	public void modifyText(ModifyEvent e) {
		redoString = getText();

		// -- now add an undoable operation to undo/redo the change
		// last edit
		AbstractOperation oper = new AbstractOperation(paramName) {
			
			private String undoString = UndoRedoSupport.this.undoString;
			private String redoString = UndoRedoSupport.this.redoString;
			
			@Override
			public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				setText(undoString);
				return Status.OK_STATUS;
			}

			@Override
			public boolean canRedo() {
				return redoString != null;
			}

			@Override
			public boolean canUndo() {
				return undoString != null;
			}

			@Override
			public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				setText(redoString);
				return Status.OK_STATUS;
			}

			@Override
			public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				redoString = getText();
				return Status.OK_STATUS;
			}
		};
		oper.addContext(undoContext);
		try {
			operationHistory.execute(oper, null, null);
		} catch (ExecutionException e1) {
			// - this should never happen as execute always returns ok_status
			EclipseAppender.logerror(e1.toString(), e1);
		}
	}
}
