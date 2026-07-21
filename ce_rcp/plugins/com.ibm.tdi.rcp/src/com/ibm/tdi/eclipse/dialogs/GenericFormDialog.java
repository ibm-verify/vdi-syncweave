/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.widget.FormWidget2;

public class GenericFormDialog extends Dialog {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private BaseConfiguration config;
	private String form;
	private int width = 600, height = 400;
	private boolean hasCancel = true;
	private String title = null;
	
	public GenericFormDialog(Shell parent, String form, BaseConfiguration config) {
		super(parent);
		this.config = config;
		this.form = form;
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = height;
		gd.widthHint = width;
		try {
			FormWidget2 fw = new FormWidget2(c, SWT.TITLE, config, form);
			fw.setLayoutData(gd);
			if (title == null && fw.getFormConfig() != null)
				title = fw.getFormConfig().getTitle();
		} catch (Exception e) {
			e.printStackTrace();
			Utils.exceptionWidget(c, e).setLayoutData(gd);
		}
		if (title != null)
			getShell().setText(title);
		return c;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(width,height);
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		if (hasCancel)
			createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}


	public void setWidthAndHeight(int w, int h) {
		width = w;
		height = h;
	}
	
	public void setNoCancel() {
		hasCancel = false;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

}
