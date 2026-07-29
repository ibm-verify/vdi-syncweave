/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim.policy.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.ibm.di.plugin.pwstore.itim.policy.MalformedResponseException;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyConnectionException;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyServiceConnection;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyServiceRequest;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyServiceResponse;

/**
 * Encapsulates an HTTP connection to the ITIM password policy servlet.
 */
public final class ITIMPolicyServiceConnectionImpl implements PolicyServiceConnection {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	private URL url;

	private HttpURLConnection conn;

	/**
	 * @param url
	 *            the ITIM server servlet's url.
	 * 
	 * @throws IllegalArgumentException
	 *             if url is null.
	 */
	ITIMPolicyServiceConnectionImpl(URL url) {
		super();
		if (url == null) {
			throw new IllegalArgumentException("Invalid URL: null");
		}
		this.url = url;
	}

	/**
	 * Send and receive messages against the policy service.
	 * 
	 * @param request
	 *            The request message.
	 * @return The response
	 * 
	 * @throws PolicyConnectionException
	 *             if a network error occurs.
	 * @throws IllegalStateException
	 *             if not connected.
	 * @throws IllegalArgumentException
	 *             if request is null.
	 * 
	 * @see PolicyServiceConnection#sendReceive(PolicyServiceRequest)
	 */
	public synchronized PolicyServiceResponse sendReceive(
			PolicyServiceRequest request) throws PolicyConnectionException,
			MalformedResponseException {
		send(request);
		return receive(request);
	}

	private void connect() throws PolicyConnectionException {
		if (conn == null) {
			try {
				conn = (HttpURLConnection) url.openConnection();
				conn.setUseCaches(false);
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("content-type",
						"text/xml;charset=utf-8");
				/*
				 * We are sending a single request and will close the connection
				 * after the response, so ensure the "Connection" header is set
				 * to "close" and not to "keep-alive".
				 */
				conn.setRequestProperty("Connection", "close");
			} catch (IOException x) {
				throw new PolicyConnectionException(x);
			}
		}
	}

	private void send(PolicyServiceRequest request)
			throws PolicyConnectionException {
		try {
			connect();
			byte[] outData = request.getMessageData().getBytes("UTF-8");

			OutputStream os = conn.getOutputStream();
			os.write(outData);
		} catch (IOException x) {
			throw new PolicyConnectionException(x);
		}
	}

	private PolicyServiceResponse receive(PolicyServiceRequest request)
			throws PolicyConnectionException, MalformedResponseException {
		ITIMPolicyServiceResponseImpl result = null;

		String msgData = receiveRawData(request);

		try {
			result = new ITIMPolicyServiceResponseImpl();
			result.setMsgData(msgData);
			result.setReqMsg(request);
			ITIMPolicyServiceResponseImpl.initFromXml(result,
					ITIMPasswordPolicyFactoryImpl.newSAXParser());
		} catch (ParserConfigurationException x) {
			throw new PolicyConnectionException(x);
		} catch (SAXException x) {
			throw new PolicyConnectionException(x);
		}

		return result;
	}

	private String receiveRawData(PolicyServiceRequest request)
			throws PolicyConnectionException {
		String result = null;

		InputStreamReader isr = null;
		try {
			int code = conn.getResponseCode();
			if (code != 200) {
				throw new PolicyConnectionException(
						code == -1 ? "Invalid HTTP response received!" : conn
								.getResponseMessage());
			}

			InputStream is = (InputStream) conn.getContent();

			if (conn.getContentEncoding() != null) {
				isr = new InputStreamReader(is, conn.getContentEncoding());
			} else {
				isr = new InputStreamReader(is);
			}

			char[] buf = new char[256];
			int charsRead = 0;
			StringBuilder msgData = new StringBuilder();
			while ((charsRead = isr.read(buf)) != -1) {
				msgData.append(buf, 0, charsRead);
			}

			result = msgData.toString();
		} catch (UnsupportedEncodingException x) {
			throw new PolicyConnectionException(x);
		} catch (IOException x) {
			throw new PolicyConnectionException(x);
		} finally {
			try {
				if (isr != null) {
					isr.close();
				}
			} catch (IOException e) {
				throw new PolicyConnectionException(e);
			} finally {
				conn.disconnect();
				conn = null;
			}
		}

		return result;
	}
}
