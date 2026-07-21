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
 * <p>Java class for PoolInstanceExhaustedEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PoolInstanceExhaustedEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="wait"/>
 *     &lt;enumeration value="fail"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PoolInstanceExhaustedEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlEnum
public enum PoolInstanceExhaustedEnum {

    @XmlEnumValue("wait")
    WAIT("wait"),
    @XmlEnumValue("fail")
    FAIL("fail");
    private final String value;

    PoolInstanceExhaustedEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PoolInstanceExhaustedEnum fromValue(String v) {
        for (PoolInstanceExhaustedEnum c: PoolInstanceExhaustedEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
