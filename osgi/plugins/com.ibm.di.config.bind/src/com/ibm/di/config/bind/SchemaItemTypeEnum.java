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
 * <p>Java class for SchemaItemTypeEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SchemaItemTypeEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Attribute"/>
 *     &lt;enumeration value="Property"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SchemaItemTypeEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlEnum
public enum SchemaItemTypeEnum {

    @XmlEnumValue("Attribute")
    ATTRIBUTE("Attribute"),
    @XmlEnumValue("Property")
    PROPERTY("Property");
    private final String value;

    SchemaItemTypeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SchemaItemTypeEnum fromValue(String v) {
        for (SchemaItemTypeEnum c: SchemaItemTypeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
