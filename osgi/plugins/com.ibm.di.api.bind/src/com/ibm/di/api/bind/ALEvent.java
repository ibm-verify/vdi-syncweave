/*
 * Copyright contributors to the SyncWeave project
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
 * Java class for ALEvent complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ALEvent">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/api}DIEvent">
 *       &lt;sequence>
 *         &lt;element name="taskStatistics" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}TaskStatistics"/>
 *       &lt;/sequence>
 *       &lt;attribute name="alGuid" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ALEvent", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = { "taskStatistics" })
@XmlRootElement(name = "alEvent", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class ALEvent extends DIEvent {

	private static final long serialVersionUID = -2624911605368781066L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", required = true)
	protected TaskStatistics taskStatistics;
	@XmlAttribute(required = true)
	protected String alGuid;

	/**
	 * Gets the value of the taskStatistics property.
	 * 
	 * @return possible object is {@link TaskStatistics }
	 * 
	 */
	public TaskStatistics getTaskStatistics() {
		return taskStatistics;
	}

	/**
	 * Sets the value of the taskStatistics property.
	 * 
	 * @param value
	 *            allowed object is {@link TaskStatistics }
	 * 
	 */
	public void setTaskStatistics(TaskStatistics value) {
		this.taskStatistics = value;
	}

	/**
	 * Gets the value of the alGuid property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getAlGuid() {
		return alGuid;
	}

	/**
	 * Sets the value of the alGuid property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAlGuid(String value) {
		this.alGuid = value;
	}

}
