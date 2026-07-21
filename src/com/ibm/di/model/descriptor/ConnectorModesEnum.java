/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.model.descriptor;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ConnectorModesEnum.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name=&quot;ConnectorModesEnum&quot;&gt;
 *   &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;&gt;
 *     &lt;enumeration value=&quot;Server&quot;/&gt;
 *     &lt;enumeration value=&quot;Iterator&quot;/&gt;
 *     &lt;enumeration value=&quot;AddOnly&quot;/&gt;
 *     &lt;enumeration value=&quot;Update&quot;/&gt;
 *     &lt;enumeration value=&quot;Delete&quot;/&gt;
 *     &lt;enumeration value=&quot;Lookup&quot;/&gt;
 *     &lt;enumeration value=&quot;CallReply&quot;/&gt;
 *     &lt;enumeration value=&quot;Delta&quot;/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ConnectorModesEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core")
@XmlEnum
public enum ConnectorModesEnum {

	@XmlEnumValue("Server")
	SERVER("Server"), @XmlEnumValue("Iterator")
	ITERATOR("Iterator"), @XmlEnumValue("AddOnly")
	ADD_ONLY("AddOnly"), @XmlEnumValue("Update")
	UPDATE("Update"), @XmlEnumValue("Delete")
	DELETE("Delete"), @XmlEnumValue("Lookup")
	LOOKUP("Lookup"), @XmlEnumValue("CallReply")
	CALL_REPLY("CallReply"), @XmlEnumValue("Delta")
	DELTA("Delta");
	private final String value;

	ConnectorModesEnum(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static ConnectorModesEnum fromValue(String v) {
		for (ConnectorModesEnum c : ConnectorModesEnum.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
