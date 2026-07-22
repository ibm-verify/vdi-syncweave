/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;

/**
 * <p>
 * Java class for ALOperationBinding complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ALOperationBinding">
 *   &lt;complexContent>
 *     &lt;extension base={http://www.ibm.com/xmlns/prod/tdi/72/config}NamedBinding">
 *       &lt;sequence>
 *         &lt;element name="schema" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}SchemaBinding" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="attributeMap" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}AttributeMapBinding" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ALOperationBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = { "schema",
		"attributeMap" })
public class ALOperationBinding extends NamedBinding implements Serializable {

	private static final long serialVersionUID = 5554324541540732614L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(name="schema")
	protected List<SchemaBinding> schema;
	@XmlElement(name="attributeMap")
	protected List<AttributeMapBinding> attributeMap;

	/**
	 * Gets the value of the schema property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the schema property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getSchema().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link SchemaBinding }
	 * 
	 * 
	 */
	public List<SchemaBinding> getSchemas() {
		if (schema == null) {
			schema = new ArrayList<SchemaBinding>(2);
		}
		return this.schema;
	}

	/**
	 * Gets the value of the attributeMap property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the attributeMap property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAttributeMap().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link AttributeMapBinding }
	 * 
	 * 
	 */
	public List<AttributeMapBinding> getAttributeMaps() {
		if (attributeMap == null) {
			attributeMap = new ArrayList<AttributeMapBinding>(2);
		}
		return this.attributeMap;
	}
}
