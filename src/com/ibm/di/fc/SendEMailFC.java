/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.StringTokenizer;

/**
 * The SendEMail Function Component uses the JavaMail API to send e-mails. By
 * connecting to an Simple Mail Transfer Protocol (SMTP) server, the SendEMail
 * Function Component can send e-mails to multiple recipients and can optionally
 * attach multiple files to e-mails. You can also attach multiple files with
 * different Multipurpose Internet Mail Extensions (MIME) types.
 */
public class SendEMailFC extends Function {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Parameter name.
	 */
	private static final String PARAM_SMTP_SERVER = "smtpServerHost";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_SMTP_PORT = "smtpServerPort";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_SMTP_USER = "username";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_SMTP_PASS = "password";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_SMTP_SSL = "useSSL";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_FROM = "from";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_RECIPIENTS = "recipients";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_REPLYTO = "replyTo";
	private static final String PARAM_CC = "cc";
	private static final String PARAM_BCC = "bcc";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_SMTP_SUBJECT = "subject";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_SMTP_ATTACH = "attachments";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_CONT_TYPE = "contentType";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_SMTP_ENCD = "encoding";

	/**
	 * Attribute name.
	 */
	private static final String IN_ATTRIBUTE_SUBJECT = "subject";
	/**
	 * Attribute name.
	 */
	private static final String IN_ATTRIBUTE_BODY = "body";
	/**
	 * Attribute name.
	 */
	private static final String IN_ATTRIBUTE_ATTACHMENT = "attachments";
	/**
	 * Attribute name.
	 */
	private static final String IN_ATTRIBUTE_FROM = "from";
	/**
	 * Attribute name.
	 */
	private static final String IN_ATTRIBUTE_RECIPIENTS = "recipients";
	/**
	 * Attribute name.
	 */
	private static final String IN_ATTRIBUTE_REPLYTO = "replyTo";
	private static final String IN_ATTRIBUTE_CC = "cc";
	private static final String IN_ATTRIBUTE_BCC = "bcc";
	/**
	 * Attribute name.
	 */
	private static final String IN_ATTRIBUTE_SMTP_SERVER = "smtpServerHost";
	/**
	 * Attribute name.
	 */
	private static final String IN_ATTRIBUTE_SMTP_PORT = "smtpServerPort";

	/**
	 * Not required part.
	 */
	private static final boolean SUBJECT_REQUIRED = false;
	/**
	 * Required part.
	 */
	private static final boolean BODY_REQUIRED = true;
	/**
	 * Not required part.
	 */
	private static final boolean FROM_REQUIRED = false;
	/**
	 * Not required part.
	 */
	private static final boolean RECIPIENTS_REQUIRED = false;
	/**
	 * Not required part.
	 */
	private static final boolean REPLYTO_REQUIRED = false;
	/**
	 * Not required part.
	 */
	private static final boolean SMTP_SERVER_REQUIRED = false;
	/**
	 * Not required part.
	 */
	private static final boolean SMTP_PORT_REQUIRED = false;
	/**
	 * Parameter name.
	 */
	private static final String OUT_ATTRIBUTE_STATUS = "status";
	/**
	 * Parameter name.
	 */
	private static final String STATUS_OK = "OK";
	/**
	 * Parameter name.
	 */
	private static final String PROP_SMTP_HOST = "mail.smtp.host";
	/**
	 * Parameter name.
	 */
	private static final String PROP_SMTP_PORT = "mail.smtp.port";
	/**
	 * Parameter name.
	 */
	private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;
	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "sendemail";

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * SMTP server host value.
	 */
	private String mSMTPServerHost = null;

	/**
	 * SMTP server port value.
	 */
	private String mSMTPServerPort = null;

	/**
	 * SMTP server user value.
	 */
	private String mSMTPServerUser = null;

	/**
	 * SMTP server password value.
	 */
	private String mSMTPServerPass = null;

	/**
	 * Is SSL required.
	 */
	private boolean mSMTPServerSSL = false;

	/**
	 * Information of the sender.
	 */
	private String mFrom = null;

	/**
	 * Recipients list.
	 */
	private String mRecipients = null;

	/**
	 * Reply to value.
	 */
	private String mReplyTo = null;

	private String mCc = null;
	private String mBcc = null;
	
	/**
	 * Subject of the letter.
	 */
	private String mSubject = null;

	/**
	 * Attachments value.
	 */
	private String mAttchments = null;

	/**
	 * Set e-mail body part's MIME content type. 'text/plain' is used if left
	 * empty.
	 */
	private String mContentType = null;

	/**
	 * Encoding of the server.
	 */
	private String mSMTPServerEncd = null;

	/**
	 * Called once to initialize the Function Component.
	 *
	 * @param obj -
	 *            ignored
	 * @throws Exception
	 *             if an error occurs.
	 *
	 */
	public void initialize(Object obj) throws Exception {

		mSMTPServerHost = getStringParameter(PARAM_SMTP_SERVER);

		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_SMTP_SERVER, mSMTPServerHost }));
		}

		mSMTPServerPort = getStringParameter(PARAM_SMTP_PORT);
		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_SMTP_PORT, mSMTPServerPort }));
		}

		mSMTPServerUser = getStringParameter(PARAM_SMTP_USER);
		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_SMTP_USER, mSMTPServerUser }));
		}

		mSMTPServerPass = getStringParameter(PARAM_SMTP_PASS);
		String strUseSSL = (String) getParam(PARAM_SMTP_SSL);

		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_SMTP_SSL, strUseSSL }));
		}

		if (strUseSSL != null) {
			mSMTPServerSSL = Boolean.valueOf(strUseSSL).booleanValue();
		}

		mFrom = getStringParameter(PARAM_FROM);
		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_FROM, mFrom }));
		}

		mRecipients = getStringParameter(PARAM_RECIPIENTS);
		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_RECIPIENTS, mRecipients }));
		}

		mReplyTo = getStringParameter(PARAM_REPLYTO);
		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_REPLYTO, mReplyTo }));
		}

		mCc = getStringParameter(PARAM_CC);
		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_CC, mCc }));
		}

		mBcc = getStringParameter(PARAM_BCC);
		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_BCC, mBcc }));
		}

		mSubject = getStringParameter(PARAM_SMTP_SUBJECT);
		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_SMTP_SUBJECT, mSubject }));
		}

		mAttchments = getStringParameter(PARAM_SMTP_ATTACH);
		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_SMTP_ATTACH, mAttchments }));
		}

		mContentType = getStringParameter(PARAM_CONT_TYPE);
		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_CONT_TYPE, mContentType }));
		}

		mSMTPServerEncd = getStringParameter(PARAM_SMTP_ENCD);
		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_SMTP_ENCD, mSMTPServerEncd }));
		}

		super.initialize(null);
	}

	/**
	 * This method accepts an object of type Entry, extracts the information for
	 * the e-mail (from, recipients , subjects , attachments), creates a new
	 * message objects with attributes listed in the incoming object and sends
	 * this message
	 *
	 * @param obj
	 *            An Entry containing the values of the parameters
	 * @return Returns the calling object
	 * @throws Exception
	 *             if the parameter is not an instance of the {@link Entry}
	 *             class, if the FC has not been initialized or if the method
	 *             fails
	 */
	public Object perform(Object obj) throws Exception {
		verifyInitialized();

		if (!(obj instanceof Entry)) {
			String errorMessage = sResHash
					.getString("INVALID.PERFORM.PARAMETER.TYPE");
			logerror(errorMessage);
			throw new Exception(errorMessage);
		}
		Entry entry = (Entry) obj;

		String subject = getStringAttribute(entry, IN_ATTRIBUTE_SUBJECT,
				SUBJECT_REQUIRED);
		subject = getPrecedingParameter(subject, mSubject, PARAM_SMTP_SUBJECT);

		String body = getStringAttribute(entry, IN_ATTRIBUTE_BODY,
				BODY_REQUIRED);

		Attribute attachments = entry.getAttribute(IN_ATTRIBUTE_ATTACHMENT);

		String from = getStringAttribute(entry, IN_ATTRIBUTE_FROM,
				FROM_REQUIRED);
		from = getPrecedingParameter(from, mFrom, PARAM_FROM);

		String recipients = getStringAttribute(entry, IN_ATTRIBUTE_RECIPIENTS,
				RECIPIENTS_REQUIRED);
		recipients = getPrecedingParameter(recipients, mRecipients,
				PARAM_RECIPIENTS);

		String replyTo = getStringAttribute(entry, IN_ATTRIBUTE_REPLYTO,
				REPLYTO_REQUIRED);
		replyTo = getPrecedingParameter(replyTo, mReplyTo, PARAM_REPLYTO);

		String cc = getStringAttribute(entry, IN_ATTRIBUTE_CC, false);
		cc = getPrecedingParameter(cc, mCc, PARAM_CC);

		String bcc = getStringAttribute(entry, IN_ATTRIBUTE_BCC, false);
		bcc = getPrecedingParameter(bcc, mBcc, PARAM_BCC);

		String smtpServerHost = getStringAttribute(entry,
				IN_ATTRIBUTE_SMTP_SERVER, SMTP_SERVER_REQUIRED);
		smtpServerHost = getPrecedingParameter(smtpServerHost, mSMTPServerHost,
				PARAM_SMTP_SERVER);

		String smtpServerPort = getStringAttribute(entry,
				IN_ATTRIBUTE_SMTP_PORT, SMTP_PORT_REQUIRED);
		smtpServerPort = getPrecedingParameter(smtpServerPort, mSMTPServerPort,
				PARAM_SMTP_PORT);

		// The mail.mime.charset System property can be used to specify the
		// default MIME charset
		// to use for encoded words and text parts that don't otherwise specify
		// a charset.
		// Normally, the default MIME charset is derived from the default Java
		// charset, as specified
		// in the file.encoding System property
		if (mSMTPServerEncd != null && !"".equals(mSMTPServerEncd.trim())) {
			System.setProperty("mail.mime.charset", mSMTPServerEncd);
		}

		Properties props = new Properties(System.getProperties());

		props.setProperty(PROP_SMTP_HOST, smtpServerHost);
		props.setProperty(PROP_SMTP_PORT, smtpServerPort);

		if (mSMTPServerSSL) {
			// If set, specifies the name of a class that implements the
			// javax.net.SocketFactory
			// interface. This class will be used to create SMTP sockets
			props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
			// If set to true, failure to create a socket using the specified
			// socket factory
			// class will cause the socket to be created using the
			// java.net.Socket class.
			props.setProperty("mail.smtp.socketFactory.fallback", "false");
			// Specifies the port to connect to when using the specified socket
			// factory.
			// If not set, the default port will be used.
			props.setProperty("mail.smtp.socketFactory.port", smtpServerPort);
		}

		Session session = null;
		//if (mSMTPServerUser == null && mSMTPServerPass == null) {  Defect #13718
			if ( (mSMTPServerUser == null || mSMTPServerUser.equals("") ) && (mSMTPServerPass == null || mSMTPServerPass.equals("") ))   {
				props.put("mail.smtp.socketFactory.fallback", "true");
				props.put("mail.smtp.auth", "false");
				session = Session.getInstance(props, null);
		} else {
			// If true, attempt to authenticate the user using the AUTH command.
			props.put("mail.smtp.auth", "true");
			session = Session.getInstance(props, new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(mSMTPServerUser,
							mSMTPServerPass);
				}
			});
		}

		// Create a new message
		MimeMessage msg = new MimeMessage(session);

		// Set the FROM field
		msg.setFrom(new InternetAddress(from));

		// Set TO field
		StringTokenizer st = new StringTokenizer(recipients, ",");
		Address[] toAddress = new Address[st.countTokens()];
		int toaddressCounter = 0;
		while (st.hasMoreTokens()) {
			toAddress[toaddressCounter++] = new InternetAddress(st.nextToken());
		}
		msg.setRecipients(Message.RecipientType.TO, toAddress); 

		// Set CC field
		st = new StringTokenizer(cc, ",");
		Address[] ccAddress = new Address[st.countTokens()];
		int ccaddressCounter = 0;
		while (st.hasMoreTokens()) {
			ccAddress[ccaddressCounter++] = new InternetAddress(st.nextToken());			
		}
		msg.setRecipients(Message.RecipientType.CC, ccAddress);

		// Set BCC field
		st = new StringTokenizer(bcc, ",");
		Address[] bccAddress = new Address[st.countTokens()];
		int bccaddressCounter = 0;		
		while (st.hasMoreTokens()) {
			bccAddress[bccaddressCounter++] = new InternetAddress(st.nextToken());
		}
		msg.setRecipients(Message.RecipientType.BCC, bccAddress);

		// Set Reply To field
		st = new StringTokenizer(replyTo, ",");
		Address[] replyAddress = new Address[st.countTokens()];
		int addressCounter = 0;
		while (st.hasMoreTokens()) {
			replyAddress[addressCounter++] = new InternetAddress(st.nextToken());
		}
		msg.setReplyTo(replyAddress);

		// Set Subject
		msg.setSubject(subject);

		Multipart multipart = new MimeMultipart();

		// Set Body
		MimeBodyPart mbpBody = new MimeBodyPart();

		if (mContentType != null && !"".equals(mContentType.trim())) {
			mbpBody.setContent(body, mContentType);
		} else {
			// setText sets this part's content, with a MIME type of
			// "text/plain"
			mbpBody.setText(body);
		}

		multipart.addBodyPart(mbpBody);

		// Set Attachment files
		if (attachments != null && attachments.size() > 0) {
			// from attributes
			for (int i = 0; i < attachments.size(); i++) {
				MimeBodyPart mbp2 = createAttachBodyPart((String) attachments
						.getValue(i));
				if (mbp2 != null) {
					multipart.addBodyPart(mbp2);
				}
			}
		} else if (mAttchments != null) {
			// from parameters
			StringTokenizer at = new StringTokenizer(mAttchments, "\r\n");
			while (at.hasMoreTokens()) {
				MimeBodyPart mbp2 = createAttachBodyPart(at.nextToken());
				if (mbp2 != null) {
					multipart.addBodyPart(mbp2);
				}
			}
		}

		msg.setContent(multipart);

		Transport.send(msg);

		entry.setAttribute(OUT_ATTRIBUTE_STATUS, STATUS_OK);

		return obj;
	}

	/**
	 * Creates and returns body part for attachment file if the specified path
	 * is correct and the file can be read.
	 *
	 * @param aPath
	 *            path of the file ,String.
	 * @return {@link MimeBodyPart}
	 * @throws MessagingException
	 *             if an error occurs.
	 */
	private MimeBodyPart createAttachBodyPart(String aPath)
			throws MessagingException {
		MimeBodyPart mbp2 = null;

		// discovering content type from name of the file
		int iGE = aPath.indexOf(">");
		String aContentType = null;
		if (iGE != -1) {
			aContentType = aPath.substring(iGE + 1);
			aPath = aPath.substring(0, iGE);
		}

		File file = new File(aPath);
		if (file.exists() && file.canRead()) {
			mbp2 = new MimeBodyPart();
			FileDataSource fds = new FileDataSource(file);
			mbp2.setDataHandler(new DataHandler(fds));
			mbp2.setFileName(fds.getName());
			if (aContentType != null) {
				mbp2.setHeader("Content-Type", aContentType);
			}
		}
		return mbp2;
	}

	/**
	 * Version information.
	 *
	 * @return version information.
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I%, 20%E%";
	}

	/**
	 * Retrieves a value, specified by the user.
	 *
	 * @param parameterName
	 *            name of the parameter , String.
	 * @return the value of the parameter.
	 * @throws Exception :
	 *             never
	 */
	private String getStringParameter(String parameterName) throws Exception {
		String parameter = (String) getParam(parameterName);
		if (parameter != null) {
			parameter = parameter.trim();
		}

		return parameter;
	}

	/**
	 * Extracts the value of the attribute of an entry and returns its String
	 * representation.
	 *
	 * @param entry
	 *            {@link Entry}
	 * @param attributeName
	 *            String
	 * @param isRequired
	 *            boolean , specifies if the attribute is required or optional
	 * @return the value of the attribute, or <code>null</code> if the value
	 *         does not exist.
	 * @throws Exception
	 *             if the attribute was required , but no values was specified.
	 */
	private String getStringAttribute(Entry entry, String attributeName,
			boolean isRequired) throws Exception {
		String value = (String) entry.getObject(attributeName);
		if (value == null) {
			if (!isRequired) {
				value = "";
			} else {
				String errorMessage = sResHash.getString(
						"ENTRY.ATTRIBUTE.NOT.PRESENT", attributeName);
				logerror(errorMessage);
				throw new Exception(errorMessage);
			}
		}

		return value;
	}

	/**
	 * Returns the first parameter if not <code>null</code> and empty String ,
	 * or the second one if it satisfies the same conditions, but the first one
	 * doesn't, otherwise it throws Exception. Note:If the first two parameter
	 * do not satisfy the conditions and the third one is {@link #PARAM_REPLYTO},
	 * the method returns an empty String
	 *
	 * @param major
	 *            String
	 * @param minor
	 *            String
	 * @param parameterName
	 *            the parameter name, String.
	 * @return String
	 * @throws Exception
	 *             conditions are not satisfied.
	 */
	private String getPrecedingParameter(String major, String minor,
			String parameterName) throws Exception {
		if (major != null && !major.equals("")) {
			return major;
		} else if (minor != null && !minor.equals("")) {
			return minor;
		} else {
			if (parameterName.equals(PARAM_REPLYTO) || parameterName.equals(PARAM_CC) || parameterName.equals(PARAM_BCC)) {
				return "";
			}
			String errorMessage = sResHash.getString(
					"NO.PARAMETER.OR.ATTRIBUTE", parameterName);
			logerror(errorMessage);
			throw new Exception(errorMessage);
		}
	}

	/**
	 * Logs an error message.
	 *
	 * @param errorMessage
	 *            the message.
	 */
	private void logerror(String errorMessage) {
		if (logger != null) {
			logger.logerror(errorMessage);
		}
	}

	/**
	 * Logs debug message if the component is in debug mode.
	 *
	 * @param debugMessage
	 *            message to write.
	 */
	private void logdebug(String debugMessage) {
		if (logger != null) {
			logger.logdebug(debugMessage);
		}
	}
}
