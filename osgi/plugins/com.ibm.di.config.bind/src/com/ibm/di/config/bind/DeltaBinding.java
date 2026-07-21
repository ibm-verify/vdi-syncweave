/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for DeltaBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeltaBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="uniqueAttribute" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="deltaDb" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="readDeleted" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="removeDeleted" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="returnUnchanged" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="fasterAlgorithm" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="allowDuplicateKeys" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="commit" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}DeltaCommitEnum" default="onEveryOp" />
 *       &lt;attribute name="rowLocking" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}DeltaRowLockingEnum" default="serializable" />
 *       &lt;attribute name="changeDetectionMode" type="{http://www.ibm.com/xmlns/prod/tdi/72/config}DeltaChangeDetectionModeEnum" default="detectAll" />
 *       &lt;attribute name="changeDetectionAttributes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeltaBinding", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
public class DeltaBinding implements Serializable {

	private static final long serialVersionUID = 7195980777895124501L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

    @XmlAttribute
    protected Boolean enabled;
    @XmlAttribute
    protected String uniqueAttribute;
    @XmlAttribute
    protected String deltaDb;
    @XmlAttribute
    protected Boolean readDeleted;
    @XmlAttribute
    protected Boolean removeDeleted;
    @XmlAttribute
    protected Boolean returnUnchanged;
    @XmlAttribute
    protected Boolean fasterAlgorithm;
    @XmlAttribute
    protected Boolean allowDuplicateKeys;
    @XmlAttribute
    protected DeltaCommitEnum commit;
    @XmlAttribute
    protected DeltaRowLockingEnum rowLocking;
    @XmlAttribute
    protected DeltaChangeDetectionModeEnum changeDetectionMode;
    @XmlAttribute
    protected String changeDetectionAttributes;

    /**
     * Gets the value of the enabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEnabled() {
        if (enabled == null) {
            return false;
        } else {
            return enabled;
        }
    }

    /**
     * Sets the value of the enabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnabled(Boolean value) {
        this.enabled = value;
    }

    /**
     * Gets the value of the uniqueAttribute property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUniqueAttribute() {
        return uniqueAttribute;
    }

    /**
     * Sets the value of the uniqueAttribute property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUniqueAttribute(String value) {
        this.uniqueAttribute = value;
    }

    /**
     * Gets the value of the deltaDb property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeltaDb() {
        return deltaDb;
    }

    /**
     * Sets the value of the deltaDb property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeltaDb(String value) {
        this.deltaDb = value;
    }

    /**
     * Gets the value of the readDeleted property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isReadDeleted() {
        if (readDeleted == null) {
            return false;
        } else {
            return readDeleted;
        }
    }

    /**
     * Sets the value of the readDeleted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setReadDeleted(Boolean value) {
        this.readDeleted = value;
    }

    /**
     * Gets the value of the removeDeleted property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRemoveDeleted() {
        if (removeDeleted == null) {
            return false;
        } else {
            return removeDeleted;
        }
    }

    /**
     * Sets the value of the removeDeleted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRemoveDeleted(Boolean value) {
        this.removeDeleted = value;
    }

    /**
     * Gets the value of the returnUnchanged property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isReturnUnchanged() {
        if (returnUnchanged == null) {
            return false;
        } else {
            return returnUnchanged;
        }
    }

    /**
     * Sets the value of the returnUnchanged property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setReturnUnchanged(Boolean value) {
        this.returnUnchanged = value;
    }

    /**
     * Gets the value of the fasterAlgorithm property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFasterAlgorithm() {
        if (fasterAlgorithm == null) {
            return false;
        } else {
            return fasterAlgorithm;
        }
    }

    /**
     * Sets the value of the fasterAlgorithm property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFasterAlgorithm(Boolean value) {
        this.fasterAlgorithm = value;
    }

    /**
     * Gets the value of the allowDuplicateKeys property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAllowDuplicateKeys() {
        return allowDuplicateKeys;
    }

    /**
     * Sets the value of the allowDuplicateKeys property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAllowDuplicateKeys(Boolean value) {
        this.allowDuplicateKeys = value;
    }

    /**
     * Gets the value of the commit property.
     * 
     * @return
     *     possible object is
     *     {@link DeltaCommitEnum }
     *     
     */
    public DeltaCommitEnum getCommit() {
        if (commit == null) {
            return DeltaCommitEnum.ON_EVERY_OP;
        } else {
            return commit;
        }
    }

    /**
     * Sets the value of the commit property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeltaCommitEnum }
     *     
     */
    public void setCommit(DeltaCommitEnum value) {
        this.commit = value;
    }

    /**
     * Gets the value of the rowLocking property.
     * 
     * @return
     *     possible object is
     *     {@link DeltaRowLockingEnum }
     *     
     */
    public DeltaRowLockingEnum getRowLocking() {
        if (rowLocking == null) {
            return DeltaRowLockingEnum.SERIALIZABLE;
        } else {
            return rowLocking;
        }
    }

    /**
     * Sets the value of the rowLocking property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeltaRowLockingEnum }
     *     
     */
    public void setRowLocking(DeltaRowLockingEnum value) {
        this.rowLocking = value;
    }

    /**
     * Gets the value of the changeDetectionMode property.
     * 
     * @return
     *     possible object is
     *     {@link DeltaChangeDetectionModeEnum }
     *     
     */
    public DeltaChangeDetectionModeEnum getChangeDetectionMode() {
        if (changeDetectionMode == null) {
            return DeltaChangeDetectionModeEnum.DETECT_ALL;
        } else {
            return changeDetectionMode;
        }
    }

    /**
     * Sets the value of the changeDetectionMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeltaChangeDetectionModeEnum }
     *     
     */
    public void setChangeDetectionMode(DeltaChangeDetectionModeEnum value) {
        this.changeDetectionMode = value;
    }

    /**
     * Gets the value of the changeDetectionAttributes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChangeDetectionAttributes() {
        return changeDetectionAttributes;
    }

    /**
     * Sets the value of the changeDetectionAttributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChangeDetectionAttributes(String value) {
        this.changeDetectionAttributes = value;
    }

}
