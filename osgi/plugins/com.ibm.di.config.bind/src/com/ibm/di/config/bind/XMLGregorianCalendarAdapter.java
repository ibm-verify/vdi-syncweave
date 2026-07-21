/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class XMLGregorianCalendarAdapter extends XmlAdapter<XMLGregorianCalendar, Long> {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private final static DatatypeFactory datatypeFactory;
	static {
		try {
			datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			// this is impossible!
			throw new InternalError(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public XMLGregorianCalendar marshal(Long v) throws Exception {
		return v != null ? longToXmlGCalendar(v) : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public Long unmarshal(XMLGregorianCalendar v) throws Exception {
		return v != null ? xmlGCalendarToLong(v) : null;
	}

	public static long xmlGCalendarToLong(XMLGregorianCalendar xmlGregCal) {
		if (xmlGregCal == null) {
			return -1;
		}
		Calendar calendar = xmlGregCal.toGregorianCalendar();
		long time = calendar.getTimeInMillis();
		return time;
	}

	public static XMLGregorianCalendar longToXmlGCalendar(long time) {
		if (time == -1) {
			return null;
		}
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(time);
		XMLGregorianCalendar xmlGregCal = datatypeFactory.newXMLGregorianCalendar(calendar);
		return xmlGregCal;
	}
}
