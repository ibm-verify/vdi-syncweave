/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.atom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents an Atom category element.
 * Custom implementation to avoid OSGi/JAXB classloading issues with Apache Wink.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "category", namespace = "http://www.w3.org/2005/Atom")
@XmlRootElement(name = "category", namespace = "http://www.w3.org/2005/Atom")
public class AtomCategory {
    
    @XmlAttribute(required = true)
    private String term;
    
    @XmlAttribute
    private String scheme;
    
    @XmlAttribute
    private String label;
    
    public AtomCategory() {
    }
    
    public AtomCategory(String term, String scheme) {
        this.term = term;
        this.scheme = scheme;
    }
    
    public String getTerm() {
        return term;
    }
    
    public void setTerm(String term) {
        this.term = term;
    }
    
    public String getScheme() {
        return scheme;
    }
    
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
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
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AtomCategory that = (AtomCategory) obj;
        
        if (term != null ? !term.equals(that.term) : that.term != null) return false;
        return scheme != null ? scheme.equals(that.scheme) : that.scheme == null;
    }
    
    @Override
    public int hashCode() {
        int result = term != null ? term.hashCode() : 0;
        result = 31 * result + (scheme != null ? scheme.hashCode() : 0);
        return result;
    }
}