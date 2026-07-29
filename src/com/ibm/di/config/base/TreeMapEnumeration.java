/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.*;
import javax.naming.*;
import com.ibm.di.config.interfaces.*;
/**
 * This class implements an Enumeration on a configuration object that belongs to a MetamergeConfig.
 * Used for historical reason.
 *
 */
@SuppressWarnings("unchecked")
public class TreeMapEnumeration implements Enumeration {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private TreeMap tm;

	private Iterator iter;

	private Object base;

	private MetamergeConfig mc;

	public TreeMapEnumeration(MetamergeConfig mc, Object base,
			BaseConfiguration cf) {
		this.mc = mc;
		this.tm = (TreeMap) cf.getData();
		this.base = base;
		this.iter = tm.keySet().iterator();
	}

	public boolean hasMoreElements() {
		return iter.hasNext();
	}

	public Object nextElement() {
		Object key = iter.next();
		// System.out.println ( "TME: " + base + ", " + key);
		try {
			Name name;
			if (base != null)
				name = MetamergeConfigFactory.parseName(base).add(
						key.toString());
			else
				name = MetamergeConfigFactory.parseName(key.toString());

			// System.out.println ( name );
			BaseConfiguration b = (BaseConfiguration) mc.lookup(name);
			return new Binding(name.get(name.size() - 1), b);

		} catch (Exception err) {
			err.printStackTrace();
			return new Binding(key.toString(), err.toString());
		}
	}
}
