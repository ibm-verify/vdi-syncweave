/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.ide.IDE;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.tdi.eclipse.builders.IncrementalConfigBuilder;
import com.ibm.tdi.eclipse.dialogs.RunOptionsDialog;
import com.ibm.tdi.eclipse.editors.RunAssemblyLineEditor;
import com.ibm.tdi.eclipse.editors.RunAssemblyLineInput;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.util.CustomEditorSettings;

public class RunAssemblyLineServerAction extends BaseAction {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public RunAssemblyLineServerAction() {
	}

	public void run(IAction action) {
		try {
			IFile al = (IFile) getFirstSelection();
			CustomEditorSettings settings = null;
			
			TDIConfigurationFile cfg = TDIConfigurationFile.loadFile(al);
			MetamergeConfigXML mx = null;
			
			//
			// Make sure project builder has updated runtime config file
			//
			try {
				al.getProject().build(IncrementalConfigBuilder.INCREMENTAL_BUILD, null);
			} catch (CoreException e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
				return;
			}
			
			//
			// Prompt for run options if runal options was chosen
			//
			if(action.getId().equals("com.ibm.tdi.rcp.runaloption.action")||
				action.getId().equals("com.ibm.tdi.rcp.debugaloption.action") ||
				action.getId().equals("com.ibm.tdi.rcp.debug.simple.option.action") ) {
				settings = new CustomEditorSettings(al);
				settings.loadSettings();
				RunOptionsDialog dlg = new RunOptionsDialog(getShell(), cfg.getDefaultConfigObject(),
						null, settings);
				if (dlg.open() != Window.OK)
					return;
			}
			
			RunAssemblyLineInput input = new RunAssemblyLineInput((AssemblyLineConfig) cfg.getDefaultConfigObject());
			int stepMode = (settings != null ? settings.getInteger(CustomEditorSettings.STEP_MODE, 0) : 0);
			input.setStepMode(stepMode);
			input.setDebug(action.getId().startsWith("com.ibm.tdi.rcp.debug"));
			if (action.getId().startsWith("com.ibm.tdi.rcp.debug.simple"))
				input.setDebugMode(1);
			input.setMetamergeConfig(mx);

			if(settings != null) {
				// Provide work entry?
				if (settings.getBoolean(CustomEditorSettings.WORK_ENABLED, false))
					input.setWorkEntry(settings.getEntry(CustomEditorSettings.WORK_ENTRY));
	
				// Provide init params?
				if (settings.getBoolean(CustomEditorSettings.INIT_PARAMS_ENABLED, false))
					input.setInitParams(settings.getEntry(CustomEditorSettings.INIT_PARAMS));

				// Operation?
				if (settings.getString(CustomEditorSettings.AL_OPERATION, null) != null)
					input.setOperation(settings.getString(CustomEditorSettings.AL_OPERATION, null));
			}

			try {
				IDE.openEditor(getWindow().getActivePage(), input, RunAssemblyLineEditor.EDITOR_ID, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
	}
}
