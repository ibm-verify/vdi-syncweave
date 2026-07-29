/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dominoUsers;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.Session;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * This class performs a user deletion from Domino.
 */
public class UserDeletion implements IDominoAction {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// the Attribute Names making up the fixed schema of Attributes
	/**
	 * Attribute name : {@value #ATTR_NAME_DELETE_MAIL_FILE}
	 */
	public static final String ATTR_NAME_DELETE_MAIL_FILE = "DEL_DeleteMailFile";

	/**
	 * Attribute name : {@value #ATTR_NAME_DELETE_GROUP_NAME}
	 */
	public static final String ATTR_NAME_DELETE_GROUP_NAME = "DEL_DeleteGroupName";

	/**
	 * 
	 */
	// DeleteMailFile values
	/**
	 * Attribute value : {@value #DELETE_MAIL_FILE_DONT}
	 */

	public static final int DELETE_MAIL_FILE_DONT = 0;

	/**
	 * Attribute value : {@value #DELETE_MAIL_FILE_DATABASE}
	 */
	public static final int DELETE_MAIL_FILE_DATABASE = 1;

	/**
	 * Attribute value : {@value #DELETE_MAIL_FILE_REPLICAS}
	 */
	public static final int DELETE_MAIL_FILE_REPLICAS = 2;

	// constants for admin request field names
	/**
	 * Admin request field name .
	 */
	private final static String REQUEST_FIELD_NAME_FORM = "Form";

	/**
	 * Admin request field name .
	 */
	private final static String REQUEST_FIELD_NAME_TYPE = "Type";

	/**
	 * Admin request field name .
	 */
	private final static String REQUEST_FIELD_NAME_PROXYACTION = "ProxyAction";

	/**
	 * Admin request field name .
	 */
	private final static String REQUEST_FIELD_NAME_PROXYPROCESS = "ProxyProcess";

	/**
	 * Admin request field name .
	 */
	private final static String REQUEST_FIELD_NAME_PROXYNAMELIST = "ProxyNameList";

	/**
	 * Admin request field name .
	 */
	private final static String REQUEST_FIELD_NAME_PROXYAUTHOR = "ProxyAuthor";

	/**
	 * Admin request field name .
	 */
	private final static String REQUEST_FIELD_NAME_PROXYDELETEMAILFILE = "ProxyDeleteMailfile";

	/**
	 * Admin request field name .
	 */
	private final static String REQUEST_FIELD_NAME_PROXYDATABASEPATH = "ProxyDatabasePath";

	/**
	 * Admin request field name .
	 */
	private final static String REQUEST_FIELD_NAME_PROXYSERVER = "ProxyServer";

	/**
	 * Admin request field name .
	 */
	private final static String REQUEST_FIELD_NAME_PROXYSOURCESERVER = "ProxySourceServer";

	/**
	 * Admin request field name .
	 */
	private final static String REQUEST_FIELD_NAME_PROXYTEXTITEM1 = "ProxyTextItem1";

	// constants for admin request field values
	/**
	 * Admin request field value .
	 */
	private final static String REQUEST_FIELD_VALUE_FORM = "AdminRequest";

	/**
	 * Admin request field value .
	 */
	private final static String REQUEST_FIELD_VALUE_TYPE = "AdminRequest";

	/**
	 * Admin request field value .
	 */
	private final static String REQUEST_FIELD_VALUE_PROXYACTION = "0";

	/**
	 * Admin request field value .
	 */
	private final static String REQUEST_FIELD_VALUE_PROXYPROCESS = "Adminp";

	/**
	 * Admin request field value .
	 */
	private final static String REQUEST_FIELD_VALUE_PROXYSERVER = "*";

	// default values
	/**
	 * default delete mail file value.
	 */
	private int mDefaultDeleteMailFile = 0;

	/**
	 * default add to group value
	 */
	private String mDefaultAddToGroup = null;

	// local data holders
	/**
	 * delete mail file value.
	 */
	private Integer mDeleteMailFile = null;

	/**
	 * add to group value
	 */
	private String mAddToGroup = null;

	/**
	 * user full name
	 */
	private String mFullName = null;

	/**
	 * mail file
	 */
	private String mMailFile = null;

	/**
	 * The DominoUsersConnector that created this Domino Action object
	 */
	private DominoUsersConnector mParent = null;

	/**
	 * Session instance.
	 */
	private Session mSession = null;

	/**
	 * Database of the admin
	 */
	private Database mAdminDatabase = null;

	/**
	 * User database
	 */
	private Database mDatabase = null;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = DominoUsersConnector
			.getResHash();

	/**
	 * Class constructor
	 * 
	 * @param aParent
	 *            DominoUsersConnector that created this Domino Action object
	 * @throws Exception
	 *             if parent session is not valid
	 */
	public UserDeletion(DominoUsersConnector aParent) throws Exception {
		mParent = aParent;

		mSession = mParent.getSession();
		if (mSession == null) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.USERDELETION.NOTES.SESSION.IS.NULL"));
		}

		mAdminDatabase = mParent.getAdminDatabase();
		if (mAdminDatabase == null) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.USERDELETION.ADMIN.DATABASE.IS.NULL"));
		}

		mDatabase = mParent.getDatabase();
		if (mDatabase == null) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.USERDELETION.CONNECTOR.DATABASE.IS.NULL"));
		}
	}

	// access methods
	/**
	 * @return default delete mail value.
	 */

	public int getDefaultDeleteMailFile() {
		return mDefaultDeleteMailFile;
	}

	/**
	 * Sets default delete mail file value
	 * 
	 * @param aDefaultDeleteMailFile
	 *            value to set
	 * @throws Exception
	 *             if parameter is not supported.
	 */
	public void setDefaultDeleteMailFile(int aDefaultDeleteMailFile)
			throws Exception {
		if (aDefaultDeleteMailFile != DELETE_MAIL_FILE_DONT
				&& aDefaultDeleteMailFile != DELETE_MAIL_FILE_DATABASE
				&& aDefaultDeleteMailFile != DELETE_MAIL_FILE_REPLICAS) {

			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.INVALID.DELMAILFILE.VALUE.EXCEPTION"));
		}

		mDefaultDeleteMailFile = aDefaultDeleteMailFile;
	}

	/**
	 * Retrieves the
	 * 
	 * @return default add to group value.
	 */
	public String getDefaultAddToGroup() {
		return mDefaultAddToGroup;
	}

	/**
	 * Sets default add to group value
	 * 
	 * @param aDefaultAddToGroup
	 *            value to set
	 * @throws Exception
	 *             if group value does not exist
	 */
	public void setDefaultAddToGroup(String aDefaultAddToGroup)
			throws Exception {
		if (aDefaultAddToGroup != null) {
			if (!DominoUtils.groupExist(mDatabase, aDefaultAddToGroup)) {
				throw new Exception(sResHash.getString(
						"CONNECTOR.DOMINOUSERSCONN.GROUP.NOT.EXIST.EXCEPTION",
						aDefaultAddToGroup));
			}
		}

		mDefaultAddToGroup = aDefaultAddToGroup;
	}

	/**
	 * @return delete mail file.
	 */
	public Integer getDeleteMailFile() {
		return mDeleteMailFile;
	}

	/**
	 * Sets delete mail value
	 * 
	 * @param aDeleteMailFile
	 *            value to set
	 */
	public void setDeleteMailFile(Integer aDeleteMailFile) {
		mDeleteMailFile = aDeleteMailFile;
	}

	/**
	 * Retrieves the name of the group the user is added to.
	 * 
	 * @return add to group value.
	 */
	public String getAddToGroup() {
		return mAddToGroup;
	}

	/**
	 * Sets add to group value
	 * 
	 * @param aAddToGroup
	 *            value to set
	 */
	public void setAddToGroup(String aAddToGroup) {
		mAddToGroup = aAddToGroup;
	}

	/**
	 * Retrives user's full name.
	 * 
	 * @return user's full name
	 */
	public String getUserFullName() {
		return mFullName;
	}

	/**
	 * Sets user's full name.
	 * 
	 * @param aUserFullName
	 *            name to set.
	 */
	public void setUserFullName(String aUserFullName) {
		mFullName = aUserFullName;
	}

	/**
	 * Retrieves mail file.
	 * 
	 * @return mail file.
	 */
	public String getUserMailFile() {
		return mMailFile;
	}

	/**
	 * Sets mail file
	 * 
	 * @param aUserMailFile
	 *            value to set
	 */
	public void setUserMailFile(String aUserMailFile) {
		mMailFile = aUserMailFile;
	}

	// implementation of the IDominoAction methods
	/**
	 * {@inheritDoc}
	 */

	public Entry extractAndStoreData(Entry aEntry) throws Exception {
		Entry entryNoFixedAttr = aEntry.clone();

		Attribute attrDeleteMailFile = aEntry
				.getAttribute(ATTR_NAME_DELETE_MAIL_FILE);
		if (attrDeleteMailFile != null) {
			mDeleteMailFile = Integer.valueOf(attrDeleteMailFile.getValue());
			entryNoFixedAttr.removeAttribute(ATTR_NAME_DELETE_MAIL_FILE);
		}

		Attribute attrDeleteGroupName = aEntry
				.getAttribute(ATTR_NAME_DELETE_GROUP_NAME);
		if (attrDeleteGroupName != null) {
			mAddToGroup = attrDeleteGroupName.getValue();
			entryNoFixedAttr.removeAttribute(ATTR_NAME_DELETE_GROUP_NAME);
		}

		return entryNoFixedAttr;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean mustPerform(Entry aEntry) throws Exception {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String canPerform() {
		if (mDeleteMailFile != null) {
			int deleteMailFile = mDeleteMailFile.intValue();
			if (deleteMailFile != DELETE_MAIL_FILE_DONT
					&& deleteMailFile != DELETE_MAIL_FILE_DATABASE
					&& deleteMailFile != DELETE_MAIL_FILE_REPLICAS) {

				return sResHash
						.getString("CONNECTOR.DOMINOUSERSCONN.INVALID.DELMAILFILE.VALUE");
			}
		}

		if (mFullName == null || mFullName.length() == 0) {
			return sResHash
					.getString("CONNECTOR.DOMINOUSERSCONN.MISSING.FULLNAME.VALUE");
		}

		// check if the specified group exists
		if (mAddToGroup != null) {
			try {
				if (!DominoUtils.groupExist(mDatabase, mAddToGroup)) {
					return sResHash.getString(
							"CONNECTOR.DOMINOUSERSCONN.GROUP.NOT.EXIST",
							mAddToGroup);
				}
			} catch (Exception e) {
				return e.getMessage();
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void perform() throws Exception {
		String deleteMailFileValue = getDeleteMailFileActionValue();
		String addToGroup = getAddToGroupActionValue();

		sendAdminRequestDeleteUser(deleteMailFileValue, addToGroup);
	}

	/**
	 * {@inheritDoc}
	 */
	public void resetData() throws Exception {
		mDeleteMailFile = null;
		mAddToGroup = null;
		mFullName = null;
		mMailFile = null;
	}

	/**
	 * Retrives the value of the delete mail action.
	 * 
	 * @return value of the delete mail file action.
	 */
	public String getDeleteMailFileActionValue() {
		String deleteMailFile = null;
		if (mDeleteMailFile != null) {
			deleteMailFile = mDeleteMailFile.toString();
		} else {
			deleteMailFile = Integer.valueOf(mDefaultDeleteMailFile).toString();
		}
		return deleteMailFile;
	}

	/**
	 * Retrieves the name of the group the user is added to , if value is not
	 * found the name of the default group is returned.
	 * 
	 * @return add to group action value
	 */
	public String getAddToGroupActionValue() {
		String addToGroup = mAddToGroup;
		if (addToGroup == null) {
			addToGroup = mDefaultAddToGroup;
		}
		return addToGroup;
	}

	/**
	 * Sends a admin request for deletion of a user.
	 * 
	 * @param aDeleteMailFile
	 *            mail file to delete
	 * @param aAddToGroup
	 *            group to be added after deletion
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void sendAdminRequestDeleteUser(String aDeleteMailFile,
			String aAddToGroup) throws Exception {
		Document adminRequest = mAdminDatabase.createDocument();
		if (adminRequest == null) {
			if (mParent.debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.DOMINOUSERSCONN.USERDELETION.COULD.NOT.CREATE.ADMIN.REQUEST.DOCUMENT"));
			}
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.USERDELETION.COULD.NOT.CREATE.ADMIN.REQUEST.DOCUMENT2"));
		}

		try {
			Item item = null;
			item = adminRequest.appendItemValue(REQUEST_FIELD_NAME_FORM,
					REQUEST_FIELD_VALUE_FORM);

			item = adminRequest.appendItemValue(REQUEST_FIELD_NAME_TYPE,
					REQUEST_FIELD_VALUE_TYPE);
			item.setProtected(true);

			item = adminRequest.appendItemValue(REQUEST_FIELD_NAME_PROXYACTION,
					REQUEST_FIELD_VALUE_PROXYACTION);
			item.setSigned(true);
			item.setProtected(true);

			item = adminRequest.appendItemValue(
					REQUEST_FIELD_NAME_PROXYPROCESS,
					REQUEST_FIELD_VALUE_PROXYPROCESS);
			item.setSigned(true);
			item.setProtected(true);

			item = adminRequest.appendItemValue(REQUEST_FIELD_NAME_PROXYSERVER,
					REQUEST_FIELD_VALUE_PROXYSERVER);
			item.setSigned(true);
			item.setNames(true);

			item = adminRequest.appendItemValue(REQUEST_FIELD_NAME_PROXYAUTHOR,
					mSession.getUserName());
			item.setSigned(true);
			item.setAuthors(true);

			String serverName = DominoUtils.getUserCanonicalName(mSession,
					mAdminDatabase.getServer());
			item = adminRequest.appendItemValue(
					REQUEST_FIELD_NAME_PROXYSOURCESERVER, serverName);
			item.setSigned(true);
			item.setNames(true);

			item = adminRequest.appendItemValue(
					REQUEST_FIELD_NAME_PROXYNAMELIST, mFullName);
			item.setSigned(true);
			item.setNames(true);

			if (mMailFile != null && mMailFile.length() > 0) {
				item = adminRequest.appendItemValue(
						REQUEST_FIELD_NAME_PROXYDATABASEPATH, mMailFile);
			} else {
				item = adminRequest.replaceItemValue(
						REQUEST_FIELD_NAME_PROXYDATABASEPATH, null);
			}
			item.setSigned(true);

			item = adminRequest.appendItemValue(
					REQUEST_FIELD_NAME_PROXYDELETEMAILFILE, aDeleteMailFile);
			item.setSigned(true);
			item.setProtected(true);

			if (aAddToGroup != null && aAddToGroup.length() > 0) {
				item = adminRequest.appendItemValue(
						REQUEST_FIELD_NAME_PROXYTEXTITEM1, aAddToGroup);
			} else {
				item = adminRequest.replaceItemValue(
						REQUEST_FIELD_NAME_PROXYTEXTITEM1, null);
			}
			item.setSigned(true);
			item.setProtected(true);

			adminRequest.sign();
			adminRequest.save(true);
		} finally {
			adminRequest.recycle();
		}
	}

	/**
	 * Log a debug message to the connector's log
	 * 
	 * @param aMessage
	 *            The message to write to the log
	 */
	private void debug(String aMessage) {
		mParent.debug(aMessage);
	}

}
