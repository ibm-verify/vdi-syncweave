/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * This widget automatically positions Label/Control pairs in a table.
 */
public class LabelFieldWidget extends Composite {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private FormToolkit tk = new FormToolkit(getDisplay());
	private Form form;
	
	public LabelFieldWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
		
		form = tk.createForm(this);
		
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		layout.bottomMargin = 3;
		form.getBody().setLayout(layout);
	}
	
	public Text addTextField(String text, String value, int style) {
		tk.createLabel(form.getBody(), text, SWT.RIGHT);
		Text tc = tk.createText(form.getBody(), value, style);
		tc.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		return tc;
	}
	
	public void setTitle(String text) {
		form.setText(text);
	}

	public Label addDescription(String text) {
		Label l = tk.createLabel(form.getBody(), text, SWT.WRAP);
		TableWrapData td = new TableWrapData();
		td.colspan = 2;
		l.setLayoutData(td);
		return l;
	}
	
	public void addSeparator() {
		Label l = tk.createSeparator(form.getBody(), 0);
		TableWrapData td = new TableWrapData();
		td.colspan = 2;
		l.setLayoutData(td);
	}

	public Button addButton(String label, String tooltip, int style) {
		tk.createLabel(form.getBody(), "", SWT.LEFT);
		Button b = tk.createButton(form.getBody(), label, style);
		return b;
	}

	public Combo addCombo(String label, int style) {
		tk.createLabel(form.getBody(), label, SWT.LEFT);
		Combo c = new Combo(form.getBody(), style);
		c.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		return c;
	}


}
