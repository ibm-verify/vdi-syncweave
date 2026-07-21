/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.swt.widgets.Composite;

import com.ibm.di.entry.Entry;

public class DiscoveredEntryWidget extends BaseWidget {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Entry entry;

	public DiscoveredEntryWidget(Composite parent, int style, Entry entry) {
		super(parent, style);
		this.entry = entry;
	}

	public Entry getEntry() {
		return entry;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}
	
}
