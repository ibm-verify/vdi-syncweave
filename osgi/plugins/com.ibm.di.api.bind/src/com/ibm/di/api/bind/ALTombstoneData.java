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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.ibm.di.config.bind.XMLGregorianCalendarAdapter;

/**
 * <p>
 * Java class for ALTombstoneData complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ALTombstoneData">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/api}TombstoneData">
 *       &lt;sequence>
 *         &lt;element name="statistics" type="{http://www.ibm.com/xmlns/prod/tdi/72/api}TaskStatistics" minOccurs="0"/>
 *         &lt;element name="userMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="alName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="configInstanceId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="startedOn" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ALTombstoneData", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = {
    "statistics",
    "userMessage"
})
@XmlRootElement(name = "alData", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class ALTombstoneData extends TombstoneData {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
	protected TaskStatistics statistics;
    @XmlElement(namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
    protected String userMessage;
	@XmlAttribute
	protected String alName;
	@XmlAttribute
	protected String configInstanceId;
	@XmlAttribute
	@XmlJavaTypeAdapter(value = XMLGregorianCalendarAdapter.class)
	protected Long startedOn;

	/**
	 * Gets the value of the statistics property.
	 * 
	 * @return possible object is {@link TaskStatistics }
	 * 
	 */
	public TaskStatistics getStatistics() {
		return statistics;
	}

	/**
	 * Sets the value of the statistics property.
	 * 
	 * @param value
	 *            allowed object is {@link TaskStatistics }
	 * 
	 */
	public void setStatistics(TaskStatistics value) {
		this.statistics = value;
	}

    /**
     * Gets the value of the userMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserMessage() {
        return userMessage;
    }

    /**
     * Sets the value of the userMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserMessage(String value) {
        this.userMessage = value;
    }

    /**
     * Gets the value of the alName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlName() {
        return alName;
    }

	/**
	 * Sets the value of the alName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAlName(String value) {
		this.alName = value;
	}

	/**
	 * Gets the value of the configInstanceId property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getConfigInstanceId() {
		return configInstanceId;
	}

	/**
	 * Sets the value of the configInstanceId property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setConfigInstanceId(String value) {
		this.configInstanceId = value;
	}

	/**
	 * Gets the value of the startedOn property.
	 * 
	 * @return the time as Long
	 * 
	 */
	public Long getStartedOn() {
		return startedOn;
	}

	/**
	 * Sets the value of the startedOn property.
	 * 
	 * @param value
	 *            allowed object is {@link Long }
	 * 
	 */
	public void setStartedOn(Long value) {
		this.startedOn = value;
	}

}
