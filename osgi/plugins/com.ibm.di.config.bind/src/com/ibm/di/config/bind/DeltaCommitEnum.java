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
 * <p>Java class for DeltaCommitEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DeltaCommitEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="onEveryOp"/>
 *     &lt;enumeration value="onAlCycle"/>
 *     &lt;enumeration value="onAlEnd"/>
 *     &lt;enumeration value="custom"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DeltaCommitEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlEnum
public enum DeltaCommitEnum {

    @XmlEnumValue("onEveryOp")
    ON_EVERY_OP("onEveryOp"),
    @XmlEnumValue("onAlCycle")
    ON_AL_CYCLE("onAlCycle"),
    @XmlEnumValue("onAlEnd")
    ON_AL_END("onAlEnd"),
    @XmlEnumValue("custom")
    CUSTOM("custom");
    private final String value;

    DeltaCommitEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DeltaCommitEnum fromValue(String v) {
        for (DeltaCommitEnum c: DeltaCommitEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
