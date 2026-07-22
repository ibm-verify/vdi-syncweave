/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import javax.jms.Message;

import com.ibm.di.api.DIException;
import com.ibm.di.entry.Entry;

/**
 * System Queue MBean interface that defines public methods exposed through JMX
 * layer.
 */
public interface SystemQueueMBean extends BaseAdminMBean {

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
	 */
	public Message getMessage(String aQueueName, int aTimeOut)
			throws DIException;

	/**
	 * Stores a Message to the specified System Queue
	 * 
	 * @param aQueueName -
	 *            the name of the queue to which the message is to be stored
	 * @param aMessage -
	 *            the Message object to be stored
	 * @throws DIException
	 *             if an error occurs during storing
	 */
	public void putMessage(String aQueueName, Message aMessage)
			throws DIException;

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
	 */
	public String getTextMessage(String aQueueName, int aTimeOut)
			throws DIException;

	/**
	 * Stores a TextMessage to the System Queue
	 * 
	 * @param aQueueName -
	 *            the name of the queue to which the message is to be stored
	 * @param aMessageText -
	 *            the text to be stored
	 * @throws DIException
	 *             if an error occurs during sending
	 */
	public void putTextMessage(String aQueueName, String aMessageText)
			throws DIException;

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
	 */
	public byte[] getBytesMessage(String aQueueName, int aTimeOut)
			throws DIException;

	/**
	 * Stores a BytesMessage in the specified System Queue
	 * 
	 * @param aQueueName -
	 *            the name of the queue to which the message is to be stored
	 * @param aMessageBytes -
	 *            the byte array to be stored
	 * @throws DIException
	 *             if an error occurs during sending
	 */
	public void putBytesMessage(String aQueueName, byte[] aMessageBytes)
			throws DIException;

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
	 */
	public Entry getEntry(String aQueueName, int aTimeOut) throws DIException;

	/**
	 * Stores an Entry object to the specified System Queue
	 * 
	 * @param aQueueName -
	 *            the name of the queue to which the Entry is to be stored
	 * @param aEntry -
	 *            the Entry object to be stored
	 * @throws DIException
	 *             if an error occurs during sending
	 */
	public void putEntry(String aQueueName, Entry aEntry) throws DIException;
}
