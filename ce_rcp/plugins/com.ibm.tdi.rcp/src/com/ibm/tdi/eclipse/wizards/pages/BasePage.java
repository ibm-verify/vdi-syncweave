/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards.pages;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.wizard.WizardPage;

import com.ibm.di.config.interfaces.BaseConfiguration;

public abstract class BasePage extends WizardPage {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String type;
	private IInputValidator nameValidator;
	private BaseConfiguration configObject;

	public BasePage(String pageName, String type) {
		super(pageName);
		this.type = type;
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setNameValidator(IInputValidator validator) {
		this.nameValidator = validator;
	}

	public IInputValidator getNameValidator() {
		return nameValidator;
	}

	public BaseConfiguration getConfigObject() {
		return configObject;
	}

	public void setConfigObject(BaseConfiguration configObject) {
		this.configObject = configObject;
	}

	public static String getComponentTitle(String str) {
		// TODO: Bad bad bad bad bad bad coding here :)
		if(str.equals("JavaProperties"))
			str = "Property file";
		else if(str.endsWith("s"))
			str = str.substring(0,str.length()-1);
		return str;
	}


}
