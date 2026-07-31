/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.handlers;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Collections;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.texteditor.FindReplaceAction;

import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class TextControlHandler extends TextEditorHandler implements SelectionListener {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Control oldFocusControl;

	public static final String FIND_TARGET = "com.ibm.tdi.findTarget";
	
	private IFindReplaceTarget findTarget;

	// Dummy bundle to make FindReplaceAction happy
	public static final ResourceBundle DUMMY_BUNDLE = new ResourceBundle() {
		public Enumeration<String> getKeys() {
			return Collections.emptyEnumeration();
		}

		protected Object handleGetObject(String key) {
			return null;
		}
	};


	public Object execute(ExecutionEvent event) throws ExecutionException {

		// -- check for disposed also
		if (oldFocusControl == null || oldFocusControl.isDisposed())
			return null;

		try {

			if (oldFocusControl instanceof Text) {
				switch (getSourceViewerCommand()) {
				case SourceViewer.DELETE:
				case SourceViewer.CUT:
					((Text) oldFocusControl).cut();
					break;
				case SourceViewer.COPY:
					((Text) oldFocusControl).copy();
					break;
				case SourceViewer.PASTE:
					((Text) oldFocusControl).paste();
					break;
				case SourceViewer.SELECT_ALL:
					((Text) oldFocusControl).selectAll();
					break;
				case STE_FIND_REPLACE:
					findReplace();
					break;
				}
			} else if (oldFocusControl instanceof StyledText) {
				switch (getSourceViewerCommand()) {
				case SourceViewer.CUT:
				case SourceViewer.DELETE:
					((StyledText) oldFocusControl).cut();
					break;
				case SourceViewer.COPY:
					((StyledText) oldFocusControl).copy();
					break;
				case SourceViewer.PASTE:
					((StyledText) oldFocusControl).paste();
					break;
				case SourceViewer.SELECT_ALL:
					((StyledText) oldFocusControl).selectAll();
					break;
				case STE_FIND_REPLACE:
					findReplace();
					break;
				}
			} else if (oldFocusControl instanceof Combo) {
				switch (getSourceViewerCommand()) {
				case SourceViewer.CUT:
				case SourceViewer.DELETE:
					((Combo) oldFocusControl).cut();
					break;
				case SourceViewer.COPY:
					((Combo) oldFocusControl).copy();
					break;
				case SourceViewer.PASTE:
					((Combo) oldFocusControl).paste();
					break;
				case SourceViewer.SELECT_ALL:
					((Combo) oldFocusControl).setSelection(new Point(0, ((Combo) oldFocusControl).getText().length()));
					break;
				}
			} else if (oldFocusControl instanceof CCombo) {
				switch (getSourceViewerCommand()) {
				case SourceViewer.DELETE:
					if (doDelete((CCombo) oldFocusControl))
						return null;
				case SourceViewer.CUT:
					((CCombo) oldFocusControl).cut();
					break;
				case SourceViewer.COPY:
					((CCombo) oldFocusControl).copy();
					break;
				case SourceViewer.PASTE:
					((CCombo) oldFocusControl).paste();
					break;
				case SourceViewer.SELECT_ALL:
					((CCombo) oldFocusControl).setSelection(new Point(0, ((CCombo) oldFocusControl).getText().length()));
					break;
				}
			}

		} catch (Throwable t) {
			EclipseAppender.logerror(t.toString(), t);
		}

		return null;
	}

	/**
	 * Deletes the next character in a CCombo.
	 * @param cc
	 * @return true if we did something.
	 */
	private boolean doDelete(CCombo cc) {
		Point p = cc.getSelection();
		if (p.x < p.y)
			return false; // One or more characters selected, the normal cut operation will do it.
		String s = cc.getText();
		if (s.length() <= p.x)
			return false; // Cannot delete after the last character.
		cc.setText(s.substring(0, p.x) + s.substring(p.x + 1));
		cc.setSelection(p);
		return true;
	}

	public void findReplace() {
		if (findTarget == null)
			return;

		// Run the standard find/replace dialog
		new FindReplaceAction(DUMMY_BUNDLE, null, oldFocusControl.getShell(), findTarget).run();
	}

	/**
	 * @param focusControl
	 */
	protected void addSelectionHandler(Control focusControl) {
		if (oldFocusControl == focusControl) {
			// We have already set this selection Handler
			return;
		}

		if (oldFocusControl == null || oldFocusControl.isDisposed()) {
			SystemFunctions.doNothing();
		} else if (oldFocusControl instanceof Text) {
			((Text) oldFocusControl).removeSelectionListener(this);
		} else if (oldFocusControl instanceof StyledText) {
			super.addSelectionHandler(null);
			((StyledText) oldFocusControl).removeSelectionListener(this);
		} else if (oldFocusControl instanceof Combo) {
			((Combo) oldFocusControl).removeSelectionListener(this);
		} else if (oldFocusControl instanceof CCombo) {
			((CCombo) oldFocusControl).removeSelectionListener(this);
		}

		if (focusControl == null || focusControl.isDisposed()) {
			oldFocusControl = null;
			return;
		} else if (focusControl instanceof Text) {
			((Text) focusControl).addSelectionListener(this);
		} else if (focusControl instanceof StyledText) {
			super.addSelectionHandler((StyledText) focusControl);
			((StyledText) focusControl).addSelectionListener(this);
		} else if (focusControl instanceof CCombo) {
			((CCombo) focusControl).addSelectionListener(this);
		}

		oldFocusControl = focusControl;
		findTarget = (IFindReplaceTarget) focusControl.getData(FIND_TARGET);
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		fireHandlerChanged(new HandlerEvent(this, true, false));
	}

	@Override
	public void setEnabled(Object evaluationContext) {

		if (evaluationContext instanceof IEvaluationContext) {
			IEvaluationContext ec = (IEvaluationContext) evaluationContext;
			Object focusControl = ec.getVariable("activeFocusControl");

			if (focusControl instanceof Text || focusControl instanceof Combo || focusControl instanceof StyledText
					|| focusControl instanceof CCombo) {
				try {
					addSelectionHandler((Control) focusControl);
				} catch (Throwable t) {
					EclipseAppender.logerror(t.toString(), t);
				}
				return;
			}
		}

		// -- clear selection listeners
		addSelectionHandler(null);
	}

	@Override
	public boolean isEnabled() {
		if (oldFocusControl != null && !oldFocusControl.isDisposed()) {
			switch (getSourceViewerCommand()) {
			case SourceViewer.CUT:
			case SourceViewer.PASTE:
			case SourceViewer.DELETE:
			case SourceViewer.COPY:
			case SourceViewer.SELECT_ALL:
				return true;
			case STE_FIND_REPLACE:
				return findTarget != null;
			}
		}
		return false;
	}
}
