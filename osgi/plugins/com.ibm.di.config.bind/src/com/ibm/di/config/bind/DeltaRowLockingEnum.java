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
 * <p>Java class for DeltaRowLockingEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DeltaRowLockingEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="readUncommited"/>
 *     &lt;enumeration value="readCommited"/>
 *     &lt;enumeration value="repeatableRead"/>
 *     &lt;enumeration value="serializable"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DeltaRowLockingEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlEnum
public enum DeltaRowLockingEnum {

    @XmlEnumValue("readUncommited")
    READ_UNCOMMITED("readUncommited"),
    @XmlEnumValue("readCommited")
    READ_COMMITED("readCommited"),
    @XmlEnumValue("repeatableRead")
    REPEATABLE_READ("repeatableRead"),
    @XmlEnumValue("serializable")
    SERIALIZABLE("serializable");
    private final String value;

    DeltaRowLockingEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DeltaRowLockingEnum fromValue(String v) {
        for (DeltaRowLockingEnum c: DeltaRowLockingEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
