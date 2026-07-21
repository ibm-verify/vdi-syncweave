/*
 * Copyright IBM Corp. 2025
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
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for TaskStatistics complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="TaskStatistics">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="stat" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}Stat" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="error" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}Exception" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaskStatistics", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = { "stat", "error" })
@XmlRootElement(name = "taskStatistics", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class TaskStatistics implements Serializable {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = -7610288539362861186L;

	@XmlElement(name = "stat", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
	protected List<Stat> stat;
	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
	protected Exception error;

	/**
	 * Gets the value of the stat property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the stat property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getStat().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Stat }
	 * 
	 * 
	 */
	public List<Stat> getStats() {
		if (stat == null) {
			stat = new ArrayList<Stat>();
		}
		return this.stat;
	}

	/**
	 * Gets the value of the error property.
	 * 
	 * @return possible object is {@link Exception }
	 * 
	 */
	public Exception getError() {
		return error;
	}

	/**
	 * Sets the value of the error property.
	 * 
	 * @param value
	 *            allowed object is {@link Exception }
	 * 
	 */
	public void setError(Exception value) {
		this.error = value;
	}

}
