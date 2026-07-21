/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dominoUsers;

import java.text.ParseException;
import java.util.Date;
import java.util.Vector;

import lotus.domino.Registration;
import lotus.domino.Session;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.util.Calendar;

/**
 * This class performs a user registration from Domino.
 */
public class UserRegistration implements IDominoAction {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// the Attribute Names making up the fixed schema of Attributes
	/**
	 * Attribute name - {@value #ATTR_NAME_REG_PERFORM}.
	 */
	public static final String ATTR_NAME_REG_PERFORM = "REG_Perform";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_ID_FILE}.
	 */
	public static final String ATTR_NAME_REG_ID_FILE = "REG_IdFile";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_USER_PW}.
	 */
	public static final String ATTR_NAME_REG_USER_PW = "REG_UserPw";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_SERVER}.
	 */
	public static final String ATTR_NAME_REG_SERVER = "REG_Server";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_CERTIFIER_ID_FILE}.
	 */
	public static final String ATTR_NAME_REG_CERTIFIER_ID_FILE = "REG_CertifierIDFile";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_CERT_PASSWORD}.
	 */
	public static final String ATTR_NAME_REG_CERT_PASSWORD = "REG_CertPassword";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_FORWARD}.
	 */
	public static final String ATTR_NAME_REG_FORWARD = "REG_Forward";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_ALT_ORG_UNIT}.
	 */
	public static final String ATTR_NAME_REG_ALT_ORG_UNIT = "REG_AltOrgUnit";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_ALT_ORG_UNIT_LANG}.
	 */
	public static final String ATTR_NAME_REG_ALT_ORG_UNIT_LANG = "REG_AltOrgUnitLang";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_CREATE_MAIL_DB}.
	 */
	public static final String ATTR_NAME_REG_CREATE_MAIL_DB = "REG_CreateMailDb";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_EXPIRATION}.
	 */
	public static final String ATTR_NAME_REG_EXPIRATION = "REG_Expiration";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_ID_TYPE}.
	 */
	public static final String ATTR_NAME_REG_ID_TYPE = "REG_IDType";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_IS_NORTH_AMERICAN}.
	 */
	public static final String ATTR_NAME_REG_IS_NORTH_AMERICAN = "REG_IsNorthAmerican";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_MIN_PASSWORD_LENGTH}.
	 */
	public static final String ATTR_NAME_REG_MIN_PASSWORD_LENGTH = "REG_MinPasswordLength";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_ORG_UNIT}.
	 */
	public static final String ATTR_NAME_REG_ORG_UNIT = "REG_OrgUnit";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_REGISTRATION_LOG}.
	 */
	public static final String ATTR_NAME_REG_REGISTRATION_LOG = "REG_RegistrationLog";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_REGISTRATION_SERVER}.
	 */
	public static final String ATTR_NAME_REG_REGISTRATION_SERVER = "REG_RegistrationServer";

	/**
	 * Attribute name - {@value #ATTR_NAME_REG_STORE_ID_IN_ADDRESS_BOOK}.
	 */
	public static final String ATTR_NAME_REG_STORE_ID_IN_ADDRESS_BOOK = "REG_StoreIDInAddressBook";

	// Attribute values
	/**
	 * Attribute value for ID type - flat id.
	 */
	public static final int ATTR_VALUE_ID_TYPE_FLAT_ID = 0;

	/**
	 * Attribute value for ID type - hierarchical id.
	 */
	public static final int ATTR_VALUE_ID_TYPE_HIERARCHICAL_ID = 1;

	/**
	 * Attribute value for ID type - certifier.
	 */
	public static final int ATTR_VALUE_ID_TYPE_CERTIFIER = 2;

	/**
	 * Values for Notes ID type
	 */
	private static final int[] ATTR_NOTES_VALUES_ID_TYPE = {
			Registration.ID_FLAT, Registration.ID_HIERARCHICAL,
			Registration.ID_CERTIFIER };

	// local data holders for the fixed schema Attributes
	/**
	 * If set to <code>true</code> the Connector will perform user
	 * registration;
	 */
	private boolean mPerform = false;

	/**
	 * Contains the full path of the ID file to be registered; for example:
	 * "c:\newuserdata\newuser.id"
	 */
	private String mIdFile = null;

	/**
	 * The user's password.
	 */
	private String mUserPw = null;

	/**
	 * The name of the server containing the user's mail file. If the Attribute
	 * is missing, the value will be obtained from the current Connector's
	 * Domino Session.
	 * 
	 */
	private String mServer = null;

	/**
	 * The full file path to the certifier ID file
	 */
	private String mCertifierIdFile = null;

	/**
	 * The password for the certifier ID file.
	 */
	private String mCertPassword = null;

	/**
	 * The forwarding domain for the user's mail file.
	 */
	private String mForward = null;

	/**
	 * Alternate names for the organizational unit to use when creating ID file.
	 */
	private Vector<Object> mAltOrgUnit = null;

	/**
	 * Alternate names for the organizational unit (LANG) to use when creating
	 * ID file.
	 */
	private Vector<Object> mAltOrgUnitLang = null;

	/**
	 * <code>true</code> - creates a mail database <code>false</code> - does
	 * not create a mail database; it will be created during setup If this
	 * Attribute is missing, a default value of <code>false</code> will be
	 * assumed.
	 * 
	 */
	private boolean mCreateMailDb = false;

	/**
	 * The expiration date to use when creating ID files. If the Attribute is
	 * missing, or its value is null, a default value of the current date + 2
	 * years is used.
	 */
	private Date mExpiration = null;

	/**
	 * The type of ID file to create:<br>
	 * <b>0</b> - create a flat ID<br>
	 * <b>1</b> - create a hierarchical ID<br>
	 * <b>2</b> - create an ID that depends on whether the certifier ID is
	 * flat or hierarchical.<br>
	 * If the Attribute is missing, a default value of 2 is used.
	 * 
	 */
	private int mIDType = ATTR_VALUE_ID_TYPE_CERTIFIER;

	/**
	 * <code>true</code> - the ID file will be North American;
	 * <code>false</code> - the ID file will not be North American;
	 */
	private boolean mIsNorthAmerican = true;

	/**
	 * The minimum number of characters required for a password in an ID file.
	 */
	private int mMinPasswordLength = 0;

	/**
	 * The organizational unit to use when creating ID files.
	 */
	private String mOrgUnit = "";

	/**
	 * The log file to use when creating Ids.
	 */
	private String mRegistrationLog = "";

	/**
	 * The server to use when creating IDs. This property is used only when the
	 * created ID is stored in the server Domino Directory, or when a mail
	 * database is created for the new user.
	 */
	private String mRegistrationServer = null;

	/**
	 * <code>true</code> - stores the ID file in the server's Domino
	 * Directory;<br>
	 * <code>false</code> - does not store the ID file in the server's Domino
	 * Directory.<br>
	 * If this Attribute is missing, a default value of <code>false</code> is
	 * used.
	 * 
	 */
	private boolean mStoreIDInAddressBook = false;

	/**
	 * Will not allow registration without creation of user document.
	 */
	private static final boolean REG_UPDATE_ADDRESS_BOOK = true;

	// local data holders for Person Document Attributes used in registration
	/**
	 * Person's first name
	 */
	private String mFirstName = null;

	/**
	 * Person's middle name
	 */
	private String mMiddleName = null;

	/**
	 * Person's last name
	 */
	private String mLastName = null;

	/**
	 * Person's document location
	 */
	private String mLocation = null;

	/**
	 * Comment
	 */
	private String mComment = null;

	/**
	 * Path of the mail database
	 */
	private String mMailDbPath = null;

	/**
	 * Alternate name.
	 */
	private String mAltName = null;

	/**
	 * Alternate name(LANG).
	 */
	private String mAltNameLang = null;

	/**
	 * The DominoUsersConnector that created this Domino Action object
	 */
	private DominoUsersConnector mParent = null;

	/**
	 * Represents the session.
	 */
	private Session mSession = null;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = DominoUsersConnector
			.getResHash();

	/**
	 * Class contructor
	 * 
	 * @param aParent
	 *            the DominoUsersConnector that created this Domino Action
	 *            object
	 * @throws Exception
	 *             if parent session is not valid.
	 */
	public UserRegistration(DominoUsersConnector aParent) throws Exception {
		mParent = aParent;

		mSession = mParent.getSession();
		if (mSession == null) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.CANNOT.INSTANTIATE.USERREGISTRATION"));
		}
	}

	// implementation of the IDominoAction methods
	/**
	 * {@inheritDoc}
	 */
	public Entry extractAndStoreData(Entry aEntry) throws Exception {
		Entry entryNoFixedAttr = aEntry.clone();
		Attribute attribute = null;

		// extract data from the Attributes from the fixed schema

		attribute = aEntry.getAttribute(ATTR_NAME_REG_PERFORM);
		if (attribute != null) {
			mPerform = Boolean.valueOf(attribute.getValue()).booleanValue();
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_PERFORM);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_ID_FILE);
		if (attribute != null) {
			mIdFile = attribute.getValue();
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_ID_FILE);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_USER_PW);
		if (attribute != null) {
			mUserPw = attribute.getValue();
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_USER_PW);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_SERVER);
		if (attribute != null) {
			mServer = attribute.getValue();
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_SERVER);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_CERTIFIER_ID_FILE);
		if (attribute != null) {
			mCertifierIdFile = attribute.getValue();
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_CERTIFIER_ID_FILE);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_CERT_PASSWORD);
		if (attribute != null) {
			mCertPassword = attribute.getValue();
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_CERT_PASSWORD);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_FORWARD);
		if (attribute != null) {
			mForward = attribute.getValue();
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_FORWARD);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_ALT_ORG_UNIT);
		if (attribute != null) {
			Object attrValue = attribute.getValue(0);
			if (attrValue == null) {
				mAltOrgUnit = null;
			} else if (attrValue instanceof Vector) {
				mAltOrgUnit = (Vector) attrValue;
			} else {
				mAltOrgUnit = new Vector<Object>();
				for (int i = 0; i < attribute.size(); i++) {
					mAltOrgUnit.add(attribute.getValue(i));
				}
			}
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_ALT_ORG_UNIT);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_ALT_ORG_UNIT_LANG);
		if (attribute != null) {
			Object attrValue = attribute.getValue(0);
			if (attrValue == null) {
				mAltOrgUnitLang = null;
			} else if (attrValue instanceof Vector) {
				mAltOrgUnitLang = (Vector) attrValue;
			} else {
				mAltOrgUnitLang = new Vector<Object>();
				for (int i = 0; i < attribute.size(); i++) {
					mAltOrgUnitLang.add(attribute.getValue(i));
				}
			}
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_ALT_ORG_UNIT_LANG);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_CREATE_MAIL_DB);
		if (attribute != null) {
			mCreateMailDb = Boolean.valueOf(attribute.getValue())
					.booleanValue();
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_CREATE_MAIL_DB);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_EXPIRATION);
		if (attribute != null) {
			Object attrValue = attribute.getValue(0);
			if (attrValue == null) {
				mExpiration = null;
			} else if (attrValue instanceof Date) {
				mExpiration = (Date) attrValue;
			} else {
				try {
					mExpiration = DateFormat.getInstance().parse(
							attrValue.toString());
				} catch (ParseException e) {
					if (mParent.debugMode()) {
						debug(sResHash
								.getString(
										"CONNECTOR.DOMINOUSERSCONN.USERREGISTRATION.COULD.NOT.PARSE",
										new Object[] { attrValue.toString() }));
					}
					mExpiration = null;
				}
			}
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_EXPIRATION);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_ID_TYPE);
		if (attribute != null) {
			mIDType = Integer.parseInt(attribute.getValue());
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_ID_TYPE);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_IS_NORTH_AMERICAN);
		if (attribute != null) {
			mIsNorthAmerican = Boolean.valueOf(attribute.getValue())
					.booleanValue();
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_IS_NORTH_AMERICAN);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_MIN_PASSWORD_LENGTH);
		if (attribute != null) {
			mMinPasswordLength = Integer.parseInt(attribute.getValue());
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_MIN_PASSWORD_LENGTH);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_ORG_UNIT);
		if (attribute != null) {
			mOrgUnit = attribute.getValue();
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_ORG_UNIT);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_REGISTRATION_LOG);
		if (attribute != null) {
			mRegistrationLog = attribute.getValue();
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_REGISTRATION_LOG);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_REGISTRATION_SERVER);
		if (attribute != null) {
			mRegistrationServer = attribute.getValue();
			entryNoFixedAttr.removeAttribute(ATTR_NAME_REG_REGISTRATION_SERVER);
		}

		attribute = aEntry.getAttribute(ATTR_NAME_REG_STORE_ID_IN_ADDRESS_BOOK);
		if (attribute != null) {
			mStoreIDInAddressBook = Boolean.valueOf(attribute.getValue())
					.booleanValue();
			entryNoFixedAttr
					.removeAttribute(ATTR_NAME_REG_STORE_ID_IN_ADDRESS_BOOK);
		}

		// extract data from the Attributes corresponding to Document Items

		attribute = aEntry
				.getAttribute(DominoUsersConnector.ATTR_NAME_FIRST_NAME);
		if (attribute != null) {
			mFirstName = attribute.getValue();
		}

		attribute = aEntry
				.getAttribute(DominoUsersConnector.ATTR_NAME_MIDDLE_INITIAL);
		if (attribute != null) {
			mMiddleName = attribute.getValue();
		}

		attribute = aEntry
				.getAttribute(DominoUsersConnector.ATTR_NAME_LAST_NAME);
		if (attribute != null) {
			mLastName = attribute.getValue();
		}

		attribute = aEntry
				.getAttribute(DominoUsersConnector.ATTR_NAME_LOCATION);
		if (attribute != null) {
			mLocation = attribute.getValue();
		}

		attribute = aEntry.getAttribute(DominoUsersConnector.ATTR_NAME_COMMENT);
		if (attribute != null) {
			mComment = attribute.getValue();
		}

		attribute = aEntry
				.getAttribute(DominoUsersConnector.ATTR_NAME_MAIL_FILE);
		if (attribute != null) {
			mMailDbPath = attribute.getValue();
		}

		attribute = aEntry
				.getAttribute(DominoUsersConnector.ATTR_NAME_ALT_FULL_NAME);
		if (attribute != null) {
			mAltName = attribute.getValue();
			entryNoFixedAttr.removeAttribute(DominoUsersConnector.ATTR_NAME_ALT_FULL_NAME);
		}

		attribute = aEntry
				.getAttribute(DominoUsersConnector.ATTR_NAME_ALT_FULL_NAME_LANGUAGE);
		if (attribute != null) {
			mAltNameLang = attribute.getValue();
		}

		return entryNoFixedAttr;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean mustPerform(Entry aEntry) throws Exception {
		Attribute attrPerform = aEntry.getAttribute(ATTR_NAME_REG_PERFORM);
		if (attrPerform != null
				&& attrPerform.getValue().equalsIgnoreCase("true")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String canPerform() {
		if (mPerform == false) {
			return sResHash
					.getString(
							"CONNECTOR.DOMINOUSERSCONN.USERREGISTRATION.SHOULD.NOT.PERFORM",
							ATTR_NAME_REG_PERFORM);
		}

		if (mIdFile == null) {
			return sResHash
					.getString(
							"CONNECTOR.DOMINOUSERSCONN.USERREGISTRATION.REQ.ATTR.MISSING",
							ATTR_NAME_REG_ID_FILE);
		}

		if (mCertifierIdFile == null) {
			return sResHash
					.getString(
							"CONNECTOR.DOMINOUSERSCONN.USERREGISTRATION.REQ.ATTR.MISSING",
							ATTR_NAME_REG_CERTIFIER_ID_FILE);
		}

		if (mCertPassword == null) {
			return sResHash
					.getString(
							"CONNECTOR.DOMINOUSERSCONN.USERREGISTRATION.REQ.ATTR.MISSING",
							ATTR_NAME_REG_CERT_PASSWORD);
		}

		if (mLastName == null) {
			return sResHash
					.getString(
							"CONNECTOR.DOMINOUSERSCONN.USERREGISTRATION.REQ.ATTR.MISSING",
							DominoUsersConnector.ATTR_NAME_LAST_NAME);
		}

		if (mIDType != ATTR_VALUE_ID_TYPE_FLAT_ID
				&& mIDType != ATTR_VALUE_ID_TYPE_HIERARCHICAL_ID
				&& mIDType != ATTR_VALUE_ID_TYPE_CERTIFIER) {

			return sResHash
					.getString(
							"CONNECTOR.DOMINOUSERSCONN.USERREGISTRATION.INVALID.ATTR.VALUE",
							new Object[] { Integer.valueOf(mIDType),
									ATTR_NAME_REG_ID_TYPE });
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void perform() throws Exception {
		Registration reg = mSession.createRegistration();
		if (reg == null) {
			if (mParent.debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.DOMINOUSERSCONN.USERREGISTRATION.COULD.NOT.CREATE.REGISTRATION"));
			}
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.USERREGISTRATION.COULD.NOT.CREATE.REGISTRATION2"));
		}

		try {
			// set properties
			if (mAltOrgUnit != null) {
				reg.setAltOrgUnit(mAltOrgUnit);
			}

			if (mAltOrgUnitLang != null) {
				reg.setAltOrgUnitLang(mAltOrgUnitLang);
			}

			if (mCertifierIdFile != null) {
				reg.setCertifierIDFile(mCertifierIdFile);
			}

			reg.setCreateMailDb(mCreateMailDb);

			if (mExpiration != null) {
				reg.setExpiration(mSession.createDateTime(mExpiration));
			} else {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.YEAR, 2);
				Date defaultExpirationDate = calendar.getTime();

				reg.setExpiration(mSession
						.createDateTime(defaultExpirationDate));
			}

			reg.setIDType(ATTR_NOTES_VALUES_ID_TYPE[mIDType]);

			reg.setNorthAmerican(mIsNorthAmerican);

			reg.setMinPasswordLength(mMinPasswordLength);

			if (mOrgUnit != null) {
				reg.setOrgUnit(mOrgUnit);
			}

			if (mRegistrationLog != null) {
				reg.setRegistrationLog(mRegistrationLog);
			}

			if (mRegistrationServer != null) {
				reg.setRegistrationServer(mRegistrationServer);
			}

			reg.setStoreIDInAddressBook(mStoreIDInAddressBook);

			reg.setUpdateAddressBook(REG_UPDATE_ADDRESS_BOOK);

			// prepare parameters
			String server = mServer;
			if (server == null) {
				server = mSession.getServerName();
			}

			// do register
			boolean sucessfulRegistration = reg.registerNewUser(mLastName,
					mIdFile, server, mFirstName, mMiddleName, mCertPassword,
					mLocation, mComment, mMailDbPath, mForward, mUserPw,
					mAltName, mAltNameLang);

			if (!sucessfulRegistration) {
				throw new Exception(sResHash.getString(
						"CONNECTOR.DOMINOUSERSCONN.CANNOT.REGISTER.EXCEPTION",
						mLastName));
			}
		} finally {
			reg.recycle();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void resetData() throws Exception {
		// reset the fixed schema members
		mPerform = false;
		mIdFile = null;
		mUserPw = null;
		mServer = null;
		mCertifierIdFile = null;
		mCertPassword = null;
		mForward = null;
		mAltOrgUnit = null;
		mAltOrgUnitLang = null;
		mCreateMailDb = false;
		mExpiration = null;
		mIDType = ATTR_VALUE_ID_TYPE_CERTIFIER;
		mIsNorthAmerican = true;
		mMinPasswordLength = 0;
		mOrgUnit = "";
		mRegistrationLog = "";
		mRegistrationServer = null;
		mStoreIDInAddressBook = false;

		// reset the Person Document members
		mFirstName = null;
		mMiddleName = null;
		mLastName = null;
		mLocation = null;
		mComment = null;
		mMailDbPath = null;
		mAltName = null;
		mAltNameLang = null;
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
