/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import com.ibm.di.config.interfaces.TDIProperties;
import com.ibm.di.entry.Entry;

// EntryArrayList no longer implements Comparator<Entry> directly.
// Java 21 added List.reversed() as a default method, which conflicts with
// Comparator.reversed() when both interfaces are implemented simultaneously.
// The ordering logic is preserved as a public static COMPARATOR field so
// callers that sort this list can reference EntryArrayList.COMPARATOR.
public class EntryArrayList extends ArrayList<Entry> {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Comparator that orders entries by status + key attribute.
	 * Extracted from the former {@code implements Comparator<Entry>} to avoid
	 * the duplicate {@code reversed()} default method conflict introduced in
	 * Java 21 between {@link java.util.List} and {@link java.util.Comparator}.
	 */
	public static final Comparator<Entry> COMPARATOR = (o1, o2) -> {
		String s1 = o1.getString("status");
		if (s1 == null)
			s1 = "local";
		String s2 = o2.getString("status");
		if (s2 == null)
			s2 = "local";

		s1 += "." + o1.getString(TDIProperties.KEY_ATTRIBUTE);
		s2 += "." + o2.getString(TDIProperties.KEY_ATTRIBUTE);
		return s1.compareTo(s2);
	};

	public EntryArrayList() {
		super();
	}

	public void updateEntry(Entry e) {
		for(Entry cur : this) {
			if(cur.getString(TDIProperties.KEY_ATTRIBUTE).equals(e.getString(TDIProperties.KEY_ATTRIBUTE))) {
				// only update if not local value
				if("server".equals(cur.getString("status"))) {
					cur.merge(e);
					cur.setAttribute("status", "server");
				}
				return;
			}
		}

		e = e.clone(e);
		e.setAttribute("status", "server");
		add(e);
	}

	public InputStream getInputStream() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		new ObjectOutputStream(bos).writeObject(this);
		return new ByteArrayInputStream(bos.toByteArray());
	}

	public static EntryArrayList load(IFile file) throws Exception, ClassNotFoundException, CoreException {
		EntryArrayList e = (EntryArrayList) new ObjectInputStream(file.getContents()).readObject();
		for(Entry entry : e) {
			if("server".equals(entry.getString("status")))
				entry.setAttribute("status", "delete");
		}
		return e;
	}

}
