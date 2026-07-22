/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for AssemblyLineEvent complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="AssemblyLineEvent">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/api}Event">
 *       &lt;sequence>
 *         &lt;element name="resultEntry" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}Entry" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="cycleDone"/>
 *             &lt;enumeration value="alStopped"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssemblyLineEvent", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = { "resultEntry" })
@XmlRootElement(name = "assemblyLineEvent", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class AssemblyLineEvent extends Event {

	private static final long serialVersionUID = -5853618343870112997L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
	protected Entry resultEntry;
	@XmlAttribute(required = true)
	protected String type;

	/**
	 * Gets the value of the resultEntry property.
	 * 
	 * @return possible object is {@link Entry }
	 * 
	 */
	public Entry getResultEntry() {
		return resultEntry;
	}

	/**
	 * Sets the value of the resultEntry property.
	 * 
	 * @param value
	 *            allowed object is {@link Entry }
	 * 
	 */
	public void setResultEntry(Entry value) {
		this.resultEntry = value;
	}

	/**
	 * Gets the value of the type property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the value of the type property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setType(String value) {
		this.type = value;
	}

}
