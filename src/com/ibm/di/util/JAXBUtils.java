/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRootElement;

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
public class JAXBUtils {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static String serializeObject(Object o) throws JAXBException {
		return serializeObject(o, getContext(o).createMarshaller());
	}

	public static String serializeObject(Object o, Marshaller m) throws JAXBException {
		try {
			return new String(serializeObjectToBytes(o, m), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static byte[] serializeObjectToBytes(Object o) throws JAXBException {
		return serializeObjectToBytes(o, getContext(o).createMarshaller());
	}

	private static JAXBContext getContext(Object o) throws JAXBException {
		if (o instanceof JAXBElement) {
			o = ((JAXBElement) o).getValue();
		}

		return JAXBContext.newInstance(o.getClass().getPackage().getName());
	}

	public static byte[] serializeObjectToBytes(Object o, Marshaller m) throws JAXBException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		serializeObjectToStream(o, bos, m);
		return bos.toByteArray();
	}

	public static void serializeObjectToStream(Object o, OutputStream os) throws JAXBException {
		serializeObjectToStream(o, os, getContext(o).createMarshaller());
	}

	public static void serializeObjectToStream(Object o, OutputStream os, Marshaller m) throws JAXBException {
		o = getXmlRootElement(o);
		m.marshal(o, os);
	}

	private static Object getXmlRootElement(Object o) {
		if (!(o instanceof JAXBElement) && o.getClass().getAnnotation(XmlRootElement.class) == null) {
			try {
				Class<?> oc = Class.forName(o.getClass().getPackage().getName() + ".ObjectFactory");
				Method creator = oc.getMethod("create" + o.getClass().getSimpleName(), new Class<?>[] { o.getClass() });
				if (creator != null && creator.getAnnotation(XmlElementDecl.class) != null) {
					o = creator.invoke(oc.newInstance(), o);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
		}
		return o;
	}

	public static <T> T deserializeObjectFromBytes(byte[] o, Class<T> c) throws JAXBException {
		JAXBContext ctx = JAXBContext.newInstance(c.getPackage().getName());
		return (T) deserializeObjectFromBytes(o, ctx.createUnmarshaller());
	}

	public static <T> T deserializeObject(String o, Class<T> c) throws JAXBException {
		JAXBContext ctx = JAXBContext.newInstance(c.getPackage().getName());
		return (T) deserializeObject(o, ctx.createUnmarshaller());
	}

	public static Object deserializeObjectFromBytes(byte[] o, Unmarshaller um) {
		try {
			return ((JAXBElement<?>) um.unmarshal(new ByteArrayInputStream(o))).getValue();
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static Object deserializeObject(String o, Unmarshaller um) {
		StringReader sr = new StringReader(o);

		try {
			return ((JAXBElement<?>) um.unmarshal(sr)).getValue();
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return null;
	}
}
