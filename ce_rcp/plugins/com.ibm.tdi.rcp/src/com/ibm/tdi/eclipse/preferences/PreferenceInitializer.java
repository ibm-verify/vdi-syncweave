/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.preferences;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ibm.icu.util.StringTokenizer;
import com.ibm.tdi.eclipse.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String NOTEPAD = "notepad.exe";
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_AUTOSTOP_SERVER, true);
		store.setDefault(PreferenceConstants.P_AUTOSTART_SERVER, true);
		store.setDefault(PreferenceConstants.P_RUN_WINDOW_LINES, 300);
		store.setDefault(PreferenceConstants.P_DEFAULT_RUN_MODE, "0");
		store.setDefault(PreferenceConstants.P_SAVE_AL_LOGS_COUNT, "0");
		store.setDefault(PreferenceConstants.P_SHOW_DEBUG_ERRORS, false);
		if(System.getProperty("os.name").indexOf("Windows") == -1)
			store.setDefault(PreferenceConstants.P_EXTERNAL_EDITOR, "/bin/vi");
		else
			store.setDefault(PreferenceConstants.P_EXTERNAL_EDITOR, getNotepadPath());
		
		store.setDefault(PreferenceConstants.P_JS_FUNCTION_CATEGORIES, JavaScriptPreferencePage.DEFAULT_CATEGORIES);
		store.setDefault(PreferenceConstants.P_JS_FUNCTION_ENABLED, true);
		for(String str : JavaScriptPreferencePage.getFunctionWidgetCategories(store)) {
			store.setDefault(PreferenceConstants.P_JS_FUNCTION_CATEGORIES + "." + str, true);
		}
		
		store.setDefault(PreferenceConstants.P_DATA_COLLECTOR_BUFFER_SIZE, 100);
		store.setDefault(PreferenceConstants.P_SHOW_ADD_COMPONENT_POPUP, true);
	}

	private String getNotepadPath() {
		String path = System.getenv("PATH");
		if (path == null)
			return NOTEPAD;
		StringTokenizer st = new StringTokenizer(path, ";");
		while (st.hasMoreTokens()) {
			String p = st.nextToken() + File.separator + NOTEPAD;
			if (new File(p).exists())
				return p;
		}
		return ""; // Cannot find notepad in PATH, probably best to return an empty string
	}
}
