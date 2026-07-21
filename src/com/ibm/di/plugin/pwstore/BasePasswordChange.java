/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore;

import java.util.Vector;

/**
 * This class implements a simple password change. 
 */
public class BasePasswordChange implements PasswordChange {

	private int type = PasswordChange.NO_CHANGE;
	
	private String id;

	private Vector<String> passwords;

	private String extData;

	private String customData;
	
	private long timestamp;

	public BasePasswordChange(int type, String id, Vector<String> passwords) {
		this(type, id, passwords, null, null);
	}
	
	public BasePasswordChange(int type, String id, String extData) {
		this(type, id, null, extData, null);
	}
	
	public BasePasswordChange(String id, Vector<String> passwords) {
		this.id = id;
		this.passwords = passwords;
		this.timestamp = System.currentTimeMillis();
	}
	
	public BasePasswordChange(int type, String id, Vector<String> passwords, String extData, String customData) {
		this(id, passwords);
		this.type = type;
		this.extData = extData;
		this.customData = customData;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getID() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vector<String> getPasswords() {
		return passwords;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtData() {
		return extData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCustomData() {
		return customData;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getTimestamp() {
		return timestamp;
	}
}
