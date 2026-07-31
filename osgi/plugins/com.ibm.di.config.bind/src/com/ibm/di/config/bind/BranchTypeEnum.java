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
 * <p>Java class for BranchTypeEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BranchTypeEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="If"/>
 *     &lt;enumeration value="ElseIf"/>
 *     &lt;enumeration value="Else"/>
 *     &lt;enumeration value="Switch"/>
 *     &lt;enumeration value="Case"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "BranchTypeEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlEnum
public enum BranchTypeEnum {

    @XmlEnumValue("If")
    IF("If"),
    @XmlEnumValue("ElseIf")
    ELSE_IF("ElseIf"),
    @XmlEnumValue("Else")
    ELSE("Else"),
    @XmlEnumValue("Switch")
    SWITCH("Switch"),
    @XmlEnumValue("Case")
    CASE("Case");
    private final String value;

    BranchTypeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BranchTypeEnum fromValue(String v) {
        for (BranchTypeEnum c: BranchTypeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
