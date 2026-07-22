/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.List;

public class ComboInputDialog extends Dialog {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
    private String title;
 	private String value = null;
	private String prompt;
	private String[] items;
	private Combo combo;
	private Text errorMessageText;
	private IInputValidator validator;
	
	public ComboInputDialog(Shell parentShell, String title, String prompt,List<String> values) {
		super(parentShell);
		this.title = title;
		this.prompt = prompt;
		items=values.toArray(new String[values.size()]);
	}

	public ComboInputDialog(Shell parentShell, String title, String prompt,
			List<String> values, IInputValidator validator) {
		this(parentShell, title, prompt, values);
		this.validator = validator;
	}

     protected void buttonPressed(int buttonId) {
    	value = combo.getText();
        super.buttonPressed(buttonId);
    }

     protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
			shell.setText(title);
		}
    }


	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		
        Label label = new Label(c, SWT.LEFT);
        label.setText(prompt);
        GridData data = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_CENTER);
        data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        label.setLayoutData(data);
        label.setFont(parent.getFont());

		combo = new Combo(c, SWT.DROP_DOWN | SWT.V_SCROLL | SWT.BORDER);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL |
				GridData.HORIZONTAL_ALIGN_FILL);
		data.verticalSpan = 3;
		combo.setLayoutData(data);
        combo.setItems(items);
 
        if (validator != null) {
            combo.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    setErrorMessage(validator.isValid(combo.getText()));
                }
            });
            errorMessageText = new Text(c, SWT.READ_ONLY | SWT.WRAP);
            errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                    | GridData.HORIZONTAL_ALIGN_FILL));
            errorMessageText.setBackground(errorMessageText.getDisplay()
                    .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
            setErrorMessage(null);
        }

        applyDialogFont(c);		
		return c;
	}

	public String getValue() {
		return value;
	}

    public void setErrorMessage(String errorMessage) {
    	if (errorMessageText != null && !errorMessageText.isDisposed()) {
    		errorMessageText.setText(errorMessage != null ? errorMessage : "");
    		errorMessageText.setEnabled(errorMessage != null);
    		errorMessageText.setVisible(errorMessage != null);
    		errorMessageText.getParent().update();
 
    		Control button = getButton(IDialogConstants.OK_ID);
    		if (button != null) {
    			button.setEnabled(errorMessage == null);
    		}
    	}
    }

}
