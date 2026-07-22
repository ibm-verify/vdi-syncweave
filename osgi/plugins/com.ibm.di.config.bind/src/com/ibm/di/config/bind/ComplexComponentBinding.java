/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;

/**
 * <p>
 * Java class for ComplexComponentBinding complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ComplexComponentBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}InheritingBinding">
 *       &lt;sequence>
 *         &lt;element name="userComment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rawConfig" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}JavaClassBinding" minOccurs="0"/>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/config}parser" minOccurs="0"/>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/config}schema" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/config}map" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="hooks" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}HooksBinding" minOccurs="0"/>
 *         &lt;element name="reconnect" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ReconnectBinding" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComplexComponentBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = { "userComment",
		"rawConfig", "parser", "schema", "map", "hooks", "reconnect" })
@XmlSeeAlso( { ConnectorBinding.class, FunctionBinding.class })
public abstract class ComplexComponentBinding extends InheritingBinding {

	private static final long serialVersionUID = 4553635127480751140L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	protected String userComment;
	protected JavaClassBinding rawConfig;
	protected ParserBinding parser;
	@XmlElement(name="schema")
	protected List<SchemaBinding> schema;
	@XmlElement(name="map")
	protected List<AttributeMapBinding> map;
	protected HooksBinding hooks;
	protected ReconnectBinding reconnect;

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
	 * Gets the value of the rawConfig property.
	 * 
	 * @return possible object is {@link JavaClassBinding }
	 * 
	 */
	public JavaClassBinding getRawConfig() {
		return rawConfig;
	}

	/**
	 * Sets the value of the rawConfig property.
	 * 
	 * @param value
	 *            allowed object is {@link JavaClassBinding }
	 * 
	 */
	public void setRawConfig(JavaClassBinding value) {
		this.rawConfig = value;
	}

	/**
	 * Gets the value of the parser property.
	 * 
	 * @return possible object is {@link ParserBinding }
	 * 
	 */
	public ParserBinding getParser() {
		return parser;
	}

	/**
	 * Sets the value of the parser property.
	 * 
	 * @param value
	 *            allowed object is {@link ParserBinding }
	 * 
	 */
	public void setParser(ParserBinding value) {
		this.parser = value;
	}

	/**
	 * Gets the value of the map property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the map property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getMap().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link AttributeMapBinding }
	 * 
	 * 
	 */
	public List<AttributeMapBinding> getMaps() {
		if (map == null) {
			map = new ArrayList<AttributeMapBinding>();
		}
		return this.map;
	}

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
			schema = new ArrayList<SchemaBinding>();
		}
		return this.schema;
	}

	/**
	 * Gets the value of the hooks property.
	 * 
	 * @return possible object is {@link HooksBinding }
	 * 
	 */
	public HooksBinding getHooks() {
		return hooks;
	}

	/**
	 * Sets the value of the hooks property.
	 * 
	 * @param value
	 *            allowed object is {@link HooksBinding }
	 * 
	 */
	public void setHooks(HooksBinding value) {
		this.hooks = value;
	}

	/**
	 * Gets the value of the reconnect property.
	 * 
	 * @return possible object is {@link ReconnectBinding }
	 * 
	 */
	public ReconnectBinding getReconnect() {
		return reconnect;
	}

	/**
	 * Sets the value of the reconnect property.
	 * 
	 * @param value
	 *            allowed object is {@link ReconnectBinding }
	 * 
	 */
	public void setReconnect(ReconnectBinding value) {
		this.reconnect = value;
	}

}
