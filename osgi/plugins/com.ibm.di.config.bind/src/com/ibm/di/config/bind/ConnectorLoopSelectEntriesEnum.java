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
 * <p>Java class for ConnectorLoopSelectEntriesEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ConnectorLoopSelectEntriesEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="onInitialize"/>
 *     &lt;enumeration value="onEveryUse"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ConnectorLoopSelectEntriesEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlEnum
public enum ConnectorLoopSelectEntriesEnum {

    @XmlEnumValue("onInitialize")
    ON_INITIALIZE("onInitialize"),
    @XmlEnumValue("onEveryUse")
    ON_EVERY_USE("onEveryUse");
    private final String value;

    ConnectorLoopSelectEntriesEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ConnectorLoopSelectEntriesEnum fromValue(String v) {
        for (ConnectorLoopSelectEntriesEnum c: ConnectorLoopSelectEntriesEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
