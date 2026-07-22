/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.queue;

/**
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class QCallbackEvent {
	private int ID = 0;

	private MemBufferQ source;

	public QCallbackEvent(MemBufferQ src, int id) {
		source = src;
		ID = id;
	}

	public MemBufferQ getSource() {
		return source;
	}

	public int getID() {
		return ID;
	}
}
