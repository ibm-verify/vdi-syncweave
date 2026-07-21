/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local.impl;

import javax.jms.Message;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.local.SystemQueue;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.systemqueue.SystemQueueEngine;

/**
 * System Queue class implements methods exposed through Server API local
 * session.
 */
public class SystemQueueImpl implements SystemQueue {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Default constructor
	 */
	private SystemQueueImpl() {
	}

	/**
	 * Gets a new Instance of this class.
	 * 
	 * @return the SystemQueue object.
	 * @throws DIException
	 *             if an error occurs.
	 */
	public static SystemQueue newInstance() throws DIException {
		try {
			SystemQueueEngine.getSystemQueue();
			return new SystemQueueImpl();
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash
					.getString("SEVER.API.COULD.NOT.GET.DEFAULT.SYSTEM.QUEUE"),
					e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Message getMessage(String aQueueName, int aTimeOut)
			throws DIException {
		try {
			return SystemQueueEngine.getSystemQueue().getMessage(aQueueName,
					aTimeOut);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.COULD.NOT.GETMESSAGE", e));
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void putMessage(String aQueueName, Message aMessage)
			throws DIException {
		try {
			SystemQueueEngine.getSystemQueue().putMessage(aQueueName, aMessage);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.COULD.NOT.PUTMESSAGE", e));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTextMessage(String aQueueName, int aTimeOut)
			throws DIException {
		try {
			return SystemQueueEngine.getSystemQueue().getTextMessage(
					aQueueName, aTimeOut);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.COULD.NOT.GETTEXTMESSAGE", e));
			return null;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void putTextMessage(String aQueueName, String aMessageText)
			throws DIException {
		try {
			SystemQueueEngine.getSystemQueue().putTextMessage(aQueueName,
					aMessageText);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.COULD.NOT.PUTTEXTMESSAGE", e));
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] getBytesMessage(String aQueueName, int aTimeOut)
			throws DIException {
		try {
			return SystemQueueEngine.getSystemQueue().getBytesMessage(
					aQueueName, aTimeOut);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.COULD.NOT.GETBYTESMESSAGE", e));
			return null;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void putBytesMessage(String aQueueName, byte[] aMessageBytes)
			throws DIException {
		try {
			SystemQueueEngine.getSystemQueue().putBytesMessage(aQueueName,
					aMessageBytes);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.COULD.NOT.PUTBYTESMESSAGE", e));
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getEntry(String aQueueName, int aTimeOut) throws DIException {
		try {
			return SystemQueueEngine.getSystemQueue().getEntry(aQueueName,
					aTimeOut);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.COULD.NOT.GETENTRY", e));
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void putEntry(String aQueueName, Entry aEntry) throws DIException {
		try {
			SystemQueueEngine.getSystemQueue().putEntry(aQueueName, aEntry);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.COULD.NOT.PUTENTRY", e));
		}
	}
}
