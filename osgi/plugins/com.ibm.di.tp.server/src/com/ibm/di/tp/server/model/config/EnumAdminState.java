/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import com.ibm.di.tp.server.Constants;

/**
 * <p>Java class for enum_AdminState.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enum_AdminState">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="enabled"/>
 *     &lt;enumeration value="disabled"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enum_AdminState", namespace = Constants.NS_SCMP)
@XmlEnum
public enum EnumAdminState {

    @XmlEnumValue("enabled")
    ENABLED("enabled"),
    @XmlEnumValue("disabled")
    DISABLED("disabled");
    private final String value;

    EnumAdminState(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumAdminState fromValue(String v) {
        for (EnumAdminState c: EnumAdminState.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
