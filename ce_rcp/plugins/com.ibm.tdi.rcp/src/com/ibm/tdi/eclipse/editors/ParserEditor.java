/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.ibm.tdi.eclipse.widget.ParserWidget;

public class ParserEditor extends BaseEditor {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private ParserWidget parser;
	
	public ParserEditor() {
	}

	@Override
	public void createPartControl(Composite parent) {
		if(getTDIConfiguration() == null) {
			super.createPartControl(parent);
			return;
		}
		parser = new ParserWidget(parent, SWT.NONE, getTDIConfiguration(), this);
		setModified(false);
	}

	@Override
	public void setFocus() {
		super.setFocus();
		if(parser != null)
			parser.setFocus();
	}

}
