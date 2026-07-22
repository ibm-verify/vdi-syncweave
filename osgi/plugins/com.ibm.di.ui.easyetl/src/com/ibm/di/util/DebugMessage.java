/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// DebugMessage.java
//
//
//

package com.ibm.di.util;

import java.io.*;
import java.net.*;
import java.util.*;
import com.ibm.di.server.*;
import com.ibm.di.script.*;

public class DebugMessage extends Object implements Serializable {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String name;

	private Hashtable props;

	public DebugMessage() {
	}

	public DebugMessage(String name, Object value) {
		this.name = name;
		setProp(name, value);
	}

	public String getName() {
		return name;
	}

	public Object getDefault() {
		return getProp(name);
	}

	public Object getProp(Object p1) {
		if (props != null)
			return props.get(p1);
		else
			return null;
	}

	public void setProp(Object p1, Object p2) {
		if (props == null)
			props = new Hashtable();
		if (p1 == null || p2 == null)
			return;
		props.put(p1, p2);
	}

}
