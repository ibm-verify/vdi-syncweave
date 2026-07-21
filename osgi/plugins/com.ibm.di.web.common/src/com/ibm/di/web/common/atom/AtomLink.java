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

/**
 * Represents an Atom link element.
 * Custom implementation to avoid OSGi/JAXB classloading issues with Apache Wink.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "link", namespace = "http://www.w3.org/2005/Atom")
@XmlRootElement(name = "link", namespace = "http://www.w3.org/2005/Atom")
public class AtomLink {
    
    @XmlAttribute(required = true)
    private String href;
    
    @XmlAttribute
    private String rel;
    
    @XmlAttribute
    private String type;
    
    @XmlAttribute
    private String hreflang;
    
    @XmlAttribute
    private String title;
    
    @XmlAttribute
    private Long length;
    
    public AtomLink() {
    }
    
    public AtomLink(String href, String rel, String type) {
        this.href = href;
        this.rel = rel;
        this.type = type;
    }
    
    public String getHref() {
        return href;
    }
    
    public void setHref(String href) {
        this.href = href;
    }
    
    public String getRel() {
        return rel;
    }
    
    public void setRel(String rel) {
        this.rel = rel;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getHreflang() {
        return hreflang;
    }
    
    public void setHreflang(String hreflang) {
        this.hreflang = hreflang;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public Long getLength() {
        return length;
    }
    
    public void setLength(Long length) {
        this.length = length;
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