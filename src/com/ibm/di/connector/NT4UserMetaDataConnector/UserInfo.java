/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.NT4UserMetaDataConnector;

import java.util.Date;
import java.util.Vector;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;

/**
 * The UserInfo class represents Windows User's data structure and encapsulates
 * methods that import and export that data to an Entry object.
 */
public class UserInfo {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The size of the array for the log on hours.
	 */
	public static final int LOGON_HOURS_ARRAY_LENGTH = 21;

	/**
	 * the following bits are set: UF_SCRIPT, UF_DONT_EXPIRE_PASSWD,
	 * UF_NORMAL_ACCOUNT
	 */

	public static final Integer DEFAULT_FLAGS_VALUE = Integer.valueOf(66049);

	/**
	 * "-1000" milliseconds presents "-1" second which is the "account never
	 * expires" value
	 */

	public static final Date ACC_NEVER_EXPIRES = new Date(-1000);

	/**
	 * User name
	 */
	private String mUserName = null;

	/**
	 * Comments for the account
	 */
	private String mAccountComment = null;

	/**
	 * Full name
	 */
	private String mFullName = null;
	/**
	 * User comment
	 */
	private String mUserComment = null;
	/**
	 * Password
	 */
	private String mPassword = null;
	/**
	 * Age password
	 */
	private Long mPasswordAge = null;
	/**
	 * Privilege level
	 */
	private Integer mPrivilegeLevel = null;
	/**
	 * Home directory
	 */
	private String mHomeDirectory = null;
	/**
	 * Flags
	 */
	private Integer mFlags = null;
	/**
	 * Script path
	 */
	private String mScriptPath = null;
	/**
	 * Atuhentication Flags
	 * 
	 */
	private Integer mAuthFlags = null;

	/**
	 * Application parameters
	 */
	private String mApplicationsParams = null;

	/**
	 * Log on Work stations
	 */
	private String mLogonWorkstations = null;

	/**
	 * Last log on
	 */
	private Date mLastLogon = null;

	/**
	 * Last log off
	 */
	private Date mLastLogoff = null;
	/**
	 * Date of expire of the account
	 */
	private Date mAccountExpDate = null;
	/**
	 * Maximal available disk space
	 */
	private Long mMaxAccDiskSpace = null;
	/**
	 * Units per week
	 */
	private Integer mUnitsPerWeek = null;
	/**
	 * Duration of log on in hours
	 */
	private byte[] mLogonHours = null;
	/**
	 * Bad password information
	 */
	private Integer mBadPasswordCnt = null;

	/**
	 * Number of logons
	 */
	private Integer mLogonsNum = null;

	/**
	 * Server logon
	 */
	private String mLogonServer = null;
	/**
	 * Country code
	 */
	private Integer mCountryCode = null;
	/**
	 * Page code
	 */
	private Integer mCodePage = null;
	/**
	 * Relative user ID
	 */
	private Integer mRelativeUserID = null;
	/**
	 * Primary group ID
	 */
	private Integer mPrimaryGroupID = null;
	/**
	 * Profile path
	 */
	private String mProfilePath = null;
	/**
	 * Home directory drive
	 */
	private String mHomeDirectoryDrive = null;
	/**
	 * Password expired
	 */
	private Integer mPasswordExpired = null;
	/**
	 * Primary group
	 */
	private String mPrimaryGroup = null;

	// user entry attributes' names
	/** User entry attribute's name */
	public static final String USER_ATTR_USER_NAME = "UserName";
	/** User entry attribute's name */
	public static final String USER_ATTR_ACCOUNT_COMMENT = "AccountComment";
	/** User entry attribute's name */
	public static final String USER_ATTR_FULL_NAME = "FullName";
	/** User entry attribute's name */
	public static final String USER_ATTR_USER_COMMENT = "UserComment";
	/** User entry attribute's name */
	public static final String USER_ATTR_PASSWORD = "Password";
	/** User entry attribute's name */
	public static final String USER_ATTR_PASSWORD_AGE = "PasswordAge";
	/** User entry attribute's name */
	public static final String USER_ATTR_PRIVILEGE_LEVEL = "PrivilegeLevel";
	/** User entry attribute's name */
	public static final String USER_ATTR_HOME_DIRECTORY = "HomeDirectory";
	/** User entry attribute's name */
	public static final String USER_ATTR_FLAGS = "Flags";
	/** User entry attribute's name */
	public static final String USER_ATTR_SCRIPTH_PATH = "ScriptPath";
	/** User entry attribute's name */
	public static final String USER_ATTR_AUTH_FLAGS = "AuthFlags";
	/** User entry attribute's name */
	public static final String USER_ATTR_APPLICATIONS_PARAMS = "ApplicationsParams";
	/** User entry attribute's name */
	public static final String USER_ATTR_LOGON_WORKSTATIONS = "LogonWorkstations";
	/** User entry attribute's name */
	public static final String USER_ATTR_LAST_LOGON = "LastLogon";
	/** User entry attribute's name */
	public static final String USER_ATTR_LAST_LOGOFF = "LastLogoff";
	/** User entry attribute's name */
	public static final String USER_ATTR_ACCOUNT_EXP_DATE = "AccountExpDate";
	/** User entry attribute's name */
	public static final String USER_ATTR_MAX_ACC_DISK_SPACE = "MaxAccDiskSpace";
	/** User entry attribute's name */
	public static final String USER_ATTR_UNITS_PER_WEEK = "UnitsPerWeek";
	/** User entry attribute's name */
	public static final String USER_ATTR_LOGON_HOURS = "LogonHours";
	/** User entry attribute's name */
	public static final String USER_ATTR_BAD_PASSWORD_CNT = "BadPasswordCnt";
	/** User entry attribute's name */
	public static final String USER_ATTR_LOGONS_NUM = "LogonsNum";
	/** User entry attribute's name */
	public static final String USER_ATTR_LOGON_SERVER = "LogonServer";
	/** User entry attribute's name */
	public static final String USER_ATTR_COUNTRY_CODE = "CountryCode";
	/** User entry attribute's name */
	public static final String USER_ATTR_CODE_PAGE = "CodePage";
	/** User entry attribute's name */
	public static final String USER_ATTR_RELATIVE_USER_ID = "RelativeUserID";
	/** User entry attribute's name */
	public static final String USER_ATTR_PRIMARY_GROUP_ID = "PrimaryGroupID";
	/** User entry attribute's name */
	public static final String USER_ATTR_PROFILE_PATH = "ProfilePath";
	/** User entry attribute's name */
	public static final String USER_ATTR_HOME_DIRECTORY_DRIVE = "HomeDirectoryDrive";
	/** User entry attribute's name */
	public static final String USER_ATTR_PASSWORD_EXPIRED = "PasswordExpired";
	/** User entry attribute's name */
	public static final String USER_ATTR_LOCAL_GROUPS = "LocalGroups";
	/** User entry attribute's name */
	public static final String USER_ATTR_GLOBAL_GROUPS = "GlobalGroups";
	/** User entry attribute's name */
	public static final String USER_ATTR_PRIMARY_GROUP = "PrimaryGroup";

	// *************************************************************************
	// Constructors
	// *************************************************************************

	/**
	 * Default constructor.
	 */
	public UserInfo() {
		super();
	}

	/**
	 * Constructs the UserInfo object and populates its members with User data
	 * from the given Entry parameter.
	 * 
	 * @param aUserEntry
	 *            The User Entry object containing data to initialize the new
	 *            UserInfo object with.
	 */
	protected UserInfo(Entry aUserEntry) {
		super();
		this.copyDataFromUserEntry(aUserEntry);
	}

	// *************************************************************************
	// get methods
	// *************************************************************************
	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return String
	 */
	public String getUserName() {
		return mUserName;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return String
	 */
	public String getAccountComment() {
		return mAccountComment;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return String
	 */
	public String getFullName() {
		return mFullName;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return String
	 */
	public String getUserComment() {
		return mUserComment;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return String
	 */
	public String getPassword() {
		return mPassword;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Long
	 */
	public Long getPasswordAge() {
		return mPasswordAge;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Integer
	 */
	public Integer getPrivilegeLevel() {
		return mPrivilegeLevel;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return String
	 */
	public String getHomeDirectory() {
		return mHomeDirectory;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Integer
	 */
	public Integer getFlags() {
		return mFlags;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return String
	 */
	public String getScriptPath() {
		return mScriptPath;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Integer
	 */
	public Integer getAuthFlags() {
		return mAuthFlags;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return String
	 */
	public String getApplicationsParams() {
		return mApplicationsParams;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return String
	 */
	public String getLogonWorkstations() {
		return mLogonWorkstations;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Date
	 */
	public Date getLastLogon() {
		if (mLastLogon != null) {
			return (Date) mLastLogon.clone();
		} else {
			return null;
		}
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Date
	 */
	public Date getLastLogoff() {
		if (mLastLogoff != null) {
			return (Date) mLastLogoff.clone();
		} else {
			return null;
		}
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Date
	 */
	public Date getAccountExpDate() {
		if (mAccountExpDate != null) {
			return (Date) mAccountExpDate.clone();
		} else {
			return null;
		}
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Long
	 */
	public Long getMaxAccDiskSpace() {
		return mMaxAccDiskSpace;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Integer
	 */
	public Integer getUnitsPerWeek() {
		return mUnitsPerWeek;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Byte array
	 */
	public byte[] getLogonHours() {
		if (mLogonHours != null) {
			return (byte[]) mLogonHours.clone();
		} else {
			return null;
		}
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Integer
	 */
	public Integer getBadPasswordCnt() {
		return mBadPasswordCnt;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Integer
	 */
	public Integer getLogonsNum() {
		return mLogonsNum;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return String
	 */
	public String getLogonServer() {
		return mLogonServer;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Integer
	 */
	public Integer getCountryCode() {
		return mCountryCode;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Integer
	 */
	public Integer getCodePage() {
		return mCodePage;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Integer
	 */
	public Integer getRelativeUserID() {
		return mRelativeUserID;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Integer
	 */
	public Integer getPrimaryGroupID() {
		return mPrimaryGroupID;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return String
	 */
	public String getProfilePath() {
		return mProfilePath;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return String
	 */
	public String getHomeDirectoryDrive() {
		return mHomeDirectoryDrive;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return Integer
	 */
	public Integer getPasswordExpired() {
		return mPasswordExpired;
	}

	/**
	 * Retrieves user defined value for the field
	 * 
	 * @return String
	 */
	public String getPrimaryGroup() {
		return mPrimaryGroup;
	}

	/**
	 * @param aUserName
	 */
	// *************************************************************************
	// set methods
	// *************************************************************************
	/**
	 * Sets user defined value for the field
	 * 
	 * @param aUserName
	 *            String
	 */
	public void setUserName(String aUserName) {
		mUserName = aUserName;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aAccountComment
	 *            String
	 */
	public void setAccountComment(String aAccountComment) {
		mAccountComment = aAccountComment;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aFullName
	 *            String
	 */
	public void setFullName(String aFullName) {
		mFullName = aFullName;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aUserComment
	 *            String
	 */
	public void setUserComment(String aUserComment) {
		mUserComment = aUserComment;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aPassword
	 *            String
	 */
	public void setPassword(String aPassword) {
		mPassword = aPassword;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aPasswordAge
	 *            Long
	 */
	public void setPasswordAge(Long aPasswordAge) {
		mPasswordAge = aPasswordAge;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aPrivilegeLevel
	 *            Integer
	 */
	public void setPrivilegeLevel(Integer aPrivilegeLevel) {
		mPrivilegeLevel = aPrivilegeLevel;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aHomeDirectory
	 *            String
	 */
	public void setHomeDirectory(String aHomeDirectory) {
		mHomeDirectory = aHomeDirectory;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aFlags
	 *            Integer
	 */
	public void setFlags(Integer aFlags) {
		mFlags = aFlags;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aScriptParh
	 *            String
	 */
	public void setScriptPath(String aScriptParh) {
		mScriptPath = aScriptParh;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aAuthFlags
	 *            Integer
	 */
	public void setAuthFlags(Integer aAuthFlags) {
		mAuthFlags = aAuthFlags;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aApplicationsParams
	 *            String
	 */
	public void setApplicationsParams(String aApplicationsParams) {
		mApplicationsParams = aApplicationsParams;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aLogonWorkstations
	 *            String
	 */
	public void setLogonWorkstations(String aLogonWorkstations) {
		mLogonWorkstations = aLogonWorkstations;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aLastLogon
	 *            Date
	 */
	public void setLastLogon(Date aLastLogon) {
		mLastLogon = new Date(aLastLogon.getTime());
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aLastLogoff
	 *            Date
	 */
	public void setLastLogoff(Date aLastLogoff) {
		mLastLogoff = new Date(aLastLogoff.getTime());
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aAccountExpDate
	 *            Date
	 */
	public void setAccountExpDate(Date aAccountExpDate) {
		mAccountExpDate = new Date(aAccountExpDate.getTime());
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aMaxAccDiskSpace
	 *            Long
	 */
	public void setMaxAccDiskSpace(Long aMaxAccDiskSpace) {
		mMaxAccDiskSpace = aMaxAccDiskSpace;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aUnitsPerWeek
	 *            Integer
	 */
	public void setUnitsPerWeek(Integer aUnitsPerWeek) {
		mUnitsPerWeek = aUnitsPerWeek;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aLogonHours
	 *            <code>byte[]</code>
	 */
	public void setLogonHours(byte[] aLogonHours) {
		mLogonHours = new byte[LOGON_HOURS_ARRAY_LENGTH];
		System.arraycopy(aLogonHours, 0, mLogonHours, 0,
				LOGON_HOURS_ARRAY_LENGTH);
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aBadPasswordCnt
	 *            Integer
	 */
	public void setBadPasswordCnt(Integer aBadPasswordCnt) {
		mBadPasswordCnt = aBadPasswordCnt;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aLogonsNum
	 *            Integer
	 */
	public void setLogonsNum(Integer aLogonsNum) {
		mLogonsNum = aLogonsNum;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aLogonServer
	 *            String
	 */
	public void setLogonServer(String aLogonServer) {
		mLogonServer = aLogonServer;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aCountryCode
	 *            Integer
	 */
	public void setCountryCode(Integer aCountryCode) {
		mCountryCode = aCountryCode;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aCodePage
	 *            Integer
	 */
	public void setCodePage(Integer aCodePage) {
		mCodePage = aCodePage;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aRelativeUserID
	 *            Integer
	 */
	public void setRelativeUserID(Integer aRelativeUserID) {
		mRelativeUserID = aRelativeUserID;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aPrimaryGroupID
	 *            Integer
	 */
	public void setPrimaryGroupID(Integer aPrimaryGroupID) {
		mPrimaryGroupID = aPrimaryGroupID;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aProfilePath
	 *            String
	 */
	public void setProfilePath(String aProfilePath) {
		mProfilePath = aProfilePath;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aHomeDirectoryDrive
	 *            String
	 */
	public void setHomeDirectoryDrive(String aHomeDirectoryDrive) {
		mHomeDirectoryDrive = aHomeDirectoryDrive;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aPasswordExpired
	 *            Integer
	 */
	public void setPasswordExpired(Integer aPasswordExpired) {
		mPasswordExpired = aPasswordExpired;
	}

	/**
	 * Sets user defined value for the field
	 * 
	 * @param aPrimaryGroup
	 *            String
	 */
	public void setPrimaryGroup(String aPrimaryGroup) {
		mPrimaryGroup = aPrimaryGroup;
	}

	// *************************************************************************
	// utilities
	// *************************************************************************

	/**
	 * Copies data from a user entry.
	 * 
	 * @param aUserEntry
	 *            The user entry to copy from.
	 */
	protected void copyDataFromUserEntry(Entry aUserEntry) {
		mUserName = InfoUtil.getStringEntryAttributeValue(aUserEntry,
				USER_ATTR_USER_NAME);
		mAccountComment = InfoUtil.getStringEntryAttributeValue(aUserEntry,
				USER_ATTR_ACCOUNT_COMMENT);
		mFullName = InfoUtil.getStringEntryAttributeValue(aUserEntry,
				USER_ATTR_FULL_NAME);
		mUserComment = InfoUtil.getStringEntryAttributeValue(aUserEntry,
				USER_ATTR_USER_COMMENT);
		mPassword = InfoUtil.getStringEntryAttributeValue(aUserEntry,
				USER_ATTR_PASSWORD);
		mPasswordAge = InfoUtil.getLongEntryAttributeValue(aUserEntry,
				USER_ATTR_PASSWORD_AGE);
		mPrivilegeLevel = InfoUtil.getIntegerEntryAttributeValue(aUserEntry,
				USER_ATTR_PRIVILEGE_LEVEL);
		mHomeDirectory = InfoUtil.getStringEntryAttributeValue(aUserEntry,
				USER_ATTR_HOME_DIRECTORY);
		mFlags = InfoUtil.getIntegerEntryAttributeValue(aUserEntry,
				USER_ATTR_FLAGS);
		mScriptPath = InfoUtil.getStringEntryAttributeValue(aUserEntry,
				USER_ATTR_SCRIPTH_PATH);
		mAuthFlags = InfoUtil.getIntegerEntryAttributeValue(aUserEntry,
				USER_ATTR_AUTH_FLAGS);
		mApplicationsParams = InfoUtil.getStringEntryAttributeValue(aUserEntry,
				USER_ATTR_APPLICATIONS_PARAMS);
		mLogonWorkstations = InfoUtil.getStringEntryAttributeValue(aUserEntry,
				USER_ATTR_LOGON_WORKSTATIONS);
		mLastLogon = InfoUtil.getDateEntryAttributeValue(aUserEntry,
				USER_ATTR_LAST_LOGON);
		mLastLogoff = InfoUtil.getDateEntryAttributeValue(aUserEntry,
				USER_ATTR_LAST_LOGOFF);
		mAccountExpDate = InfoUtil.getDateEntryAttributeValue(aUserEntry,
				USER_ATTR_ACCOUNT_EXP_DATE);
		mMaxAccDiskSpace = InfoUtil.getLongEntryAttributeValue(aUserEntry,
				USER_ATTR_MAX_ACC_DISK_SPACE);
		mUnitsPerWeek = InfoUtil.getIntegerEntryAttributeValue(aUserEntry,
				USER_ATTR_UNITS_PER_WEEK);
		mLogonHours = InfoUtil.getByteArrayEntryAttributeValue(aUserEntry,
				USER_ATTR_LOGON_HOURS);
		mBadPasswordCnt = InfoUtil.getIntegerEntryAttributeValue(aUserEntry,
				USER_ATTR_BAD_PASSWORD_CNT);
		mLogonsNum = InfoUtil.getIntegerEntryAttributeValue(aUserEntry,
				USER_ATTR_LOGONS_NUM);
		mLogonServer = InfoUtil.getStringEntryAttributeValue(aUserEntry,
				USER_ATTR_LOGON_SERVER);
		mCountryCode = InfoUtil.getIntegerEntryAttributeValue(aUserEntry,
				USER_ATTR_COUNTRY_CODE);
		mCodePage = InfoUtil.getIntegerEntryAttributeValue(aUserEntry,
				USER_ATTR_CODE_PAGE);
		mRelativeUserID = InfoUtil.getIntegerEntryAttributeValue(aUserEntry,
				USER_ATTR_RELATIVE_USER_ID);
		mPrimaryGroupID = InfoUtil.getIntegerEntryAttributeValue(aUserEntry,
				USER_ATTR_PRIMARY_GROUP_ID);
		mProfilePath = InfoUtil.getStringEntryAttributeValue(aUserEntry,
				USER_ATTR_PROFILE_PATH);
		mHomeDirectoryDrive = InfoUtil.getStringEntryAttributeValue(aUserEntry,
				USER_ATTR_HOME_DIRECTORY_DRIVE);
		mPasswordExpired = InfoUtil.getIntegerEntryAttributeValue(aUserEntry,
				USER_ATTR_PASSWORD_EXPIRED);
		mPrimaryGroup = InfoUtil.getStringEntryAttributeValue(aUserEntry,
				USER_ATTR_PRIMARY_GROUP);
	}

	/**
	 * All "null" data members are assigned the values of the corresponding
	 * aUserInfo's data members.
	 * 
	 * @param aUserInfo
	 *            The UserInfo instance which data members' data will be copied.
	 */
	protected void completeUserData(UserInfo aUserInfo) {
		if (mUserName == null) {
			setUserName(aUserInfo.getUserName());
		}

		if (mAccountComment == null) {
			setAccountComment(aUserInfo.getAccountComment());
		}

		if (mFullName == null) {
			setFullName(aUserInfo.getFullName());
		}

		if (mUserComment == null) {
			setUserComment(aUserInfo.getUserComment());
		}

		if (mPassword == null) {
			setPassword(aUserInfo.getPassword());
		}

		if (mPasswordAge == null) {
			setPasswordAge(aUserInfo.getPasswordAge());
		}

		if (mPrivilegeLevel == null) {
			setPrivilegeLevel(aUserInfo.getPrivilegeLevel());
		}

		if (mHomeDirectory == null) {
			setHomeDirectory(aUserInfo.getHomeDirectory());
		}

		if (mFlags == null) {
			setFlags(aUserInfo.getFlags());
		}

		if (mScriptPath == null) {
			setScriptPath(aUserInfo.getScriptPath());
		}

		if (mAuthFlags == null) {
			setAuthFlags(aUserInfo.getAuthFlags());
		}

		if (mApplicationsParams == null) {
			setApplicationsParams(aUserInfo.getApplicationsParams());
		}

		if (mLogonWorkstations == null) {
			setLogonWorkstations(aUserInfo.getLogonWorkstations());
		}

		if (mLastLogon == null) {
			setLastLogon(aUserInfo.getLastLogon());
		}

		if (mLastLogoff == null) {
			setLastLogoff(aUserInfo.getLastLogoff());
		}

		if (mAccountExpDate == null) {
			setAccountExpDate(aUserInfo.getAccountExpDate());
		}

		if (mMaxAccDiskSpace == null) {
			setMaxAccDiskSpace(aUserInfo.getMaxAccDiskSpace());
		}

		if (mUnitsPerWeek == null) {
			setUnitsPerWeek(aUserInfo.getUnitsPerWeek());
		}

		if (mLogonHours == null) {
			setLogonHours(aUserInfo.getLogonHours());
		}

		if (mBadPasswordCnt == null) {
			setBadPasswordCnt(aUserInfo.getBadPasswordCnt());
		}

		if (mLogonsNum == null) {
			setLogonsNum(aUserInfo.getLogonsNum());
		}

		if (mLogonServer == null) {
			setLogonServer(aUserInfo.getLogonServer());
		}

		if (mCountryCode == null) {
			setCountryCode(aUserInfo.getCountryCode());
		}

		if (mCodePage == null) {
			setCodePage(aUserInfo.getCodePage());
		}

		if (mRelativeUserID == null) {
			setRelativeUserID(aUserInfo.getRelativeUserID());
		}

		if (mPrimaryGroupID == null) {
			setPrimaryGroupID(aUserInfo.getPrimaryGroupID());
		}

		if (mProfilePath == null) {
			setProfilePath(aUserInfo.getProfilePath());
		}

		if (mHomeDirectoryDrive == null) {
			setHomeDirectoryDrive(aUserInfo.getHomeDirectoryDrive());
		}

		if (mPasswordExpired == null) {
			setPasswordExpired(aUserInfo.getPasswordExpired());
		}

		if (mPrimaryGroup == null) {
			setPrimaryGroup(aUserInfo.getPrimaryGroup());
		}
	}

	/**
	 * Creates and adds basic Windows user's attributes to the given Entry
	 * object.
	 * 
	 * @param aUserEntry
	 *            The entry object that is going to be populated with
	 *            attributes.
	 * @param aUserInfo
	 *            The UserInfo structure containing user's attributes values.
	 */
	protected static void createAndAddUserBasicAttributes(Entry aUserEntry,
			UserInfo aUserInfo) {
		InfoUtil.createAndAddEntryAttribute(aUserEntry, USER_ATTR_USER_NAME,
				aUserInfo.getUserName());
		InfoUtil.createAndAddEntryAttribute(aUserEntry,
				USER_ATTR_ACCOUNT_COMMENT, aUserInfo.getAccountComment());
		InfoUtil.createAndAddEntryAttribute(aUserEntry, USER_ATTR_FULL_NAME,
				aUserInfo.getFullName());
		InfoUtil.createAndAddEntryAttribute(aUserEntry, USER_ATTR_USER_COMMENT,
				aUserInfo.getUserComment());
		InfoUtil.createAndAddEntryAttribute(aUserEntry, USER_ATTR_PASSWORD,
				aUserInfo.getPassword());
		InfoUtil.createAndAddEntryAttribute(aUserEntry, USER_ATTR_PASSWORD_AGE,
				aUserInfo.getPasswordAge());
		InfoUtil.createAndAddEntryAttribute(aUserEntry,
				USER_ATTR_PRIVILEGE_LEVEL, aUserInfo.getPrivilegeLevel());
		InfoUtil.createAndAddEntryAttribute(aUserEntry,
				USER_ATTR_HOME_DIRECTORY, aUserInfo.getHomeDirectory());
		InfoUtil.createAndAddEntryAttribute(aUserEntry, USER_ATTR_FLAGS,
				aUserInfo.getFlags());
		InfoUtil.createAndAddEntryAttribute(aUserEntry, USER_ATTR_SCRIPTH_PATH,
				aUserInfo.getScriptPath());
		InfoUtil.createAndAddEntryAttribute(aUserEntry, USER_ATTR_AUTH_FLAGS,
				aUserInfo.getAuthFlags());
		InfoUtil.createAndAddEntryAttribute(aUserEntry,
				USER_ATTR_APPLICATIONS_PARAMS, aUserInfo
						.getApplicationsParams());
		InfoUtil.createAndAddEntryAttribute(aUserEntry,
				USER_ATTR_LOGON_WORKSTATIONS, aUserInfo.getLogonWorkstations());
		InfoUtil.createAndAddEntryAttribute(aUserEntry, USER_ATTR_LAST_LOGON,
				aUserInfo.getLastLogon());
		InfoUtil.createAndAddEntryAttribute(aUserEntry, USER_ATTR_LAST_LOGOFF,
				aUserInfo.getLastLogoff());
		InfoUtil.createAndAddEntryAttribute(aUserEntry,
				USER_ATTR_ACCOUNT_EXP_DATE, aUserInfo.getAccountExpDate());
		InfoUtil.createAndAddEntryAttribute(aUserEntry,
				USER_ATTR_MAX_ACC_DISK_SPACE, aUserInfo.getMaxAccDiskSpace());
		InfoUtil.createAndAddEntryAttribute(aUserEntry,
				USER_ATTR_UNITS_PER_WEEK, aUserInfo.getUnitsPerWeek());
		InfoUtil.createAndAddEntryAttribute(aUserEntry, USER_ATTR_LOGON_HOURS,
				aUserInfo.getLogonHours());
		InfoUtil.createAndAddEntryAttribute(aUserEntry,
				USER_ATTR_BAD_PASSWORD_CNT, aUserInfo.getBadPasswordCnt());
		InfoUtil.createAndAddEntryAttribute(aUserEntry, USER_ATTR_LOGONS_NUM,
				aUserInfo.getLogonsNum());
		InfoUtil.createAndAddEntryAttribute(aUserEntry, USER_ATTR_LOGON_SERVER,
				aUserInfo.getLogonServer());
		InfoUtil.createAndAddEntryAttribute(aUserEntry, USER_ATTR_COUNTRY_CODE,
				aUserInfo.getCountryCode());
		InfoUtil.createAndAddEntryAttribute(aUserEntry, USER_ATTR_CODE_PAGE,
				aUserInfo.getCodePage());
		InfoUtil.createAndAddEntryAttribute(aUserEntry,
				USER_ATTR_RELATIVE_USER_ID, aUserInfo.getRelativeUserID());
		InfoUtil.createAndAddEntryAttribute(aUserEntry,
				USER_ATTR_PRIMARY_GROUP_ID, aUserInfo.getPrimaryGroupID());
		InfoUtil.createAndAddEntryAttribute(aUserEntry, USER_ATTR_PROFILE_PATH,
				aUserInfo.getProfilePath());
		InfoUtil.createAndAddEntryAttribute(aUserEntry,
				USER_ATTR_HOME_DIRECTORY_DRIVE, aUserInfo
						.getHomeDirectoryDrive());
		InfoUtil.createAndAddEntryAttribute(aUserEntry,
				USER_ATTR_PASSWORD_EXPIRED, aUserInfo.getPasswordExpired());
		InfoUtil.createAndAddEntryAttribute(aUserEntry,
				USER_ATTR_PRIMARY_GROUP, aUserInfo.getPrimaryGroup());
	}

	/**
	 * Creates and adds user's LocalGroup attribute to the given Entry object.
	 * The LocalGroup attribute is a Vector of local group names.
	 * 
	 * @param aUserEntry
	 *            The entry object that the LocalGroup attribute will be added
	 *            to.
	 * @param aGroupNames
	 *            The vector containing the names of the user's local groups.
	 */
	protected static void createAndAddUserLocalGroupAttribute(Entry aUserEntry,
			Vector aGroupNames) {
		Attribute groups = aUserEntry.newAttribute(USER_ATTR_LOCAL_GROUPS);
		addVectorToAttribute(groups, aGroupNames);
	}

	/**
	 * Creates and adds user's GlobalGroup attribute to the given Entry object.
	 * The GlobalGroup attribute is a Vector of global group names.
	 * 
	 * @param aUserEntry
	 *            The entry object that the GlobalGroup attribute will be added
	 *            to.
	 * @param aGroupNames
	 *            The vector containing the names of the user's global groups.
	 */
	protected static void createAndAddUserGlobalGroupAttribute(
			Entry aUserEntry, Vector aGroupNames) {
		Attribute groups = aUserEntry.newAttribute(USER_ATTR_GLOBAL_GROUPS);
		addVectorToAttribute(groups, aGroupNames);
	}

	/**
	 * Adds all Vector's elements as values in the given Attribute.
	 * 
	 * @param aAttribute
	 *            the attribute to add objects to.
	 * @param aVector
	 *            the source of the objects.
	 */
	private static void addVectorToAttribute(Attribute aAttribute,
			Vector aVector) {
		int vectorSize = aVector.size();
		for (int i = 0; i < vectorSize; i++) {
			aAttribute.addValue(aVector.elementAt(i));
		}
	}

	/**
	 * Retrieves connector user entry's structure.
	 * 
	 * @return Vector of elements of type Entry describing each user's attribute
	 *         structure.
	 */
	protected static Vector queryUserSchema() {
		Vector schema = new Vector();

		InfoUtil.addSchemaEntry(schema, USER_ATTR_USER_NAME,
				InfoUtil.QSS_STRING, Integer.valueOf(256));
		InfoUtil.addSchemaEntry(schema, USER_ATTR_ACCOUNT_COMMENT,
				InfoUtil.QSS_STRING, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_FULL_NAME,
				InfoUtil.QSS_STRING, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_USER_COMMENT,
				InfoUtil.QSS_STRING, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_PASSWORD,
				InfoUtil.QSS_STRING, Integer.valueOf(14));
		InfoUtil.addSchemaEntry(schema, USER_ATTR_PASSWORD_AGE,
				InfoUtil.QSS_LONG, Long.valueOf(InfoUtil.MAX_UNSIGNED_DWORD));
		InfoUtil.addSchemaEntry(schema, USER_ATTR_PRIVILEGE_LEVEL,
				InfoUtil.QSS_INTEGER, Long.valueOf(2));
		InfoUtil.addSchemaEntry(schema, USER_ATTR_HOME_DIRECTORY,
				InfoUtil.QSS_STRING, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_FLAGS, InfoUtil.QSS_INTEGER,
				null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_SCRIPTH_PATH,
				InfoUtil.QSS_STRING, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_AUTH_FLAGS,
				InfoUtil.QSS_STRING, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_APPLICATIONS_PARAMS,
				InfoUtil.QSS_STRING, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_LOGON_WORKSTATIONS,
				InfoUtil.QSS_STRING, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_LAST_LOGON,
				InfoUtil.QSS_DATE, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_LAST_LOGOFF,
				InfoUtil.QSS_DATE, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_ACCOUNT_EXP_DATE,
				InfoUtil.QSS_DATE, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_MAX_ACC_DISK_SPACE,
				InfoUtil.QSS_LONG, Long.valueOf(InfoUtil.MAX_UNSIGNED_DWORD));
		InfoUtil.addSchemaEntry(schema, USER_ATTR_UNITS_PER_WEEK,
				InfoUtil.QSS_INTEGER, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_LOGON_HOURS,
				InfoUtil.QSS_BYTE_ARRAY, Integer.valueOf(21));
		InfoUtil.addSchemaEntry(schema, USER_ATTR_BAD_PASSWORD_CNT,
				InfoUtil.QSS_INTEGER, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_LOGONS_NUM,
				InfoUtil.QSS_INTEGER, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_LOGON_SERVER,
				InfoUtil.QSS_STRING, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_COUNTRY_CODE,
				InfoUtil.QSS_INTEGER, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_CODE_PAGE,
				InfoUtil.QSS_INTEGER, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_RELATIVE_USER_ID,
				InfoUtil.QSS_INTEGER, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_PRIMARY_GROUP_ID,
				InfoUtil.QSS_INTEGER, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_PROFILE_PATH,
				InfoUtil.QSS_STRING, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_HOME_DIRECTORY_DRIVE,
				InfoUtil.QSS_STRING, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_PASSWORD_EXPIRED,
				InfoUtil.QSS_INTEGER, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_LOCAL_GROUPS,
				InfoUtil.QSS_VECTOR, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_GLOBAL_GROUPS,
				InfoUtil.QSS_VECTOR, null);
		InfoUtil.addSchemaEntry(schema, USER_ATTR_PRIMARY_GROUP,
				InfoUtil.QSS_STRING, Integer.valueOf(256));

		return schema;
	}

}
