/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for Entry complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="Entry">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="property" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}EntryProperty" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="attribute" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}EntryAttribute" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Entry", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = { "property", "attribute" })
@XmlSeeAlso( { TaskCallBlock.class })
@XmlRootElement(name = "entry", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class Entry implements Serializable {

	private static final long serialVersionUID = 8325300725204405565L;
	
	@XmlElement(name = "property", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
	protected List<EntryProperty> property;
	@XmlElement(name = "attribute", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
	protected List<EntryAttribute> attribute;

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
	 * {@link EntryProperty }
	 * 
	 * 
	 */
	public List<EntryProperty> getProperties() {
		if (property == null) {
			property = new ArrayList<EntryProperty>();
		}
		return this.property;
	}

	/**
	 * Gets the value of the attribute property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the attribute property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAttribute().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link EntryAttribute }
	 * 
	 * 
	 */
	public List<EntryAttribute> getAttributes() {
		if (attribute == null) {
			attribute = new ArrayList<EntryAttribute>();
		}
		return this.attribute;
	}

}
