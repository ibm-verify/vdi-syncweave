/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.internal.atom;

import javax.xml.bind.annotation.XmlValue;

import org.apache.wink.common.model.atom.AtomText;

/**
 * Jackson provider is not able to properly represent mixed content
 * annotation and thus the actual content gets lost. Override to provide new
 * simplified view of the original {@link AtomText} class.<br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 *
 * @since 7.2
 */
public class StringAtomText extends AtomText {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlValue
	private String value;

	public StringAtomText() {
		super();
	}

	public StringAtomText(String text) {
		super(text);
	}

	@Override
	public void setValue(Object value) {
		super.setValue(value);
		this.value = value == null ? null : value.toString();
	}

	@Override
	public String getValue() {
		return this.value;
	}

}
