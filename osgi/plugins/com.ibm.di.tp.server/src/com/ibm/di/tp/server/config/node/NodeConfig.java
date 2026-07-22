/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.config.node;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
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
public abstract class NodeConfig implements Cloneable {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	@XmlID
	@XmlAttribute(required = true, namespace = Constants.NS_TDI_71_TP)
	private String id;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private String author;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private String summary;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private String title;

	@XmlElement(namespace = Constants.NS_TDI_71_TP)
	private String email;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author
	 *            the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return the summary
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * @param summary
	 *            the summary to set
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public NodeConfig clone() {
		try {
			return (NodeConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
