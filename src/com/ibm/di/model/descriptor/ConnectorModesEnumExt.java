/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.model.descriptor;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for ConnectorModesEnumExt.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ConnectorModesEnumExt">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="all"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ConnectorModesEnumExt", namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core")
@XmlEnum
public enum ConnectorModesEnumExt {

    @XmlEnumValue("all")
    ALL("all");
    private final String value;

    ConnectorModesEnumExt(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ConnectorModesEnumExt fromValue(String v) {
        for (ConnectorModesEnumExt c: ConnectorModesEnumExt.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
