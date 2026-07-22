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
 * <p>Java class for LoopTypeEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="LoopTypeEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="While"/>
 *     &lt;enumeration value="Connector"/>
 *     &lt;enumeration value="Collection"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "LoopTypeEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlEnum
public enum LoopTypeEnum {

    @XmlEnumValue("While")
    WHILE("While"),
    @XmlEnumValue("Connector")
    CONNECTOR("Connector"),
    @XmlEnumValue("Collection")
    COLLECTION("Collection");
    private final String value;

    LoopTypeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LoopTypeEnum fromValue(String v) {
        for (LoopTypeEnum c: LoopTypeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
