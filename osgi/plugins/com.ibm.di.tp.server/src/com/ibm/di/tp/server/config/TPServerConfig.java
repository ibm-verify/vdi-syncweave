/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.config.node.NodeConfigsContainer;
import com.ibm.di.tp.server.config.persistence.PersistenceConfig;
import com.ibm.di.tp.server.config.security.EncryptionConfig;
import com.ibm.di.tp.server.config.template.TemplateConfig;

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
@XmlRootElement(namespace = Constants.NS_TDI_71_TP)
@XmlAccessorType(XmlAccessType.FIELD)
public class TPServerConfig implements Cloneable {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlAttribute(required = true, namespace = Constants.NS_TDI_71_TP)
	private String version;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private EncryptionConfig encryptionConfig;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private PersistenceConfig persistenceConfig;
	
	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private TemplateConfig templateConfig;

	@XmlElement(name = "nodeConfigs", namespace = Constants.NS_TDI_71_TP)
	private NodeConfigsContainer nodeConfigs;

	public TPServerConfig() {
	}

	/**
	 * @return the nodeConfigs
	 */
	public NodeConfigsContainer getNodeConfigs() {
		if (nodeConfigs == null) {
			nodeConfigs = new NodeConfigsContainer();
		}
		return nodeConfigs;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the encryptionConfig
	 */
	public EncryptionConfig getEncryptionConfig() {
		if (encryptionConfig == null) {
			encryptionConfig = new EncryptionConfig();
		}
		return encryptionConfig;
	}

	/**
	 * @param persistenceConfig
	 *            the persistenceConfig to set
	 */
	public void setPersistenceConfig(PersistenceConfig persistenceConfig) {
		this.persistenceConfig = persistenceConfig;
	}

	/**
	 * @return the persistenceConfig
	 */
	public PersistenceConfig getPersistenceConfig() {
		if (persistenceConfig == null) {
			persistenceConfig = new PersistenceConfig();
		}
		return persistenceConfig;
	}

	/**
	 * @param templateConfig
	 *            the templateConfig to set
	 */
	public void setTemplateConfig(TemplateConfig templateConfig) {
		this.templateConfig = templateConfig;
	}

	/**
	 * @return the templateConfig
	 */
	public TemplateConfig getTemplateConfig() {
		if (templateConfig == null) {
			templateConfig = new TemplateConfig();
		}

		return templateConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public TPServerConfig clone() {
		try {
			TPServerConfig clone = (TPServerConfig) super.clone();
			if (encryptionConfig != null) {
				clone.encryptionConfig = encryptionConfig.clone();
			}

			if (nodeConfigs != null) {
				clone.nodeConfigs = nodeConfigs.clone();
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
