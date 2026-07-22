/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim.policy.impl;

import java.io.IOException;
import java.io.StringWriter;

import com.ibm.di.plugin.pwstore.itim.policy.PolicyServiceMessage;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyServiceRequest;

/**
 * Represents and formats PSWD_REQ_MSG requests to be sent to the ITIM password
 * policy servlet.
 */
public final class ITIMPolicyServiceRequestImpl implements PolicyServiceRequest {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	private static final String BEGIN_ROOT_ELEMENT = "<PSWD_REQ_MSG>";

	private static final String END_ROOT_ELEMENT = "</PSWD_REQ_MSG>";

	private static final String BEGIN_CRED_ELEMENT = "<CREDENTIALS ";

	private static final String END_CRED_ELEMENT = "</CREDENTIALS>";

	private static final String BEGIN_REQUEST_ELEMENT = "<REQUEST ";

	private static final String END_REQUEST_ELEMENT = "</REQUEST>";

	private static final String ATTR_NAME_PRINCIPAL = "principal";

	private static final String ATTR_NAME_PASSWORD = "pswd";

	private static final String ATTR_NAME_SRCDN = "srcDN";

	private static final String ATTR_NAME_USERDN = "userDN";

	private static final String ATTR_OP_CHECK = "op=\"check\" ";

	private static final String ATTR_OP_SYNCH = "op=\"synch\" ";

	private PolicyServiceMessage.ServiceOp reqOp;

	private String sourceDn;

	private String userName;

	private String password;

	private String principalName;

	private String principalPswd;

	ITIMPolicyServiceRequestImpl() {
		super();
	}

	/**
	 * Format an ITIM password synch service request. The general format of such
	 * requests is shown below:<br>
	 * <br>
	 * &lt;PSWD_REQ_MSG&gt; &lt;CREDENTIALS principal="",pswd="" /&gt; &lt;REQUEST
	 * op="check", srcDN="", userDN="", pswd="" /&gt; &lt;/PSWD_REQ_MSG&gt; <br>
	 * <br>
	 * 
	 * @see PolicyServiceMessage#getMessageData()
	 */
	public String getMessageData() throws IOException {
		StringWriter sw = new StringWriter();
		sw.write(ITIMPolicyServiceRequestImpl.BEGIN_ROOT_ELEMENT);

		sw.write(ITIMPolicyServiceRequestImpl.BEGIN_CRED_ELEMENT);
		sw.write(ITIMPolicyServiceRequestImpl.ATTR_NAME_PRINCIPAL);
		sw.write("=\"");
		sw.write(principalName);
		sw.write("\" ");
		sw.write(ITIMPolicyServiceRequestImpl.ATTR_NAME_PASSWORD);
		sw.write("=\"");
		sw.write(principalPswd);
		sw.write("\" >");
		sw.write(ITIMPolicyServiceRequestImpl.END_CRED_ELEMENT);

		sw.write(ITIMPolicyServiceRequestImpl.BEGIN_REQUEST_ELEMENT);
		if (reqOp == ServiceOp.SYNC_PASSWORD) {
			sw.write(ITIMPolicyServiceRequestImpl.ATTR_OP_SYNCH);
		} else {
			sw.write(ITIMPolicyServiceRequestImpl.ATTR_OP_CHECK);
		}
		sw.write(ITIMPolicyServiceRequestImpl.ATTR_NAME_SRCDN);
		sw.write("=\"");
		sw.write(sourceDn);
		sw.write("\" ");
		sw.write(ITIMPolicyServiceRequestImpl.ATTR_NAME_USERDN);
		sw.write("=\"");
		sw.write(userName);
		sw.write("\" ");
		sw.write(ITIMPolicyServiceRequestImpl.ATTR_NAME_PASSWORD);
		sw.write("=\"");
		sw.write(password);
		sw.write("\" >");
		sw.write(ITIMPolicyServiceRequestImpl.END_REQUEST_ELEMENT);

		sw.write(ITIMPolicyServiceRequestImpl.END_ROOT_ELEMENT);

		sw.close();
		return sw.toString();
	}

	/**
	 * @see PolicyServiceMessage#getOperation()
	 */
	public ServiceOp getOperation() {
		return reqOp;
	}

	void setOperation(PolicyServiceMessage.ServiceOp op) {
		if (op == null) {
			throw new IllegalArgumentException();
		}

		reqOp = op;
	}

	/**
	 * @return Returns the password.
	 */
	String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            The password to set.
	 */
	void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return Returns the reqOp.
	 */
	PolicyServiceMessage.ServiceOp getReqOp() {
		return reqOp;
	}

	/**
	 * @return Returns the sourceDn.
	 */
	String getSourceDn() {
		return sourceDn;
	}

	/**
	 * @param sourceDn
	 *            The sourceDn to set.
	 */
	void setSourceDn(String sourceDn) {
		this.sourceDn = sourceDn;
	}

	/**
	 * @return Returns the userName.
	 */
	String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            The userName to set.
	 */
	void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return Returns the principalName.
	 */
	String getPrincipalName() {
		return principalName;
	}

	/**
	 * @param principalName
	 *            The princpalName to set.
	 */
	void setPrincipalName(String principalName) {
		this.principalName = principalName;
	}

	/**
	 * @return Returns the principalPswd.
	 */
	String getPrincipalPswd() {
		return principalPswd;
	}

	/**
	 * @param principalPswd
	 *            The principalPswd to set.
	 */
	void setPrincipalPswd(String principalPswd) {
		this.principalPswd = principalPswd;
	}
}
