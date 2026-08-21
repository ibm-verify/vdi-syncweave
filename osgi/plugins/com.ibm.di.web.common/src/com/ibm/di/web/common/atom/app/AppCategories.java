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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.ibm.di.web.common.atom.AtomCategory;

/**
 * Represents an AtomPub categories element.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "categories", namespace = "http://www.w3.org/2007/app")
public class AppCategories {

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
