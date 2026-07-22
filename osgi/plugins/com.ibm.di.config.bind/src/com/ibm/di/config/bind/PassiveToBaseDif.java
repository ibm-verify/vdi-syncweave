/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for PassiveToBaseDif.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PassiveToBaseDif">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Passive"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PassiveToBaseDif", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlEnum
public enum PassiveToBaseDif {

    @XmlEnumValue("Passive")
    PASSIVE("Passive");
    private final String value;

    PassiveToBaseDif(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PassiveToBaseDif fromValue(String v) {
        for (PassiveToBaseDif c: PassiveToBaseDif.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
