/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * 				This is the AssemblyLine config object.
 * 			
 * 
 * <p>Java class for AssemblyLineBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AssemblyLineBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/tdi/72/config}NamedBinding">
 *       &lt;sequence>
 *         &lt;element name="settings" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ParametersBinding" minOccurs="0"/>
 *         &lt;element name="hooks" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}HooksBinding" minOccurs="0"/>
 *         &lt;element name="sandbox" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ALSandboxBinding" minOccurs="0"/>
 *         &lt;element name="simulation" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ALSimulationBinding" minOccurs="0"/>
 *         &lt;element name="logging" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}LogBinding" minOccurs="0"/>
 *         &lt;element name="threading" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ParametersBinding" minOccurs="0"/>
 *         &lt;element name="operations" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ALOperationsBinding" minOccurs="0"/>
 *         &lt;element name="initParams" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ALInitParamsBinding" minOccurs="0"/>
 *         &lt;element name="null" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}NullBinding" minOccurs="0"/>
 *         &lt;element name="container" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}ALComponentsBinding" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssemblyLineBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config", propOrder = {
    "settings",
    "hooks",
    "sandbox",
    "simulation",
    "logging",
    "threading",
    "operations",
    "initParams",
    "_null",
    "container"
})
@XmlRootElement(name = "assemblyLine", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class AssemblyLineBinding
    extends NamedBinding implements Serializable
{

	private static final long serialVersionUID = -5519998227032580012L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    protected ParametersBinding settings;
    protected HooksBinding hooks;
    protected ALSandboxBinding sandbox;
    protected ALSimulationBinding simulation;
    protected LogBinding logging;
    protected ParametersBinding threading;
    protected ALOperationsBinding operations;
    protected ALInitParamsBinding initParams;
    @XmlElement(name = "null")
    protected NullBinding _null;
    @XmlElement(name="container")
    protected List<ALComponentsBinding> container;

    /**
     * Gets the value of the settings property.
     * 
     * @return
     *     possible object is
     *     {@link ParametersBinding }
     *     
     */
    public ParametersBinding getSettings() {
        return settings;
    }

    /**
     * Sets the value of the settings property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParametersBinding }
     *     
     */
    public void setSettings(ParametersBinding value) {
        this.settings = value;
    }

    /**
     * Gets the value of the hooks property.
     * 
     * @return
     *     possible object is
     *     {@link HooksBinding }
     *     
     */
    public HooksBinding getHooks() {
        return hooks;
    }

    /**
     * Sets the value of the hooks property.
     * 
     * @param value
     *     allowed object is
     *     {@link HooksBinding }
     *     
     */
    public void setHooks(HooksBinding value) {
        this.hooks = value;
    }

    /**
     * Gets the value of the sandbox property.
     * 
     * @return
     *     possible object is
     *     {@link ALSandboxBinding }
     *     
     */
    public ALSandboxBinding getSandbox() {
        return sandbox;
    }

    /**
     * Sets the value of the sandbox property.
     * 
     * @param value
     *     allowed object is
     *     {@link ALSandboxBinding }
     *     
     */
    public void setSandbox(ALSandboxBinding value) {
        this.sandbox = value;
    }

    /**
     * Gets the value of the simulation property.
     * 
     * @return
     *     possible object is
     *     {@link ALSimulationBinding }
     *     
     */
    public ALSimulationBinding getSimulation() {
        return simulation;
    }

    /**
     * Sets the value of the simulation property.
     * 
     * @param value
     *     allowed object is
     *     {@link ALSimulationBinding }
     *     
     */
    public void setSimulation(ALSimulationBinding value) {
        this.simulation = value;
    }

    /**
     * Gets the value of the logging property.
     * 
     * @return
     *     possible object is
     *     {@link LogBinding }
     *     
     */
    public LogBinding getLogging() {
        return logging;
    }

    /**
     * Sets the value of the logging property.
     * 
     * @param value
     *     allowed object is
     *     {@link LogBinding }
     *     
     */
    public void setLogging(LogBinding value) {
        this.logging = value;
    }

    /**
     * Gets the value of the threading property.
     * 
     * @return
     *     possible object is
     *     {@link ParametersBinding }
     *     
     */
    public ParametersBinding getThreading() {
        return threading;
    }

    /**
     * Sets the value of the threading property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParametersBinding }
     *     
     */
    public void setThreading(ParametersBinding value) {
        this.threading = value;
    }

    /**
     * Gets the value of the operations property.
     * 
     * @return
     *     possible object is
     *     {@link ALOperationsBinding }
     *     
     */
    public ALOperationsBinding getOperations() {
        return operations;
    }

    /**
     * Sets the value of the operations property.
     * 
     * @param value
     *     allowed object is
     *     {@link ALOperationsBinding }
     *     
     */
    public void setOperations(ALOperationsBinding value) {
        this.operations = value;
    }

    /**
     * Gets the value of the initParams property.
     * 
     * @return
     *     possible object is
     *     {@link ALInitParamsBinding }
     *     
     */
    public ALInitParamsBinding getInitParams() {
        return initParams;
    }

    /**
     * Sets the value of the initParams property.
     * 
     * @param value
     *     allowed object is
     *     {@link ALInitParamsBinding }
     *     
     */
    public void setInitParams(ALInitParamsBinding value) {
        this.initParams = value;
    }

    /**
     * Gets the value of the null property.
     * 
     * @return
     *     possible object is
     *     {@link NullBinding }
     *     
     */
    public NullBinding getNull() {
        return _null;
    }

    /**
     * Sets the value of the null property.
     * 
     * @param value
     *     allowed object is
     *     {@link NullBinding }
     *     
     */
    public void setNull(NullBinding value) {
        this._null = value;
    }

    /**
     * Gets the value of the container property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the container property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContainer().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ALComponentsBinding }
     * 
     * 
     */
    public List<ALComponentsBinding> getContainers() {
        if (container == null) {
            container = new ArrayList<ALComponentsBinding>();
        }
        return this.container;
    }

}
