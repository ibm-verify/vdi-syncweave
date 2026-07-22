/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.ui.PlatformUI;

import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.views.JavaScriptView;
import com.ibm.tdi.eclipse.widget.SimpleTextEditor;

public class TestScriptAction extends TDIAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public TestScriptAction() {
		super();
	}

	@Override
	public void run() {
		try {
			JavaScriptView view = (JavaScriptView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(JavaScriptView.VIEW_ID);
			if(view == null)
				throw new Exception("JavaScript view not found!");
			String script = null;

			if (getSource() instanceof SimpleTextEditor) 
			{
				script = ((SimpleTextEditor)getSource()).getSelected();
				if (script == null || script.trim().length() == 0)
					script = ((SimpleTextEditor)getSource()).getText();
			}

			if ((script == null || script.length() == 0) &&
				 getEditingConfiguration() != null) {
					script = getEditingConfiguration().getScript();
			}
			if(script == null)
				script = "";
			
			view.testScript(script);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		}
	}
	
}
