/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.impl.tdi;

import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.tp.server.model.TouchpointType;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public enum TouchpointTypeScheme {

	/** the scheme for a type that represent a tdi connector as tp */
	SYSTEM(MetamergeConfigFactory.SYSTEM_NAMESPACE),

	/** the scheme for a type that represents a custom template as tp */
	FILE("file"),

	/** the scheme for a type that represents a virtual template as tp */
	VIRTUAL("virtual");

	private final String value;

	private TouchpointTypeScheme(String value) {
		this.value = value;
	}

	/**
	 * @return the string representation of the scheme
	 */
	@Override
	public String toString() {
		return value;
	}

	public static TouchpointTypeScheme fromString(String str) {
		for (TouchpointTypeScheme tts : values()) {
			if (tts.value.equals(str)) {
				return tts;
			}
		}
		return null;
	}

	public static TouchpointTypeScheme fromType(TouchpointType tt) {
		String id = tt.getId();
		int colPos = id.indexOf(':');
		return colPos > 0 ? fromString(id.substring(0, colPos)) : null;
	}
}
