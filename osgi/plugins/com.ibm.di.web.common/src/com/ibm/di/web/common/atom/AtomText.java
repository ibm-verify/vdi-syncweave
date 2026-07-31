/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.atom;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

/**
 * Represents an Atom text construct (used for title, summary, etc.).
 * Custom implementation to avoid OSGi/JAXB classloading issues with Apache Wink.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "text", namespace = "http://www.w3.org/2005/Atom")
public class AtomText {
    
    @XmlAttribute
    private String type;
    
    @XmlValue
    private String value;
    
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<>();
    
    public AtomText() {
    }
    
    public AtomText(String value) {
        this.value = value;
        this.type = "text";
    }
    
    public AtomText(String value, String type) {
        this.value = value;
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value == null ? null : value.toString();
    }
    
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }
    
    /**
     * Get base URI attribute.
     * For compatibility with unit tests - returns null (not used in custom implementation).
     */
    public String getBase() {
        return null;
    }
    
    /**
     * Get language attribute.
     * For compatibility with unit tests - returns null (not used in custom implementation).
     */
    public String getLang() {
        return null;
    }
}