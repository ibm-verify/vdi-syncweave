/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for ConfigFileEventTypeEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ConfigFileEventTypeEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="create"/>
 *     &lt;enumeration value="createLocked"/>
 *     &lt;enumeration value="checkOut"/>
 *     &lt;enumeration value="checkIn"/>
 *     &lt;enumeration value="checkInLocked"/>
 *     &lt;enumeration value="unlock"/>
 *     &lt;enumeration value="delete"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ConfigFileEventTypeEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
@XmlEnum
public enum ConfigFileEventTypeEnum {

    @XmlEnumValue("create")
    CREATE("create"),
    @XmlEnumValue("createLocked")
    CREATE_LOCKED("createLocked"),
    @XmlEnumValue("checkOut")
    CHECK_OUT("checkOut"),
    @XmlEnumValue("checkIn")
    CHECK_IN("checkIn"),
    @XmlEnumValue("checkInLocked")
    CHECK_IN_LOCKED("checkInLocked"),
    @XmlEnumValue("unlock")
    UNLOCK("unlock"),
    @XmlEnumValue("delete")
    DELETE("delete");
    private final String value;

    ConfigFileEventTypeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ConfigFileEventTypeEnum fromValue(String v) {
        for (ConfigFileEventTypeEnum c: ConfigFileEventTypeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
