/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import com.ibm.di.tp.server.Constants;

/**
 * <p>
 * Java class for PropertyDefinitionType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="PropertyDefinitionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/scmp}BasePropertyType">
 *       &lt;sequence>
 *         &lt;element name="label" type="{http://www.ibm.com/xmlns/prod/scmp}LabelType" maxOccurs="unbounded"/>
 *         &lt;element name="option" type="{http://www.ibm.com/xmlns/prod/scmp}OptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="defaultValue" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="multiple" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="propertyType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="readonly" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="hidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="required" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PropertyDefinitionType", namespace = Constants.NS_SCMP, propOrder = { "label", "option", "defaultValue" })
public class PropertyDefinition extends BaseProperty {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(name = "label", required = true)
	protected List<Label> label;
	@XmlElement(name="option")
	protected List<Option> option;
	@XmlSchemaType(name = "anySimpleType")
	@XmlElement(name="defaultValue")
	protected List<Object> defaultValue;
	@XmlAttribute
	protected Boolean multiple;
	@XmlAttribute(required = true)
	protected String propertyType;
	@XmlAttribute
	protected Boolean readonly;
	@XmlAttribute
	protected Boolean hidden;
	@XmlAttribute
	protected Boolean required;

	/**
	 * Gets the value of the label property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the label property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getLabel().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Label }
	 * 
	 * 
	 */
	public List<Label> getLabel() {
		if (label == null) {
			label = new ArrayList<Label>();
		}
		return this.label;
	}

	/**
	 * Gets the value of the option property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the option property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getOption().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Option }
	 * 
	 * 
	 */
	public List<Option> getOption() {
		if (option == null) {
			option = new ArrayList<Option>();
		}
		return this.option;
	}

	/**
	 * Gets the value of the defaultValue property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the defaultValue property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDefaultValue().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Object }
	 * 
	 * 
	 */
	public List<Object> getDefaultValue() {
		if (defaultValue == null) {
			defaultValue = new ArrayList<Object>();
		}
		return this.defaultValue;
	}

	/**
	 * Gets the value of the multiple property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isMultiple() {
		if (multiple == null) {
			return false;
		} else {
			return multiple;
		}
	}

	/**
	 * Sets the value of the multiple property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setMultiple(Boolean value) {
		this.multiple = value;
	}

	/**
	 * Gets the value of the propertyType property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPropertyType() {
		return propertyType;
	}

	/**
	 * Sets the value of the propertyType property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPropertyType(String value) {
		this.propertyType = value;
	}

	/**
	 * Gets the value of the readonly property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isReadonly() {
		if (readonly == null) {
			return false;
		} else {
			return readonly;
		}
	}

	/**
	 * Sets the value of the readonly property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setReadonly(Boolean value) {
		this.readonly = value;
	}

	/**
	 * Gets the value of the hidden property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isHidden() {
		if (hidden == null) {
			return false;
		} else {
			return hidden;
		}
	}

	/**
	 * Sets the value of the hidden property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setHidden(Boolean value) {
		this.hidden = value;
	}

	/**
	 * Gets the value of the required property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isRequired() {
		if (required == null) {
			return false;
		} else {
			return required;
		}
	}

	/**
	 * Sets the value of the required property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setRequired(Boolean value) {
		this.required = value;
	}

}
