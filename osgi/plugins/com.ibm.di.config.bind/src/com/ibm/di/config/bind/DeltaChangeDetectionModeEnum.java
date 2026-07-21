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
 * <p>Java class for DeltaChangeDetectionModeEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DeltaChangeDetectionModeEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ignoreAttributes"/>
 *     &lt;enumeration value="detectAttributes"/>
 *     &lt;enumeration value="detectAll"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DeltaChangeDetectionModeEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlEnum
public enum DeltaChangeDetectionModeEnum {

    @XmlEnumValue("ignoreAttributes")
    IGNORE_ATTRIBUTES("ignoreAttributes"),
    @XmlEnumValue("detectAttributes")
    DETECT_ATTRIBUTES("detectAttributes"),
    @XmlEnumValue("detectAll")
    DETECT_ALL("detectAll");
    private final String value;

    DeltaChangeDetectionModeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DeltaChangeDetectionModeEnum fromValue(String v) {
        for (DeltaChangeDetectionModeEnum c: DeltaChangeDetectionModeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
