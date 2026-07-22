/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.validators;

import java.util.Date;

import org.eclipse.jface.dialogs.IInputValidator;

import com.ibm.di.config.interfaces.BaseConfiguration;

public class ConfigParameterValidator implements IInputValidator {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public final static int MUST_NOT_EXIST = 1;
	public final static int SYNTAX_STRING = 2;
	public final static int SYNTAX_NUMERIC = 4;
	public final static int SYNTAX_DATE= 8;

	private BaseConfiguration config;
	private int flags = MUST_NOT_EXIST;
	
	public ConfigParameterValidator(BaseConfiguration config, int flags) {
		super();
		this.config = config;
		this.flags = flags;
	}

	public BaseConfiguration getConfig() {
		return config;
	}

	public void setConfig(BaseConfiguration config) {
		this.config = config;
	}

	public String isValid(String newText) {
		if(isSet(MUST_NOT_EXIST) && config.hasParameter(newText)) {
			return "Parameter already exists";
		}
		
		if(isSet(SYNTAX_NUMERIC)) {
			try {
				Integer.parseInt(newText);
			} catch (Exception e) {
				return e.toString();
			}
		} else if(isSet(SYNTAX_DATE)) {
			try {
				Date.parse(newText);
			} catch (Exception e) {
				return e.toString();
			}
		}
		// NOTE: nothing currently to do for this
		// case, so we are commenting it out...
		// if(isSet(SYNTAX_STRING)) { }
		
		return null;
	}

	private boolean isSet(int flag) {
		return ( (flags & flag) > 0 );
	}

}
