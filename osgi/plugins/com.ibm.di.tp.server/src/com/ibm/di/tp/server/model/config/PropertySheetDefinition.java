/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;

import com.ibm.di.tp.server.Constants;

/**
 * <p>
 * Java class for PropertySheetDefinitionType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="PropertySheetDefinitionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="propertyDefinition" type="{http://www.ibm.com/xmlns/prod/scmp}PropertyDefinitionType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PropertySheetDefinitionType", namespace = Constants.NS_SCMP, propOrder = { "propertyDefinition" })
@XmlRootElement(name = "propertySheetDefinition")
public class PropertySheetDefinition {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(name="propertyDefinition")
	protected List<PropertyDefinition> propertyDefinition;
	@XmlAttribute
	protected String name;

	@XmlTransient
	private String schemaLoc;

	/**
	 * Gets the value of the propertyDefinition property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the propertyDefinition property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getPropertyDefinition().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link PropertyDefinition }
	 * 
	 * 
	 */
	public List<PropertyDefinition> getPropertyDefinition() {
		if (propertyDefinition == null) {
			propertyDefinition = new ArrayList<PropertyDefinition>();
		}
		return this.propertyDefinition;
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
	 * @param schemaLoc the schemaLoc to set
	 */
	public void setSchemaLocation(String schemaLoc) {
		this.schemaLoc = schemaLoc;
	}

	/**
	 * @return the schemaLoc
	 */
	public String getSchemaLocation() {
		return schemaLoc;
	}

}
