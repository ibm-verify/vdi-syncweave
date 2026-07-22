/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InputTextAreaDialog extends Dialog {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private Text text;

	private String value = null;

	private String prompt;

	private String title;

	public InputTextAreaDialog(Shell parentShell, String title, String prompt, String defval) {
		super(parentShell);
		this.title = title;
		this.prompt = prompt;
		this.value = defval;
	}

	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		GridLayout gl = new GridLayout(1,false);
		gl.marginLeft = 5;
		gl.marginRight = 5;
		c.setLayout(gl);
		
		new Label(c, SWT.LEFT).setText(prompt);
		
		text = new Text(c, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		if(value != null)
			text.setText(value);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				value = text.getText();
			}
		});
		value = "";
		
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 300;
		gd.heightHint = 100;
		text.setLayoutData(gd);
		
		getShell().setText(title);
		
		return c;
	}

	public String getValue() {
		return value;
	}

}
