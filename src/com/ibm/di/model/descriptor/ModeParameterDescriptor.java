/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.ibm.di.model.descriptor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ModeParameterDescriptor complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;ModeParameterDescriptor&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base=&quot;{http://www.ibm.com/xmlns/prod/tdi/71/core}ParameterDescriptor&quot;&gt;
 *       &lt;attribute name=&quot;supportedModes&quot; default=&quot;all&quot;&gt;
 *         &lt;simpleType&gt;
 *           &lt;union memberTypes=&quot; {http://www.ibm.com/xmlns/prod/tdi/71/core}ConnectorModesEnum {http://www.ibm.com/xmlns/prod/tdi/71/core}ConnectorModesEnumExt&quot;&gt;
 *           &lt;/union&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModeParameterDescriptor", namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core")
@XmlRootElement
public class ModeParameterDescriptor extends ParameterDescriptor {

	private static final long serialVersionUID = -6552871915809954098L;

	@XmlAttribute
	protected String supportedModes;

	/**
	 * Gets the value of the supportedModes property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSupportedModes() {
		if (supportedModes == null) {
			return "all";
		} else {
			return supportedModes;
		}
	}

	/**
	 * Sets the value of the supportedModes property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSupportedModes(String value) {
		this.supportedModes = value;
	}

}
