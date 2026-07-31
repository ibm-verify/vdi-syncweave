/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl;

import java.rmi.RemoteException;

import javax.jms.Message;

import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.SystemQueue;
import com.ibm.di.entry.Entry;

/**
 * System Queue class implements methods exposed through Server API remote
 * session.
 */
public class SystemQueueImpl extends APIRemoteObject implements SystemQueue {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = -2874374296748401233L;

	/**
	 * local system queue
	 */
	private transient com.ibm.di.api.local.SystemQueue mLocalSystemQueue;

	/**
	 * Constructor. Private, because instances are get with createInstance()
	 * 
	 * @param localSystemQueue
	 *            local system queue
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private SystemQueueImpl(com.ibm.di.api.local.SystemQueue localSystemQueue)
			throws RemoteException {
		mLocalSystemQueue = localSystemQueue;
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @param localSystemQueue
	 *            local system queue
	 * @return SystemQueue object
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static SystemQueue newInstance(
			com.ibm.di.api.local.SystemQueue localSystemQueue)
			throws RemoteException {
		return new SystemQueueImpl(localSystemQueue);
	}

	/**
	 * {@inheritDoc}
	 */
	public Message getMessage(String aQueueName, int aTimeOut)
			throws DIException, RemoteException {
		return mLocalSystemQueue.getMessage(aQueueName, aTimeOut);
	}

	/**
	 * {@inheritDoc}
	 */
	public void putMessage(String aQueueName, Message aMessage)
			throws DIException, RemoteException {
		mLocalSystemQueue.putMessage(aQueueName, aMessage);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTextMessage(String aQueueName, int aTimeOut)
			throws DIException, RemoteException {
		return mLocalSystemQueue.getTextMessage(aQueueName, aTimeOut);
	}

	/**
	 * {@inheritDoc}
	 */
	public void putTextMessage(String aQueueName, String aMessageText)
			throws DIException, RemoteException {
		mLocalSystemQueue.putTextMessage(aQueueName, aMessageText);
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] getBytesMessage(String aQueueName, int aTimeOut)
			throws DIException, RemoteException {
		return mLocalSystemQueue.getBytesMessage(aQueueName, aTimeOut);
	}

	/**
	 * {@inheritDoc}
	 */
	public void putBytesMessage(String aQueueName, byte[] aMessageBytes)
			throws DIException, RemoteException {
		mLocalSystemQueue.putBytesMessage(aQueueName, aMessageBytes);
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getEntry(String aQueueName, int aTimeOut) throws DIException,
			RemoteException {
		return mLocalSystemQueue.getEntry(aQueueName, aTimeOut);
	}

	/**
	 * {@inheritDoc}
	 */
	public void putEntry(String aQueueName, Entry aEntry) throws DIException,
			RemoteException {
		mLocalSystemQueue.putEntry(aQueueName, aEntry);
	}
}
