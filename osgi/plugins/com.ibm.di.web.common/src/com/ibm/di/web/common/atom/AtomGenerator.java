/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.atom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * Represents an Atom generator element.
 * Custom implementation to avoid OSGi/JAXB classloading issues with Apache Wink.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "generator", namespace = "http://www.w3.org/2005/Atom")
public class AtomGenerator {
    
    @XmlAttribute
    private String uri;
    
    @XmlAttribute
    private String version;
    
    @XmlValue
    private String value;
    
    public AtomGenerator() {
    }
    
    public AtomGenerator(String value) {
        this.value = value;
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
}