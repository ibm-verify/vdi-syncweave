/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.tdi.eclipse.Messages;

public class ALMappingWidget extends BaseWidget {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public ALMappingWidget(Composite parent, int style, AttributeMapConfig attributeMap) {
		super(parent, style, attributeMap);
		setLayout(new GridLayout(1, false));
		createUI(this);
	}

	private void createUI(Composite parent) {
		Group group = WidgetUtils.createGroup(parent);
		group.setText(getEditingConfig().getShortName());
		group.setLayout(new GridLayout(2, false));
		Button b = new Button(group, SWT.CHECK);
		b.setText(Messages.getString("ALMappingUI.enabled")); //$NON-NLS-1$
		b.setSelection(getEditingConfig().getEnabled());
		b.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				getEditingConfig().setEnabled(((Button) e.widget).getSelection());
			}
		});
	}

}
