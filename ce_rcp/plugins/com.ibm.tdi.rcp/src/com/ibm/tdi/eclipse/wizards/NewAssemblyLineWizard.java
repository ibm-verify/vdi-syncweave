/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.ide.IDE;

import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.tdi.easyetl.ETLEditor;

/**
 * Wizard to create a new AssemblyLine
 */
public class NewAssemblyLineWizard extends NewComponentBaseWizard {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public NewAssemblyLineWizard() {
		super("assemblyline", MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER, "miadmin.menu.Object.NewAssemblyLine.label"); //$NON-NLS-1$ //$NON-NLS-2$
		setShowTypes(false);
	}

	@Override
	protected void openEditorForFile(IFile file) throws Exception {
		if (getConfigTypePage().isSimpleAssemblyLine()) {
			IDE.openEditor(workbench.getActiveWorkbenchWindow()
					.getActivePage(), file, ETLEditor.EDITOR_ID);
		} else {
			super.openEditorForFile(file);
		}
	}

}
