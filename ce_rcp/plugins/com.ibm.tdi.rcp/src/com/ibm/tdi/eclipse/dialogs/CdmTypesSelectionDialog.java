/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

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
 * This class is used to display a dialog box listing the CDM types. For CI
 * types it only lists the classes names, while for Relationships, besides its
 * class, are displayed the classes of source and target CI for each
 * relationship.
 */
public class CdmTypesSelectionDialog extends Dialog {

	/**
	 * Prefix used for label strings.
	 */
	private static final String LOCALIZED = "FormUI.Localized.";

	/**
	 * The main combo, listing the CDM types.
	 */
	private Combo relationships;

	/**
	 * The map containing the listed values.
	 */
	private Map<String, Object> map;

	/**
	 * The number of rows visible for the main Combo.
	 */
	private int visibleItemCount;

	/**
	 * The string for the Label of the main combo.
	 */
	private String classesString;

	/**
	 * The selected value from the mian combo.
	 */
	private String value;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            the parent shell of this dialog.
	 * @param map
	 *            the map, containing the displayed values.
	 * @param classesString
	 *            the String to be displayed by the main combo Label.
	 * @param visibleItemCount
	 *            the number of visible rows, in the main combo list.
	 */
	private CdmTypesSelectionDialog(Shell parent, Map<String, Object> map, String classesString, int visibleItemCount) {
		super(parent);
		this.map = map;
		this.classesString = classesString;
		this.visibleItemCount = visibleItemCount;
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
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		c.setLayout(new GridLayout(2, true));

		String sourceString = "Sources:";
		String targetString = "Targets:";

		String translated = Messages.getString(LOCALIZED + "Source.Label");
		if (translated != null) {
			sourceString = translated;
		}
		translated = Messages.getString(LOCALIZED + "Target.Label");
		if (translated != null) {
			targetString = translated;
		}

		// create the main combo and its label
		Label classTypesLabel = new Label(c, SWT.NONE);
		classTypesLabel.setText(classesString);
		classTypesLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false, 2, 1));

		relationships = new Combo(c, SWT.READ_ONLY);
		relationships.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
		if (visibleItemCount > 0) {
			relationships.setVisibleItemCount(visibleItemCount);
		}

		// if there are values to display
		if (map != null && !map.isEmpty()) {
			for (String key : map.keySet()) {
				relationships.add(key);
			}

			// check if this dialog should display a CI or a Relationship
			boolean forRelationships = false;
			Iterator<Object> it = map.values().iterator();
			if (it.hasNext() && it.next() != null) {
				forRelationships = true;
			}

			if (forRelationships) {
				Label sourceLabel = new Label(c, SWT.NONE);
				sourceLabel.setText(sourceString);
				sourceLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));

				Label targetLabel = new Label(c, SWT.NONE);
				targetLabel.setText(targetString);
				targetLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));

				final Combo sourceClasses = new Combo(c, SWT.SIMPLE);
				sourceClasses.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
				sourceClasses.setVisibleItemCount(10);
				sourceClasses.setEnabled(false);

				final Combo targetClasses = new Combo(c, SWT.SIMPLE);
				targetClasses.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
				targetClasses.setVisibleItemCount(10);
				targetClasses.setEnabled(false);

				relationships.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						String chosenRelationship = relationships.getText();
						TreeSet<Object> sourceItems = ((ArrayList<TreeSet<Object>>) map.get(chosenRelationship)).get(0);
						sourceClasses.removeAll();
						for (Object item : sourceItems) {
							sourceClasses.add((String) item);
						}
						sourceClasses.setEnabled(true);

						TreeSet<Object> targetItems = ((ArrayList<TreeSet<Object>>) map.get(chosenRelationship)).get(1);
						targetClasses.removeAll();
						for (Object item : targetItems) {
							targetClasses.add((String) item);
						}
						targetClasses.setEnabled(true);
					}
				});
			}
		}

		getShell().setText(classesString);
		return c;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void okPressed() {
		value = relationships.getText();
		super.okPressed();
	}

	/**
	 * Returns the value of the item selected from the main combo.
	 * 
	 * @return the value selected from the main combo.
	 */
	private String getSelectedClassType() {
		return value;
	}

	/**
	 * Opens a modal dialog where the user can select the CDM of the desired
	 * CI/Relationship. If there is additional information for the Relationships
	 * (e.g. if DIS's CDM is used) it is displayed as well. It consists of the
	 * class names of CI that can play the role of sources or targets of the
	 * Relationship.
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
	public static Object chooseCdmTypesFromList(Shell parent, String prompt, Map<String, Object> map, int visibleItemCount) {
		if (prompt == null) {
			prompt = "";
		} else {
			String translated = Messages.getString(LOCALIZED + prompt.replaceAll(" ", "."));
			if (translated != null) {
				prompt = translated;
			}
		}

		CdmTypesSelectionDialog dlg = new CdmTypesSelectionDialog(parent, map, prompt, visibleItemCount);
		dlg.open();
		return dlg.getSelectedClassType();
	}
}
