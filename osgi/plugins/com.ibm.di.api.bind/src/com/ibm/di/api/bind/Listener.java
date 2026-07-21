/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * <p>
 * Java class for Listener complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="Listener">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ibm.com/xmlns/prod/tdi/72/api}channel" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Listener", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api", propOrder = { "channel" })
@XmlSeeAlso( { LogListener.class, DIEventListener.class, ConfigFileListener.class })
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = LogListener.class, name = "logListener"),
    @JsonSubTypes.Type(value = DIEventListener.class, name = "diEventListener"),
    @JsonSubTypes.Type(value = ConfigFileListener.class, name = "configFileListener"),
    @JsonSubTypes.Type(value = AssemblyLineListener.class, name = "assemblyLineListener")
})
public abstract class Listener {

	@XmlElementRef
	protected TransportChannel channel;

	/**
	 * Gets the value of the channel property.
	 * 
	 * @return possible object is {@link TransportChannel } {@link PollChannel }
	 *         {@link PushChannel }
	 * 
	 */
	public TransportChannel getChannel() {
		return channel;
	}

	/**
	 * Sets the value of the channel property.
	 * 
	 * @param value
	 *            allowed object is {@link TransportChannel } {@link PollChannel }
	 *            {@link PushChannel }
	 * 
	 */
	public void setChannel(TransportChannel value) {
		this.channel = value;
	}

}
