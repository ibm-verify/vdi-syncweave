/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.ibm.tdi.eclipse.databrowser.DataBrowser;

public class DataBrowserEditor extends BaseEditor {

	@SuppressWarnings("unused") 
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public static final String EDITOR_ID = "com.ibm.tdi.eclipse.editors.DataBrowserEditor";
	private DataBrowser browser;

	public DataBrowserEditor() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		boolean isMapping = true;
		if(getEditorInput() instanceof TDIConfigEditorInput) {
			isMapping = !ConfigSettingsEditor.ID.equals(((TDIConfigEditorInput)getEditorInput()).getEditor());
		}
		browser = DataBrowser.getInstance(parent, getTDIConfiguration(), isMapping);
	}

	@Override
	protected void reloadEditor() {
		if(browser != null && browser.getEditingConfig() != null && browser.getEditingConfig().getModified()) {
			browser.resetConnectorForm();
		}
	}

	public DataBrowser getBrowser() {
		return browser;
	}

	@Override
	public void dispose() {
		if(browser != null)
			browser.dispose();
		super.dispose();
	}

}
