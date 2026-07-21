/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import com.ibm.di.tp.server.Constants;

/**
 * <p>
 * Java class for enum_OpState.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="enum_OpState">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="available"/>
 *     &lt;enumeration value="unavailable"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enum_OpState", namespace = Constants.NS_SCMP)
@XmlEnum
public enum EnumOpState {

	@XmlEnumValue("available")
	AVAILABLE("available"), @XmlEnumValue("unavailable")
	UNAVAILABLE("unavailable");
	private final String value;

	EnumOpState(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static EnumOpState fromValue(String v) {
		for (EnumOpState c : EnumOpState.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
