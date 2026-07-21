/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.application;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.ibm.tdi.eclipse.views.EntryCollectorView;
import com.ibm.tdi.eclipse.views.ServerView;

public class PerspectiveFactoryETL implements IPerspectiveFactory {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public void createInitialLayout(IPageLayout layout) {
		
		String editor = layout.getEditorArea();
		layout.addStandaloneView("com.ibm.tdi.rcp.navigator.etl", false, IPageLayout.LEFT, 0.26f, editor);

		// Bottom right: Console view, Server View and Data collector view
		IFolderLayout bottomRight = layout.createFolder("bottomRight", IPageLayout.BOTTOM, 0.70f, editor);
		bottomRight.addView("org.eclipse.ui.console.ConsoleView");
		bottomRight.addView(ServerView.VIEW_ID);
		bottomRight.addView(EntryCollectorView.VIEW_ID);
		defineActions(layout);
	}

    /**
     * Defines the initial actions for a page.  
     * @param layout The layout we are filling
     */
    public void defineActions(IPageLayout layout) {
		layout.addPerspectiveShortcut("com.ibm.tdi.rcp.perspective.etl");
		layout.addPerspectiveShortcut("com.ibm.tdi.rcp.perspective");
		layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView");
		layout.addShowViewShortcut("org.eclipse.ui.console.ConsoleView");
    }
}
