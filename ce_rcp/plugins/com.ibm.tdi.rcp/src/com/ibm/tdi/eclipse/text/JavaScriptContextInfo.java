/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.text;

import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;

public class JavaScriptContextInfo implements IContextInformation {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String displayString;
	private Image image;
	private String informationDisplayString;
	
	public JavaScriptContextInfo() {
		super();
		this.displayString = "Default display string";
		this.image = null;
		this.informationDisplayString = "Info display string";
	}

	public JavaScriptContextInfo(String displayString, Image image, String informationDisplayString) {
		super();
		this.displayString = displayString;
		this.image = image;
		this.informationDisplayString = informationDisplayString;
	}

	public String getContextDisplayString() {
		return displayString;
	}

	public Image getImage() {
		return image;
	}

	public String getInformationDisplayString() {
		return informationDisplayString;
	}

}
