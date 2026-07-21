/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.atom.app;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.ibm.di.web.common.atom.AtomCategory;
import com.ibm.di.web.common.atom.AtomText;

/**
 * Represents an AtomPub collection element.
 * Custom implementation to avoid OSGi/JAXB classloading issues with Apache Wink.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "collection", namespace = "http://www.w3.org/2007/app", propOrder = {
    "title", "accept", "categories"
})
public class AppCollection {
    
    @XmlAttribute(required = true)
    private String href;
    
    @XmlElement(namespace = "http://www.w3.org/2005/Atom", required = true)
    private AtomText title;
    
    @XmlElement(namespace = "http://www.w3.org/2007/app")
    private List<String> accept;
    
    @XmlElement(namespace = "http://www.w3.org/2007/app")
    private AppCategories categories;
    
    public AppCollection() {
    }
    
    public String getHref() {
        return href;
    }
    
    public void setHref(String href) {
        this.href = href;
    }
    
    public AtomText getTitle() {
        return title;
    }
    
    public void setTitle(AtomText title) {
        this.title = title;
    }
    
    public List<String> getAccept() {
        if (accept == null) {
            accept = new ArrayList<String>();
        }
        return accept;
    }
    
    public void setAccept(List<String> accept) {
        this.accept = accept;
    }
    
    public AppCategories getCategories() {
        return categories;
    }
    
    public void setCategories(AppCategories categories) {
        this.categories = categories;
    }
}

/**
 * Represents an AtomPub categories element.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "categories", namespace = "http://www.w3.org/2007/app")
class AppCategories {
    
    @XmlAttribute
    private String href;
    
    @XmlAttribute
    private String fixed;
    
    @XmlAttribute
    private String scheme;
    
    @XmlElement(namespace = "http://www.w3.org/2005/Atom")
    private List<AtomCategory> category;
    
    public AppCategories() {
    }
    
    public String getHref() {
        return href;
    }
    
    public void setHref(String href) {
        this.href = href;
    }
    
    public String getFixed() {
        return fixed;
    }
    
    public void setFixed(String fixed) {
        this.fixed = fixed;
    }
    
    public String getScheme() {
        return scheme;
    }
    
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
    
    public List<AtomCategory> getCategory() {
        if (category == null) {
            category = new ArrayList<AtomCategory>();
        }
        return category;
    }
    
    public void setCategory(List<AtomCategory> category) {
        this.category = category;
    }
}