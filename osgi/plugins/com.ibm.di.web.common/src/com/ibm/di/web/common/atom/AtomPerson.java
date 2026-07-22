/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.atom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents an Atom person construct (used for author, contributor).
 * Custom implementation to avoid OSGi/JAXB classloading issues with Apache Wink.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "person", namespace = "http://www.w3.org/2005/Atom", propOrder = {
    "name", "uri", "email"
})
public class AtomPerson {
    
    @XmlElement(required = true)
    private String name;
    
    @XmlElement
    private String uri;
    
    @XmlElement
    private String email;
    
    public AtomPerson() {
    }
    
    public AtomPerson(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Get any additional elements (for extensibility).
     * For compatibility with unit tests - returns empty list.
     */
    public java.util.List<Object> getAny() {
        return new java.util.ArrayList<Object>();
    }
}