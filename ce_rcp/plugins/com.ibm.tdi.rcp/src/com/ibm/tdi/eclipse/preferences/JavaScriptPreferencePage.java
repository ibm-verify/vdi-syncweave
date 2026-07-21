/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;

public class JavaScriptPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String CAT_SCRIPT_LIBRARY = "Script Library";
	public static final String CAT_STRING_DATE = "String & Date";
	public static final String CAT_ENTRY_ATTRIBUTE = "Entry & Attribute";
	public static final String CAT_LOCAL_VARS_FUNCTIONS = "Local Functions & Variables";
	public static final String CAT_GLOBAL_VARS = "Global Variables";
	public static final String CAT_CODE_SNIPPETS = "Code Snippets";
	
	public static final String DEFAULT_CATEGORIES =
		CAT_SCRIPT_LIBRARY + "\t" +
		CAT_STRING_DATE + "\t" +
		CAT_ENTRY_ATTRIBUTE + "\t" +
		CAT_LOCAL_VARS_FUNCTIONS + "\t" +
		CAT_GLOBAL_VARS + "\t" +
		CAT_CODE_SNIPPETS;
	
	private CheckboxTableViewer list;
	private List<String> catlist = new ArrayList<String>();
	private Button enabled;

	public JavaScriptPreferencePage() {
		this(true);
	}
	
	public JavaScriptPreferencePage(boolean defaultAndApplyButtons) {
		super("JavaScript Settings");
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription(Messages.getString("JavaScriptPrefs.title")); //$NON-NLS-1$
		if(!defaultAndApplyButtons)
			noDefaultAndApplyButton();
		catlist = getFunctionWidgetCategories(store);
	}
	
	/**
	 * Returns true if the function list widget is enabled in the preference settings.
	 * 
	 * @return
	 */
	public static boolean isFunctionWidgetEnabled() {
		return Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_JS_FUNCTION_ENABLED);
	}

	/**
	 * Returns true if the specified category is a standard category.
	 * 
	 * @param category
	 * @return
	 */
	public static boolean isStandardFunctionWidgetCategory(String category) {
		return DEFAULT_CATEGORIES.toLowerCase().indexOf(category.toLowerCase() + "=") != -1;
	}
	
	/**
	 * Returns true if the category is explicitly enabled. For unknown keys true is always returned.
	 * 
	 * @param category
	 * @return
	 */
	public static boolean isFunctionWidgetCategoryEnabled(String category) {
		String str = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_JS_FUNCTION_CATEGORIES + "." + category);
		if(str == null || "true".equals(str))
			return true;
		else
			return false;
	}
	
	/**
	 * Returns all enabled function widget categories.
	 * 
	 * @param store
	 * @return
	 */
	public static List<String> getFunctionWidgetCategories(IPreferenceStore store) {
		String str = store.getString(PreferenceConstants.P_JS_FUNCTION_CATEGORIES);
		if (str == null || str.length() == 0) {
			str = DEFAULT_CATEGORIES;
		}
		List<String> list= new ArrayList<String>();
		for (String cat : str.split("\t")) {
			if (cat.trim().length() > 0) {
				list.add(cat.trim());
			}
		}
		return list;
	}

	@Override
	public boolean performOk() {
		StringBuffer buf = new StringBuffer();
		for (String str : catlist) {
			if (buf.length() > 0)
				buf.append("\t");
			buf.append(str);
			getPreferenceStore().setValue(PreferenceConstants.P_JS_FUNCTION_CATEGORIES + "." + str, list.getChecked(str));
		}
		getPreferenceStore().setValue(PreferenceConstants.P_JS_FUNCTION_CATEGORIES, buf.toString());
		getPreferenceStore().setValue(PreferenceConstants.P_JS_FUNCTION_ENABLED, enabled.getSelection());
		return super.performOk();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite c = new Composite(parent, 0);
		c.setLayout(new GridLayout(1, false));

		enabled = new Button(c, SWT.CHECK);
		enabled.setText(Messages.getString("JavaScriptPrefs.enable"));
		enabled.setSelection(isFunctionWidgetEnabled());
		
		enabled.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				list.getTable().setEnabled(enabled.getSelection());
			}
		});
		
		new Label(c, SWT.LEFT).setText(Messages.getString("JavaScriptPrefs.categories"));
		list = CheckboxTableViewer.newCheckList(c, 0);
		list.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

		list.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof List<?>) {
					return ((List<?>) inputElement).toArray();
				}
				return new Object[0];
			}
		});
		
		list.setInput(catlist);
		
		list.getTable().setEnabled(enabled.getSelection());
		
		for(String cat : catlist) {
			if(isFunctionWidgetCategoryEnabled(cat))
				list.setChecked(cat, true);
		}

		Button b = new Button(c, SWT.PUSH);
		b.setText(Messages.getString("JavaScriptPrefs.categories.add"));
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				InputDialog id = new InputDialog(getShell(), Messages.getString("JavaScriptPrefs.categories.add"), Messages.getString("JavaScriptPrefs.categories.add.prompt"), "", null);
				if (id.open() == Window.OK) {
					catlist.add(id.getValue());
					list.refresh();
					list.setChecked(id.getValue(), true);
				}
			}
		});

		return c;
	}

	public void init(IWorkbench workbench) {
	}

}
