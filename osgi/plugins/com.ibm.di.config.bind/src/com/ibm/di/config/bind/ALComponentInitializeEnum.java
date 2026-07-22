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
 * <p>Java class for ALComponentInitializeEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ALComponentInitializeEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="onStartup"/>
 *     &lt;enumeration value="onConfigModify"/>
 *     &lt;enumeration value="onFirstUse"/>
 *     &lt;enumeration value="onEveryUse"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ALComponentInitializeEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlEnum
public enum ALComponentInitializeEnum {

    @XmlEnumValue("onStartup")
    ON_STARTUP("onStartup"),
    @XmlEnumValue("onConfigModify")
    ON_CONFIG_MODIFY("onConfigModify"),
    @XmlEnumValue("onFirstUse")
    ON_FIRST_USE("onFirstUse"),
    @XmlEnumValue("onEveryUse")
    ON_EVERY_USE("onEveryUse");
    private final String value;

    ALComponentInitializeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ALComponentInitializeEnum fromValue(String v) {
        for (ALComponentInitializeEnum c: ALComponentInitializeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
