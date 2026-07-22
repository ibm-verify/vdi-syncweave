/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for LogListener complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LogListener">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/api}Listener">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LogListener", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
@XmlSeeAlso({
    AssemblyLineListener.class
})
@XmlRootElement(name = "logListener", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
public class LogListener
    extends Listener
{
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

}
