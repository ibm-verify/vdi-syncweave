/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.util;

import java.lang.reflect.Method;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.swt.IFocusService;
import org.eclipse.ui.texteditor.FindReplaceAction;

import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.handlers.TextControlHandler;
import com.ibm.tdi.eclipse.handlers.TextEditorHandler;
import com.ibm.tdi.eclipse.widget.BaseWidget;
import com.ibm.tdi.eclipse.widget.TDIExpressionEditor;

public class TextEditorContextMenu implements MenuDetectListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// -- The text widget we operate on
	private Control control;

	private MenuManager mm;
	
	// -- These are used internally
	private static final int[] sourceActions = new int[] { 
			SourceViewer.CUT,
			SourceViewer.COPY, 
			SourceViewer.PASTE, 
			SourceViewer.SELECT_ALL };

	private static final String[] actionNames = new String[] { 
		Messages.getString("common.Cut.name"),
		Messages.getString("common.Copy.name"),
		Messages.getString("common.Paste.name"),
		Messages.getString("TextEditorContextMenu.selectAll") };
	
	// -- These are the actual action objects handling the command
	private TextAction[] actions = new TextAction[sourceActions.length];

	/**
	 * This class retargets the Cut, Copy, Paste and Select All commands on the main menu bar. In
	 * addition it adds these commands to the context menu of the control.
	 *  
	 * @param text
	 */
	public TextEditorContextMenu(Text text) {
		this.control = text;
		
		createActions();

		// Update menu items when the menu is about to be shown
		text.addMenuDetectListener(this);

		addMenu(text);
	}

	public TextEditorContextMenu(StyledText text) {
		this.control = text;

		createActions();

		// Update menu items when the menu is about to be shown
		text.addMenuDetectListener(this);
	
		addMenu(text);
	}
	
	public TextEditorContextMenu(StyledText textWidget, IFindReplaceTarget findReplaceTarget) {
		this(textWidget);
		textWidget.setData(TextControlHandler.FIND_TARGET, findReplaceTarget);
		TextAction find = new TextAction(textWidget, TextEditorHandler.STE_FIND_REPLACE);
		find.setText(Messages.getString("TextEditorContextMenu.findReplace"));
		find.findTarget = findReplaceTarget;
		mm.add(find);
	}

	public TextEditorContextMenu(Combo combo) {
		this.control = combo;
		createActions();
		// -- We don't have access to Combo's text widget
		addMenu(combo);
	}

	private void createActions() {
		// -- Create the actions
		for (int i = 0; i < sourceActions.length; i++) {
			actions[i] = new TextAction(control, sourceActions[i]);
			actions[i].setText(actionNames[i]);
		}

		if (control instanceof TDIExpressionEditor)
			return;
		
		// -- Add context menu for text fields (override native platform context menu)
		try {
			IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
			if (part instanceof EditorPart) {
				((IFocusService)part.getSite().getService(IFocusService.class)).addFocusTracker(control, "com.ibm.tdi.text.control");
				return;
			}
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}

		// Failed to find an editor, try following parent pointers
		try {
			Composite c = control.getParent();
			while (c != null) {
				if (c instanceof BaseWidget) {
					EditorPart e = ((BaseWidget)c).getEditor();
					if (e != null) {
						((IFocusService)e.getSite().getService(IFocusService.class)).addFocusTracker(control, "com.ibm.tdi.text.control");
						return;
					}
				}
				c = c.getParent();
			}
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
	}
	
	public void addControlListeners() {
	}
	
	public void addMenu(Control control) {
		// -- Create the context menu for the text widget
		mm = new MenuManager();
		Menu menu = mm.createContextMenu(control);
		control.setMenu(menu);
		for(IAction a : actions) {
			mm.add(a);
		}
	}

	/**
	 * This method is called when the menu is about to be shown.
	 * Update the state for each action 
	 */
	public void menuDetected(MenuDetectEvent e) {
		for (TextAction a : actions)
			a.updateState();
	}

	private static class TextAction extends Action {

		private Control text;
		private int command;
		private boolean readOnly;
		private boolean copyAllowed;
		private IFindReplaceTarget findTarget;

		public TextAction(Control text, int command) {
			super();
			this.text = text;
			this.command = command;
			readOnly = (text.getStyle() & SWT.READ_ONLY) != 0;
			if (text instanceof Text)
				copyAllowed = ((Text)text).getEchoChar() == 0;
			else
				copyAllowed = true;
		}
		
		public int selectionCount() {
			if(text instanceof Text)
				return ((Text)text).getSelectionCount();
			else
				return ((StyledText)text).getSelectionCount();
		}

		public void updateState() {
			switch (command) {
			case SourceViewer.CUT:
				setEnabled(copyAllowed && !readOnly && selectionCount() > 0);
				break;
			case SourceViewer.COPY:
				setEnabled(copyAllowed && selectionCount() > 0);
				break;
			case SourceViewer.PASTE:
				setEnabled(!readOnly);
				break;
			default:
				setEnabled(true);
			}
		}

		@Override
		public void run() {
			if (text.isDisposed())
				return;
			switch(command) {
			case SourceViewer.COPY:
				execute("copy", text);
				break;
			case SourceViewer.CUT:
				execute("cut", text);
				break;
			case SourceViewer.PASTE:
				execute("paste", text);
				break;
			case SourceViewer.SELECT_ALL:
				if(text instanceof Combo) {
					Combo c = (Combo) text;
					c.setSelection(new Point(0, c.getText().length()));
				} else {
					execute("selectAll", text);
				}
				break;
			case TextEditorHandler.STE_FIND_REPLACE:
				if (findTarget != null)
					new FindReplaceAction(TextControlHandler.DUMMY_BUNDLE, null, text.getShell(), findTarget).run();
			}
		}

		public void execute(String method, Control text) {
			try {
				Method m = text.getClass().getMethod(method);
				m.invoke(text, (Object[])null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
