/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.widget.FormWidget2;
import com.ibm.tdi.eclipse.widget.ScriptWidget;

public class ScriptEditor extends BaseEditor implements SelectionListener {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public final static String ID = "com.ibm.tie.editors.ScriptEditor"; //$NON-NLS-1$

	private ScriptWidget view;
	
	private CTabFolder tabs = null;
	
	private final static String[] defaultHandlers = {
		ActionFactory.CUT.getId(), 
		ActionFactory.COPY.getId(),
		ActionFactory.PASTE.getId(), 
		ActionFactory.DELETE.getId(),
		ActionFactory.SELECT_ALL.getId(),
	};
	
	public ScriptEditor() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		if(getTDIConfiguration() == null) {
			super.createPartControl(parent);
			return;
		}
		getTDIConfiguration().setModified(false);
		installHandlers();
		
		if(getTDIConfiguration() instanceof ScriptConfig) {
			tabs = new CTabFolder(parent, SWT.TOP);
			tabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			CTabItem item = new CTabItem(tabs, SWT.NONE);
			item.setText(Messages.getString("editor.name.1")); //$NON-NLS-1$
			view = new ScriptWidget(getTDIConfiguration(), tabs, SWT.BORDER, this);
			item.setControl(view);
			
			item = new CTabItem(tabs, SWT.LEFT);
			item.setText(Messages.getString("PropertyStore.Configuration")); //$NON-NLS-1$
			try {
				String scriptName = "Global Script";
				if (Utils.getParentConfig(getTDIConfiguration(), AssemblyLineConfig.class) != null)
					scriptName = "Local Script";
				FormWidget2 form = new FormWidget2(tabs, SWT.NONE, getTDIConfiguration(), scriptName, false); //$NON-NLS-1$
				form.setEditorWindowDisabled(true);
				form.initialize();
				item.setControl(form);
			} catch (Exception e) {
				item.setControl(Utils.exceptionWidget(tabs, e));
			}
			tabs.setSelection(0);
			tabs.addSelectionListener(this);
		} else {
			view = new ScriptWidget(getTDIConfiguration(), parent, SWT.BORDER);
			view.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
		view.setEditor(this);
		setScriptEditor(view.getScriptEditor());
		getEditor().registerActions(this);
		

		// -- delegate Find to text editor
		registerAction(ActionFactory.FIND.getId(), new Action() {
			@Override
			public void run() {
				getEditor().findReplace();
			}
		});
		
		updateActionBars();
		setModified(false);
	}

	@Override
	public void setFocus() {
		super.setFocus();

		if (tabs == null || tabs.getSelectionIndex() == 0) {
			getEditor().registerActions(this);			
			updateActionBars();
		} else {
			installHandlers();
		}
		
		if (tabs != null )
			tabs.setFocus();
		else if(view != null)
			view.setFocus();
	}
	
	private void installHandlers() {
		//Actually just remove global handlers
		IActionBars bars = getEditorSite().getActionBars();
		if (bars != null) {
			for (String handler:defaultHandlers) {
				bars.setGlobalActionHandler(handler, null);
				registerAction(handler, null);
			}
			bars.updateActionBars();
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		if (tabs.getSelectionIndex() == 0) {
			getEditor().registerActions(this);			
			updateActionBars();
		} else {
			installHandlers();
		}
	}

}
