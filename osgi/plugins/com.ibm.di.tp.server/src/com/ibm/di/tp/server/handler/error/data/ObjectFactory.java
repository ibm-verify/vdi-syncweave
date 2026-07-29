/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.handler.error.data;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the com.ibm.di.tp.server.handler.error.data
 * package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private final static QName _Error_QNAME = new QName("http://www.ibm.com/xmlns/prod/scmp", "error");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package:
	 * com.ibm.di.tp.server.handler.error.data
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link Error }
	 * 
	 */
	public Error createError() {
		return new Error();
	}

	/**
	 * Create an instance of {@link Error.Details }
	 * 
	 */
	public Error.Details createErrorDetails() {
		return new Error.Details();
	}

	/**
	 * Create an instance of {@link Error.Details.Detail }
	 * 
	 */
	public Error.Details.Detail createErrorDetailsDetail() {
		return new Error.Details.Detail();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Error }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/scmp", name = "error")
	public JAXBElement<Error> createError(Error value) {
		return new JAXBElement<Error>(_Error_QNAME, Error.class, null, value);
	}

}
