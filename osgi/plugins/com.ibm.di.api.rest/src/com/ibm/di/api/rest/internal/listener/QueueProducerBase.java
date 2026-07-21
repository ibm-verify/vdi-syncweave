/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.listener;

import java.io.Serializable;
import java.rmi.server.Unreferenced;

import com.ibm.di.web.common.atom.AtomText;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.PollChannel;
import com.ibm.di.api.remote.RemoteListener;
import com.ibm.di.api.rest.internal.registry.ListenerRegistry;
import com.ibm.di.api.rest.internal.util.EnvUtils;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public abstract class QueueProducerBase implements RemoteListener, Unreferenced {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final Logger log = LoggerFactory.getLogger(QueueProducerBase.class);

	private final ServletContext sctx;

	private QueueSession s;
	private QueueSender qSender;
	private TemporaryQueue tempQueue;

	public QueueProducerBase(PollChannel channel, ServletContext sctx) throws DIException {
		this.sctx = sctx;
		try {
			QueueConnection qc = EnvUtils.getQueueConnection(sctx);
			s = qc.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
			tempQueue = s.createTemporaryQueue();
			qSender = s.createSender(tempQueue);
			qSender.setDeliveryMode(Boolean.getBoolean("api.rest.jmsdriver.queue.sender.persistance") ? DeliveryMode.PERSISTENT
					: DeliveryMode.NON_PERSISTENT);
			qSender.setTimeToLive(Long.getLong("api.rest.jmsdriver.queue.sender.timeToLive", 0L));
		} catch (Exception e) {
			throw new DIException(e);
		}
	}

	protected void publishMessage(Serializable data) throws DIException {
		try {
			ObjectMessage msg = s.createObjectMessage(data);
			qSender.send(msg);
			s.commit();
		} catch (JMSException e) {
			log.error(e.getMessage(), e);
			throw new DIException(e);
		}
	}

	public void unreferenced() {
		ListenerRegistry reg = EnvUtils.getListenerRegistry(sctx);
		reg.unregister(this);
		synchronized (this) {
			if (tempQueue != null) {
				try {
					tempQueue.delete();
				} catch (JMSException e) {
					log.error(e.getMessage(), e);
				} finally {
					tempQueue = null;
				}
			}
		}

		if (qSender != null) {
			try {
				qSender.close();
			} catch (JMSException e) {
				log.error(e.getMessage(), e);
			} finally {
				qSender = null;
			}
		}

		if (s != null) {
			try {
				s.close();
			} catch (JMSException e) {
				log.error(e.getMessage(), e);
			} finally {
				s = null;
			}
		}
	}

	/**
	 * Provides thread safe access to the {@link TemporaryQueue}
	 * 
	 * @return the tempQueue
	 */
	public TemporaryQueue getTempQueue() {
		synchronized (this) {
			return tempQueue;
		}
	}

}
