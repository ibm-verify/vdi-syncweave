/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.AttributeValue;
import com.ibm.di.entry.Entry;
import com.ibm.jscript.types.FBSNull;

/**
 * This class is used to transport information about non-serializable Objects.
 * Used by debugger.
 * 
 * @author Jens
 * @since 7.0
 * 
 */
public class NotSerializable implements Serializable {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 987654321L;

	public static final int MAX_CHILDREN = 200;

	private String className;
	private String value;
	private String reason;
	private Object[] child;
	private String[] childName;

	NotSerializable(Object o, Throwable t) {
		className = o.getClass().getName();
		try {
			value = o.toString();
		} catch (Throwable e) {
			value = className + "(...)";
		}
		if (t != null)
			reason = t.getMessage();
	}

	private void createChildren(int n) {
		n = Math.max(n, MAX_CHILDREN + 1);
		child = new Object[n];
		childName = new String[n];
	}

	private void setChild(int n, String name, Object val, SimpleMap map) {
		if (n >= child.length) {
			child[child.length - 1] = "...";
			childName[child.length - 1] = "...";
		} else {
			child[n] = convertObject(val, map);
			childName[n] = name;
		}
	}
	
	public Object getChild(int i) {
		return child[i];
	}

	public String getChildName(int i) {
		return childName[i];
	}
	

	public String getClassName() {
		return className;
	}

	public String getValue() {
		return value;
	}

	public String getReason() {
		return reason;
	}
	
	public int numChildren() {
		return child == null ? 0 : child.length;
	}

	public String toString() {
		StringBuilder ret = new StringBuilder();
		if (reason != null) {
			ret.append(reason);
			ret.append(": ");
		}
		ret.append(value);
		return ret.toString();
	}

	/**
	 * Checks if entry is of type Entry and traverses all attributes and values
	 * to see if any of them are not serializable. Non serializable values are
	 * converted to its string representation in the new returned entry.
	 * 
	 * @param e
	 *            The entry object to convert
	 * @return The converted entry
	 */
	public static Entry convertEntry(Entry e) {
		return convertEntry(e, new SimpleMap());
	}

	private static Entry convertEntry(Entry e, SimpleMap map) {
		if (map.get(e) instanceof Entry)
			return (Entry) map.get(e);
		Entry safeEntry = new Entry();
		map.put(e, safeEntry);
		safeEntry.setOp(e.getOp());
		for (String attrName : e.getAttributeNames())
			safeEntry.setAttribute(attrName, convertAttribute(e.getAttribute(attrName), map));

		for (String name : e.getPropertyNames())
			safeEntry.setProperty(name, convertObject(e.getProperty(name), map));

		return safeEntry;
	}

	/**
	 * Converts the values in the attribute to a serializable representation if
	 * needed.
	 * 
	 * @param a
	 *            The attribute to convert
	 * @return A new Attribute with serializable values
	 */
	public static Attribute convertAttribute(Attribute a) {
		return convertAttribute(a, new SimpleMap());
	}
	
	private static Attribute convertAttribute(Attribute a, SimpleMap map) {
		if (map.get(a) instanceof Attribute)
			return (Attribute) map.get(a);
		Attribute na = new Attribute(a.getName());
		map.put(a, na);
		na.setOper(a.getOper());
		na.setProtected(a.getProtected(), false);
		for (Object value : a.getValuesAV()) {
			if (value instanceof AttributeValue) {
				AttributeValue av = (AttributeValue) value;
				na.addValue(convertObject(av.getValue(), map), av.getOper());
			} else {
				na.addValue(convertObject(value, map));
			}
		}
		return na;
	}

	/**
	 * Converts an object to a serializable representation if needed.
	 * 
	 * @param value
	 *            Object to convert.
	 * @return A serializable representation of the object.
	 */
	public static Object convertObject(Object value) {
		return convertObject(value, new SimpleMap());
	}
	
	/**
	 * Converts an object to a serializable representation if needed.
	 * 
	 * @param value
	 *            Object to convert.
	 * @param map Map mapping objects to their representation, used to avoid loops
	 * @return A serializable representation of the object.
	 */
	public static Object convertObject(Object value, SimpleMap map) {
		if (value == null || value instanceof FBSNull)
			return new NullValue();

		if (map.get(value) != null)
			return map.get(value);
		
        if (value instanceof String ||
                value instanceof Integer ||
                value instanceof Long ||
                value instanceof Short ||
                value instanceof Byte ||
                value instanceof Double ||
                value instanceof Character ||
                value instanceof Float ||
                value instanceof Exception ||
                value instanceof NotSerializable ||
                value instanceof Boolean) {
         	return value;
        }

		if (value instanceof Entry)
			return convertEntry((Entry) value, map);

		if (value instanceof Attribute)
			return convertAttribute((Attribute) value, map);

		if (value instanceof Map)
			return convertMap((Map<?,?>) value, map);
		
		if (value instanceof List)
			return convertList((List<?>)value, map);
		
		if (value instanceof Object[])
			return convertArray((Object[]) value, map);
		
		Object ret = new NotSerializable(value, null);
		map.put(value, ret);
		return ret;
	}

	private static Object convertMap(Map<?,?> val, SimpleMap map) {
		if (map.get(val) != null)
			return map.get(val);
		NotSerializable ret = new NotSerializable(val, null);
		map.put(val, ret);
		int n = val.size();
		if (n > 0) {
			ret.createChildren(n);
			int i = 0;
			for (Map.Entry<?,?> e: val.entrySet()) {
				ret.setChild(i++, e.getKey().toString(), e.getValue(), map);
			}		
		}
		return ret;
	}

	private static Object convertList(List<?> list, SimpleMap map) {
		if (map.get(list) != null)
			return map.get(list);
		NotSerializable value = new NotSerializable(list, null);
		map.put(list, value);
		int n = list.size();
		if (n > 0) {
			value.createChildren(n);
			for (int i=0;i<n;i++) {
				value.setChild(i, String.valueOf(i), list.get(i), map);
			}
		}
		return value;
	}

	private static Object convertArray(Object[] arr, SimpleMap map) {
		if (map.get(arr) != null)
			return map.get(arr);
		NotSerializable value = new NotSerializable(arr, null);
		map.put(arr, value);
		int n = arr.length;
		if (n > 0) {
			value.createChildren(n);
			for (int i=0;i<n;i++) {
				value.setChild(i, String.valueOf(i), arr[i], map);
			}
		}
		return value;
	}
	
	/**
	 * private class used as a simple map.
	 * This avoids using the hashcode of the keys, which may create problems in some cases.
	 * @author jens thomassen
	 *
	 */
	static class SimpleMap {
		private Vector<Object> keys = new Vector<Object>();
		private Vector<Object> values = new Vector<Object>();
		private Map<Object, Object> map = new Hashtable<Object, Object>();

		public Object get(Object key) {
			if (key == null)
				return null;
			if (useMap(key))
				return map.get(key);
			for (int i = keys.size() - 1; i >= 0 ;i--) {
				if (keys.get(i) == key)
					return values.get(i);
			}
			return null;
		}

		public void put(Object key, Object value) {
			if (key == null)
				return;
			if (useMap(key)) {
				map.put(key, value);
				return;
			}
			keys.add(key);
			values.add(value);
		}

		private boolean useMap(Object key) {
			if (key instanceof Map)
				return false;
			try {
				key.hashCode();
				return true;
			} catch (Throwable t) {
				return false;
			}
		}

	}
}
