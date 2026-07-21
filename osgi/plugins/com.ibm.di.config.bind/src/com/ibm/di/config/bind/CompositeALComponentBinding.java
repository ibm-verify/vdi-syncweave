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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for CompositeALComponentBinding complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="CompositeALComponentBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}ALComponentBinding">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/config}compositeConfig" minOccurs="0"/>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/config}component" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="state" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ALComponentStateEnum" default="Enabled" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompositeALComponentBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
		"compositeConfig", "component" })
@XmlRootElement(name = "composite", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class CompositeALComponentBinding extends ALComponentBinding {

	private static final long serialVersionUID = 6645953042127185323L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElementRef
	protected CompositeComponentBinding compositeConfig;
	@XmlElementRef
	protected List<ALComponentBinding> component;
	@XmlAttribute
	protected ALComponentStateEnum state;

	/**
	 * Gets the value of the compositeConfig property.
	 * 
	 * @return
	 */
	public CompositeComponentBinding getCompositeConfig() {
		return compositeConfig;
	}

	/**
	 * Sets the value of the compositeConfig property.
	 * 
	 * @param value
	 */
	public void setComposite(CompositeComponentBinding value) {
		this.compositeConfig = value;
	}

	/**
	 * Gets the value of the component property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the component property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getComponent().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ALComponentBinding }
	 * 
	 * 
	 */
	public List<ALComponentBinding> getComponents() {
		if (component == null) {
			component = new ArrayList<ALComponentBinding>();
		}
		return this.component;
	}

	/**
	 * Gets the value of the state property.
	 * 
	 * @return possible object is {@link ALComponentStateEnum }
	 * 
	 */
	public ALComponentStateEnum getState() {
		if (state == null) {
			return ALComponentStateEnum.ENABLED;
		} else {
			return state;
		}
	}

	/**
	 * Sets the value of the state property.
	 * 
	 * @param value
	 *            allowed object is {@link ALComponentStateEnum }
	 * 
	 */
	public void setState(ALComponentStateEnum value) {
		this.state = value;
	}

}
