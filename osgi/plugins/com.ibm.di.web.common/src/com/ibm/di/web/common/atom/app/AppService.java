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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents an AtomPub service document.
 * Custom implementation to avoid OSGi/JAXB classloading issues with Apache Wink.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "service", namespace = "http://www.w3.org/2007/app", propOrder = {
    "workspace"
})
@XmlRootElement(name = "service", namespace = "http://www.w3.org/2007/app")
public class AppService {
    
    @XmlElement(namespace = "http://www.w3.org/2007/app")
    private List<AppWorkspace> workspace;
    
    public AppService() {
    }
    
    public List<AppWorkspace> getWorkspace() {
        if (workspace == null) {
            workspace = new ArrayList<AppWorkspace>();
        }
        return workspace;
    }
    
    public void setWorkspace(List<AppWorkspace> workspace) {
        this.workspace = workspace;
    }
    
    /**
     * Get JAXB unmarshaller (Wink compatibility method).
     * For compatibility with unit tests - not implemented, returns null.
     */
    public static Object getUnmarshaller() {
        return null;
    }
}