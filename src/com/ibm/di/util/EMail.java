/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// EMail.java
//
//
//
package com.ibm.di.util;

import java.io.*;

import javax.mail.*;
import javax.mail.event.*;
import javax.mail.internet.*;
import javax.activation.*;

public class EMail {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private MimeMessage message;

	private Object parts;

	private MimeMultipart multipart;

	public EMail(MimeMessage message) throws IOException, MessagingException {
		this.message = message;
		this.parts = message.getContent();
		if (this.parts instanceof MimeMultipart)
			multipart = (MimeMultipart) parts;
		else
			multipart = null;

	}

	public int getBodyPartCount() throws MessagingException {
		if (multipart != null)
			return multipart.getCount();
		else
			return 1;
	}

	public String getContentType(int bodypart) throws MessagingException {
		if (multipart == null)
			return message.getContentType();
		else
			return multipart.getBodyPart(bodypart).getContentType();
	}

	public BodyPart getBodyPart(int bodypart) throws MessagingException {
		if (multipart == null)
			return null;
		else
			return multipart.getBodyPart(bodypart);
	}
}
