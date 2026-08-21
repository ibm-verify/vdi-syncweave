/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.atom;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.w3c.dom.Element;

/**
 * Represents an Atom entry element.
 * Custom implementation to avoid OSGi/JAXB classloading issues with Apache Wink.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entry", namespace = "http://www.w3.org/2005/Atom", propOrder = {
    "id", "title", "updated", "published", "author", "contributor", 
    "link", "category", "content", "summary", "rights", "source"
})
@XmlRootElement(name = "entry", namespace = "http://www.w3.org/2005/Atom")
public class AtomEntry {
    
    @XmlElement(required = true)
    private String id;
    
    @XmlElement(required = true)
    private AtomText title;
    
    @XmlElement(required = true)
    private Long updated;
    
    @XmlElement
    private Long published;
    
    @XmlElement
    private List<AtomPerson> author;
    
    @XmlElement
    private List<AtomPerson> contributor;
    
    @XmlElement
    private List<AtomLink> link;
    
    @XmlElement
    private List<AtomCategory> category;
    
    @XmlElement
    private AtomContent content;
    
    @XmlElement
    private AtomText summary;
    
    @XmlElement
    private AtomText rights;
    
    @XmlElement
    private Object source;

    @XmlAnyElement
    private List<Element> any;

    @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
    private String base;

    @XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace")
    private String lang;

    public AtomEntry() {
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
    
    public Long getPublished() {
        return published;
    }
    
    public void setPublished(Long published) {
        this.published = published;
    }
    
    public List<AtomPerson> getAuthor() {
        if (author == null) {
            author = new ArrayList<AtomPerson>();
        }
        return author;
    }
    
    public void setAuthor(List<AtomPerson> author) {
        this.author = author;
    }
    
    // Alias for compatibility with unit tests
    public List<AtomPerson> getAuthors() {
        return getAuthor();
    }
    
    public List<AtomPerson> getContributor() {
        if (contributor == null) {
            contributor = new ArrayList<AtomPerson>();
        }
        return contributor;
    }
    
    public void setContributor(List<AtomPerson> contributor) {
        this.contributor = contributor;
    }
    
    // Alias for compatibility with unit tests
    public List<AtomPerson> getContributors() {
        return getContributor();
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
    
    public AtomContent getContent() {
        return content;
    }
    
    public void setContent(AtomContent content) {
        this.content = content;
    }
    
    public AtomText getSummary() {
        return summary;
    }
    
    public void setSummary(AtomText summary) {
        this.summary = summary;
    }
    
    public AtomText getRights() {
        return rights;
    }
    
    public void setRights(AtomText rights) {
        this.rights = rights;
    }
    
    public Object getSource() {
        return source;
    }
    
    public void setSource(Object source) {
        this.source = source;
    }
    
    /**
     * Helper method to get links by relation type.
     * For compatibility with unit tests.
     */
    public List<AtomLink> getLinksByRelation(String rel) {
        List<AtomLink> result = new ArrayList<AtomLink>();
        if (link != null && rel != null) {
            for (AtomLink l : link) {
                if (rel.equals(l.getRel())) {
                    result.add(l);
                }
            }
        }
        return result;
    }
    
    /**
     * Get any additional elements (for extensibility).
     */
    public List<Element> getAny() {
        if (any == null) {
            any = new ArrayList<Element>();
        }
        return any;
    }
    
    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public static Marshaller getMarshaller() {
        try {
            return JAXBContext.newInstance(AtomEntry.class).createMarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static Unmarshaller getUnmarshaller() {
        try {
            return JAXBContext.newInstance(AtomEntry.class).createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}