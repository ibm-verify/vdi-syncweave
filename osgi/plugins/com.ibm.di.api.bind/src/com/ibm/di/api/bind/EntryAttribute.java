/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for EntryAttribute complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="EntryAttribute">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="property" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}AttributeProperty" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/api}value" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/api}attribute" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="namespace" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="protect" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EntryAttribute", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = { "property", "children" })
@XmlRootElement(name = "attribute", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class EntryAttribute implements Serializable {

	private static final long serialVersionUID = -2634762553870263912L;
	
	@XmlElement(name = "property", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
	protected List<AttributeProperty> property;
	@XmlElements( {
			@XmlElement(name = "attribute", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", type = EntryAttribute.class),
			@XmlElement(name = "entry", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", type = Entry.class),
			@XmlElement(name = "value", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", type = AttributeValue.class) })
	protected List<Object> children;
	@XmlAttribute(required = true)
	protected String name;
	@XmlAttribute
	@XmlSchemaType(name = "anyURI")
	protected String namespace;
	@XmlAttribute
	protected Boolean protect;

	/**
	 * Gets the value of the property property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the property property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getProperty().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link AttributeProperty }
	 * 
	 * 
	 */
	public List<AttributeProperty> getProperties() {
		if (property == null) {
			property = new ArrayList<AttributeProperty>();
		}
		return this.property;
	}

	/**
	 * Gets the value of the children property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the valueOrAttribute property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getValueOrAttribute().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link EntryAttribute } {@link AttributeValue }
	 * 
	 * 
	 */
	public List<Object> getChildren() {
		if (children == null) {
			children = new ArrayList<Object>();
		}
		return this.children;
	}

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setName(String value) {
		this.name = value;
	}

	/**
	 * Gets the value of the namespace property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Sets the value of the namespace property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNamespace(String value) {
		this.namespace = value;
	}

	/**
	 * Gets the value of the protect property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isProtect() {
		if (protect == null) {
			return false;
		} else {
			return protect;
		}
	}

	/**
	 * Sets the value of the protect property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setProtect(Boolean value) {
		this.protect = value;
	}

}
