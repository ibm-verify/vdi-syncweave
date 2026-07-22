/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.config.security;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
public class EncryptionConfig implements Cloneable {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlAttribute(namespace = Constants.NS_TDI_71_TP)
	private String stash;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private String keyStore;

	@XmlElement(defaultValue = "jks", namespace = Constants.NS_TDI_71_TP)
	private String keyStoreType;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private String keyAlias;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private String transformation;

	/**
	 * @return the stash
	 */
	public String getStash() {
		return stash;
	}

	/**
	 * @param stash
	 *            the stash to set
	 */
	public void setStash(String stash) {
		this.stash = stash;
	}

	/**
	 * @return the keyStore
	 */
	public String getKeyStore() {
		return keyStore;
	}

	/**
	 * @param keyStore
	 *            the keyStore to set
	 */
	public void setKeyStore(String keyStore) {
		this.keyStore = keyStore;
	}

	/**
	 * @return the keyStoreType
	 */
	public String getKeyStoreType() {
		return keyStoreType;
	}

	/**
	 * @param keyStoreType
	 *            the keyStoreType to set
	 */
	public void setKeyStoreType(String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}

	/**
	 * @return the keyAlias
	 */
	public String getKeyAlias() {
		return keyAlias;
	}

	/**
	 * @param keyAlias
	 *            the keyAlias to set
	 */
	public void setKeyAlias(String keyAlias) {
		this.keyAlias = keyAlias;
	}

	/**
	 * @return the transformation
	 */
	public String getTransformation() {
		return transformation;
	}

	/**
	 * @param transformation
	 *            the transformation to set
	 */
	public void setTransformation(String transformation) {
		this.transformation = transformation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public EncryptionConfig clone() {
		try {
			return (EncryptionConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
