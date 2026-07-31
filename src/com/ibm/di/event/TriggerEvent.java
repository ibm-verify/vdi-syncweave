/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.event;

import java.awt.event.*;
import java.util.*;

public class TriggerEvent extends ActionEvent {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Hashtable params = new Hashtable();

	public TriggerEvent(Object source) {
		super(source, 0, null);
	}

	public void setParam(Object p1, Object p2) {
		params.put(p1, p2);
	}

	public Object getParam(Object p1) {
		return params.get(p1);
	}

}
