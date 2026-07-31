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
 * <p>Java class for AttributeMapItemTypeEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="AttributeMapItemTypeEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Simple"/>
 *     &lt;enumeration value="Advanced"/>
 *     &lt;enumeration value="Substitution"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "AttributeMapItemTypeEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlEnum
public enum AttributeMapItemTypeEnum {

    @XmlEnumValue("Simple")
    SIMPLE("Simple"),
    @XmlEnumValue("Advanced")
    ADVANCED("Advanced"),
    @XmlEnumValue("Substitution")
    SUBSTITUTION("Substitution");
    private final String value;

    AttributeMapItemTypeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AttributeMapItemTypeEnum fromValue(String v) {
        for (AttributeMapItemTypeEnum c: AttributeMapItemTypeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
