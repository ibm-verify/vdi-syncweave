/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.util;

import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.core.resources.IFile;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.util.StringUtils;
import com.ibm.tdi.eclipse.TDI;

public class CustomEditorSettings {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public static final String STEP_MODE = "stepMode";
	public static final String WORK_ENABLED = "workEnabled";
	public static final String WORK_ENTRY = "workEntry";
	public static final String AL_OPERATION = "operation";
	public static final String AL_FEED_FLOW = "show.feedflow";
	public static final String AL_SHOW_ATTRS = "show.attrs";
	public static final String AL_SHOW_HOOKS = "show.hooks";
	public static final String AL_SIMULATE = "simulate";
	public static final String DEBUG_MODE = "debugMode";
	public static final String TARGET_SERVER = "targetServer";
	public static final String INIT_PARAMS_ENABLED = "initParamsEnabled";
	public static final String INIT_PARAMS = "initParams";
	public static final String REGRESSION_ENABLED = "regressionEnabled";
	public static final String REGRESSION_FILE = "regressionFile";
	public static final String REGRESSION_WRITE = "regressionWrite";
	
	private IFile file;
	private Hashtable<String, String> data = new Hashtable<String, String>();
	private char sep = 8;

	public CustomEditorSettings(BaseConfiguration config) {
		this(((TDIConfigurationFile)config.getMetamergeConfig()).getFile());
	}
	
	public CustomEditorSettings(IFile file) {
		this.file = file;
	}
	
	public void loadSettings() {
		try {
			String opts = file.getPersistentProperty(TDI.RUN_OPTIONS_QNAME);
			if(opts == null || opts.length() == 0)
				return;
			for(String str : opts.split("\n")) {
				int i = str.indexOf("=");
				data.put(str.substring(0,i), str.substring(i+1));
			}
		} catch (Exception e) {
			data.clear();
			e.printStackTrace();
		}
	}
	
	public void saveSettings() {
		try {
			StringBuffer buf = new StringBuffer();
			for(Enumeration<String> e = data.keys(); e.hasMoreElements(); ) {
				String key = e.nextElement();
				buf.append(key+"="+data.get(key)+"\n");
			}
			file.setPersistentProperty(TDI.RUN_OPTIONS_QNAME, buf.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Removes the key from the settings array and returns the current value
	 * 
	 * @param key
	 * @return
	 */
	public Object removeProperty(String key) {
		return data.remove(key);
	}

	public void setProperty(String key, Object val) {
		if(val instanceof Entry)
			data.put(key, "E:"+entry2string((Entry)val));
		else if(val instanceof Integer)
			data.put(key, "I:"+val);
		else if(val instanceof Boolean)
			data.put(key, "B:"+val);
		else
			data.put(key, "S:"+val);
	}
	
	private String entry2string(Entry entry) {
		StringBuffer buf = new StringBuffer();
		buf.append(entry.getOperation()+sep);
		for(String attr : entry.getAttributeNames()) {
			Attribute a = entry.getAttribute(attr);
			buf.append("A:" + a.getName()+sep);
			for(Object obj : a.getValues())
				buf.append("V:" + StringUtils.toPrint(""+obj)+sep);
		}
		return buf.toString();
	}
	
	private Entry string2entry(String str) {
		String[] list = str.split(""+sep);
		Entry e = new Entry();
		e.setOperation(list[0]);
		Attribute a = null;
		for(int i = 1; i < list.length; i++) {
			if(list[i].startsWith("A:"))
				a = e.newAttribute(list[i].substring(2));
			else if (list[i].startsWith("V:"))
				a.addValue(list[i].substring(2));
		}
		return e;
	}
	
	public Object getProperty(String key) {
		String prop = data.get(key);
		if(prop == null)
			return null;
		if(prop.startsWith("E:"))
			return string2entry(prop.substring(2));
		else if(prop.startsWith("I:"))
			return Integer.parseInt(prop.substring(2));
		else if(prop.startsWith("B:"))
			return Boolean.parseBoolean(prop.substring(2));
		else if(prop.startsWith("S:"))
			return prop.substring(2);
		else
			return prop;
	}
	
	public Entry getEntry(String key) {
		Object obj = getProperty(key);
		if(obj instanceof Entry)
			return (Entry) obj;
		else
			return null;
	}

	public int getInteger(String key, int defval) {
		Object obj = getProperty(key);
		if(obj == null)
			return defval;
		else if (obj instanceof Integer)
			return (Integer)obj;
		else
			return Integer.parseInt(obj.toString());
	}

	public boolean getBoolean(String key, boolean defval) {
		Object obj = getProperty(key);
		if(obj == null)
			return defval;
		else if (obj instanceof Boolean)
			return (Boolean) obj;
		else
			return Boolean.parseBoolean(obj.toString());
	}

	public String getString(String key, String defval) {
		Object val = getProperty(key);
		if(val == null)
			return defval;
		else
			return val.toString();
	}

	public void setProperty(String key, String value, boolean save) {
		setProperty(key, value);
		if(save)
			saveSettings();
	}
}
