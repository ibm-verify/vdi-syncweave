/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class CommandBarGrid extends CommandBar {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public CommandBarGrid(Composite parent, int style, Object listener, int columns, boolean makeColumnsEqual) {
		super(parent, style, listener);
		setLayout(new GridLayout(columns, makeColumnsEqual));
	}

	@Override
	protected FormData setFormData(Control item) {
		return null;
	}

	@Override
	public Text addTextField(String str, String tooltip, String command, int size) {
		// TODO Auto-generated method stub
		Text text = super.addTextField(str, tooltip, command, size);
		
		GridData gd = new GridData();
		gd.widthHint = size * 10;
		text.setLayoutData(gd);
		
		return text;
	}

	@Override
	public void setBanner(String string) {
	}

}
