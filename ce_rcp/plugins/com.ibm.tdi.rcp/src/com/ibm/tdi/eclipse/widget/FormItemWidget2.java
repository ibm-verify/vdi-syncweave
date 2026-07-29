/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.FormItemConfig;

public class FormItemWidget2 extends BaseWidget {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String DROPDOWN_SYNTAX = "dropdown";
	private static final String DROPLIST_SYNTAX = "droplist";
	private static final String BOOLEAN_SYNTAX = "boolean";
	private Control control;

	public FormItemWidget2(Composite parent, int style, FormItemConfig itemConfig, BaseConfiguration editingConfig) {
		super(parent, style, editingConfig);
		GridLayout layout = new GridLayout(4, false);
		setLayout(layout);
		setBackground(parent.getBackground());

		GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		gd.horizontalSpan = layout.numColumns;
		
		String str = itemConfig.getLabel();
		if(str != null) {
			Label label = new Label(this, SWT.LEFT);
			label.setText(str);
			label.setBackground(getBackground());
			label.setLayoutData(gd);
		}
		
		
		if(BOOLEAN_SYNTAX.equalsIgnoreCase(itemConfig.getSyntax()))
			control = new Button(this, SWT.CHECK);
		else if(DROPLIST_SYNTAX.equalsIgnoreCase(itemConfig.getSyntax()))
			control = new Combo(this, SWT.DROP_DOWN|SWT.READ_ONLY);
		else if(DROPDOWN_SYNTAX.equalsIgnoreCase(itemConfig.getSyntax()))
			control = new Combo(this, SWT.DROP_DOWN);
		else
			control = new Text(this, SWT.BORDER);
			
		control.setBackground(getBackground());
		control.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		
		// Button
		if(itemConfig.getScript() != null) {
			Button b1 = new Button(this, SWT.PUSH);
			b1.setText(itemConfig.getScriptLabel());
			if(itemConfig.getScriptToolTip() != null)
				b1.setToolTipText(itemConfig.getScriptToolTip());
		}
		
		// Button
		if(itemConfig.getScript2() != null) {
			Button b2 = new Button(this, SWT.PUSH);
			b2.setText(itemConfig.getScriptLabel2());
			if(itemConfig.getScriptToolTip2() != null)
				b2.setToolTipText(itemConfig.getScriptToolTip2());
		}
		
	}

}
