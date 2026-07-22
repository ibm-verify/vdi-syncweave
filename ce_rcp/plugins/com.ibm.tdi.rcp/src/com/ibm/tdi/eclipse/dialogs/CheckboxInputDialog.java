/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.dialogs;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class CheckboxInputDialog extends InputDialog {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String checkboxText;
	private String cbTooltip;
	private boolean isSelected;
	private Button checkbox;

	public CheckboxInputDialog(Shell parentShell, String dialogTitle,
			String dialogMessage, String initialValue,
			String checkboxText, String cbTooltip, boolean initSelected,
			IInputValidator validator) {
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		this.checkboxText = checkboxText;
		this.cbTooltip = cbTooltip;
		this.isSelected = initSelected;
	}
	
    protected Control createDialogArea(Composite parent) {
        // create composite
        Composite composite = (Composite) super.createDialogArea(parent);
 
        checkbox = new Button(composite, SWT.CHECK);
        checkbox.setText(checkboxText);
        if (cbTooltip != null)
        	checkbox.setToolTipText(cbTooltip);
        checkbox.setSelection(isSelected);
        checkbox.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        checkbox.addSelectionListener(new SelectionListener() {			
			public void widgetSelected(SelectionEvent e) {
				isSelected = checkbox.getSelection();
			}			
			public void widgetDefaultSelected(SelectionEvent e) {
				isSelected = checkbox.getSelection();
			}
		});
        
        return composite;
    }
    
    public boolean getSelection() {
    	return isSelected;
    }
}
