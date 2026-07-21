/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ExposedPropertyBinding complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ExposedPropertyBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}NamedBinding">
 *       &lt;sequence>
 *         &lt;element name="userComment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="label" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="category" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="storeName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExposedPropertyBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = { "userComment" })
public class ExposedPropertyBinding extends NamedBinding {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = -434874847473308415L;

	protected String userComment;
	@XmlAttribute
	protected String label;
	@XmlAttribute
	protected String category;
	@XmlAttribute
	protected String storeName;

	/**
	 * Gets the value of the userComment property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUserComment() {
		return userComment;
	}

	/**
	 * Sets the value of the userComment property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUserComment(String value) {
		this.userComment = value;
	}

	/**
	 * Gets the value of the label property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the value of the label property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLabel(String value) {
		this.label = value;
	}

	/**
	 * Gets the value of the category property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Sets the value of the category property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCategory(String value) {
		this.category = value;
	}

	/**
	 * Gets the value of the storeName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getStoreName() {
		return storeName;
	}

	/**
	 * Sets the value of the storeName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setStoreName(String value) {
		this.storeName = value;
	}

}
