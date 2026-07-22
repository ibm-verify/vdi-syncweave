/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for AssemblyLineListener complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="AssemblyLineListener">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/api}LogListener">
 *       &lt;attribute name="deliverLogs" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="deliverEntry" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssemblyLineListener", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
@XmlRootElement(name = "assemblyLineListener", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class AssemblyLineListener extends LogListener {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlAttribute
	protected Boolean deliverLogs;
	@XmlAttribute
	protected Boolean deliverEntry;

	/**
	 * Gets the value of the deliverLogs property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isDeliverLogs() {
		if (deliverLogs == null) {
			return false;
		} else {
			return deliverLogs;
		}
	}

	/**
	 * Sets the value of the deliverLogs property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setDeliverLogs(Boolean value) {
		this.deliverLogs = value;
	}

	/**
	 * Gets the value of the deliverEntry property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isDeliverEntry() {
		if (deliverEntry == null) {
			return false;
		} else {
			return deliverEntry;
		}
	}

	/**
	 * Sets the value of the deliverEntry property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setDeliverEntry(Boolean value) {
		this.deliverEntry = value;
	}
}
