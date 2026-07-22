/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.ibm.tdi.eclipse.widget.SchedulerWidget;

public class SchedulerEditor extends BaseEditor {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private SchedulerWidget widget;

	@Override
	public void createPartControl(Composite parent) {
		widget = new SchedulerWidget(parent, SWT.NONE, getTDIConfiguration());
	}
	
	@Override
	public void setFocus() {
		if(widget != null)
			widget.setFocus();
	}


}
