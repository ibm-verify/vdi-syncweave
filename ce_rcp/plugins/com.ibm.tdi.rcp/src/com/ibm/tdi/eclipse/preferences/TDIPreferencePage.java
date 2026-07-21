/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class TDIPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	@SuppressWarnings("unused") 
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public TDIPreferencePage() {
		super(GRID);
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription(Messages.getString("TDIPreferencePage.title")); //$NON-NLS-1$
	}

	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(PreferenceConstants.P_AUTOSTART_SERVER, Messages.getString("TDIPreferencePage.autostart"), getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(PreferenceConstants.P_AUTOSTOP_SERVER, Messages.getString("TDIPreferencePage.autostop"), getFieldEditorParent())); //$NON-NLS-1$
		addField(new IntegerFieldEditor(PreferenceConstants.P_RUN_WINDOW_LINES, Messages.getString("TDIPreferencePage.numLines"), getFieldEditorParent()));
		
		String[][] entryNamesAndValues = new String[][] {
			new String[]{Messages.getString("RunOptionsWidget.2"), "0"},
			new String[]{Messages.getString("RunOptionsWidget.3"), "1"},
			new String[]{Messages.getString("RunOptionsWidget.4"), "2"}
		};
		addField(new ComboFieldEditor(PreferenceConstants.P_DEFAULT_RUN_MODE, Messages.getString("RunOptionsWidget.1"), entryNamesAndValues, getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_SHOW_DEBUG_ERRORS, Messages.getString("TDIPreferencePage.debugdialog"), getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(PreferenceConstants.P_SHOW_ADD_COMPONENT_POPUP, Messages.getString("TDIPreferencePage.addCompDialog"), getFieldEditorParent())); //$NON-NLS-1$
		addField(new FileFieldEditor(PreferenceConstants.P_EXTERNAL_EDITOR, Messages.getString("TDIPreferencePage.external.editor"), getFieldEditorParent())); //$NON-NLS-1$
		
		// -- Save al logs count
		addField(new IntegerFieldEditor(PreferenceConstants.P_SAVE_AL_LOGS_COUNT, Messages.getString("TDIPreferencePage.saveALLogs"), getFieldEditorParent()));

		// -- Data collector buffer size
		addField(new IntegerFieldEditor(PreferenceConstants.P_DATA_COLLECTOR_BUFFER_SIZE, Messages.getString("DataCollector.buffer.size"), getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
