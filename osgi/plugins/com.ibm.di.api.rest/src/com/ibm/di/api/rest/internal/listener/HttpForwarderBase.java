/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.listener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.ibm.di.web.common.atom.AtomText;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.di.api.bind.ObjectFactory;
import com.ibm.di.api.bind.PushChannel;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.util.JAXBUtils;

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
public abstract class HttpForwarderBase {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static Logger log = LoggerFactory.getLogger(HttpForwarderBase.class);

	private final URL out;
	private final URL err;

	protected HttpForwarderBase(PushChannel channel) throws MalformedURLException {
		this.out = channel != null && channel.getOut().trim().length() > 0 ? new URL(channel.getOut()) : null;
		this.err = channel != null && channel.getError().trim().length() > 0 ? new URL(channel.getError()) : null;
	}

	protected void sendMessage(Object payload) {
		try {
			sendMessageTo(payload, out);
		} catch (IOException e) {
			log.error(AppConstants.L10N.getString("REST.API.HTTP.LISTENER.ERROR", out), e);

			if (err != null) {
				try {
					sendMessageTo(payload, err);
				} catch (IOException e1) {
					log.error(AppConstants.L10N.getString("REST.API.HTTP.LISTENER.ERROR", err), e1);
				}
			}
		}
	}

	private void sendMessageTo(Object payload, URL dest) throws IOException {
		URLConnection outConn = dest.openConnection();
		outConn.setDoOutput(true);
		if (outConn instanceof HttpURLConnection) {
			((HttpURLConnection) outConn).setRequestMethod("POST");
			((HttpURLConnection) outConn).setRequestProperty("ContentType", AppConstants.MT_LISTENER_XML);
		}

		try {
			JAXBUtils.serializeObjectToStream(payload, outConn.getOutputStream(), ObjectFactory.getMarshaller());
		} catch (JAXBException e) {
			log.error(AppConstants.L10N.getString("REST.API.SERIALIZATION.ERROR", payload), e);
		}
	}
}
