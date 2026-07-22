/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.tam;

import java.util.Date;
import java.text.ParsePosition;
import java.util.Iterator;
import java.util.Vector;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.tivoli.pd.jutil.PDContext;
import com.tivoli.pd.jutil.PDMessages;
import com.tivoli.pd.jutil.PDMessage;
import com.tivoli.pd.jutil.PDException;

/**
 * Utility class to enable classes to log to the LogProxyImpl
 * <p>
 * LogProxyImpl contains a copy of the log passed in from the Connector
 */
public class CommonBase {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String QSN_NAME = "name";

	private static final String QSN_SYNTAX = "syntax";

	private static final String QSN_SIZE = "size";

	public static final int MAX_SIGNED_DWORD = 0x7fffffff;

	public static final long MAX_UNSIGNED_DWORD = 0x100000000L;

	public static final String QSS_BOOLEAN = "java.lang.Boolean";

	public static final String QSS_BYTE_ARRAY = "java.lang.Byte[]";

	public static final String QSS_DATE = "java.sql.Date";

	public static final String QSS_INTEGER = "java.lang.Integer";

	public static final String QSS_LONG = "java.lang.Long";

	public static final String QSS_STRING = "java.lang.String";

	public static final String QSS_VECTOR = "java.util.Vector";

	public static final String DATE_FORMAT = "yyyyMMdd";

	protected Log mLogProxy;

	protected PDMessages mPDMessages;

	protected PDContext mPDContext;

	protected CommonBase() {
		mPDMessages = new PDMessages();
		mLogProxy = new Log(TMSMessageGetter.MESSAGE_PROPERTIES_NAME);
	}

	protected CommonBase(PDContext context) {
		mPDMessages = new PDMessages();
		mLogProxy = new Log(TMSMessageGetter.MESSAGE_PROPERTIES_NAME);
		mPDContext = context;
	}

	protected CommonBase(PDContext context, Log logger) {
		mPDMessages = new PDMessages();
		mPDContext = context;
		mLogProxy = logger;
	}

	protected void logmsg(String msg) {
		mLogProxy.loginfo(msg);
	}

	protected void debug(String msg) {
		if (mLogProxy.getDebug())
			mLogProxy.logdebug(msg);
	}

	protected void debug(String msg, Log log) {
		mLogProxy = log;
		if (mLogProxy.getDebug()) {
			mLogProxy.logdebug(msg);
		}
	}

	protected void error(String error) {
		mLogProxy.error(error);
	}

	protected void processMsgs(PDMessages msgs) {
		if (msgs != null) {
			Iterator iter = msgs.iterator();
			while (iter.hasNext()) {
				logmsg(((PDMessage) iter.next()).getMsgText());
			}
			msgs.clear();
		}
	}

	/**
	 * Create and add the a schema entry to a schema vector.
	 * 
	 * @param vector -
	 *            Vector to add the schema entry to
	 * @param name -
	 *            Name of the schema element to add
	 * @param syntax -
	 *            Syntax of the schema element (QSS_*)
	 * @param size -
	 *            Size of the schema element
	 */
	protected static void addSchemaEntry(Vector vector, String name,
			String syntax, Object size) {
		Entry entry = new Entry();
		entry.setAttribute(QSN_NAME, name);
		entry.setAttribute(QSN_SYNTAX, syntax);
		if (size != null)
			entry.setAttribute(QSN_SIZE, size);
		vector.addElement(entry);
	}

	/**
	 * Create and add attribute to an entry object.
	 * 
	 * @param entry -
	 *            Entry to add the attribute to
	 * @param attributeName -
	 *            Name of the attribute to add
	 * @param value -
	 *            Value of the attribute to add
	 */
	protected void createAndAddEntryAttribute(Entry entry,
			String attributeName, Object value) {
		Attribute attribute = entry.newAttribute(attributeName);
		if (value != null)
			attribute.addValue(value);
	}

	/**
	 * Retrieve an attribute from the entry object as a string.
	 * 
	 * @param entry -
	 *            Entry to read the attribute from
	 * @param s -
	 *            Name of the attribute value to return
	 * @return String value of specified attribute name from the provided entry
	 */
	protected String getStringEntryAttributeValue(Entry entry, String s) {
		String s1 = null;
		Attribute attribute = entry.getAttribute(s);
		if (attribute != null && attribute.size() > 0
				&& attribute.getValue().length() != 0)
			s1 = attribute.getValue(0).toString();
		return s1;
	}

	/**
	 * Retrieve an attribute from the entry object as an integer.
	 * 
	 * @param entry -
	 *            Entry to read the attribute from
	 * @param s -
	 *            Name of the attribute value to return
	 * @return Integer value of specified attribute name from the provided entry
	 */
	protected Integer getIntegerEntryAttributeValue(Entry entry, String s) {
		Integer integer = null;
		Attribute attribute = entry.getAttribute(s);
		if (attribute != null && attribute.size() > 0
				&& attribute.getValue().length() != 0) {
			Object obj = attribute.getValue(0);
			if (obj instanceof Double)
				integer = Integer.valueOf(((Double) obj).intValue());
			else if (obj instanceof Long)
				integer = Integer.valueOf(((Long) obj).intValue());
			else if (obj instanceof Integer)
				integer = (Integer) obj;
			else
				integer = Integer.valueOf((String) obj);
		}
		return integer;
	}

	/**
	 * Retrieve an attribute from the entry object as a Long.
	 * 
	 * @param entry -
	 *            Entry to read the attribute from
	 * @param s -
	 *            Name of the attribute value to return
	 * @return Long value of specified attribute name from the provided entry
	 */
	protected Long getLongEntryAttributeValue(Entry entry, String s) {
		Long long1 = null;
		Attribute attribute = entry.getAttribute(s);
		if (attribute != null && attribute.size() > 0
				&& attribute.getValue().length() != 0) {
			Object obj = attribute.getValue(0);
			if (obj instanceof Double)
				long1 = Long.valueOf(((Double) obj).longValue());
			else if (obj instanceof Integer)
				long1 = Long.valueOf(((Integer) obj).longValue());
			else if (obj instanceof Long)
				long1 = (Long) obj;
			else
				long1 = Long.valueOf((String) obj);
		}
		return long1;
	}

	/**
	 * Retrieve an attribute from the entry object as a Boolean.
	 * 
	 * @param entry -
	 *            Entry to read the attribute from
	 * @param s -
	 *            Name of the attribute value to return
	 * @return Boolean value of specified attribute name from the provided entry
	 */
	protected Boolean getBooleanEntryAttributeValue(Entry entry, String s) {
		Boolean boolean1 = null;
		Attribute attribute = entry.getAttribute(s);
		if (attribute != null && attribute.size() > 0
				&& attribute.getValue().length() != 0) {
			Object obj = attribute.getValue(0);
			if (obj instanceof Boolean)
				boolean1 = (Boolean) obj;
			else if (obj.toString().equalsIgnoreCase("true")
					|| obj.toString().equalsIgnoreCase("false"))
				boolean1 = Boolean.valueOf((String) obj);
			else if (obj.toString().equalsIgnoreCase("1"))
				boolean1 = Boolean.TRUE;
			else if (obj.toString().equalsIgnoreCase("0"))
				boolean1 = Boolean.FALSE;
		}
		return boolean1;
	}

	/**
	 * Retrieve an attribute from the entry object as a Date.
	 * 
	 * @param entry -
	 *            Entry to read the attribute from
	 * @param s -
	 *            Name of the attribute value to return
	 * @return Date value of specified attribute name from the provided entry
	 */
	protected Date getDateEntryAttributeValue(Entry entry, String s) {
		Date date = null;
		Attribute attribute = entry.getAttribute(s);
		if (attribute != null && attribute.size() > 0
				&& attribute.getValue().length() != 0) {
			Object obj = attribute.getValue(0);
			if (obj instanceof Date)
				date = (Date) obj;
			else {
				SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
				date = df.parse((String) obj, new ParsePosition(0));
			}
		}
		return date;
	}

	/**
	 * Retrieve an attribute from the entry object as a byte array.
	 * 
	 * @param entry
	 *            Entry to read the attribute from
	 * @param s
	 *            Name of the attribute value to return
	 * 
	 * @return Attribute from the entry object as a byte array.
	 */
	protected byte[] getByteArrayEntryAttributeValue(Entry entry, String s) {
		byte abyte0[] = null;
		Attribute attribute = entry.getAttribute(s);
		if (attribute != null && attribute.size() > 0
				&& attribute.getValue().length() != 0)
			abyte0 = (byte[]) attribute.getValue(0);
		return abyte0;
	}

	protected void printEntry(Entry entry) {
		logmsg(entry.toString());
	}

	protected String getPDMessage(PDException pde) {
		debug("Entered CommonBase.getPDMessage");
		PDMessages msgs = pde.getMessages();
		StringBuffer err = null;
		if (msgs != null) {
			err = new StringBuffer("");
			Iterator pdi = msgs.iterator();
			PDMessage msg = null;
			while (pdi.hasNext()) {
				msg = (PDMessage) pdi.next();
				logmsg(msg.getMsgText());
				err.append(msg.getMsgText());
				if (pdi.hasNext())
					err.append(";");
			}
		} else {
			err = new StringBuffer(pde.getLocalizedMessage());
		}
		debug("Exited CommonBase.getPDMessage");
		return err.toString();
	}
}
