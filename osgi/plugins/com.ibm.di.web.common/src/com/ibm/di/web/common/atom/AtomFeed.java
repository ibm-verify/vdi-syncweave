/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.atom;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents an Atom feed element.
 * Custom implementation to avoid OSGi/JAXB classloading issues with Apache Wink.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "feed", namespace = "http://www.w3.org/2005/Atom", propOrder = {
    "id", "title", "updated", "author", "link", "category", "contributor",
    "generator", "icon", "logo", "rights", "subtitle", "entry"
})
@XmlRootElement(name = "feed", namespace = "http://www.w3.org/2005/Atom")
public class AtomFeed {
    
    @XmlElement(required = true)
    private String id;
    
    @XmlElement(required = true)
    private AtomText title;
    
    @XmlElement(required = true)
    private Long updated;
    
    @XmlElement
    private List<AtomPerson> author;
    
    @XmlElement
    private List<AtomLink> link;
    
    @XmlElement
    private List<AtomCategory> category;
    
    @XmlElement
    private List<AtomPerson> contributor;
    
    @XmlElement
    private AtomGenerator generator;
    
    @XmlElement
    private String icon;
    
    @XmlElement
    private String logo;
    
    @XmlElement
    private AtomText rights;
    
    @XmlElement
    private AtomText subtitle;
    
    @XmlElement
    private List<AtomEntry> entry;
    
    public AtomFeed() {
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public AtomText getTitle() {
        return title;
    }
    
    public void setTitle(AtomText title) {
        this.title = title;
    }
    
    public Long getUpdated() {
        return updated;
    }
    
    public void setUpdated(Long updated) {
        this.updated = updated;
    }
    
    public List<AtomPerson> getAuthors() {
        if (author == null) {
            author = new ArrayList<AtomPerson>();
        }
        return author;
    }
    
    public void setAuthors(List<AtomPerson> author) {
        this.author = author;
    }
    
    public List<AtomLink> getLinks() {
        if (link == null) {
            link = new ArrayList<AtomLink>();
        }
        return link;
    }
    
    public void setLinks(List<AtomLink> link) {
        this.link = link;
    }
    
    public List<AtomCategory> getCategories() {
        if (category == null) {
            category = new ArrayList<AtomCategory>();
        }
        return category;
    }
    
    public void setCategories(List<AtomCategory> category) {
        this.category = category;
    }
    
    public List<AtomPerson> getContributors() {
        if (contributor == null) {
            contributor = new ArrayList<AtomPerson>();
        }
        return contributor;
    }
    
    public void setContributors(List<AtomPerson> contributor) {
        this.contributor = contributor;
    }
    
    public AtomGenerator getGenerator() {
        return generator;
    }
    
    public void setGenerator(AtomGenerator generator) {
        this.generator = generator;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getLogo() {
        return logo;
    }
    
    public void setLogo(String logo) {
        this.logo = logo;
    }
    
    public AtomText getRights() {
        return rights;
    }
    
    public void setRights(AtomText rights) {
        this.rights = rights;
    }
    
    public AtomText getSubtitle() {
        return subtitle;
    }
    
    public void setSubtitle(AtomText subtitle) {
        this.subtitle = subtitle;
    }
    
    public List<AtomEntry> getEntries() {
        if (entry == null) {
            entry = new ArrayList<AtomEntry>();
        }
        return entry;
    }
    
    public void setEntries(List<AtomEntry> entry) {
        this.entry = entry;
    }
    
    /**
     * Get OpenSearch itemsPerPage element.
     * For compatibility with unit tests - returns null (not implemented in custom version).
     */
    public Integer getItemsPerPage() {
        return null;
    }
    
    /**
     * Get OpenSearch startIndex element.
     * For compatibility with unit tests - returns null (not implemented in custom version).
     */
    public Integer getStartIndex() {
        return null;
    }
    
    /**
     * Get OpenSearch totalResults element.
     * For compatibility with unit tests - returns null (not implemented in custom version).
     */
    public Integer getTotalResults() {
        return null;
    }
    
    /**
     * Get base URI attribute.
     * For compatibility with unit tests - returns null (not used in custom implementation).
     */
    public String getBase() {
        return null;
    }
    
    /**
     * Convert to Synd feed (Wink compatibility method).
     * For compatibility with unit tests - not implemented, returns null.
     */
    public Object toSynd(Object syndFeed) {
        return null;
    }
    
    /**
     * Get JAXB marshaller (Wink compatibility method).
     * For compatibility with unit tests - not implemented, returns null.
     */
    public static Object getMarshaller() {
        return null;
    }
    
    /**
     * Get JAXB unmarshaller (Wink compatibility method).
     * For compatibility with unit tests - not implemented, returns null.
     */
    public static Object getUnmarshaller() {
        return null;
    }
}