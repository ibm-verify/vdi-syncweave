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
 * <p>Java class for ConnectorModeEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ConnectorModeEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Iterator"/>
 *     &lt;enumeration value="AddOnly"/>
 *     &lt;enumeration value="Delete"/>
 *     &lt;enumeration value="Lookup"/>
 *     &lt;enumeration value="Update"/>
 *     &lt;enumeration value="CallReply"/>
 *     &lt;enumeration value="Server"/>
 *     &lt;enumeration value="ReplyChannel"/>
 *     &lt;enumeration value="Delta"/>
 *     &lt;enumeration value="Script"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ConnectorModeEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/config")
@XmlEnum
public enum ConnectorModeEnum {

    @XmlEnumValue("Iterator")
    ITERATOR("Iterator"),
    @XmlEnumValue("AddOnly")
    ADD_ONLY("AddOnly"),
    @XmlEnumValue("Delete")
    DELETE("Delete"),
    @XmlEnumValue("Lookup")
    LOOKUP("Lookup"),
    @XmlEnumValue("Update")
    UPDATE("Update"),
    @XmlEnumValue("CallReply")
    CALL_REPLY("CallReply"),
    @XmlEnumValue("Server")
    SERVER("Server"),
    @XmlEnumValue("ReplyChannel")
    REPLY_CHANNEL("ReplyChannel"),
    @XmlEnumValue("Delta")
    DELTA("Delta"),
    @XmlEnumValue("Script")
    SCRIPT("Script");

    private final String value;

    ConnectorModeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ConnectorModeEnum fromValue(String v) {
        for (ConnectorModeEnum c: ConnectorModeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
