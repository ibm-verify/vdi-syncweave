/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for LoopBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LoopBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}CompositeComponentBinding">
 *       &lt;choice>
 *         &lt;element name="whileCondition" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ConditionBinding" minOccurs="0"/>
 *         &lt;element name="connectorCondition" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ConnectorLoopBinding" minOccurs="0"/>
 *         &lt;element name="collectionCondition" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}CollectionLoopBinding" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LoopBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "whileCondition",
    "connectorCondition",
    "collectionCondition"
})
@XmlRootElement(name = "loop", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class LoopBinding
    extends CompositeComponentBinding implements Serializable
{

	private static final long serialVersionUID = -2913773841249077901L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    protected ConditionBinding whileCondition;
    protected ConnectorLoopBinding connectorCondition;
    protected CollectionLoopBinding collectionCondition;

    /**
     * Gets the value of the whileCondition property.
     * 
     * @return
     *     possible object is
     *     {@link ConditionBinding }
     *     
     */
    public ConditionBinding getWhileCondition() {
        return whileCondition;
    }

    /**
     * Sets the value of the whileCondition property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConditionBinding }
     *     
     */
    public void setWhileCondition(ConditionBinding value) {
        this.whileCondition = value;
    }

    /**
     * Gets the value of the connectorCondition property.
     * 
     * @return
     *     possible object is
     *     {@link ConnectorLoopBinding }
     *     
     */
    public ConnectorLoopBinding getConnectorCondition() {
        return connectorCondition;
    }

    /**
     * Sets the value of the connectorCondition property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConnectorLoopBinding }
     *     
     */
    public void setConnectorCondition(ConnectorLoopBinding value) {
        this.connectorCondition = value;
    }

    /**
     * Gets the value of the collectionCondition property.
     * 
     * @return
     *     possible object is
     *     {@link CollectionLoopBinding }
     *     
     */
    public CollectionLoopBinding getCollectionCondition() {
        return collectionCondition;
    }

    /**
     * Sets the value of the collectionCondition property.
     * 
     * @param value
     *     allowed object is
     *     {@link CollectionLoopBinding }
     *     
     */
    public void setCollectionCondition(CollectionLoopBinding value) {
        this.collectionCondition = value;
    }

}
