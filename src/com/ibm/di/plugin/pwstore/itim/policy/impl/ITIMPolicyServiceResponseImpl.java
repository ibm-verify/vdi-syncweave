/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim.policy.impl;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.di.plugin.pwstore.itim.policy.MalformedResponseException;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyServiceMessage;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyServiceRequest;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyServiceResponse;

/**
 * Represents and parses responses from the ITIM password policy servlet.
 */
public final class ITIMPolicyServiceResponseImpl implements PolicyServiceResponse {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	static final String SUCCESS = "success";

	/**
	 * SAX handler for SYNCH_PSWDS_RESP response messages received from ITIM
	 * password policy servlet. This handler is used to initialize
	 * ITIMPolicyServiceResponseImpl instances.
	 */
	private static class ITIMPolicyServiceResponseHandler extends
			DefaultHandler {
		private static final String RESPONSE_ELEMENT_NAME = "SYNCH_PSWDS_RESP";

		private static final String ATTR_NAME_CODE = "code";

		private static final String ATTR_NAME_DESC = "desc";

		ITIMPolicyServiceResponseImpl respObj;

		ITIMPolicyServiceResponseHandler(ITIMPolicyServiceResponseImpl resp) {
			if (resp == null) {
				throw new IllegalArgumentException();
			}

			respObj = resp;
		}

		/**
		 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
		 */
		public void fatalError(SAXParseException e) throws SAXException {
			respObj.setValidFlag(false);
			super.fatalError(e);
		}

		/**
		 * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
		 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {

			if (qName
					.compareTo(ITIMPolicyServiceResponseHandler.RESPONSE_ELEMENT_NAME) == 0) {
				for (int i = 0; i < attributes.getLength(); ++i) {
					if (attributes.getQName(i).compareTo(
							ITIMPolicyServiceResponseHandler.ATTR_NAME_CODE) == 0) {
						respObj.setSuccessFlag(attributes.getValue(i));
					} else if (attributes.getQName(i).compareTo(
							ITIMPolicyServiceResponseHandler.ATTR_NAME_DESC) == 0) {
						respObj.setMsgDesc(attributes.getValue(i));
					}
				}
			}
		}
	}

	private String msgData;

	private PolicyServiceRequest reqMsg;

	private String msgDesc;

	private boolean successFlag;

	private boolean validFlag;

	ITIMPolicyServiceResponseImpl() {
		super();
		successFlag = false;
		validFlag = false;
	}

	/**
	 * @see PolicyServiceResponse#getRequest()
	 */
	public PolicyServiceRequest getRequest() {
		return reqMsg;
	}

	/**
	 * @see PolicyServiceMessage#getMessageData()
	 */
	public String getMessageData() throws IOException {
		return msgData;
	}

	/**
	 * @see PolicyServiceMessage#getOperation()
	 */
	public ServiceOp getOperation() {
		return getRequest().getOperation();
	}

	/**
	 * @see PolicyServiceResponse#getResponseMessage()
	 */
	public String getResponseMessage() {
		return msgDesc;
	}

	/**
	 * @see PolicyServiceResponse#isSuccess()
	 */
	public boolean isSuccess() {
		return successFlag;
	}

	/**
	 * @see PolicyServiceResponse#isValid()
	 */
	public boolean isValid() {
		return validFlag;
	}

	/**
	 * @param msgData
	 *            The msgData to set.
	 */
	void setMsgData(String msgData) {
		this.msgData = msgData;
	}

	/**
	 * @param msgDesc
	 *            The msgDesc to set.
	 */
	void setMsgDesc(String msgDesc) {
		this.msgDesc = msgDesc;
	}

	/**
	 * @param reqMsg
	 *            The reqMsg to set.
	 */
	void setReqMsg(PolicyServiceRequest reqMsg) {
		this.reqMsg = reqMsg;
	}

	/**
	 * @param successFlag
	 *            The successFlag to set.
	 */
	void setSuccessFlag(boolean successFlag) {
		this.successFlag = successFlag;
	}

	/**
	 * @param successFlagStr
	 *            The successFlag to set.
	 */
	void setSuccessFlag(String successFlagStr) {
		this.successFlag = (successFlagStr
				.compareToIgnoreCase(ITIMPolicyServiceResponseImpl.SUCCESS) == 0);
	}

	/**
	 * @param validFlag
	 *            The validFlag to set.
	 */
	void setValidFlag(boolean validFlag) {
		this.validFlag = validFlag;
	}

	/**
	 * Sets the data members of <code>msg</code> by performing a SAX parse of
	 * the <code>msgData</code>. This is a convenience method for the
	 * factory.
	 * 
	 * @param msg
	 * @param parser
	 * @throws IllegalArgumentException
	 *             if any of the parameters are <code>null</code>.
	 * @throws MalformedResponseException
	 *             if the <code>msgData</code> could not be parsed.
	 */
	static void initFromXml(ITIMPolicyServiceResponseImpl msg, SAXParser parser)
			throws IllegalArgumentException, MalformedResponseException {
		DefaultHandler saxHandler = new ITIMPolicyServiceResponseImpl.ITIMPolicyServiceResponseHandler(
				msg);
		try {
			InputSource is = new InputSource(new StringReader(msg
					.getMessageData()));
			parser.parse(is, saxHandler);
			msg.setValidFlag(true);
		} catch (SAXException e) {
			throw new MalformedResponseException(e);
		} catch (IOException e) {
			throw new MalformedResponseException(e);
		}

	}
}
