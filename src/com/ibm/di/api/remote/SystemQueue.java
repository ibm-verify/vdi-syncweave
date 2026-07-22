/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote;

import com.ibm.di.api.DIException;
import com.ibm.di.entry.Entry;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jms.Message;

/**
 * System Queue inteface that defines public methods exposed through Server API
 * remote session.
 */
public interface SystemQueue extends Remote {

	/**
	 * Retrieves a JMS Message from the specified System Queue
	 * 
	 * @param aQueueName -
	 *            the name of the queue from which the message is retrieved
	 * @param aTimeOut -
	 *            specifies the maximum time in seconds to wait for a new
	 *            message; if 0 is specified - if there is no message available
	 *            this method returns immediately; if a negative number is
	 *            specified, this method will wait indefinitely or until a
	 *            message becomes available
	 * @return the javax.jms.Message object.
	 * @throws DIException
	 *             if an error occurs during receiving
	 * @throws RemoteException
	 *             If the Server API RMI connection fails
	 */
	public Message getMessage(String aQueueName, int aTimeOut)
			throws DIException, RemoteException;

	/**
	 * Stores a Message to the specified System Queue
	 * 
	 * @param aQueueName -
	 *            the name of the queue to which the message is to be stored
	 * @param aMessage -
	 *            the Message object to be stored
	 * @throws DIException
	 *             if an error occurs during storing
	 * @throws RemoteException
	 *             If the Server API RMI connection fails
	 */
	public void putMessage(String aQueueName, Message aMessage)
			throws DIException, RemoteException;

	/**
	 * Retrieves a TextMessage from the specified System Queue
	 * 
	 * @param aQueueName -
	 *            the name of the queue from which the message is retrieved
	 * @param aTimeOut -
	 *            specifies the maximum time in seconds to wait for a new
	 *            message; if 0 is specified - if there is no message available
	 *            this method returns immediately; if a negative number is
	 *            specified, this method will wait indefinitely or until a
	 *            message becomes available
	 * @return The text of the message
	 * @throws DIException
	 *             if an error occurs during receiving
	 * @throws RemoteException
	 *             If the Server API RMI connection fails
	 */
	public String getTextMessage(String aQueueName, int aTimeOut)
			throws DIException, RemoteException;

	/**
	 * Stores a TextMessage to the System Queue
	 * 
	 * @param aQueueName -
	 *            the name of the queue to which the message is to be stored
	 * @param aMessageText -
	 *            the text to be stored
	 * @throws DIException
	 *             if an error occurs during sending
	 * @throws RemoteException
	 *             If the Server API RMI connection fails
	 */
	public void putTextMessage(String aQueueName, String aMessageText)
			throws DIException, RemoteException;

	/**
	 * Retrieves a BytesMessage from the specified System Queue
	 * 
	 * @param aQueueName -
	 *            the name of the queue from which the message is retrieved
	 * @param aTimeOut -
	 *            specifies the maximum time in seconds to wait for a new
	 *            message; if 0 is specified - if there is no message available
	 *            this method returns immediately; if a negative number is
	 *            specified, this method will wait indefinitely or until a
	 *            message becomes available
	 * @return The bytes of the message in a byte array
	 * @throws DIException
	 *             if an error occurs during receiving
	 * @throws RemoteException
	 *             If the Server API RMI connection fails
	 */
	public byte[] getBytesMessage(String aQueueName, int aTimeOut)
			throws DIException, RemoteException;

	/**
	 * Stores a BytesMessage in the specified System Queue
	 * 
	 * @param aQueueName -
	 *            the name of the queue to which the message is to be stored
	 * @param aMessageBytes -
	 *            the byte array to be stored
	 * @throws DIException
	 *             if an error occurs during sending
	 * @throws RemoteException
	 *             If the Server API RMI connection fails
	 */
	public void putBytesMessage(String aQueueName, byte[] aMessageBytes)
			throws DIException, RemoteException;

	/**
	 * Retrieves an Entry object from the specified System Queue
	 * 
	 * @param aQueueName -
	 *            the name of the queue from which the message is retrieved
	 * @param aTimeOut -
	 *            specifies the maximum time in seconds to wait for a new
	 *            message; if 0 is specified - if there is no message available
	 *            this method returns immediately; if a negative number is
	 *            specified, this method will wait indefinitely or until a
	 *            message becomes available
	 * @return The retrieved com.ibm.di.entry.Entry object
	 * @throws DIException
	 *             if an error occurs during receiving, or if the message
	 *             retrieved is not an ObjectMessage or if the ObjectMessage
	 *             retrieved does not store a com.ibm.di.entry.Entry object
	 * @throws RemoteException
	 *             If the Server API RMI connection fails
	 */
	public Entry getEntry(String aQueueName, int aTimeOut) throws DIException,
			RemoteException;

	/**
	 * Stores an Entry object to the specified System Queue
	 * 
	 * @param aQueueName -
	 *            the name of the queue to which the Entry is to be stored
	 * @param aEntry -
	 *            the Entry object to be stored
	 * @throws DIException
	 *             if an error occurs during sending
	 * @throws RemoteException
	 *             If the Server API RMI connection fails
	 */
	public void putEntry(String aQueueName, Entry aEntry) throws DIException,
			RemoteException;
}
