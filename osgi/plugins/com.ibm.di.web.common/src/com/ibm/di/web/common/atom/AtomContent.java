/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.atom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * Represents an Atom content element.
 * Custom implementation to avoid OSGi/JAXB classloading issues with Apache Wink.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "content", namespace = "http://www.w3.org/2005/Atom")
@XmlRootElement(name = "content", namespace = "http://www.w3.org/2005/Atom")
public class AtomContent {
    
    @XmlAttribute
    private String type;
    
    @XmlAttribute
    private String src;
    
    @XmlValue
    private Object value;
    
    public AtomContent() {
    }
    
    public AtomContent(String type, Object value) {
        this.type = type;
        this.value = value;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSrc() {
        return src;
    }
    
    public void setSrc(String src) {
        this.src = src;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
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