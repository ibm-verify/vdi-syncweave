/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.dialogs;


import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.ibm.tdi.eclipse.Messages;

/**
 * This class is used to display a dialog box listing the MSS.
 * A text area lists all the non-null values for MSS
 */

public class MSSDetailsDialog extends Dialog{
	
	/**
	 * Prefix used for label strings.
	 */
	private static final String LOCALIZED = "FormUI.Localized.";
	
	/**
	 * The main combo listing MSSName 
	 */
	private Combo mssName;
	
	/**
	 * The string for label of main combo.
	 */
	private String mssString;
	
	/**
	 * The number of rows visible for the main Combo.
	 */
	private int visibleItemCount;
	
	/**
	 *  The map containing the listed values.
	 */
	private Map<String,Map<String,?>> map;
	
	/**
	 *  The selected value from the main combo.
	 */
	private String value;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            the parent shell of this dialog.
	 * @param map
	 *            the map, containing the displayed values.
	 * @param mssString
	 *            the String to be displayed by the main combo Label.
	 * @param visibleItemCount
	 *            the number of visible rows, in the main combo list.
	 */
	protected MSSDetailsDialog(Shell parent, Map<String,Map<String,?>> map, String mssString, int visibleItemCount) {
		super(parent);
		this.map = map;
		this.mssString = mssString;
		this.visibleItemCount=visibleItemCount;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(350, 100);
		super.configureShell(newShell);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void okPressed() {
		value = mssName.getText();
		super.okPressed();
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		c.setLayout(new GridLayout(1, true));

		String mssHeader = "MSS Details :";

		String translated = Messages.getString(LOCALIZED + "MSS.Label");
		if (translated != null) {
			mssHeader = translated;
		}

		// create the main combo and its label
		Label mssLabel = new Label(c, SWT.NONE);
		mssLabel.setText(mssString);
		mssLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));

		mssName = new Combo(c, SWT.READ_ONLY);
		mssName.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		if (visibleItemCount > 0) {
			mssName.setVisibleItemCount(visibleItemCount);
		}

		// if there are values to display
		if (map != null && !map.isEmpty()) {
			for (String key : map.keySet()) {
				mssName.add(key);
			}

			Label mssDetailsLabel = new Label(c, SWT.NONE);
			mssDetailsLabel.setText(mssHeader);
			mssDetailsLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));

			final Combo mssDetails = new Combo(c, SWT.SIMPLE);
			mssDetails.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
			mssDetails.setVisibleItemCount(10);
			mssDetails.setEnabled(false);

			mssName.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					String chosenMSS = mssName.getText();
					Map<String,?> sourceItems = map.get(chosenMSS);
					mssDetails.removeAll();

					for (Map.Entry<String,?> entry: sourceItems.entrySet()){
						Object value = entry.getValue();
						if ( value != null )
							mssDetails.add(entry.getKey() + ": " + value);
					}
					mssDetails.setEnabled(true);
				}
			});
		}

		getShell().setText(mssString);
		return c;
	}
	
	/**
	 * Opens a modal dialog where the user can select the MSS from the registered MSS in DIS database.
	 *  For every MSS its additional details are displayed.
	 * 
	 * @param parent
	 *            the parent shell of this dialog.
	 * @param prompt
	 *            the message to be displayed on the dialog.
	 * @param map
	 *            the map, containing the displayed values.
	 * @param visibleItemCount
	 *            the number of lines in the main combo's list (the one
	 *            displaying the classes names).
	 * @return the selected value, or <b>null</b> if nothing was selected.
	 */
	public static Object chooseMSSFromList(Shell parent, String prompt, Map<String,Map<String,?>> map, int visibleItemCount) {
		if (prompt == null) {
			prompt = "";
		} else {
			String translated = Messages.getString(LOCALIZED + prompt.replaceAll(" ", "."));
			if (translated != null) {
				prompt = translated;
			}
		}

		MSSDetailsDialog dlg = new MSSDetailsDialog(parent, map, prompt, visibleItemCount);
		dlg.open();
		return dlg.getSelectedMSS();
	}

	/**
	 * Returns the value of the item selected from the main combo.
	 * 
	 * @return the value selected from the main combo.
	 */
	private String getSelectedMSS() {
		
		return value;
	}

}
