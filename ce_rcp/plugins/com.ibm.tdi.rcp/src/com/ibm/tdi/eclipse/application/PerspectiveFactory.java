/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.application;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.ibm.tdi.eclipse.views.EntryCollectorView;

public class PerspectiveFactory implements IPerspectiveFactory {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public void createInitialLayout(IPageLayout layout) {

		String editor = layout.getEditorArea();
//		layout.addStandaloneView("com.ibm.tdi.rcp.navigator", true, IPageLayout.LEFT, 0.26f, editor);
		// Use IFolder to Work around eclipse bug 379803
		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.26f, editor);
		topLeft.addView("com.ibm.tdi.rcp.navigator");
		
//		layout.addStandaloneView("com.ibm.tdi.rcp.serverview", false, IPageLayout.BOTTOM, 0.70f, "com.ibm.tdi.rcp.navigator");	
		IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.70f, "topLeft");
		bottomLeft.addView("com.ibm.tdi.rcp.serverview");
		

		// Bottom right views: problems, JavaScript and Console
		IFolderLayout bottomRight = layout.createFolder("bottomRight", IPageLayout.BOTTOM, 0.70f, editor);
		bottomRight.addView(IPageLayout.ID_PROBLEM_VIEW);
		bottomRight.addView("com.ibm.tdi.eclipse.views.JavaScriptView");
		bottomRight.addView("org.eclipse.ui.console.ConsoleView");
		
		defineActions(layout);
	}
	
    /**
     * Defines the initial actions for a page.  
     * @param layout The layout we are filling
     */
    public void defineActions(IPageLayout layout) {
        // Add TDI wizards
        layout.addNewWizardShortcut("com.ibm.tdi.eclipse.wizards.NewProject");
        layout.addNewWizardShortcut("com.ibm.tdi.eclipse.wizards.NewAssemblyLineWizard");
        layout.addNewWizardShortcut("com.ibm.tdi.eclipse.wizards.NewAttributeMapWizard");
        layout.addNewWizardShortcut("com.ibm.tdi.eclipse.wizards.NewConnectorWizard");
        layout.addNewWizardShortcut("com.ibm.tdi.eclipse.wizards.NewFunctionWizard");
        layout.addNewWizardShortcut("com.ibm.tdi.eclipse.wizards.NewScriptWizard");
        layout.addNewWizardShortcut("com.ibm.tdi.eclipse.wizards.NewParserWizard");
        layout.addNewWizardShortcut("com.ibm.tdi.eclipse.wizards.NewPropertiesWizard");
        layout.addNewWizardShortcut("com.ibm.tdi.eclipse.wizards.NewSchedulerWizard");
        layout.addNewWizardShortcut("com.ibm.tdi.eclipse.wizards.NewSequenceWizard");
        layout.addNewWizardShortcut("com.ibm.tdi.eclipse.wizards.NewServerWizard");

        // Add "new wizards".
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
        
        // Add "perspective" buttons
		layout.addPerspectiveShortcut("com.ibm.tdi.rcp.perspective.etl");
		layout.addPerspectiveShortcut("com.ibm.tdi.rcp.perspective");
        
        // Add "show views".
		layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView");
		layout.addShowViewShortcut("org.eclipse.ui.console.ConsoleView");
        layout.addShowViewShortcut(IPageLayout.ID_BOOKMARKS);
        layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
        layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
        layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
        layout.addShowViewShortcut(EntryCollectorView.VIEW_ID);

        layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
    }
}
