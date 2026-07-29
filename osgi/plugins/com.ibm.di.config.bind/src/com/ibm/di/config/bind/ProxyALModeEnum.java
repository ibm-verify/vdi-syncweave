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
 * <p>Java class for ProxyALModeEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ProxyALModeEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Sync"/>
 *     &lt;enumeration value="Async"/>
 *     &lt;enumeration value="Manual"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ProxyALModeEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlEnum
public enum ProxyALModeEnum {

    @XmlEnumValue("Sync")
    SYNC("Sync"),
    @XmlEnumValue("Async")
    ASYNC("Async"),
    @XmlEnumValue("Manual")
    MANUAL("Manual");
    private final String value;

    ProxyALModeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ProxyALModeEnum fromValue(String v) {
        for (ProxyALModeEnum c: ProxyALModeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
