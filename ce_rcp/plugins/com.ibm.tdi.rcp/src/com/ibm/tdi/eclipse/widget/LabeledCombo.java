/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

public class LabeledCombo extends Composite {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Combo combo;
	
	private ArrayList<String> values = new ArrayList<String>();
	
	public LabeledCombo(Composite parent, int style) {
		super(parent, 0);
		setLayout(new FillLayout());
		combo = new Combo(this, style);
	}
	
	public Combo getCombo() {
		return combo;
	}
	
	public void addListener(int eventType, Listener listener) {
		combo.addListener(eventType, listener);
	}
	
	public void removeListener(int eventType, Listener listener) {
		combo.removeListener(eventType, listener);
	}

	public void addLabel(String label, String value) {
		combo.add(label);
		values.add(value);
	}
	
	public void setValue(String value) {
		int i = values.indexOf(value);
		System.out.println("setValue: " + value + "; " + i);
		if(i == -1)
			return;
		
		combo.setText(combo.getItem(i));
	}
	
	public String getValue() {
		int i = 0;
		String sel = combo.getText();
		for(String str : combo.getItems()) {
			if(sel.equals(str))
				return values.get(i);
			i++;
		}
		return null;
	}

}
