/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.Flags.Flag;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.HeaderTerm;
import javax.mail.search.IntegerComparisonTerm;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.MessageNumberTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import javax.mail.search.SizeTerm;
import javax.mail.search.SubjectTerm;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.SearchCriteria.rscSearch;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.StringTokenizer;
import com.sun.mail.pop3.POP3Message;

/**
 * Simple access to POP/IMAP based mailboxes.
 */
public class MailboxConnector extends Connector implements ConnectorInterface, MessageCountListener {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "mailboxconnector";

	/**
	 * Parameter name in the configuration for mailServer.
	 */
	private final static String PARAM_MAIL_SERVER = "mailServer";

	/**
	 * Parameter name in the configuration for useSSL.
	 */
	private final static String PARAM_USE_SSL = "useSSL";

	/**
	 * Parameter name in the configuration for mailProtocol.
	 */
	private final static String PARAM_MAIL_PROTOCOL = "mailProtocol";

	/**
	 * Parameter name in the configuration for mailUser.
	 */
	private final static String PARAM_MAIL_USERNAME = "mailUser";

	/**
	 * Parameter name in the configuration for mailPassword.
	 */
	private final static String PARAM_MAIL_PASSWORD = "mailPassword";

	/**
	 * Parameter name in the configuration for mailFolder.
	 */
	private final static String PARAM_MAIL_FOLDER = "mailFolder";

	/**
	 * Parameter name in the configuration for pollInterval.
	 */
	private final static String PARAM_MAIL_POLL_INTERVAL = "pollInterval";

	/**
	 * Parameter name in the configuration for createFolder.
	 */
	private final static String PARAM_CREATE_FOLDER = "createFolder";

	/**
	 * Parameter name in the configuration for getSubfolders.
	 */
	private final static String PARAM_GET_SUBFOLDERS = "getSubfolders";

	/**
	 * Parameter name in the configuration for excludeFolders.
	 */
	private final static String PARAM_EXCLUDE_FOLDERS = "excludeFolders";

	/**
	 * Default ssl socket factory full package name.
	 */
	private final static String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

	/**
	 * Poll interval to be used if the "pollInterval" parameter is not set or is
	 * empty
	 */
	private final static long POLL_INTERVAL_DEFAULT = 60;

	/**
	 * Value for poll interval if you need the Connector to go through the
	 * mailbox and terminate without polling again.
	 */
	private final static long POLL_INTERVAL_TERMINATE = -1;

	/**
	 * IMAP protocol string
	 */
	private final static String IMAP = "imap";

	/**
	 * POP3 protocol string
	 */
	private final static String POP3 = "pop3";

	/**
	 * The name of the output Attributte, holding message or array of messages,
	 * which should be appended to the datasource by the connector in Addonly
	 * mode
	 */
	private final static String ATTRIBUTE_ADD_MESSAGE = "mail.addMessage";

	/**
	 * The name of the mail message 'From' Header
	 */
	private final static String HEADER_FROM = "From";

	/**
	 * The name of the mail message 'Sender' header
	 */
	private final static String HEADER_SENDER = "Sender";

	/**
	 * The name of the mail message 'To' header
	 */
	private final static String HEADER_TO = "To";

	/**
	 * The name of the mail message 'Reply-To' header
	 */
	private final static String HEADER_REPLYTO = "Reply-To";

	/**
	 * The name of the mail message 'Cc' header
	 */
	private final static String HEADER_CC = "Cc";

	/**
	 * Prefix used for each mail attribute.
	 */
	private final static String MAIL_ATTRIBUTE_PREFIX = "mail.";

	/**
	 * ServerName used in Connector
	 */
	private String mServerName;

	/**
	 * ServerPort used in Connector
	 */
	private int mServerPort;

	/**
	 * If true Connector initialize SSL Connection
	 */
	private boolean mUseSSL;

	/**
	 * javax.mail.Session used in Connector
	 */
	private Session mSession;

	/**
	 * javax.mail.Store used in Connector
	 */
	private Store mStore;

	/**
	 * javax.mail.Folder used in Connector
	 */
	private Folder mFolder;

	/**
	 * username used in Connector
	 */
	private String mUsername;

	/**
	 * password used in Connector
	 */
	private String mPassword;

	/**
	 * protocol used in Connector /imap or pop3/
	 */
	private String mProtocol;

	/**
	 * pollInterval used in Connector in seconds
	 */
	private long mPollInterval;

	/**
	 * Name of mail folder used in Connector
	 */
	private String mFolderName;

	/**
	 * List of folders, which the Connector will iterate through
	 */
	private List<Folder> iteratedFolders;

	/**
	 * downloaded mail messages
	 */
	private Message[] mMessages;

	/**
	 * message index
	 */
	private int mMessageIndex;

	/**
	 * termination flag
	 */
	private boolean mIsTerminated;

	/**
	 * Names of the excluded mail folders when iterating through the whole
	 * mailbox. Case insensitive.
	 */
	private List<String> excludedFolders;

	/**
	 * flag used for getting the subfolders of the specified folder
	 */
	private boolean getSubfolders;

	/**
	 * flag used to create the specified folder, if it does not exist
	 */
	private boolean createFolder;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = new ResourceHash(PROPERTIES_FILE);

	/**
	 * Constructor for the MailboxConnector object
	 */
	public MailboxConnector() {
		super();
		setName("MailboxConnector");
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE, ConnectorConfig.LOOKUP_MODE, ConnectorConfig.DELETE_MODE,
				ConnectorConfig.ADDONLY_MODE, ConnectorConfig.UPDATE_MODE });
	}

	/**
	 * Sets the internetAddress attribute of the MailboxConnector object
	 * 
	 * @param entry
	 *            The Entry, where the information is added
	 * @param name
	 *            The name of Attribute assigned to the Entry
	 * @param msgHeader
	 *            Values of the message Header to be added
	 */
	private void setAddressHeader(Entry entry, String name, String[] msgHeader) {
		if (msgHeader == null) {
			return;
		}

		Attribute attr = new Attribute(name);
		for (int i = 0; i < msgHeader.length; i++) {
			attr.addValue(msgHeader[i]);
		}

		entry.setAttribute(attr);
	}

	/**
	 * Initialize SSL if useSSL flag is set
	 * 
	 * @return Session
	 */
	private Session initializeSSL() {
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.MAILBOX.SSLINIT.INFO"));
		}
		// get our own copy of the system properties
		Properties props = (Properties) System.getProperties().clone();
		String port = String.valueOf(mServerPort);
		if (mServerPort == 0) {
			if (IMAP.equals(mProtocol)) {
				port = "993";
			} else {
				port = "995";
			}
		}
		props.setProperty("mail." + mProtocol + ".socketFactory.class", SSL_FACTORY);
		props.setProperty("mail." + mProtocol + ".socketFactory.fallback", "false");
		props.setProperty("mail." + mProtocol + ".port", port);
		props.setProperty("mail." + mProtocol + ".socketFactory.port", port);

		Session session = Session.getInstance(props);
		return session;
	}

	/**
	 * Reads connector parameter's values and initialize the Connector.
	 * 
	 * @param aObj
	 *            Null, Socket or ConnectorMode class
	 * 
	 * @throws Exception
	 *             If invalid Connector parameter values are supplied.
	 */
	public void initialize(Object aObj) throws Exception {
		String serverStr = getParam(PARAM_MAIL_SERVER);
		if (serverStr == null) {
			throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.MISSING.MAILSERVER"));
		}

		mProtocol = getParam(PARAM_MAIL_PROTOCOL);
		if (((ConnectorConfig) this.getConfiguration()).getMode().equals(ConnectorConfig.ADDONLY_MODE)
				&& mProtocol.equalsIgnoreCase(POP3)) {
			throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.POP3.NOT.SUPPORT.APPEND"));
		}
		mUsername = getParam(PARAM_MAIL_USERNAME);
		if (mUsername == null || mUsername.trim().length() == 0) {
			throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.MISSING.MAILUSER"));
		}

		mPassword = getParam(PARAM_MAIL_PASSWORD);
		mFolderName = getParam(PARAM_MAIL_FOLDER);
		if (mFolderName == null || mFolderName.trim().length() == 0) {
			mFolderName = null; // no folder value

			if (((ConnectorConfig) this.getConfiguration()).getMode().equals(ConnectorConfig.ADDONLY_MODE)) {
				throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.MISSING.MAILFOLDER"));
			}

			if (mProtocol.equalsIgnoreCase(POP3)) {
				mFolderName = "INBOX";
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.MAILBOX.POP3.SET.TO.INBOX"));
				}
			}
		} else if (mProtocol.equalsIgnoreCase(POP3) && !(mFolderName.trim().equalsIgnoreCase("INBOX"))) {
			throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.POP3.NOTINBOX.EXCEP"));
		}

		String strUseSSL = getParam(PARAM_USE_SSL);
		mIsTerminated = false;
		mUseSSL = false;
		if (strUseSSL != null) {
			mUseSSL = Boolean.valueOf(strUseSSL).booleanValue();
		}

		String pollIntervalStr = getParam(PARAM_MAIL_POLL_INTERVAL);
		try {
			mPollInterval = (pollIntervalStr == null) ? POLL_INTERVAL_DEFAULT : Long.parseLong(pollIntervalStr);
		} catch (NumberFormatException e) {
			mPollInterval = POLL_INTERVAL_DEFAULT;
			logmsg(sResHash.getString("CONNECTOR.MAILBOX.INVALID.POLLINTERVAL", new Object[] { pollIntervalStr,
					"" + POLL_INTERVAL_DEFAULT }));
		}
		if (mPollInterval < 0 && mPollInterval != POLL_INTERVAL_TERMINATE) {
			mPollInterval = POLL_INTERVAL_DEFAULT;
			logmsg(sResHash.getString("CONNECTOR.MAILBOX.INVALID.POLLINTERVAL2", new Object[] { pollIntervalStr,
					"" + POLL_INTERVAL_DEFAULT }));
		}

		Properties props = System.getProperties();
		mSession = mUseSSL ? initializeSSL() : Session.getInstance(props);
		mStore = mSession.getStore(mProtocol);

		// following handles port specification: defect 459 ibmdi_510, gwb
		mServerName = null;

		// initialize just in order to avoid NullPointerException when calling
		// 'addMsg()'
		mMessages = new Message[0];

		serverStr = serverStr.trim();
		int delimloc = serverStr.indexOf(" ");
		if (delimloc > -1) {
			// assume a port is specified
			mServerName = serverStr.substring(0, delimloc);
			String remainder = (serverStr.substring((delimloc + 1))).trim();
			mServerPort = 0;
			try {
				mServerPort = Integer.valueOf(remainder).intValue();
			} catch (NumberFormatException e) {
				// garbage passed in, ignore port specification, and continue
				throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.BAD.PORT", e.toString()));
			}
		} else {
			mServerName = serverStr;
		}

		String strCreateFolder = getParam(PARAM_CREATE_FOLDER);
		createFolder = false;
		if (strCreateFolder != null) {
			createFolder = Boolean.valueOf(strCreateFolder).booleanValue();
		}

		String strGetSubfolders = getParam(PARAM_GET_SUBFOLDERS);
		getSubfolders = false;
		if (strGetSubfolders != null && (!mProtocol.equalsIgnoreCase(POP3))) {
			getSubfolders = Boolean.valueOf(strGetSubfolders).booleanValue();
		}

		String strExcludeFolders = getParam(PARAM_EXCLUDE_FOLDERS);
		excludedFolders = new LinkedList<String>();
		if (strExcludeFolders != null && strExcludeFolders.trim().length() > 0) {
			StringTokenizer st = new StringTokenizer(strExcludeFolders, ",");
			while (st.hasMoreTokens()) {
				excludedFolders.add(st.nextToken().trim().toLowerCase(Locale.ENGLISH));
			}
		}

		iteratedFolders = new ArrayList<Folder>();

		connectServer();
	}

	/**
	 * Connects to Mail Server and opens Folders
	 * 
	 * @exception Exception
	 *                if can not connect to Mail Server
	 */
	private void connectServer() throws Exception {
		if (mServerPort != 0) {
			// Connect to message store using 4-arg
			mStore.connect(mServerName, mServerPort, mUsername, mPassword);
		} else {
			// Connect to message store using 3-arg
			mStore.connect(mServerName, mUsername, mPassword);
		}

		if (!((ConnectorConfig) this.getConfiguration()).getMode().equals(ConnectorConfig.ADDONLY_MODE)) {
			if (mFolderName == null) {
				listAllFolders(mStore);
			} else {
				Folder mainFolder = mStore.getFolder(mFolderName);
				if (mainFolder == null || !mainFolder.exists()) {
					throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.FOLDERNOTEXIST.EXCEP", mFolderName));
				}
				if (getSubfolders) {
					getFolder(mainFolder);
				} else {
					iteratedFolders.add(mainFolder);
				}
			}
			loadAllMessages();
		} else {
			mFolder = mStore.getFolder(mFolderName);
			// Open the Folder
			if (mFolder == null || !mFolder.exists()) {
				if (!createFolder) {
					throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.CHECK.AUTOCREATE.FOLDER.EXCEP", mFolderName));
				}
				boolean isCreated = mFolder.create(Folder.HOLDS_MESSAGES);
				if (!isCreated) {
					throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.CREATE.FOLDER.EXCP", mFolderName));
				}
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.MAILBOX.FOLDER.CREATED", mFolderName));
				}
			}
			mFolder.open(Folder.READ_WRITE);
		}

	}

	/**
	 * Gets the next message from an open MailboxConnector object
	 * 
	 * @return The next Entry
	 * @throws Exception
	 *             If retrieving the next Entry fails.
	 */
	public Entry getNextEntry() throws Exception {
		Entry result = null;

		while (!mIsTerminated) {
			if (mMessageIndex >= mMessages.length) {
				if (mPollInterval == POLL_INTERVAL_TERMINATE) {
					result = null;
					disconnectServer();
					break;
				}

				if ((mPollInterval * 1000) < 0) {
					mPollInterval = POLL_INTERVAL_DEFAULT;
					logmsg(sResHash.getString("CONNECTOR.MAILBOX.INVALID.POLLINTERVAL2", new Object[] {
							String.valueOf(mPollInterval), "" + POLL_INTERVAL_DEFAULT }));
				}

				try {
					mMessages = new Message[0];
					Thread.sleep(mPollInterval * 1000);
				} catch (InterruptedException ex) {
					result = null;
					break;
				}

				// if POP3 protocol is specified, then the Connector extracts in
				// its buffer all available messages from the store on each
				// poll.
				// If IMAP is used, getMessageCount() method is called for each
				// Folder, so that the buffer is updated only with the new
				// messages.
				if (mProtocol.equalsIgnoreCase(POP3)) {
					disconnectServer();
					connectServer();
				} else {
					for (Folder folder : iteratedFolders) {
						if (folder.getType() != Folder.HOLDS_FOLDERS) {
							folder.getMessageCount();
						}
					}
				}
				mMessageIndex = 0;
			} else {
				Message m = mMessages[mMessageIndex++];
				if (m == null) {
					continue;
				}
				try {
					result = msg2entry(m);
					break;
				} catch (Exception ex) {
					logmsg(sResHash.getString("CONNECTOR.MAILBOX.CREATEENTRY.WARN", new Object[] { "" + mMessageIndex,
							ex.toString() }));
					continue;
				}
			}
		}

		return result;
	}

	/**
	 * Returns the {@link Store} object used by the Connector.
	 * 
	 * @return the Store object this Connector is working with.
	 */
	public Store getStore() {
		return mStore;
	}

	/**
	 * Gets notification for added message
	 * 
	 * @param aMce
	 *            MessageCountEvent
	 */
	public void messagesAdded(MessageCountEvent aMce) {
		mMessages = addMsg(aMce.getMessages());
	}

	/**
	 * Gets notification for removed message
	 * 
	 * @param aMce
	 *            MessageCountEvent
	 */
	public void messagesRemoved(MessageCountEvent aMce) {
		removeMsg(aMce.getMessages());
	}

	/**
	 * Add messages to messages object
	 * 
	 * @param aMessagesAdded
	 *            messages that must be added
	 * @return all messages
	 */
	private Message[] addMsg(Message[] aMessagesAdded) {
		synchronized (mMessages) {
			ArrayList<Message> messages2 = new ArrayList<Message>();
			messages2.addAll(Arrays.asList(mMessages));
			messages2.addAll(Arrays.asList(aMessagesAdded));
			return messages2.toArray(new Message[0]);
		}
	}

	/**
	 * Remove messages from mMessages object.
	 * 
	 * @param aMessagesRemoved
	 *            messages that must be removed
	 */
	private void removeMsg(Message[] aMessagesRemoved) {
		synchronized (mMessages) {
			for (int i = 0; i < aMessagesRemoved.length; i++) {
				for (int j = 0; j < mMessages.length; j++) {
					try {
						if (mMessages[j] != null && mMessages[j].isExpunged() //
								&& mMessages[j].getMessageNumber() == aMessagesRemoved[i].getMessageNumber())
							mMessages[j] = null;
					} catch (Exception ex) {
						logmsg(sResHash.getString("CONNECTOR.MAILBOX.DELETEENTRY.WARN", new Object[] { "" + mMessageIndex,
								ex.toString() }));
						continue;
					}
				}
			}
		}
	}

	/**
	 * Prepare the Connector for sequential read
	 * 
	 */
	public void selectEntries() {
		mMessageIndex = 0;
	}

	/**
	 * Finds an existing entry. The search criteria specifies which entry to
	 * locate
	 * 
	 * @param aSearch
	 *            The search criteria used to locate the entry to be modified,
	 *            search criteria can be /mail.messagenumber, mail.from,
	 *            mail.to, mail.cc, mail.subject, mail.messageid
	 * 
	 * @return The entry found, or null if no or multiple entries found
	 * @exception Exception
	 *                Any exceptions thrown by the connector's underlying
	 *                classes
	 */
	public Entry findEntry(SearchCriteria aSearch) throws Exception {
		clearFindEntries();

		SearchTerm[] terms = new SearchTerm[aSearch.size()];
		SearchTermsHandler handler = new SearchTermsHandler();
		for (int i = 0; i < aSearch.size(); i++) {
			rscSearch searchCriterion = aSearch.getCriteria(i);
			terms[i] = handler.createSearchTerm(searchCriterion.name, searchCriterion.value, searchCriterion.match,
					searchCriterion.negate);
		}

		SearchTerm resultingTerm = null;
		if (terms.length == 1) {
			resultingTerm = terms[0];
		} else {
			if (aSearch.getType() == SearchCriteria.SEARCH_OR) {
				resultingTerm = new OrTerm(terms);
			} else {
				resultingTerm = new AndTerm(terms);
			}
		}

		Vector<Message> msgs = new Vector<Message>();
		for (Folder folder : iteratedFolders) {
			if (folder.getType() != Folder.HOLDS_FOLDERS) {
				msgs.addAll(Arrays.asList(folder.search(resultingTerm)));
			} else {
				logmsg(sResHash.getString("CONNECTOR.MAILBOX.DOES.NOT.HOLD.MESSAGES", folder));
			}
		}

		for (int i = 0; i < msgs.size() && addFindEntry(msg2entry(msgs.elementAt(i))); i++) {
		}

		if (getFindEntryCount() == 1) {
			return getFirstFindEntry();
		}
		return null;
	}

	/**
	 * Deletes an existing entry. The search criteria specifies which entry to
	 * modify.
	 * 
	 * @param aEntry
	 *            The entry data
	 * @param aSearch
	 *            The search criteria used to locate the entry to be deleted
	 * 
	 * @exception Exception
	 *                Any exceptions thrown by the connector's underlying
	 *                classes
	 */
	public void deleteEntry(Entry aEntry, SearchCriteria aSearch) throws Exception {
		if (aEntry == null)
			aEntry = findEntry(aSearch);

		if (aEntry == null && getFindEntryCount() > 0) {
			throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.DELETE.MORETHANONE.EXCEP"));
		}

		if (aEntry == null) {
			throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.DELETE.NOTFOUND.EXCEP"));
		}

		Attribute attr = aEntry.getAttribute("mail.message");
		if (attr == null) {
			throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.DELETE.ATTRMISSING.EXCEP"));
		}
		Message m = (Message) attr.getValue(0);

		if (m == null) {
			throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.DELETE.ATTRMISSING.EXCEP"));
		}
		m.setFlag(Flags.Flag.DELETED, true);
	}

	/**
	 * Sets all needed attributes in the Entry.
	 * 
	 * @param aMsg
	 *            Mail message object
	 * @return The Entry
	 * 
	 * @exception Exception
	 *                If message is corrupted or deleted
	 */
	private Entry msg2entry(Message aMsg) throws Exception {

		Entry e = new Entry();

		// Get system flags. Note that only the enabled ones are returned.
		Flag[] systemFlags = aMsg.getFlags().getSystemFlags();
		for (Flag systemFlag : systemFlags) {
			String flagName = SystemFlags.getFullFlagName(systemFlag);
			if (flagName != null) {
				e.setAttribute(flagName, aMsg.isSet(systemFlag));
			}
		}

		// Get user flags
		String[] userFlags = aMsg.getFlags().getUserFlags();
		for (String userFlag : userFlags) {
			e.setAttribute(SystemFlags.FLAG_ATTRIBUTE_PREFIX + userFlag, aMsg.getFlags().contains(userFlag));
		}

		// The Connector object
		e.setAttribute("mail.originator", this);

		// The Connector object
		e.setAttribute("event.originator", this);

		// The Java session object (javax.mail.Session)
		e.setAttribute("mail.session", mSession);

		// The Java session object (javax.mail.Session)
		e.setAttribute("mailbox.session", mSession);

		// The message store object (javax.mail.Store)
		e.setAttribute("mail.store", mStore);

		// The message store object (javax.mail.Store)
		e.setAttribute("mailbox.store", mStore);

		// The folder object (javax.mail.Folder)
		e.setAttribute("mail.folder", aMsg.getFolder());

		// The folder object (javax.mail.Folder)
		e.setAttribute("mailbox.folder", aMsg.getFolder());

		// Mailbox Operation
		e.setAttribute("mail.operation", "EXISTING");

		// Mailbox Operation
		e.setAttribute("mailbox.operation", "EXISTING");

		// Message object
		e.setAttribute("mail.message", aMsg);

		// Message object
		e.setAttribute("mailbox.message", aMsg);

		// Sender/From
		String[] fromHdr = aMsg.getHeader(HEADER_FROM);
		if (fromHdr == null) {
			fromHdr = aMsg.getHeader(HEADER_SENDER);
		}
		setAddressHeader(e, "mail.from", fromHdr);

		// Reply To
		setAddressHeader(e, "mail.replyto", aMsg.getHeader(HEADER_REPLYTO));

		// Subject
		e.setAttribute("mail.subject", aMsg.getSubject());

		// Message-ID
		try {
			e.setAttribute("mail.messageid", aMsg.getHeader("message-id")[0]);
		} catch (Exception ignore) {
			e.setAttribute("mail.messageid", ignore);
		}

		// Internal message number
		e.setAttribute("mail.messagenumber", Integer.valueOf(aMsg.getMessageNumber()));

		// Recipients
		setAddressHeader(e, "mail.to", aMsg.getHeader(HEADER_TO));
		setAddressHeader(e, "mail.cc", aMsg.getHeader(HEADER_CC));

		// Sent date
		e.setAttribute("mail.sent", aMsg.getSentDate());

		// Received date
		e.setAttribute("mail.received", aMsg.getReceivedDate());

		// Size
		e.setAttribute("mail.size", Integer.valueOf(aMsg.getSize()));

		// Body parts
		Object content = aMsg.getContent();

		if (content instanceof Multipart) {
			Multipart mp = (Multipart) content;
			Attribute attr = new Attribute("mail.bodyparts");
			for (int i = 0; i < mp.getCount(); i++) {
				attr.addValue(mp.getBodyPart(i));
			}
			e.setAttribute(attr);
		} else {
			e.setAttribute("mail.body", content);
		}
		return e;
	}

	/**
	 * Disconnects from Mail Server.
	 * 
	 * @exception Exception
	 *                if can not disconnect from Mail Server
	 */
	private void disconnectServer() throws Exception {
		try {
			if (mFolder != null) {
				mFolder.close(true); // expunge
				mFolder = null;
			}

			while (!iteratedFolders.isEmpty()) {
				Folder folder = iteratedFolders.remove(0);
				if (folder != null && folder.isOpen()) {
					folder.close(true); // expunge
				}
			}
		} catch (Exception e) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.MAILBOX.CLOSEEXCEPT.INFO", e.toString()));
			}
		}
		mStore.close();
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() throws Exception {
		mIsTerminated = true;
		super.terminate();
		disconnectServer();
	}

	/**
	 * Adds a new entry to the data source
	 * 
	 * @param entry
	 *            The entry data to add
	 * @exception Exception
	 *                Any exceptions thrown when trying to add entry in update
	 *                mode or when the appending of the message fails.
	 */
	public void putEntry(Entry entry) throws Exception {
		if (((ConnectorConfig) this.getConfiguration()).getMode().equals(ConnectorConfig.UPDATE_MODE)) {
			throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.CANNOT.ADD.IN.UPDATE.MODE"));
		}

		if (entry == null) {
			throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.NO.ENTRY"));
		}

		Object messageObject = entry.getObject(ATTRIBUTE_ADD_MESSAGE);
		List<String> flagNames = getFlagNames(entry);
		try {
			if (messageObject instanceof Message) {
				Message message = (Message) messageObject;
				addFlagsToMessage(message, entry, flagNames);

				Message[] messageArray = new Message[] { message };
				mFolder.appendMessages(messageArray);
			} else if (messageObject instanceof Message[]) {
				Message[] messageArray = (Message[]) messageObject;
				if (flagNames.size() > 0) {
					for (Message message : messageArray) {
						addFlagsToMessage(message, entry, flagNames);
					}
				}
				mFolder.appendMessages(messageArray);
			} else {
				throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.MESSAGE.FORMAT.NOT.SUPPORTED", ATTRIBUTE_ADD_MESSAGE));
			}
		} catch (MessagingException e) {
			throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.APPEND.MESSAGE.EXCP", e.toString()));
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.MAILBOX.MESSAGES.ADDED", mFolder));
		}

	}

	/**
	 * Modifies an existing entry. The new entry data is given by the
	 * <i>entry</i> parameter and the search criteria specifies which entry to
	 * modify.
	 * 
	 * @param entry
	 *            The entry data
	 * @param search
	 *            The search criteria used to locate the entry to be modified
	 * @param old
	 *            The old entry found by the search criteria
	 * @exception Exception
	 *                Any exceptions thrown when updating the flags of a message
	 */
	public void modEntry(Entry entry, SearchCriteria search, Entry old) throws Exception {
		if (entry == null) {
			throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.NO.ENTRY"));
		}

		if (old == null) {
			throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.UPDATE.NOTFOUND.EXCEP"));
		}

		Attribute attr = old.getAttribute("mail.message");
		if (attr == null) {
			throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.UPDATE.ATTRMISSING.EXCEP"));
		}
		Message m = (Message) attr.getValue(0);
		if (m == null) {
			throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.UPDATE.ATTRMISSING.EXCEP"));
		}

		addFlagsToMessage(m, entry, getFlagNames(entry));
	}

	/**
	 * Retrieves all flags from the provided entry and sets them to the message.
	 * 
	 * @param msg
	 *            the Message object.
	 * @param entry
	 *            the incoming entry.
	 * @param flagNames
	 *            the flags to be added.
	 */
	private void addFlagsToMessage(Message msg, Entry entry, List<String> flagNames) {
		for (String flagName : flagNames) {
			String value = entry.getString(flagName);
			if (value != null && value.trim().length() != 0) {
				boolean flagValue = Boolean.valueOf(value).booleanValue();
				addFlagToMessage(msg, flagName, flagValue);
			}
		}

	}

	/**
	 * Adds a flag with the specified name to the message.
	 * 
	 * @param msg
	 *            java mail Message object.
	 * @param flagName
	 *            the flag name.
	 * @param value
	 *            the flag value.
	 */
	private void addFlagToMessage(Message msg, String flagName, boolean value) {
		boolean isPOP3 = (msg instanceof POP3Message);
		try {
			try {
				Flag sysFlag = SystemFlags.getSystemFlag(flagName).getValue();
				if (isPOP3 && (sysFlag == Flag.RECENT)) {
					logmsg(sResHash.getString("CONNECTOR.MAILBOX.CANNOT.SET.POP3.RECENT"));
				}
				msg.setFlag(sysFlag, value);
			} catch (IllegalArgumentException iae) {
				// user flag
				Flags userFlags = new Flags();
				userFlags.add(SystemFlags.getUnprefixedFlagName(flagName));
				msg.setFlags(userFlags, value);
			}
		} catch (MessagingException e) {
			logmsg(sResHash.getString("CONNECTOR.MAILBOX.CANNOT.SET.FLAG", new Object[] { flagName, e.toString() }));
		}
	}

	/**
	 * Returns the flag attributes from the entry.
	 * 
	 * @param entry
	 *            the provided entry.
	 * @return a list of attribute names.
	 */
	private List<String> getFlagNames(Entry entry) {
		List<String> flagNames = new LinkedList<String>();
		for (String attrName : entry.getAttributeNames()) {
			if (attrName != null && SystemFlags.isFlag(attrName)) {
				flagNames.add(attrName);
			}
		}

		return flagNames;
	}

	/**
	 * Extracts all subfolders of a given folder.
	 * 
	 * @param folder
	 *            Folder object, where the subfolders are extracted from
	 * 
	 * @exception Exception
	 *                If a problem with extracting the subfolders occurrs
	 */
	private void getFolder(Folder folder) throws Exception {
		String name = folder.getFullName();
		logmsg(sResHash.getString("CONNECTOR.MAILBOX.SEARCH.SUBFOLDERS", name));

		if (folder.exists()) {
			if (!excludedFolders.contains(name.toLowerCase(Locale.ENGLISH))) {
				iteratedFolders.add(folder);
			}

			if (folder.getType() != Folder.HOLDS_MESSAGES) {
				Folder[] subFolder = folder.list();
				for (int i = 0; i < subFolder.length; i++) {
					getFolder(subFolder[i]);
				}
			} else {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.MAILBOX.NO.SUBFOLDERS", name));
				}
			}
		} else {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.MAILBOX.FOLDER.NOT.EXIST", name));
			}
		}
	}

	/**
	 * Opens all folders specified in the collection 'iteratedFolders'. The
	 * method also stores all messages found in the opened folders in the
	 * 'mMessages' object
	 * 
	 * @exception Exception
	 *                If a problem with extracting the subfolders occurs
	 */
	private void loadAllMessages() throws Exception {
		for (int i = 0; i < iteratedFolders.size(); i++) {
			Folder folder = iteratedFolders.get(i);
			if (folder.getType() != Folder.HOLDS_FOLDERS) {
				try {
					folder.open(Folder.READ_WRITE);
					if (IMAP.equalsIgnoreCase(mProtocol)) {
						folder.addMessageCountListener(this);
					}
					mMessages = this.addMsg(folder.getMessages());
					logmsg(sResHash.getString("CONNECTOR.MAILBOX.FOLDEROPEN.INFO", folder.getFullName().trim()));
				} catch (Exception e) {
					logmsg(sResHash.getString("CONNECTOR.MAILBOX.CANNOT.BE.OPENED", folder.getFullName()));
					if (i == 0 && mFolderName != null) {
						// if we cannot open the main folder specified by the
						// user, propagate the Exception, so that the AL fails
						throw e;
					}
				}
			} else {
				logmsg(sResHash.getString("CONNECTOR.MAILBOX.DOES.NOT.HOLD.MESSAGES", folder));
			}
		}
	}

	/**
	 * Lists all folders for the given Store.
	 * 
	 * @param store
	 *            The Store object, where the folders are searched
	 * 
	 * @exception Exception
	 *                If a problem with extracting the subfolders occurrs
	 */
	private void listAllFolders(Store store) throws Exception {
		Folder defaultFolder = store.getDefaultFolder();
		Folder[] sharedNamespace = store.getSharedNamespaces();
		Folder[] userNamespace = store.getUserNamespaces(mUsername);

		getFolder(defaultFolder);

		searchNamespaces(sharedNamespace, store, "SharedNamespaces");
		searchNamespaces(userNamespace, store, "UserNamespaces for user:" + mUsername);

	}

	/**
	 * Search folders in the given Namespaces
	 * 
	 * @param namespacesList
	 *            A list of Folder objects, presenting the namespaces
	 * 
	 * @param store
	 *            The Store object, where the namespaces are searched
	 * 
	 * @param namespace
	 *            Holds the name of the searched namespace
	 * 
	 * @exception Exception
	 *                If a problem with extracting the subfolders occurrs
	 */
	private void searchNamespaces(Folder[] namespacesList, Store store, String namespace) throws Exception {
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.MAILBOX.SEARCH.NAMESPACE", namespace));
		}
		for (int i = 0; i < namespacesList.length; i++) {
			URLName folderURL = namespacesList[i].getURLName();
			Folder folder = store.getFolder(folderURL);
			getFolder(folder);
		}
	}

	/**
	 * Version information.
	 * 
	 * @return the version information
	 */
	public String getVersion() {
		return "2.3-di7.1.1 %I%, 20%E%";
	}

	/**
	 * This class is used for transforming SearchCriteria elements into Java
	 * mail search terms.
	 */
	private static class SearchTermsHandler {

		/**
		 * For parsing short date strings.
		 */
		private final DateFormat shortDateFormatter;

		/**
		 * Constructor.
		 */
		public SearchTermsHandler() {
			// The time and zone parts of the date are ignored by the IMAP
			// protocol.
			// See arguments SENTBEFORE, SENTSINCE, SINCE, BEFORE in
			// http://tools.ietf.org/html/rfc3501#section-6.4.4
			shortDateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		}

		/**
		 * <p>Creates a SearchTerm using the provided data from a TDI search
		 * criterion. A predefined set of attributes are transformed in to
		 * well-known Java Mail Terms - 'mail.cc', 'mail.to', 'mail.from',
		 * 'mail.subject', 'mail.messageid', 'mail.messagenumber', 'mail.body',
		 * 'mail.size', 'mail.sent', 'mail.received', 'Flag.Answered',
		 * 'Flag.Deleted', 'Flag.Draft', 'Flag.Recent', 'Flag.Seen',
		 * 'Flag.Flagged'.</p>
		 * 
		 * <p> The unknown flag headers (prefixed with 'Flag.') are mapped to
		 * custom user flag terms. All unknown headers (prefixed with 'mail.')
		 * are mapped as custom string headers. If any other attribute is
		 * provided, an Exception is thrown.</p>
		 * 
		 * @param name
		 *            the attribute name as provided in the Link Criteria.
		 * @param value
		 *            the attribute value.
		 * @param match
		 *            the matching condition used.
		 * @param negate
		 *            whether the statement is explicitly negated or not.
		 * @return the Java Mail Search term corresponding to this attribute.
		 * @throws Exception
		 *             if there is a problem with the transformation.
		 */
		public SearchTerm createSearchTerm(String name, Object value, int match, boolean negate) throws Exception {
			SearchTerm term = null;

			// check if it is an incomparable header
			term = getIncomparableSearchTerm(name, value.toString(), match);

			// check if it is a comparable header
			if (term == null) {
				term = getComparableSearchTerm(name, value, match);
			}

			// handle as unknown string header
			if (term == null && name.startsWith(MAIL_ATTRIBUTE_PREFIX)) {
				name = name.substring(MAIL_ATTRIBUTE_PREFIX.length());
				term = new HeaderTerm(name, value.toString());
				if (match == SearchCriteria.NOT_STRING) {
					term = new NotTerm(term);
				}
			}

			// check if it is a flag header
			if (term == null && SystemFlags.isFlag(name)) {
				term = getFlagSearchTerm(name, Boolean.parseBoolean(value.toString()), match);
			}

			// unsupported attribute
			if (term == null) {
				throw new Exception(sResHash.getString("CONNECTOR.MAILBOX.NOTSUPPORTEDATTR.EXCEP"));
			}

			if (negate) {
				term = new NotTerm(term);
			}
			return term;
		}

		/**
		 * Checks if this attribute is incomparable - supports only 'equals' and
		 * 'not equals' matching. If so its corresponding SearchTerm is
		 * returned. Otherwise, <b>null</b> is returned.
		 * 
		 * @param name
		 *            attribute name.
		 * @param value
		 *            attribute value.
		 * @param match
		 *            matching condition.
		 * @return the Java mail search term.
		 */
		private SearchTerm getIncomparableSearchTerm(String name, String value, int match) {
			name = name.toLowerCase(Locale.ENGLISH);

			SearchTerm resultTerm = null;
			if (name.equals("mail.from")) {
				resultTerm = new FromStringTerm(value);
			} else if (name.equals("mail.to")) {
				resultTerm = new RecipientStringTerm(Message.RecipientType.TO, value);
			} else if (name.equals("mail.cc")) {
				resultTerm = new RecipientStringTerm(Message.RecipientType.CC, value);
			} else if (name.equals("mail.subject")) {
				resultTerm = new SubjectTerm(value);
			} else if (name.equals("mail.messageid")) {
				resultTerm = new MessageIDTerm(value);
			} else if (name.equals("mail.body")) {
				resultTerm = new BodyTerm(value);
			}
			// if 'not equals' comparison is used
			if (resultTerm != null && match == SearchCriteria.NOT_STRING) {
				resultTerm = new NotTerm(resultTerm);
			}
			return resultTerm;
		}

		/**
		 * Checks if this attribute is comparable - supports wide range of
		 * matching conditions ('eq', 'ne', 'lt', 'le', 'gt', 'ge', etc.) If so
		 * its corresponding SearchTerm is returned. Otherwise, <b>null</b> is
		 * returned.
		 * 
		 * @param name
		 *            attribute name.
		 * @param value
		 *            attribute value.
		 * @param match
		 *            matching condition.
		 * @return the Java mail search term.
		 * @throws ParseException
		 *             if a problem occurs when parsing the terms value.
		 */
		private ComparisonTerm getComparableSearchTerm(String name, Object value, int match) throws ParseException {
			name = name.toLowerCase(Locale.ENGLISH);
			ComparisonTerm resultTerm = null;
			int comparison = convertMatchingComparison(match);

			if (name.equals("mail.received")) {
				Date dateValue = convertObjectToDate(value);
				if (dateValue != null) {
					resultTerm = new ReceivedDateTerm(comparison, dateValue);
				}
			} else if (name.equals("mail.sent")) {
				Date dateValue = convertObjectToDate(value);
				if (dateValue != null) {
					resultTerm = new SentDateTerm(comparison, dateValue);
				}
			} else if (name.equals("mail.size")) {
				int sizeValue = Integer.parseInt(value.toString());
				resultTerm = new SizeTerm(comparison, sizeValue);
			} else if (name.equals("mail.messagenumber")) {
				int number = Integer.parseInt(value.toString());
				resultTerm = new TDIMessageNumberTerm(comparison, number);
			}

			return resultTerm;
		}

		/**
		 * If the provided Objects is a String, it is parsed to a Date object
		 * and returned. If it is a Date, it is directly returned. In all other
		 * cases <b>null</b> is returned.
		 * 
		 * @param dateObject
		 *            the data object.
		 * @return a java.util.Date object.
		 * @throws ParseException
		 *             if there is a problem with the date parsing.
		 */
		private Date convertObjectToDate(Object dateObject) throws ParseException {
			Date date = null;
			if (dateObject instanceof String) {
				date = shortDateFormatter.parse((String) dateObject);
			} else if (dateObject instanceof Date) {
				date = (Date) dateObject;
			}
			return date;
		}

		/**
		 * Converts the SearchCriteria matching condition to its corresponding
		 * Java Mail value (see class {@link ComparisonTerm} for details.)
		 * 
		 * @param tdiMatchingComparison
		 *            matching comparison used in the search criteria.
		 * @return a Java Mail matching comparison.
		 */
		private int convertMatchingComparison(int tdiMatchingComparison) {
			int mailMatchComparison = -1;
			switch (tdiMatchingComparison) {
			case SearchCriteria.EXACT:
				mailMatchComparison = ComparisonTerm.EQ;
				break;
			case SearchCriteria.GREATER_THAN:
				mailMatchComparison = ComparisonTerm.GT;
				break;
			case SearchCriteria.GREATER_THAN_OR_EQUAL:
				mailMatchComparison = ComparisonTerm.GE;
				break;
			case SearchCriteria.LESS_THAN:
				mailMatchComparison = ComparisonTerm.LT;
				break;
			case SearchCriteria.LESS_THAN_OR_EQUAL:
				mailMatchComparison = ComparisonTerm.LE;
				break;
			case SearchCriteria.NOT_STRING:
				mailMatchComparison = ComparisonTerm.NE;
				break;
			default:
				mailMatchComparison = ComparisonTerm.EQ;
			}
			return mailMatchComparison;
		}

		/**
		 * If this attribute is any of the system flags it is transformed into
		 * its corresponding FlagTerm. Otherwise, it is created as a custom user
		 * flag term.
		 * 
		 * @param name
		 *            attribute name.
		 * @param value
		 *            attribute value.
		 * @param match
		 *            matching condition.
		 * @return the Java mail search term.
		 */
		private FlagTerm getFlagSearchTerm(String name, boolean value, int match) {
			FlagTerm resultTerm = null;
			try {
				Flag flag = SystemFlags.getSystemFlag(name).getValue();
				resultTerm = new FlagTerm(new Flags(flag), isFlagSet(value, match));
			} catch (IllegalArgumentException iae) {
				name = SystemFlags.getUnprefixedFlagName(name);
				resultTerm = new FlagTerm(new Flags(name), isFlagSet(value, match));
			}

			return resultTerm;
		}

		/**
		 * Checks if the value of the provided flag is set.
		 * 
		 * @param value
		 *            flag value.
		 * @param comaprison
		 *            comparison condition.
		 * @return either <b>true</b>, or <b>false</b>.
		 */
		private boolean isFlagSet(boolean value, int comaprison) {
			if (comaprison != SearchCriteria.NOT_STRING) {
				return value;
			}
			return !value;
		}

	}

	/**
	 * The system flags supported by Java Mail., As well as several utility
	 * functions to work with them.
	 */
	private static enum SystemFlags {

		/** Answered */
		Answered(Flag.ANSWERED), //
		/** Deleted */
		Deleted(Flag.DELETED), //
		/** Draft */
		Draft(Flag.DRAFT), //
		/** Flagged */
		Flagged(Flag.FLAGGED), //
		/** Recent */
		Recent(Flag.RECENT), //
		/** Sent */
		Seen(Flag.SEEN); //

		/**
		 * A prefix used for the entry attributes holding mail flags.
		 */
		public final static String FLAG_ATTRIBUTE_PREFIX = "Flag.";

		/**
		 * A map containing the system flag names.
		 */
		private static Map<Flag, String> flagNames = new HashMap<Flag, String>();
		static {
			for (SystemFlags flag : SystemFlags.values()) {
				flagNames.put(flag.getValue(), flag.getFullName());
			}
		}

		/**
		 * Gets the entry attribute name of the corresponding flag.
		 * 
		 * @param flag
		 *            the java mail flag.
		 * @return the flag attribute name.
		 */
		public static String getFullFlagName(Flag flag) {
			return flagNames.get(flag);
		}

		/**
		 * Gets the system flag for the corresponding name.
		 * 
		 * @param flagName
		 *            the flag name with or without the 'Flag.' prefix.
		 * @return the SystemFlags object.
		 */
		public static SystemFlags getSystemFlag(String flagName) {
			flagName = getUnprefixedFlagName(flagName);

			// capitalize the flag name
			if (flagName.length() > 0) {
				flagName = flagName.substring(0, 1).toUpperCase(Locale.ENGLISH) + flagName.substring(1).toLowerCase(Locale.ENGLISH);
			}

			return valueOf(flagName);
		}

		/**
		 * Gets the unprefixed flag name.
		 * 
		 * @param flagName
		 *            the flag name without the 'Flag.' prefix.
		 * @return the flag attribute name.
		 */
		public static String getUnprefixedFlagName(String flagName) {
			if (isFlag(flagName)) {
				flagName = flagName.substring(FLAG_ATTRIBUTE_PREFIX.length());
			}
			return flagName;
		}

		/**
		 * Checks if the provided attribute name is a flag.
		 * 
		 * @param flagName
		 *            the flag name including the 'Flag.' prefix.
		 * @return true if the Attribute is prefixed with 'Flag.'(case
		 *         insensitive).
		 */
		public static boolean isFlag(String flagName) {
			return flagName.toLowerCase(Locale.ENGLISH).startsWith(FLAG_ATTRIBUTE_PREFIX.toLowerCase(Locale.ENGLISH));
		}

		/**
		 * The flag id.
		 */
		private transient Flag id = Flag.USER;

		/**
		 * Constructor.
		 * 
		 * @param id
		 *            the flag's id.
		 */
		private SystemFlags(Flag id) {
			this.id = id;
		}

		/**
		 * Retrieves the javax.mail.Flags.Flag.
		 * 
		 * @return the flag.
		 */
		public Flag getValue() {
			return id;
		}

		/**
		 * Returns the name of the entry attribute corresponding to this flag.
		 * 
		 * @return name.
		 */
		public String getFullName() {
			return FLAG_ATTRIBUTE_PREFIX + name();
		}

	}

	/**
	 * A custom search term to be used for searching by message number. Unlike
	 * the default {@link SearchTerm} provided by Java (
	 * {@link MessageNumberTerm}) this permits comparing match conditions (e.g.
	 * less-than, greater-than). ).
	 */
	private static class TDIMessageNumberTerm extends IntegerComparisonTerm {

		/**
		 * Constructor.
		 * 
		 * @param comparison
		 *            the comparison condition.
		 * @param number
		 *            the message number.
		 */
		public TDIMessageNumberTerm(int comparison, int number) {
			super(comparison, number);
		}

		/**
		 * Matches the current criterion against the provided message.
		 * 
		 * @param message
		 *            checked message.
		 * @return <b>true</b> if the message matches, <b>false</b> otherwise.
		 */
		public boolean match(Message message) {
			try {
				int number = message.getMessageNumber();
				return super.match(number);
			} catch (Exception exception) {
				return false;
			}
		}

	}
}
