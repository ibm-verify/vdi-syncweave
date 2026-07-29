/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import com.ibm.tdi.eclipse.Activator;

public class JavaDocReader {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private static HashMap<String, StringBuffer> cache = new HashMap<String, StringBuffer>();

	/**
	 * Returns the segment of the javadoc file describing a particular method.
	 * 
	 * @param clazz
	 * @param m
	 * @return Null if the method wasn't found
	 * @throws Exception 
	 */
	public static StringBuffer getJavaDocs(Class<?> clazz, Method m) throws Exception {
		String key = clazz.getName() + ":" + method2String(m);
		StringBuffer buf = cache.get(key);
		if(buf == null) {
			DocReader doc = new DocReader(getFilename(clazz));
			String tag = getDocSignature(m);
			if(doc.skipTo("<A NAME=\"" + tag + "\"><!-- --></A><H3>")) {
				doc.collectUntil("<HR>");
				buf = doc.getCollectionString();
				if(buf != null) {
					buf.insert(0, "<H3>");
				}
			}
			if(buf != null && buf.length() > 0)
				cache.put(key, buf);
		}
		
		return buf;
	}
	
	/**
	 * Returns the segment of the javadoc file describing a particular method.
	 * 
	 * @param clazz
	 * @param m
	 * @return Null if the method wasn't found
	 * @throws Exception 
	 */
	public static StringBuffer getJavaDocs(Class<?> clazz, Field f) throws Exception {
		String key = clazz.getName() + ":" + f.getName();
		StringBuffer buf = cache.get(key);
		if(buf == null) {
			DocReader doc = new DocReader(getFilename(clazz));
			if(doc.skipTo("<A NAME=\"" + f.getName() + "\"><!-- --></A><H3>")) {
				doc.collectUntil("<HR>");
				buf = doc.getCollectionString();
				if(buf != null) {
					buf.insert(0, "<H3>");
				}
			}
			if(buf != null && buf.length() > 0)
				cache.put(key, buf);
		}
		
		return buf;
	}
	
	private static String getFilename(Class<?> clazz) {
		String name = clazz.getName();
		name = name.replaceAll("\\.", "/");
		String path = Activator.getInstallPath() + File.separator + "docs" + File.separator + "api" +
			File.separator + name + ".html";
		if(new File(path).exists())
			return path;
		
		String jvmdocs = System.getProperty("com.ibm.tdi.eclipse.jvm.docs");
		if(jvmdocs == null)
			return path;
		
		path = jvmdocs + File.separator + File.separator + "api" +
			File.separator + name + ".html";
		return path;
	}

	/**
	 * Convert a Method to a nice display String. Method.toString() does not
	 * look nice
	 * 
	 * @param m
	 *            Method name
	 * @return formated method
	 */
	private static String getDocSignature(Method m) {
		StringBuffer s = new StringBuffer();
		s.append(m.getName());
		s.append("(");
		Class<?>[] params = m.getParameterTypes();
		for (int i = 0; i < params.length; i++) {
			if (i > 0)
				s.append(", ");
			s.append(getClassName(params[i]));
		}
		s.append(")");
		return s.toString();
	}

	/**
	 * Convert a Method to a nice display String. Method.toString() does not
	 * look nice
	 * 
	 * @param m
	 *            Method name
	 * @return formated method
	 */
	private static String method2String(Method m) {
		StringBuffer s = new StringBuffer();
		s.append(m.getReturnType().getName());
		s.append(" ");
		s.append(m.getName());
		s.append("(");
		Class<?>[] params = m.getParameterTypes();
		for (int i = 0; i < params.length; i++) {
			if (i > 0)
				s.append(", ");
			s.append(getClassName(params[i]));
			s.append(" p" + (i + 1));
		}
		s.append(")");
		return s.toString();
	}

	/**
	 * Convert a Class to a nice display String. Class.getName() does not look
	 * nice for arrays
	 * 
	 * @param c
	 *            Class
	 * @return formated class
	 */
	private static String getClassName(Class<?> c) {
		if (c.isArray())
			return getClassName(c.getComponentType()) + "[]";
		else
			return c.getName();
	}

	
	private static class DocReader {
		private BufferedReader inp;
		private String lastLine;
		private ArrayList<String> collection;
		private boolean collecting;
		
		public DocReader(String file) throws FileNotFoundException {
			if(file == null)
				throw new FileNotFoundException();
			else
				inp = new BufferedReader(new FileReader(file));
		}
		
		public String readLine() throws IOException {
			lastLine = inp.readLine();
			if(isCollecting())
				collection.add(lastLine);
			return lastLine;
		}
		
		public boolean readNext() throws IOException {
			return readLine() != null;
		}
		
		public boolean skipTo(String str) throws IOException {
			while(readNext()) {
				if(lastLine.startsWith(str))
					return true;
			}
			return false;
		}
		
		public boolean collectUntil(String str) throws IOException {
			setCollecting(true);
			try {
				return skipTo(str);
			} finally {
				setCollecting(false);
			}
		}

		public boolean isCollecting() {
			return collecting;
		}

		public void setCollecting(boolean collecting) {
			this.collecting = collecting;
			if(collecting)
				collection = new ArrayList<String>();
		}

		public ArrayList<String> getCollection() {
			return collection;
		}
		
		public StringBuffer getCollectionString() {
			if(collection == null)
				return null;
			StringBuffer buf = new StringBuffer();
			for(String str : collection) {
				buf.append(str);
				buf.append("\n");
			}
			return buf;
		}
	}
}
