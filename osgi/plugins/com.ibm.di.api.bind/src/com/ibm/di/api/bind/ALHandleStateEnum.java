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
 * <p>
 * Java class for ALHandleStateEnum.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="ALHandleStateEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="init"/>
 *     &lt;enumeration value="processing"/>
 *     &lt;enumeration value="done"/>
 *     &lt;enumeration value="closed"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ALHandleStateEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/72/api")
@XmlEnum
public enum ALHandleStateEnum {

	@XmlEnumValue("init")
	INIT("init"), @XmlEnumValue("processing")
	PROCESSING("processing"), @XmlEnumValue("done")
	DONE("done"), @XmlEnumValue("closed")
	CLOSED("closed");
	private final String value;

	ALHandleStateEnum(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static ALHandleStateEnum fromValue(String v) {
		for (ALHandleStateEnum c : ALHandleStateEnum.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
