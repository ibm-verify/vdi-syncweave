/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import javax.jms.Message;

import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.DIException;
import com.ibm.di.api.jmx.JMXAgent;
import com.ibm.di.entry.Entry;

/**
 * SystemQueue class implements public methods exposed through JMX layer.
 */
public class SystemQueue extends BaseAdmin implements SystemQueueMBean {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Type of the MBean.
	 */
	public static final String MBEAN_TYPE = "SystemQueue";

	/**
	 * Id of the MBean.
	 */
	public static final String MBEAN_ID = "SystemQueue";

	/**
	 * {@link com.ibm.di.api.local.SystemQueue}
	 */
	private com.ibm.di.api.local.SystemQueue mLocalSystemQueue = null;

	/**
	 * 
	 * @param aLocalSystemQueue
	 *            {@link com.ibm.di.api.local.SystemQueue}
	 * @throws DIException
	 */
	public SystemQueue(com.ibm.di.api.local.SystemQueue aLocalSystemQueue)
			throws DIException {
		mLocalSystemQueue = aLocalSystemQueue;
	}

	// MBean interface

	/**
	 * {@inheritDoc}
	 */
	public String getType() {
		return MBEAN_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return MBEAN_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	public Message getMessage(String aQueueName, int aTimeOut)
			throws DIException {
		canExecuteAll();
		return mLocalSystemQueue.getMessage(aQueueName, aTimeOut);
	}

	/**
	 * {@inheritDoc}
	 */
	public void putMessage(String aQueueName, Message aMessage)
			throws DIException {
		canExecuteAll();
		mLocalSystemQueue.putMessage(aQueueName, aMessage);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTextMessage(String aQueueName, int aTimeOut)
			throws DIException {
		canExecuteAll();
		return mLocalSystemQueue.getTextMessage(aQueueName, aTimeOut);
	}

	/**
	 * {@inheritDoc}
	 */
	public void putTextMessage(String aQueueName, String aMessageText)
			throws DIException {
		canExecuteAll();
		mLocalSystemQueue.putTextMessage(aQueueName, aMessageText);
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] getBytesMessage(String aQueueName, int aTimeOut)
			throws DIException {
		canExecuteAll();
		return mLocalSystemQueue.getBytesMessage(aQueueName, aTimeOut);
	}

	/**
	 * {@inheritDoc}
	 */
	public void putBytesMessage(String aQueueName, byte[] aMessageBytes)
			throws DIException {
		canExecuteAll();
		mLocalSystemQueue.putBytesMessage(aQueueName, aMessageBytes);
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getEntry(String aQueueName, int aTimeOut) throws DIException {
		canExecuteAll();
		return mLocalSystemQueue.getEntry(aQueueName, aTimeOut);
	}

	/**
	 * {@inheritDoc}
	 */
	public void putEntry(String aQueueName, Entry aEntry) throws DIException {
		canExecuteAll();
		mLocalSystemQueue.putEntry(aQueueName, aEntry);
	}

	/**
	 * Checks whether specified user is allowed to execute everything.
	 * 
	 * @throws DIException
	 *             if user isn't allowed execute do the operations.
	 * 
	 */
	private void canExecuteAll() throws DIException {
		String userId = getCurrentUserId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAll(userId)) {
			throw new AuthorizationException();
		}
	}
}
