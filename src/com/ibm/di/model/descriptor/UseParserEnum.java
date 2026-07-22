/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.ibm.di.model.descriptor;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for UseParserEnum.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name=&quot;UseParserEnum&quot;&gt;
 *   &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;&gt;
 *     &lt;enumeration value=&quot;required&quot;/&gt;
 *     &lt;enumeration value=&quot;optional&quot;/&gt;
 *     &lt;enumeration value=&quot;prohibit&quot;/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "UseParserEnum", namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core")
@XmlEnum
public enum UseParserEnum implements Serializable {

	@XmlEnumValue("required")
	REQUIRED("required"), @XmlEnumValue("optional")
	OPTIONAL("optional"), @XmlEnumValue("prohibit")
	PROHIBIT("prohibit");
	private final String value;

	UseParserEnum(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static UseParserEnum fromValue(String v) {
		for (UseParserEnum c : UseParserEnum.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
