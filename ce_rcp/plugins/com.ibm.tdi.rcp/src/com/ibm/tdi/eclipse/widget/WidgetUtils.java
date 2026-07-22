/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class WidgetUtils {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static Button createNewButton(Composite parent) {
		Button b = new Button(parent, SWT.PUSH);
		b.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_NEW_WIZARD));
		return b;
	}

	public static Button createCutButton(Composite parent) {
		Button b = new Button(parent, SWT.PUSH);
		b.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_CUT));
		return b;
	}
	
	/**
	 * Creates a Group component used by the AL editor and other to provide a fixed height
	 * Group component.
	 * @param parent
	 * @return the Group component
	 */
	public static Group createGroup(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_IN);
		group.setLayout(new GridLayout(1, false));

		GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		gd.heightHint = 45;
		group.setLayoutData(gd);

		return group;
	}
}
