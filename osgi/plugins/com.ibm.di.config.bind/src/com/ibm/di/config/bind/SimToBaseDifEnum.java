/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.bind;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for SimToBaseDifEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SimToBaseDifEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Simulated"/>
 *     &lt;enumeration value="Proxy"/>
 *     &lt;enumeration value="Scripted"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SimToBaseDifEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlEnum
public enum SimToBaseDifEnum {

    @XmlEnumValue("Simulated")
    SIMULATED("Simulated"),
    @XmlEnumValue("Proxy")
    PROXY("Proxy"),
    @XmlEnumValue("Scripted")
    SCRIPTED("Scripted");
    private final String value;

    SimToBaseDifEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SimToBaseDifEnum fromValue(String v) {
        for (SimToBaseDifEnum c: SimToBaseDifEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
