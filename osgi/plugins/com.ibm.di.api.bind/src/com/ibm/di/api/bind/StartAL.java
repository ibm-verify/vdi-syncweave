/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for StartAL complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="StartAL">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/api}assemblyLineListener" minOccurs="0"/>
 *         &lt;element name="iwe" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}Entry" minOccurs="0"/>
 *         &lt;element name="tcb" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}TaskCallBlock" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="sync" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="manual" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StartAL", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = { "assemblyLineListener", "iwe",
		"tcb" })
@XmlRootElement(name = "startAL", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class StartAL {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	protected AssemblyLineListener assemblyLineListener;
	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
	protected Entry iwe;
	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
	protected TaskCallBlock tcb;
	@XmlAttribute(required = true)
	protected String name;
	@XmlAttribute
	protected Boolean sync;
	@XmlAttribute
	protected Boolean manual;

	/**
	 * Gets the value of the logListener property.
	 * 
	 * @return
	 */
	public AssemblyLineListener getAssemblyLineListener() {
		return assemblyLineListener;
	}

	/**
	 * Sets the value of the logListener property.
	 * 
	 * @param value
	 */
	public void setAssemblyLineListener(AssemblyLineListener value) {
		this.assemblyLineListener = value;
	}

	/**
	 * Gets the value of the iwe property.
	 * 
	 * @return possible object is {@link Entry }
	 * 
	 */
	public Entry getIwe() {
		return iwe;
	}

	/**
	 * Sets the value of the iwe property.
	 * 
	 * @param value
	 *            allowed object is {@link Entry }
	 * 
	 */
	public void setIwe(Entry value) {
		this.iwe = value;
	}

	/**
	 * Gets the value of the tcb property.
	 * 
	 * @return possible object is {@link TaskCallBlock }
	 * 
	 */
	public TaskCallBlock getTcb() {
		return tcb;
	}

	/**
	 * Sets the value of the tcb property.
	 * 
	 * @param value
	 *            allowed object is {@link TaskCallBlock }
	 * 
	 */
	public void setTcb(TaskCallBlock value) {
		this.tcb = value;
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
	 * Gets the value of the sync property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isSync() {
		if (sync == null) {
			return false;
		} else {
			return sync;
		}
	}

	/**
	 * Sets the value of the sync property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setSync(Boolean value) {
		this.sync = value;
	}

	/**
	 * Gets the value of the manual property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isManual() {
		if (manual == null) {
			return false;
		} else {
			return manual;
		}
	}

	/**
	 * Sets the value of the manual property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setManual(Boolean value) {
		this.manual = value;
	}

}
