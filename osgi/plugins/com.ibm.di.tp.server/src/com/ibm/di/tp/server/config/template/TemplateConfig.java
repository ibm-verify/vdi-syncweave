/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.config.template;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.ibm.di.tp.server.Constants;

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
@XmlType(namespace = Constants.NS_TDI_71_TP)
@XmlAccessorType(XmlAccessType.FIELD)
public class TemplateConfig {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private String baseTemplate;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private String customTemplatesDir;

	/**
	 * @return the baseTemplate
	 */
	public String getBaseTemplate() {
		return baseTemplate;
	}

	/**
	 * @param baseTemplate
	 *            the baseTemplate to set
	 */
	public void setBaseTemplate(String baseTemplate) {
		this.baseTemplate = baseTemplate;
	}

	/**
	 * @return the customTemplatesDir
	 */
	public String getCustomTemplatesDir() {
		return customTemplatesDir;
	}

	/**
	 * @param customTemplatesDir
	 *            the customTemplatesDir to set
	 */
	public void setCustomTemplatesDir(String customTemplatesDir) {
		this.customTemplatesDir = customTemplatesDir;
	}
}
