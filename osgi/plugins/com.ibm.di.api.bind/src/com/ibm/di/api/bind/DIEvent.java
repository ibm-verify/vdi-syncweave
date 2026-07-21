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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.ibm.di.config.bind.XMLGregorianCalendarAdapter;

/**
 * <p>
 * Java class for DIEvent complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="DIEvent">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/api}Event">
 *       &lt;sequence>
 *         &lt;element name="data" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}Data" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="created" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="ciId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DIEvent", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = { "data" })
@XmlSeeAlso( { CIEvent.class, ALEvent.class })
@XmlRootElement(name = "diEvent", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class DIEvent extends Event {

	private static final long serialVersionUID = 6964848089573827700L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
	protected Data data;
	@XmlAttribute(required = true)
	protected String type;
	@XmlAttribute(required = true)
	protected String id;
	@XmlAttribute
	@XmlJavaTypeAdapter(value = XMLGregorianCalendarAdapter.class)
	protected Long created;
	@XmlAttribute
	protected String ciId;

	/**
	 * Gets the value of the data property.
	 * 
	 * @return possible object is {@link Data }
	 * 
	 */
	public Data getData() {
		return data;
	}

	/**
	 * Sets the value of the data property.
	 * 
	 * @param value
	 *            allowed object is {@link Data }
	 * 
	 */
	public void setData(Data value) {
		this.data = value;
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

	/**
	 * Gets the value of the id property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the value of the id property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setId(String value) {
		this.id = value;
	}

	/**
	 * Gets the value of the created property.
	 * 
	 * @return possible object is {@link Long }
	 * 
	 */
	public Long getCreated() {
		return created;
	}

	/**
	 * Sets the value of the created property.
	 * 
	 * @param value
	 *            allowed object is {@link Long }
	 * 
	 */
	public void setCreated(Long value) {
		this.created = value;
	}

	/**
	 * Gets the value of the ciId property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCiId() {
		return ciId;
	}

	/**
	 * Sets the value of the ciId property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCiId(String value) {
		this.ciId = value;
	}

}
