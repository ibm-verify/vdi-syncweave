/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.NT4UserMetaDataConnector;

import java.util.Date;
import java.util.Vector;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;

/**
 * The InfoUtil class encapsulates common constants and static methods for
 * processing User/Group data structures.
 */
public class InfoUtil {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/** querySchema attribute name */
	private static final String QSN_NAME = "name";
	/** querySchema syntax name */
	private static final String QSN_SYNTAX = "syntax";
	/** querySchema size name */
	private static final String QSN_SIZE = "size";
	/** querySchema signed integer attribute limits */
	protected static final int MAX_SIGNED_DWORD = 2147483647;
	/** querySchema unsigned integer attribute limits */
	protected static final long MAX_UNSIGNED_DWORD = 4294967296L;
	/** querySchema attribute syntaxes - Boolean */
	protected static final String QSS_BOOLEAN = "java.lang.Boolean";
	/** querySchema attribute syntaxes - Byte array */
	protected static final String QSS_BYTE_ARRAY = "java.lang.Byte[]";
	/** querySchema attribute syntaxes - Date */
	protected static final String QSS_DATE = "java.util.Date";
	/** querySchema attribute syntaxes - Integer */
	protected static final String QSS_INTEGER = "java.lang.Integer";
	/** querySchema attribute syntaxes - Long */
	protected static final String QSS_LONG = "java.lang.Long";
	/** querySchema attribute syntaxes - String */
	protected static final String QSS_STRING = "java.lang.String";
	/** querySchema attribute syntaxes - Vector */
	protected static final String QSS_VECTOR = "java.util.Vector";

	/**
	 * Adds an entry describing an attribute's structure to the given vector.
	 * 
	 * @param aSchema
	 *            The vector representing the connector's entry schema. The new
	 *            entry will be added to this vector.
	 * @param aName
	 *            The name of the attribute.
	 * @param aSyntax
	 *            The definition of the attribute.
	 * @param aSize
	 *            The size of the attribute . If null it is not included in the
	 *            schema.
	 */
	protected static void addSchemaEntry(Vector aSchema, String aName,
			String aSyntax, Object aSize) {
		Entry entry = new Entry();
		entry.setAttribute(QSN_NAME, aName);
		entry.setAttribute(QSN_SYNTAX, aSyntax);
		if (aSize != null) {
			entry.setAttribute(QSN_SIZE, aSize);
		}
		aSchema.addElement(entry);
	}

	/**
	 * Creates and adds attribute to the given Entry object.
	 * 
	 * @param aEntry
	 *            The entry object to attach the new attribute to.
	 * @param aAttrName
	 *            The name of the new attribute.
	 * @param aAttrValue
	 *            The value of the new attribute.
	 */
	protected static void createAndAddEntryAttribute(Entry aEntry,
			String aAttrName, Object aAttrValue) {
		Attribute attr = aEntry.newAttribute(aAttrName);
		if (aAttrValue != null) {
			attr.addValue(aAttrValue);
		}
	}

	/**
	 * Retrieves the specified Attribute's value as a String object.
	 * 
	 * @param aEntry
	 *            The Entry object which Attribute's value will be retrieved.
	 * @param aAttrName
	 *            The name of the Entry's Attribute.
	 * @return The String value of the specified Entry's Attribute; "null" if
	 *         the specified Attribute does not exist.
	 */
	protected static String getStringEntryAttributeValue(Entry aEntry,
			String aAttrName) {
		String strObject = null;
		Attribute attr = aEntry.getAttribute(aAttrName);

		if ((attr != null) && (attr.size() > 0)) {
			strObject = attr.getValue(0).toString();
		}

		return strObject;
	}

	/**
	 * Retrieves the specified Attribute's value as an Integer object. This
	 * method performs conversion from Double,Long and String to Integer.
	 * 
	 * @param aEntry
	 *            The Entry object which Attribute's value will be retrieved.
	 * @param aAttrName
	 *            The name of the Entry's Attribute.
	 * @return The Integer value of the specified Entry's Attribute; "null" if
	 *         the specified Attribute does not exist.
	 */
	protected static Integer getIntegerEntryAttributeValue(Entry aEntry,
			String aAttrName) {
		Integer intObject = null;
		Attribute attr = aEntry.getAttribute(aAttrName);

		if ((attr != null) && (attr.size() > 0)) {
			Object aValue = attr.getValue(0);

			if (aValue instanceof Double) {
				intObject = Integer.valueOf(((Double) aValue).intValue());
			} else if (aValue instanceof Long) {
				intObject = Integer.valueOf(((Long) aValue).intValue());
			} else if (aValue instanceof String) {
				intObject = Integer.valueOf((String) aValue);
			} else {
				intObject = (Integer) aValue;
			}
		}
		return intObject;
	}

	/**
	 * Retrieves the specified Attribute's value as a Long object. This method
	 * performs conversion from Double,Integer and String to Long.
	 * 
	 * @param aEntry
	 *            The Entry object which Attribute's value will be retrieved.
	 * @param aAttrName
	 *            The name of the Entry's Attribute.
	 * @return The Long value of the specified Entry's Attribute; "null" if the
	 *         specified Attribute does not exist.
	 */
	protected static Long getLongEntryAttributeValue(Entry aEntry,
			String aAttrName) {
		Long longObject = null;
		Attribute attr = aEntry.getAttribute(aAttrName);

		if ((attr != null) && (attr.size() > 0)) {
			Object aValue = attr.getValue(0);

			if (aValue instanceof Double) {
				longObject = Long.valueOf(((Double) aValue).longValue());
			} else if (aValue instanceof Integer) {
				longObject = Long.valueOf(((Integer) aValue).longValue());
			} else if (aValue instanceof String) {
				longObject = Long.valueOf((String) aValue);
			} else {
				longObject = (Long) aValue;
			}
		}
		return longObject;
	}

	/**
	 * Retrieves the specified Attribute's value as a Boolean object. This
	 * method performs conversion from String to Boolean.
	 * 
	 * @param aEntry
	 *            The Entry object which Attribute's value will be retrieved.
	 * @param aAttrName
	 *            The name of the Entry's Attribute.
	 * @return The Boolean value of the specified Entry's Attribute; "null" if
	 *         the specified Attribute does not exist.
	 */
	protected static Boolean getBooleanEntryAttributeValue(Entry aEntry,
			String aAttrName) {
		Boolean boolObject = null;
		Attribute attr = aEntry.getAttribute(aAttrName);

		if ((attr != null) && (attr.size() > 0)) {
			Object aValue = attr.getValue(0);

			if (aValue instanceof String) {
				boolObject = Boolean.valueOf((String) aValue);
			} else {
				boolObject = (Boolean) aValue;
			}
		}
		return boolObject;
	}

	/**
	 * Retrieves the specified Attribute's value as a java.util.Date object.
	 * 
	 * @param aEntry
	 *            The Entry object which Attribute's value will be retrieved.
	 * @param aAttrName
	 *            The name of the Entry's Attribute.
	 * @return The java.util.Date value of the specified Entry's Attribute;
	 *         "null" if the specified Attribute does not exist.
	 */
	protected static Date getDateEntryAttributeValue(Entry aEntry,
			String aAttrName) {
		Date dateObject = null;
		Attribute attr = aEntry.getAttribute(aAttrName);

		if ((attr != null) && (attr.size() > 0)) {
			dateObject = (Date) attr.getValue(0);
		}

		return dateObject;
	}

	/**
	 * Retrieves the specified Attribute's value as a byte array object.
	 * 
	 * @param aEntry
	 *            The Entry object which Attribute's value will be retrieved.
	 * @param aAttrName
	 *            The name of the Entry's Attribute.
	 * @return The byte[] value of the specified Entry's Attribute; "null" if
	 *         the specified Attribute does not exist.
	 */
	protected static byte[] getByteArrayEntryAttributeValue(Entry aEntry,
			String aAttrName) {
		byte[] byteArrayObject = null;
		Attribute attr = aEntry.getAttribute(aAttrName);

		if ((attr != null) && (attr.size() > 0)) {
			byteArrayObject = (byte[]) attr.getValue(0);
		}

		return byteArrayObject;
	}

}
