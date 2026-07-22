/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.queue;

/**
 * @author Vishakha
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class IDGenerator {
	private int ID = 0;

	synchronized public int getID() {
		return ++ID;
	}

	synchronized public void reset() {
		ID = 0;
	}
}
