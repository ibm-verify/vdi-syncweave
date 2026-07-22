/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class TitledComposite2 extends Composite {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private FormToolkit tk;
	private Form form;
	private ArrayList<Listener> listeners = new ArrayList<Listener>();
	private boolean expanded;

	public TitledComposite2(Composite parent, int style) {
		super(parent, style);
		tk = new FormToolkit(getDisplay());
		form = tk.createForm(parent);
		form.setFont(JFaceResources.getDefaultFont());
		//tk.decorateFormHeading(form);
	}

	public void setText(String comptitle) {
		form.setText(comptitle);
	}

	public void addMaxListener(Listener listener) {
		listeners.add(listener);
	}

	public String getText() {
		return form.getText();
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public boolean isExpanded() {
		return expanded;
	}

}
