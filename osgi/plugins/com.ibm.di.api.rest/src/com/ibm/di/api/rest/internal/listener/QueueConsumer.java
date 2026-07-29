/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.listener;

import java.util.ArrayList;
import java.util.List;

import com.ibm.di.web.common.atom.AtomText;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.servlet.http.HttpSession;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.BatchEvent;
import com.ibm.di.api.bind.Event;
import com.ibm.di.api.bind.PollChannel;
import com.ibm.di.api.remote.RemoteListener;
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
public class QueueConsumer {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static Event consume(RemoteListener l, PollChannel channel, HttpSession sess) throws DIException {
		if (l instanceof QueueProducerBase == false) {
			throw new IllegalArgumentException(l.getClass().getName());
		}
		QueueSession s = getSession(sess);

		try {
			QueueReceiver r = s.createReceiver(((QueueProducerBase) l).getTempQueue());
			return receive(r, s, channel);
		} catch (JMSException e) {
			throw new DIException(e);
		}
	}

	private static QueueSession getSession(HttpSession sess) throws DIException {
		QueueSession s = (QueueSession) sess.getAttribute(QueueSession.class.getName());

		synchronized (sess) {
			s = (QueueSession) sess.getAttribute(QueueSession.class.getName());
			if (s == null) {
				try {
					QueueConnection qc = EnvUtils.getQueueConnection(sess.getServletContext());
					s = qc.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
					sess.setAttribute(QueueSession.class.getName(), s);
				} catch (JMSException e) {
					throw new DIException(e);
				} catch (Exception e) {
					throw new DIException(e);
				}
			}
		}
		return s;
	}

	private static Event receive(QueueReceiver r, QueueSession s, PollChannel channel) throws JMSException {
		Event result = null;
		try {
			if (channel.getBatchCap() == 1) {
				if (channel.getWaitTimeout() > 0) {
					Message m = r.receive(channel.getWaitTimeout() * 1000);
					if (m != null) {
						result = convertMessage(m);
					}
				}
			} else if (channel.getBatchCap() == 0) {
				List<Message> ms = new ArrayList<Message>();
				Message m;
				while ((m = r.receiveNoWait()) != null) {
					ms.add(m);
				}
				result = convertMessages(ms);
			} else {
				long left = channel.getWaitTimeout() * 1000;
				long start = System.currentTimeMillis();
				List<Message> ms = new ArrayList<Message>();
				Message m;
				if (channel.isFillBatch()) {
					while ((left = (left - (System.currentTimeMillis() - start))) > 0
							&& (channel.getBatchCap() > ms.size() && (m = r.receive(left)) != null)) {
						ms.add(m);
					}
				} else {
					while ((channel.getBatchCap() > ms.size() && (m = r.receiveNoWait()) != null)) {
						ms.add(m);
					}
				}

				if (
				/* we have reached the batch cap */
				channel.getBatchCap() == ms.size() || (
				/* at least a single entry is found */
				ms.size() > 1 && (
				/* we are not required to fill the batch in order to return */
				!channel.isFillBatch() ||
				/* user want as much as we have */
				channel.isOnTimeoutGetAll()))) {
					// deliver what we have so far
					result = convertMessages(ms);
				} // else deliver none
			}

			return result;
		} finally {
			if (result != null) {
				s.commit();
			} else {
				s.rollback();
			}
			r.close();
		}
	}

	private static BatchEvent convertMessages(List<Message> ms) throws JMSException {
		BatchEvent batch = new BatchEvent();

		for (Message m : ms) {
			batch.getEvents().add(convertMessage(m));
		}
		return batch;
	}

	private static Event convertMessage(Message m) throws JMSException {
		return (Event) ((ObjectMessage) m).getObject();
	}
}
