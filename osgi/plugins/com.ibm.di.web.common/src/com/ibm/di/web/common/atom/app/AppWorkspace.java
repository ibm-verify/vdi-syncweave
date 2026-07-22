/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.atom.app;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.ibm.di.web.common.atom.AtomText;

/**
 * Represents an AtomPub workspace element.
 * Custom implementation to avoid OSGi/JAXB classloading issues with Apache Wink.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "workspace", namespace = "http://www.w3.org/2007/app", propOrder = {
    "title", "collection"
})
public class AppWorkspace {
    
    @XmlElement(namespace = "http://www.w3.org/2005/Atom", required = true)
    private AtomText title;
    
    @XmlElement(namespace = "http://www.w3.org/2007/app")
    private List<AppCollection> collection;
    
    public AppWorkspace() {
    }
    
    public AtomText getTitle() {
        return title;
    }
    
    public void setTitle(AtomText title) {
        this.title = title;
    }
    
    public List<AppCollection> getCollection() {
        if (collection == null) {
            collection = new ArrayList<AppCollection>();
        }
        return collection;
    }
    
    public void setCollection(List<AppCollection> collection) {
        this.collection = collection;
    }
}