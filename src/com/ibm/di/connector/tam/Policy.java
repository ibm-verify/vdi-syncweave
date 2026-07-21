/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.tam;

import java.util.List;
import java.util.Date;
import java.util.Vector;
import java.util.HashMap;
import com.ibm.di.server.Log;
import com.ibm.di.server.Trace;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.SearchCriteria;
import com.tivoli.pd.jadmin.PDPolicy;
import com.tivoli.pd.jadmin.PDUser;
import com.tivoli.pd.jutil.PDContext;
import com.tivoli.pd.jutil.PDException;
import com.tivoli.pd.jutil.PDMessages;

public class Policy extends CommonBase {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Boolean mAcctDisableTimeEnforced;

	private Boolean mAcctDisableTimeUnlimited;

	private Boolean mAcctExpDateEnforced;

	private Boolean mAcctExpDateUnlimited;

	private Boolean mMaxFailedLoginsEnforced;

	private Boolean mMaxPwdAgeEnforced;

	private Boolean mMaxPwdRepCharsEnforced;

	private Boolean mMinPwdAlphasEnforced;

	private Boolean mMaxConcWebSessionsEnforced;

	private Boolean mMaxConcWebSessionsDisplaced;

	private Boolean mMaxConcWebSessionsUnlimited;

	private Boolean mMinPwdLenEnforced;

	private Boolean mMinPwdNonAlphasEnforced;

	private Boolean mPwdSpacesAllowed;

	private Boolean mPwdSpacesAllowedEnforced;

	private Boolean mTodAccessEnforced;

	private Long mAcctDisableTimeInterval;

	private Date mAcctExpDate;

	private Integer mMaxFailedLogins;

	private Integer mMaxConcWebSessions;

	private Long mMaxPwdAge;

	private Integer mMaxPwdRepChars;

	private Integer mMinPwdAlphas;

	private Integer mMinPwdLen;

	private Integer mMinPwdNonAlphas;

	private Long mAccessEndTime;

	private Long mAccessibleDays;

	private Long mAccessStartTime;

	private Long mAccessTimezone;

	private String mUserID;

	private static final String POLICY_ATTR_ACCT_DISABLE_TIME_ENFORCED = "AcctDisableTimeEnforced";

	private static final String POLICY_ATTR_ACCT_DISABLE_TIME_UNLIMITED = "AcctDisableTimeUnlimited";

	private static final String POLICY_ATTR_ACCT_EXP_DATE_ENFORCED = "AcctExpDateEnforced";

	private static final String POLICY_ATTR_ACCT_EXP_DATE_UNLIMITED = "AcctExpDateUnlimited";

	private static final String POLICY_ATTR_MAX_FAILED_LOGINS_ENFORCED = "MaxFailedLoginsEnforced";

	private static final String POLICY_ATTR_MAX_CONC_WEB_SESSIONS_ENFORCED = "MaxConcWebSessionsEnforced";

	private static final String POLICY_ATTR_MAX_CONC_WEB_SESSIONS_UNLIMITED = "MaxConcWebSessionsUnlimited";

	private static final String POLICY_ATTR_MAX_CONC_WEB_SESSIONS_DISPLACED = "MaxConcWebSessionsDisplaced";

	private static final String POLICY_ATTR_MAX_PWD_AGE_ENFORCED = "MaxPwdAgeEnforced";

	private static final String POLICY_ATTR_MAX_PWD_REP_CHARS_ENFORCED = "MaxPwdRepCharsEnforced";

	private static final String POLICY_ATTR_MIN_PWD_ALPHAS_ENFORCED = "MinPwdAlphasEnforced";

	private static final String POLICY_ATTR_MIN_PWD_LEN_ENFORCED = "MinPwdLenEnforced";

	private static final String POLICY_ATTR_MIN_PWD_NON_ALPHAS_ENFORCED = "MinPwdNonAlphasEnforced";

	private static final String POLICY_ATTR_PWD_SPACES_ALLOWED = "PwdSpacesAllowed";

	private static final String POLICY_ATTR_PWD_SPACES_ALLOWED_ENFORCED = "PwdSpacesAllowedEnforced";

	private static final String POLICY_ATTR_TOD_ACCESS_ENFORCED = "TodAccessEnforced";

	private static final String POLICY_ATTR_ACCT_DISABLE_TIME_INTERVAL = "AcctDisableTimeInterval";

	private static final String POLICY_ATTR_ACCT_EXP_DATE = "AcctExpDate";

	private static final String POLICY_ATTR_MAX_FAILED_LOGINS = "MaxFailedLogins";

	private static final String POLICY_ATTR_MAX_CONC_WEB_SESSIONS = "MaxConcWebSessions";

	private static final String POLICY_ATTR_MAX_PWD_AGE = "MaxPwdAge";

	private static final String POLICY_ATTR_MAX_PWD_REP_CHARS = "MaxPwdRepChars";

	private static final String POLICY_ATTR_MIN_PWD_ALPHAS = "MinPwdAlphas";

	private static final String POLICY_ATTR_MIN_PWD_LEN = "MinPwdLen";

	private static final String POLICY_ATTR_MIN_PWD_NON_ALPHAS = "MinPwdNonAlphas";

	private static final String POLICY_ATTR_ACCESS_END_TIME = "AccessEndTime";

	private static final String POLICY_ATTR_ACCESSIBLE_DAYS = "AccessibleDays";

	private static final String POLICY_ATTR_ACCESS_START_TIME = "AccessStartTime";

	private static final String POLICY_ATTR_ACCESS_TIMEZONE = "AccessTimezone";

	public static final String POLICY_ATTR_USER_ID = "UserName";

	private static final String POLICY = "Policy";

	/**
	 * Policy Contructor
	 * 
	 * @param s
	 *            The Policy name
	 * @param context
	 *            The TAM Context
	 * @param log
	 *            The IBM Tivoli Directory Integrator Connector log
	 * 
	 * @throws PDException
	 */
	public Policy(String s, PDContext context, Log log) throws PDException {
		super(context, log);
		Trace.entrymin(this, "Policy Constructor #1", log);
		PDPolicy pdPolicy = new PDPolicy(mPDContext, s, mPDMessages);
		processMsgs(mPDMessages);
		set(pdPolicy);
		Trace.exitmin(this, "Policy Constructor #1");
	}

	/**
	 * Policy Constructor
	 * 
	 * @param entry
	 *            The Entry data from IBM Tivoli Directory Integrator
	 * @param context
	 *            The TAM Context
	 * @param log
	 *            The IBM Tivoli Directory Integrator Connector log
	 */
	public Policy(Entry entry, PDContext context, Log log) {
		super(context, log);
		Trace.entrymin(this, "Policy Constructor #2", log);
		set(entry);
		Trace.exitmin(this, "Policy Constructor #2");
	}

	/**
	 * Policy Constructor
	 * 
	 * @param searchcriteria
	 *            The Search criteria
	 * @param context
	 *            The TAM Context
	 * @param log
	 *            The IBM Tivoli Directory Integrator Connector Log
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public Policy(SearchCriteria searchcriteria, PDContext context, Log log)
			throws TAMConnectorException, PDException {
		super(context, log);
		Trace.entrymin(this, "Policy Constructor #3", log);
		String s = searchcriteria.getFirstCriteriaName();
		int i = searchcriteria.getFirstCriteriaMatch();
		if (!s.equalsIgnoreCase(POLICY_ATTR_USER_ID)
				|| i != SearchCriteria.EXCACT)
			throw new TAMConnectorException(TMSMessageGetter
					.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
		PDPolicy policy = new PDPolicy(mPDContext, searchcriteria
				.getFirstCriteriaValue(), mPDMessages);
		processMsgs(mPDMessages);
		set(policy);
		Trace.exitmin(this, "Policy Constructor #3");
	}

	/**
	 * Policy Constructor. Constructs a Policy initialised only with the
	 * SearchCriteria attributes.
	 * 
	 * 
	 * @param context
	 *            The TAM Context.
	 * @param searchcriteria
	 *            The Search criteria
	 * @param log
	 *            The IBM Tivoli Directory Integrator Connector Log
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public Policy(PDContext context, SearchCriteria searchcriteria, Log log)
			throws TAMConnectorException, PDException {
		super(context, log);
		Trace.entrymin(this, "Policy Constructor #4", log);
		String s = searchcriteria.getFirstCriteriaName();
		int i = searchcriteria.getFirstCriteriaMatch();
		String sv = searchcriteria.getFirstCriteriaValue();
		if (!s.equalsIgnoreCase(POLICY_ATTR_USER_ID)
				|| i != SearchCriteria.EXCACT)
			throw new TAMConnectorException(TMSMessageGetter
					.getMessage(TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA));
		if (sv == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, POLICY_ATTR_USER_ID));
		}
		mUserID = sv;
		Trace.exitmin(this, "Policy Constructor #4");
	}

	/**
	 * Set the object with the Entry data
	 * 
	 * @param entry
	 *            The IBM Tivoli Directory Integrator Entry data
	 */
	public void set(Entry entry) {
		Trace.entrymin(this, "Policy.set");
		Boolean adte = getBooleanEntryAttributeValue(entry,
				POLICY_ATTR_ACCT_DISABLE_TIME_ENFORCED);
		if (adte != null)
			mAcctDisableTimeEnforced = adte;
		debug(POLICY_ATTR_ACCT_DISABLE_TIME_ENFORCED + " = "
				+ mAcctDisableTimeEnforced);
		Boolean dtu = getBooleanEntryAttributeValue(entry,
				POLICY_ATTR_ACCT_DISABLE_TIME_UNLIMITED);
		if (dtu != null)
			mAcctDisableTimeUnlimited = dtu;
		debug(POLICY_ATTR_ACCT_DISABLE_TIME_UNLIMITED + " = "
				+ mAcctDisableTimeUnlimited);
		Boolean ede = getBooleanEntryAttributeValue(entry,
				POLICY_ATTR_ACCT_EXP_DATE_ENFORCED);
		if (ede != null)
			mAcctExpDateEnforced = ede;
		debug(POLICY_ATTR_ACCT_EXP_DATE_ENFORCED + " = " + mAcctExpDateEnforced);
		Boolean edu = getBooleanEntryAttributeValue(entry,
				POLICY_ATTR_ACCT_EXP_DATE_UNLIMITED);
		if (edu != null)
			mAcctExpDateUnlimited = edu;
		debug(POLICY_ATTR_ACCT_EXP_DATE_UNLIMITED + " = "
				+ mAcctExpDateUnlimited);
		Boolean maxFLE = getBooleanEntryAttributeValue(entry,
				POLICY_ATTR_MAX_FAILED_LOGINS_ENFORCED);
		if (maxFLE != null)
			mMaxFailedLoginsEnforced = maxFLE;
		debug(POLICY_ATTR_MAX_FAILED_LOGINS_ENFORCED + " = "
				+ mMaxFailedLoginsEnforced);
		Boolean maxCWSE = getBooleanEntryAttributeValue(entry,
				POLICY_ATTR_MAX_CONC_WEB_SESSIONS_ENFORCED);
		if (maxCWSE != null)
			mMaxConcWebSessionsEnforced = maxCWSE;
		debug(POLICY_ATTR_MAX_CONC_WEB_SESSIONS_ENFORCED + " = "
				+ mMaxConcWebSessionsEnforced);
		Boolean maxCWSU = getBooleanEntryAttributeValue(entry,
				POLICY_ATTR_MAX_CONC_WEB_SESSIONS_UNLIMITED);
		if (maxCWSU != null)
			mMaxConcWebSessionsUnlimited = maxCWSU;
		debug(POLICY_ATTR_MAX_CONC_WEB_SESSIONS_UNLIMITED + " = "
				+ mMaxConcWebSessionsUnlimited);
		Boolean maxCWSD = getBooleanEntryAttributeValue(entry,
				POLICY_ATTR_MAX_CONC_WEB_SESSIONS_DISPLACED);
		if (maxCWSD != null)
			mMaxConcWebSessionsDisplaced = maxCWSD;
		debug(POLICY_ATTR_MAX_CONC_WEB_SESSIONS_DISPLACED + " = "
				+ mMaxConcWebSessionsDisplaced);
		Boolean maxPAE = getBooleanEntryAttributeValue(entry,
				POLICY_ATTR_MAX_PWD_AGE_ENFORCED);
		if (maxPAE != null)
			mMaxPwdAgeEnforced = maxPAE;
		debug(POLICY_ATTR_MAX_PWD_AGE_ENFORCED + " = " + mMaxPwdAgeEnforced);
		Boolean prc = getBooleanEntryAttributeValue(entry,
				POLICY_ATTR_MAX_PWD_REP_CHARS_ENFORCED);
		if (prc != null)
			mMaxPwdRepCharsEnforced = prc;
		debug(POLICY_ATTR_MAX_PWD_REP_CHARS_ENFORCED + " = "
				+ mMaxPwdRepCharsEnforced);
		Boolean mpae = getBooleanEntryAttributeValue(entry,
				POLICY_ATTR_MIN_PWD_ALPHAS_ENFORCED);
		if (mpae != null)
			mMinPwdAlphasEnforced = mpae;
		debug(POLICY_ATTR_MIN_PWD_ALPHAS_ENFORCED + " = "
				+ mMinPwdAlphasEnforced);
		Boolean ple = getBooleanEntryAttributeValue(entry,
				POLICY_ATTR_MIN_PWD_LEN_ENFORCED);
		if (ple != null)
			mMinPwdLenEnforced = ple;
		debug(POLICY_ATTR_MIN_PWD_LEN_ENFORCED + " = " + mMinPwdLenEnforced);
		Boolean pnae = getBooleanEntryAttributeValue(entry,
				POLICY_ATTR_MIN_PWD_NON_ALPHAS_ENFORCED);
		if (pnae != null)
			mMinPwdNonAlphasEnforced = pnae;
		debug(POLICY_ATTR_MIN_PWD_NON_ALPHAS_ENFORCED + " = "
				+ mMinPwdNonAlphasEnforced);
		Boolean psa = getBooleanEntryAttributeValue(entry,
				POLICY_ATTR_PWD_SPACES_ALLOWED);
		if (psa != null)
			mPwdSpacesAllowed = psa;
		debug(POLICY_ATTR_PWD_SPACES_ALLOWED + " = " + mPwdSpacesAllowed);
		Boolean pase = getBooleanEntryAttributeValue(entry,
				POLICY_ATTR_PWD_SPACES_ALLOWED_ENFORCED);
		if (pase != null)
			mPwdSpacesAllowedEnforced = pase;
		debug(POLICY_ATTR_PWD_SPACES_ALLOWED_ENFORCED + " = "
				+ mPwdSpacesAllowedEnforced);
		Boolean tae = getBooleanEntryAttributeValue(entry,
				POLICY_ATTR_TOD_ACCESS_ENFORCED);
		if (tae != null)
			mTodAccessEnforced = tae;
		debug(POLICY_ATTR_TOD_ACCESS_ENFORCED + " = " + mTodAccessEnforced);
		Long adt = getLongEntryAttributeValue(entry,
				POLICY_ATTR_ACCT_DISABLE_TIME_INTERVAL);
		if (adt != null)
			mAcctDisableTimeInterval = adt;
		debug(POLICY_ATTR_ACCT_DISABLE_TIME_INTERVAL + " = "
				+ mAcctDisableTimeInterval);
		Date aed = getDateEntryAttributeValue(entry, POLICY_ATTR_ACCT_EXP_DATE);
		if (aed != null)
			mAcctExpDate = aed;
		debug(POLICY_ATTR_ACCT_EXP_DATE + " = " + mAcctExpDate);
		Integer maxFL = getIntegerEntryAttributeValue(entry,
				POLICY_ATTR_MAX_FAILED_LOGINS);
		if (maxFL != null)
			mMaxFailedLogins = maxFL;
		debug(POLICY_ATTR_MAX_FAILED_LOGINS + " = " + mMaxFailedLogins);
		Integer maxCWS = getIntegerEntryAttributeValue(entry,
				POLICY_ATTR_MAX_CONC_WEB_SESSIONS);
		if (maxCWS != null)
			mMaxConcWebSessions = maxCWS;
		debug(POLICY_ATTR_MAX_CONC_WEB_SESSIONS + " = " + mMaxConcWebSessions);
		Long maxPA = getLongEntryAttributeValue(entry, POLICY_ATTR_MAX_PWD_AGE);
		if (maxPA != null)
			mMaxPwdAge = maxPA;
		debug(POLICY_ATTR_MAX_PWD_AGE + " = " + mMaxPwdAge);
		Integer minPR = getIntegerEntryAttributeValue(entry,
				POLICY_ATTR_MAX_PWD_REP_CHARS);
		if (minPR != null)
			mMaxPwdRepChars = minPR;
		debug(POLICY_ATTR_MAX_PWD_REP_CHARS + " = " + mMaxPwdRepChars);
		Integer minPA = getIntegerEntryAttributeValue(entry,
				POLICY_ATTR_MIN_PWD_ALPHAS);
		if (minPA != null)
			mMinPwdAlphas = minPA;
		debug(POLICY_ATTR_MIN_PWD_ALPHAS + " = " + mMinPwdAlphas);
		Integer minPL = getIntegerEntryAttributeValue(entry,
				POLICY_ATTR_MIN_PWD_LEN);
		if (minPL != null)
			mMinPwdLen = minPL;
		debug(POLICY_ATTR_MIN_PWD_LEN + " = " + mMinPwdLen);
		Integer minP = getIntegerEntryAttributeValue(entry,
				POLICY_ATTR_MIN_PWD_NON_ALPHAS);
		if (minP != null)
			mMinPwdNonAlphas = minP;
		debug(POLICY_ATTR_MIN_PWD_NON_ALPHAS + " = " + mMinPwdNonAlphas);
		Long et = getLongEntryAttributeValue(entry, POLICY_ATTR_ACCESS_END_TIME);
		if (et != null)
			mAccessEndTime = et;
		debug(POLICY_ATTR_ACCESS_END_TIME + " = " + mAccessEndTime);
		Long ad = getLongEntryAttributeValue(entry, POLICY_ATTR_ACCESSIBLE_DAYS);
		if (ad != null)
			mAccessibleDays = ad;
		debug(POLICY_ATTR_ACCESSIBLE_DAYS + " = " + mAccessibleDays);
		Long st = getLongEntryAttributeValue(entry,
				POLICY_ATTR_ACCESS_START_TIME);
		if (st != null)
			mAccessStartTime = st;
		debug(POLICY_ATTR_ACCESS_START_TIME + " = " + mAccessStartTime);
		Long at = getLongEntryAttributeValue(entry, POLICY_ATTR_ACCESS_TIMEZONE);
		if (at != null)
			mAccessTimezone = at;
		debug(POLICY_ATTR_ACCESS_TIMEZONE + " = " + mAccessTimezone);
		String userId = getStringEntryAttributeValue(entry, POLICY_ATTR_USER_ID);
		if (userId != null && userId.length() > 0)
			mUserID = userId;
		debug(POLICY_ATTR_USER_ID + " = " + mUserID);
		Trace.exitmin(this, "Policy.set");
	}

	private void set(PDPolicy pdPolicy) throws PDException {
		Trace.entrymin(this, "Policy.set");
		mAcctDisableTimeEnforced = Boolean.valueOf(pdPolicy
				.acctDisableTimeEnforced());
		mAcctDisableTimeUnlimited = Boolean.valueOf(pdPolicy
				.acctDisableTimeUnlimited());
		mAcctExpDateEnforced = Boolean.valueOf(pdPolicy.acctExpDateEnforced());
		mAcctExpDateUnlimited = Boolean
				.valueOf(pdPolicy.acctExpDateUnlimited());
		mMaxFailedLoginsEnforced = Boolean.valueOf(pdPolicy
				.maxFailedLoginsEnforced());
		mMaxConcWebSessionsEnforced = Boolean.valueOf(pdPolicy
				.maxConcurrentWebSessionsEnforced());
		mMaxConcWebSessionsUnlimited = Boolean.valueOf(pdPolicy
				.maxConcurrentWebSessionsUnlimited());
		mMaxConcWebSessionsDisplaced = Boolean.valueOf(pdPolicy
				.maxConcurrentWebSessionsDisplaced());
		mMaxPwdAgeEnforced = Boolean.valueOf(pdPolicy.maxPwdAgeEnforced());
		mMaxPwdRepCharsEnforced = Boolean.valueOf(pdPolicy
				.maxPwdRepCharsEnforced());
		mMinPwdAlphasEnforced = Boolean
				.valueOf(pdPolicy.minPwdAlphasEnforced());
		mMinPwdLenEnforced = Boolean.valueOf(pdPolicy.minPwdLenEnforced());
		mMinPwdNonAlphasEnforced = Boolean.valueOf(pdPolicy
				.minPwdNonAlphasEnforced());
		mPwdSpacesAllowed = Boolean.valueOf(pdPolicy.pwdSpacesAllowed());
		mPwdSpacesAllowedEnforced = Boolean.valueOf(pdPolicy
				.pwdSpacesAllowedEnforced());
		mTodAccessEnforced = Boolean.valueOf(pdPolicy.todAccessEnforced());
		mAcctDisableTimeInterval = Long.valueOf(pdPolicy
				.getAcctDisableTimeInterval());
		mAcctExpDate = pdPolicy.getAcctExpDate();
		mMaxFailedLogins = Integer.valueOf(pdPolicy.getMaxFailedLogins());
		mMaxConcWebSessions = Integer.valueOf(pdPolicy
				.getMaxConcurrentWebSessions());
		mMaxPwdAge = Long.valueOf(pdPolicy.getMaxPwdAge());
		mMaxPwdRepChars = Integer.valueOf(pdPolicy.getMaxPwdRepChars());
		mMinPwdAlphas = Integer.valueOf(pdPolicy.getMinPwdAlphas());
		mMinPwdLen = Integer.valueOf(pdPolicy.getMinPwdLen());
		mMinPwdNonAlphas = Integer.valueOf(pdPolicy.getMinPwdNonAlphas());
		mAccessEndTime = Long.valueOf(pdPolicy.getAccessEndTime());
		mAccessibleDays = Long.valueOf(pdPolicy.getAccessibleDays());
		mAccessStartTime = Long.valueOf(pdPolicy.getAccessStartTime());
		mAccessTimezone = Long.valueOf(pdPolicy.getAccessTimezone());
		mUserID = pdPolicy.getId();
		Trace.exitmin(this, "Policy.set");
	}

	private void reset(PDPolicy pdPolicy) throws PDException {
		Trace.entrymin(this, "Policy.reset");
		if (mAcctDisableTimeEnforced == null) {
			mAcctDisableTimeEnforced = Boolean.valueOf(pdPolicy
					.acctDisableTimeEnforced());
		}
		if (mAcctDisableTimeUnlimited == null) {
			mAcctDisableTimeUnlimited = Boolean.valueOf(pdPolicy
					.acctDisableTimeUnlimited());
		}
		if (mAcctExpDateEnforced == null) {
			mAcctExpDateEnforced = Boolean.valueOf(pdPolicy
					.acctExpDateEnforced());
		}
		if (mAcctExpDateUnlimited == null) {
			mAcctExpDateUnlimited = Boolean.valueOf(pdPolicy
					.acctExpDateUnlimited());
		}
		if (mMaxFailedLoginsEnforced == null) {
			mMaxFailedLoginsEnforced = Boolean.valueOf(pdPolicy
					.maxFailedLoginsEnforced());
		}
		if (mMaxConcWebSessionsEnforced == null) {
			mMaxConcWebSessionsEnforced = Boolean.valueOf(pdPolicy
					.maxConcurrentWebSessionsEnforced());
		}
		if (mMaxConcWebSessionsUnlimited == null) {
			mMaxConcWebSessionsUnlimited = Boolean.valueOf(pdPolicy
					.maxConcurrentWebSessionsUnlimited());
		}
		if (mMaxConcWebSessionsDisplaced == null) {
			mMaxConcWebSessionsDisplaced = Boolean.valueOf(pdPolicy
					.maxConcurrentWebSessionsDisplaced());
		}
		if (mMaxPwdAgeEnforced == null) {
			mMaxPwdAgeEnforced = Boolean.valueOf(pdPolicy.maxPwdAgeEnforced());
		}
		if (mMaxPwdRepCharsEnforced == null) {
			mMaxPwdRepCharsEnforced = Boolean.valueOf(pdPolicy
					.maxPwdRepCharsEnforced());
		}
		if (mMinPwdAlphasEnforced == null) {
			mMinPwdAlphasEnforced = Boolean.valueOf(pdPolicy
					.minPwdAlphasEnforced());
		}
		if (mMinPwdLenEnforced == null) {
			mMinPwdLenEnforced = Boolean.valueOf(pdPolicy.minPwdLenEnforced());
		}
		if (mMinPwdNonAlphasEnforced == null) {
			mMinPwdNonAlphasEnforced = Boolean.valueOf(pdPolicy
					.minPwdNonAlphasEnforced());
		}
		if (mPwdSpacesAllowed == null) {
			mPwdSpacesAllowed = Boolean.valueOf(pdPolicy.pwdSpacesAllowed());
		}
		if (mPwdSpacesAllowedEnforced == null) {
			mPwdSpacesAllowedEnforced = Boolean.valueOf(pdPolicy
					.pwdSpacesAllowedEnforced());
		}
		if (mTodAccessEnforced == null) {
			mTodAccessEnforced = Boolean.valueOf(pdPolicy.todAccessEnforced());
		}
		if (mAcctDisableTimeInterval == null) {
			mAcctDisableTimeInterval = Long.valueOf(pdPolicy
					.getAcctDisableTimeInterval());
		}
		if (mAcctExpDate == null) {
			mAcctExpDate = pdPolicy.getAcctExpDate();
		}
		if (mMaxFailedLogins == null) {
			mMaxFailedLogins = Integer.valueOf(pdPolicy.getMaxFailedLogins());
		}
		if (mMaxConcWebSessions == null) {
			mMaxConcWebSessions = Integer.valueOf(pdPolicy
					.getMaxConcurrentWebSessions());
		}
		if (mMaxPwdAge == null) {
			mMaxPwdAge = Long.valueOf(pdPolicy.getMaxPwdAge());
		}
		if (mMaxPwdRepChars == null) {
			mMaxPwdRepChars = Integer.valueOf(pdPolicy.getMaxPwdRepChars());
		}
		if (mMinPwdAlphas == null) {
			mMinPwdAlphas = Integer.valueOf(pdPolicy.getMinPwdAlphas());
		}
		if (mMinPwdLen == null) {
			mMinPwdLen = Integer.valueOf(pdPolicy.getMinPwdLen());
		}
		if (mMinPwdNonAlphas == null) {
			mMinPwdNonAlphas = Integer.valueOf(pdPolicy.getMinPwdNonAlphas());
		}
		if (mAccessEndTime == null) {
			mAccessEndTime = Long.valueOf(pdPolicy.getAccessEndTime());
		}
		if (mAccessibleDays == null) {
			mAccessibleDays = Long.valueOf(pdPolicy.getAccessibleDays());
		}
		if (mAccessStartTime == null) {
			mAccessStartTime = Long.valueOf(pdPolicy.getAccessStartTime());
		}
		if (mAccessTimezone == null) {
			mAccessTimezone = Long.valueOf(pdPolicy.getAccessTimezone());
		}
		mUserID = pdPolicy.getId();
		Trace.exitmin(this, "Policy.set");
	}

	/**
	 * Creates a Policy in TAM
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public void put() throws TAMConnectorException, PDException {
		Trace.entrymin(this, "Policy.put");
		if (mUserID == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, POLICY_ATTR_USER_ID));
		}
		debug(POLICY_ATTR_USER_ID + " is: " + mUserID);
		logmsg(TMSMessageGetter.getMessage(TMSMsgId.POLICY_ADD, mUserID));
		// send through the error message incase of Exception
		update(TMSMessageGetter.getMessage(TMSMsgId.CREATE_ERROR, POLICY),
				false);
		Trace.exitmin(this, "Policy.put");
	}

	/**
	 * Modifies a Policy in TAM
	 * 
	 * @throws TAMConnectorException
	 */
	public void modify() throws TAMConnectorException {
		Trace.entrymin(this, "Policy.modify");
		// send through the error message incase of Exception
		update(TMSMessageGetter.getMessage(TMSMsgId.MODIFY_ERROR, POLICY),
				false);
		Trace.exitmin(this, "Policy.modify");
	}

	/**
	 * Modifies a Policy in TAM
	 * 
	 * @throws TAMConnectorException
	 */
	public void modify_postset() throws TAMConnectorException {
		Trace.entrymin(this, "Policy.modify_postset");
		// send through the error message incase of Exception
		update(TMSMessageGetter.getMessage(TMSMsgId.MODIFY_ERROR, POLICY), true);
		Trace.exitmin(this, "Policy.modify_postset");
	}

	private void update(String errMsg, boolean reset)
			throws TAMConnectorException {
		Trace.entrymin(this, "Policy.update");
		HashMap failed = new HashMap(10);
		PDPolicy pdPolicy = null;
		try {
			pdPolicy = new PDPolicy(mPDContext, mUserID, mPDMessages);
			if (reset) {
				reset(pdPolicy);
			}
		} catch (PDException pde) {
			throw new TAMConnectorException(getPDMessage(pde));
		}
		try {
			if (mMaxFailedLoginsEnforced != null && mMaxFailedLogins != null) {
				pdPolicy.setMaxFailedLogins(mPDContext, mMaxFailedLogins
						.intValue(), mMaxFailedLoginsEnforced.booleanValue(),
						mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(POLICY_ATTR_MAX_FAILED_LOGINS, getPDMessage(pde));
		}
		try {
			if (mMaxConcWebSessionsUnlimited != null
					&& mMaxConcWebSessionsDisplaced != null
					&& mMaxConcWebSessionsEnforced != null
					&& mMaxConcWebSessions != null) {
				pdPolicy.setMaxConcurrentWebSessions(mPDContext,
						mMaxConcWebSessions.intValue(),
						mMaxConcWebSessionsEnforced.booleanValue(),
						mMaxConcWebSessionsDisplaced.booleanValue(),
						mMaxConcWebSessionsUnlimited.booleanValue(),
						mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(POLICY_ATTR_MAX_CONC_WEB_SESSIONS, getPDMessage(pde));
		}
		try {
			if (mAcctExpDateEnforced != null || mAcctExpDate != null
					|| mAcctExpDateUnlimited != null) {

				if (mAcctExpDate != null) {
					if (mAcctExpDateEnforced == null) {
						debug("AcctExpDateEnforced has not been provided. Setting AcctExpDateEnforced to true");
						mAcctExpDateEnforced = Boolean.TRUE;
					}
					if (mAcctExpDateUnlimited == null) {
						debug("AcctExpDateUnlimited has not been provided. Setting AcctExpDateUnlimited to false");
						mAcctExpDateUnlimited = Boolean.FALSE;
					}
				} else if (mAcctExpDateEnforced != null) {
					if (mAcctExpDateUnlimited == null) {
						debug("AcctExpDateUnlimited has not been provided. Setting AcctExpDateUnlimited to false");
						mAcctExpDateUnlimited = Boolean.FALSE;
					}
				} else if (mAcctExpDateUnlimited != null) {
					if (mAcctExpDateUnlimited.booleanValue() == true) {
						if (mAcctExpDateEnforced == null) {
							debug("AcctExpDateEnforced has not been provided. Setting AcctExpDateEnforced to true");
							mAcctExpDateEnforced = Boolean.TRUE;
						}
					} else {
						if (mAcctExpDateEnforced == null) {
							debug("AcctExpDateEnforced has not been provided. Setting AcctExpDateEnforced to false");
							mAcctExpDateEnforced = Boolean.FALSE;
						}
					}
				}

				pdPolicy.setAcctExpDate(mPDContext, mAcctExpDate,
						mAcctExpDateUnlimited.booleanValue(),
						mAcctExpDateEnforced.booleanValue(), mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(POLICY_ATTR_ACCT_EXP_DATE, getPDMessage(pde));
		}
		try {
			if (mAcctDisableTimeEnforced != null
					&& mAcctDisableTimeInterval != null
					&& mAcctDisableTimeUnlimited != null) {
				pdPolicy.setAcctDisableTime(mPDContext,
						mAcctDisableTimeInterval.longValue(),
						mAcctDisableTimeUnlimited.booleanValue(),
						mAcctDisableTimeEnforced.booleanValue(), mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(POLICY_ATTR_ACCT_DISABLE_TIME_INTERVAL,
					getPDMessage(pde));
		}
		try {
			if (mPwdSpacesAllowedEnforced != null && mPwdSpacesAllowed != null) {
				pdPolicy.setPwdSpacesAllowed(mPDContext, mPwdSpacesAllowed
						.booleanValue(), mPwdSpacesAllowedEnforced
						.booleanValue(), mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(POLICY_ATTR_PWD_SPACES_ALLOWED, getPDMessage(pde));
		}
		try {
			if (mMaxPwdAgeEnforced != null && mMaxPwdAge != null) {
				pdPolicy.setMaxPwdAge(mPDContext, mMaxPwdAge.longValue(),
						mMaxPwdAgeEnforced.booleanValue(), mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(POLICY_ATTR_MAX_PWD_AGE, getPDMessage(pde));
		}
		try {
			if (mMaxPwdRepCharsEnforced != null && mMaxPwdRepChars != null) {
				pdPolicy.setMaxPwdRepChars(mPDContext, mMaxPwdRepChars
						.intValue(), mMaxPwdRepCharsEnforced.booleanValue(),
						mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(POLICY_ATTR_MAX_PWD_REP_CHARS, getPDMessage(pde));
		}
		try {
			if (mMinPwdAlphasEnforced != null && mMinPwdAlphas != null) {
				pdPolicy.setMinPwdAlphas(mPDContext, mMinPwdAlphas.intValue(),
						mMinPwdAlphasEnforced.booleanValue(), mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(POLICY_ATTR_MIN_PWD_ALPHAS, getPDMessage(pde));
		}
		try {
			if (mMinPwdNonAlphasEnforced != null && mMinPwdNonAlphas != null) {
				pdPolicy.setMinPwdNonAlphas(mPDContext, mMinPwdNonAlphas
						.intValue(), mMinPwdNonAlphasEnforced.booleanValue(),
						mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(POLICY_ATTR_MIN_PWD_NON_ALPHAS, getPDMessage(pde));
		}
		try {
			if (mTodAccessEnforced != null && mAccessibleDays != null
					&& mAccessStartTime != null && mAccessEndTime != null
					&& mAccessTimezone != null) {
				pdPolicy.setTodAccess(mPDContext, mAccessibleDays.longValue(),
						mAccessStartTime.longValue(), mAccessEndTime
								.longValue(), mAccessTimezone.intValue(),
						mTodAccessEnforced.booleanValue(), mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(POLICY_ATTR_TOD_ACCESS_ENFORCED, getPDMessage(pde));
		}
		try {
			if (mMinPwdLenEnforced != null && mMinPwdLen != null) {
				pdPolicy.setMinPwdLen(mPDContext, mMinPwdLen.intValue(),
						mMinPwdLenEnforced.booleanValue(), mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(POLICY_ATTR_MIN_PWD_LEN, getPDMessage(pde));
		}
		if (failed.size() > 0) {
			logmsg(errMsg);
			throw new TAMConnectorException(failed, errMsg);
		}
		Trace.exitmin(this, "Policy.update");
	}

	/**
	 * Deletes a Policy from TAM
	 * <p>
	 * Unsets each value for the user
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public void delete() throws TAMConnectorException, PDException {
		Trace.entrymin(this, "Policy.delete");
		if (mUserID == null)
			throw new TAMConnectorException(TMSMessageGetter
					.getMessage(TMSMsgId.POLICY_DELETE_FAIL));
		debug(POLICY_ATTR_USER_ID + " is: " + mUserID);
		logmsg(TMSMessageGetter.getMessage(TMSMsgId.POLICY_DELETE, mUserID));
		PDPolicy pdPolicy = new PDPolicy(mPDContext, mUserID, mPDMessages);
		pdPolicy.setMaxFailedLogins(mPDContext, 0, false, mPDMessages);
		// this could be a problem cos TAM5.1 does not support
		pdPolicy.setMaxConcurrentWebSessions(mPDContext, 0, false, false,
				false, mPDMessages);
		processMsgs(mPDMessages);
		pdPolicy.setAcctExpDate(mPDContext, new Date(0), false, false,
				mPDMessages);
		processMsgs(mPDMessages);
		pdPolicy.setAcctDisableTime(mPDContext, 0, false, false, mPDMessages);
		processMsgs(mPDMessages);
		pdPolicy.setPwdSpacesAllowed(mPDContext, false, false, mPDMessages);
		processMsgs(mPDMessages);
		pdPolicy.setMaxPwdAge(mPDContext, 0, false, mPDMessages);
		processMsgs(mPDMessages);
		pdPolicy.setMaxPwdRepChars(mPDContext, 0, false, mPDMessages);
		processMsgs(mPDMessages);
		pdPolicy.setMinPwdAlphas(mPDContext, 0, false, mPDMessages);
		processMsgs(mPDMessages);
		pdPolicy.setMinPwdNonAlphas(mPDContext, 0, false, mPDMessages);
		processMsgs(mPDMessages);
		pdPolicy.setTodAccess(mPDContext, 0, 0, 0, 0, false, mPDMessages);
		processMsgs(mPDMessages);
		pdPolicy.setMinPwdLen(mPDContext, 0, false, mPDMessages);
		Trace.exitmin(this, "Policy.delete");
	}

	/**
	 * Returns the Policy details in the form of an Entry object
	 * 
	 * @return Entry
	 */
	public Entry getAttributes() {
		Entry entry = new Entry();
		createAndAddEntryAttribute(entry,
				POLICY_ATTR_ACCT_DISABLE_TIME_ENFORCED,
				mAcctDisableTimeEnforced);
		createAndAddEntryAttribute(entry,
				POLICY_ATTR_ACCT_DISABLE_TIME_UNLIMITED,
				mAcctDisableTimeUnlimited);
		createAndAddEntryAttribute(entry, POLICY_ATTR_ACCT_EXP_DATE_ENFORCED,
				mAcctExpDateEnforced);
		createAndAddEntryAttribute(entry, POLICY_ATTR_ACCT_EXP_DATE_UNLIMITED,
				mAcctExpDateUnlimited);
		createAndAddEntryAttribute(entry,
				POLICY_ATTR_MAX_CONC_WEB_SESSIONS_UNLIMITED,
				mMaxConcWebSessionsUnlimited);
		createAndAddEntryAttribute(entry,
				POLICY_ATTR_MAX_CONC_WEB_SESSIONS_ENFORCED,
				mMaxConcWebSessionsEnforced);
		createAndAddEntryAttribute(entry,
				POLICY_ATTR_MAX_CONC_WEB_SESSIONS_DISPLACED,
				mMaxConcWebSessionsDisplaced);
		createAndAddEntryAttribute(entry,
				POLICY_ATTR_MAX_FAILED_LOGINS_ENFORCED,
				mMaxFailedLoginsEnforced);
		createAndAddEntryAttribute(entry, POLICY_ATTR_MAX_PWD_AGE_ENFORCED,
				mMaxPwdAgeEnforced);
		createAndAddEntryAttribute(entry,
				POLICY_ATTR_MAX_PWD_REP_CHARS_ENFORCED, mMaxPwdRepCharsEnforced);
		createAndAddEntryAttribute(entry, POLICY_ATTR_MIN_PWD_ALPHAS_ENFORCED,
				mMinPwdAlphasEnforced);
		createAndAddEntryAttribute(entry, POLICY_ATTR_MIN_PWD_LEN_ENFORCED,
				mMinPwdLenEnforced);
		createAndAddEntryAttribute(entry,
				POLICY_ATTR_MIN_PWD_NON_ALPHAS_ENFORCED,
				mMinPwdNonAlphasEnforced);
		createAndAddEntryAttribute(entry, POLICY_ATTR_PWD_SPACES_ALLOWED,
				mPwdSpacesAllowed);
		createAndAddEntryAttribute(entry,
				POLICY_ATTR_PWD_SPACES_ALLOWED_ENFORCED,
				mPwdSpacesAllowedEnforced);
		createAndAddEntryAttribute(entry, POLICY_ATTR_TOD_ACCESS_ENFORCED,
				mTodAccessEnforced);
		createAndAddEntryAttribute(entry,
				POLICY_ATTR_ACCT_DISABLE_TIME_INTERVAL,
				mAcctDisableTimeInterval);
		createAndAddEntryAttribute(entry, POLICY_ATTR_ACCT_EXP_DATE,
				mAcctExpDate);
		createAndAddEntryAttribute(entry, POLICY_ATTR_MAX_FAILED_LOGINS,
				mMaxFailedLogins);
		createAndAddEntryAttribute(entry, POLICY_ATTR_MAX_CONC_WEB_SESSIONS,
				mMaxConcWebSessions);
		createAndAddEntryAttribute(entry, POLICY_ATTR_MAX_PWD_AGE, mMaxPwdAge);
		createAndAddEntryAttribute(entry, POLICY_ATTR_MAX_PWD_REP_CHARS,
				mMaxPwdRepChars);
		createAndAddEntryAttribute(entry, POLICY_ATTR_MIN_PWD_ALPHAS,
				mMinPwdAlphas);
		createAndAddEntryAttribute(entry, POLICY_ATTR_MIN_PWD_LEN, mMinPwdLen);
		createAndAddEntryAttribute(entry, POLICY_ATTR_MIN_PWD_NON_ALPHAS,
				mMinPwdNonAlphas);
		createAndAddEntryAttribute(entry, POLICY_ATTR_ACCESS_END_TIME,
				mAccessEndTime);
		createAndAddEntryAttribute(entry, POLICY_ATTR_ACCESSIBLE_DAYS,
				mAccessibleDays);
		createAndAddEntryAttribute(entry, POLICY_ATTR_ACCESS_START_TIME,
				mAccessStartTime);
		createAndAddEntryAttribute(entry, POLICY_ATTR_ACCESS_TIMEZONE,
				mAccessTimezone);
		createAndAddEntryAttribute(entry, POLICY_ATTR_USER_ID, mUserID);
		return entry;
	}

	/**
	 * Get the schema for TAM policy.
	 * 
	 * @return vector with the schema description
	 */
	public static Vector schema() {
		Vector vector = new Vector();
		addSchemaEntry(vector, POLICY_ATTR_ACCT_DISABLE_TIME_ENFORCED,
				QSS_BOOLEAN, null);
		addSchemaEntry(vector, POLICY_ATTR_ACCT_DISABLE_TIME_UNLIMITED,
				QSS_BOOLEAN, null);
		addSchemaEntry(vector, POLICY_ATTR_ACCT_EXP_DATE_ENFORCED, QSS_BOOLEAN,
				null);
		addSchemaEntry(vector, POLICY_ATTR_ACCT_EXP_DATE_UNLIMITED,
				QSS_BOOLEAN, null);
		addSchemaEntry(vector, POLICY_ATTR_MAX_FAILED_LOGINS_ENFORCED,
				QSS_BOOLEAN, null);
		addSchemaEntry(vector, POLICY_ATTR_MAX_CONC_WEB_SESSIONS_ENFORCED,
				QSS_BOOLEAN, null);
		addSchemaEntry(vector, POLICY_ATTR_MAX_CONC_WEB_SESSIONS_UNLIMITED,
				QSS_BOOLEAN, null);
		addSchemaEntry(vector, POLICY_ATTR_MAX_CONC_WEB_SESSIONS_DISPLACED,
				QSS_BOOLEAN, null);
		addSchemaEntry(vector, POLICY_ATTR_MAX_PWD_AGE_ENFORCED, QSS_BOOLEAN,
				null);
		addSchemaEntry(vector, POLICY_ATTR_MAX_PWD_REP_CHARS_ENFORCED,
				QSS_BOOLEAN, null);
		addSchemaEntry(vector, POLICY_ATTR_MIN_PWD_ALPHAS_ENFORCED,
				QSS_BOOLEAN, null);
		addSchemaEntry(vector, POLICY_ATTR_MIN_PWD_LEN_ENFORCED, QSS_BOOLEAN,
				null);
		addSchemaEntry(vector, POLICY_ATTR_MIN_PWD_NON_ALPHAS_ENFORCED,
				QSS_BOOLEAN, null);
		addSchemaEntry(vector, POLICY_ATTR_PWD_SPACES_ALLOWED, QSS_BOOLEAN,
				null);
		addSchemaEntry(vector, POLICY_ATTR_PWD_SPACES_ALLOWED_ENFORCED,
				QSS_BOOLEAN, null);
		addSchemaEntry(vector, POLICY_ATTR_TOD_ACCESS_ENFORCED, QSS_BOOLEAN,
				null);
		addSchemaEntry(vector, POLICY_ATTR_ACCT_DISABLE_TIME_INTERVAL,
				QSS_LONG, null);
		addSchemaEntry(vector, POLICY_ATTR_ACCT_EXP_DATE, QSS_DATE, null);
		addSchemaEntry(vector, POLICY_ATTR_MAX_FAILED_LOGINS, QSS_INTEGER, null);
		addSchemaEntry(vector, POLICY_ATTR_MAX_CONC_WEB_SESSIONS, QSS_INTEGER,
				null);
		addSchemaEntry(vector, POLICY_ATTR_MAX_PWD_AGE, QSS_LONG, null);
		addSchemaEntry(vector, POLICY_ATTR_MAX_PWD_REP_CHARS, QSS_INTEGER, null);
		addSchemaEntry(vector, POLICY_ATTR_MIN_PWD_ALPHAS, QSS_INTEGER, null);
		addSchemaEntry(vector, POLICY_ATTR_MIN_PWD_LEN, QSS_INTEGER, null);
		addSchemaEntry(vector, POLICY_ATTR_MIN_PWD_NON_ALPHAS, QSS_INTEGER,
				null);
		addSchemaEntry(vector, POLICY_ATTR_ACCESS_END_TIME, QSS_LONG, null);
		addSchemaEntry(vector, POLICY_ATTR_ACCESSIBLE_DAYS, QSS_LONG, null);
		addSchemaEntry(vector, POLICY_ATTR_ACCESS_START_TIME, QSS_LONG, null);
		addSchemaEntry(vector, POLICY_ATTR_ACCESS_TIMEZONE, QSS_LONG, null);
		addSchemaEntry(vector, POLICY_ATTR_USER_ID, QSS_STRING, Integer
				.valueOf(256));
		return vector;
	}

	/**
	 * Returns a list (ArrayList) of all the Users for the TAM Context matching
	 * search criteria value.
	 * 
	 * @param mPDContext
	 *            The TAM Context
	 * 
	 * @param searchCriteria
	 *            The IDI search criteria
	 * 
	 * @return List (ArrayList)
	 * 
	 * @throws PDException
	 */
	public static List list(SearchCriteria searchCriteria, PDContext mPDContext)
			throws PDException {
		return User.list(searchCriteria.getFirstCriteriaValue(), mPDContext);
	}

	/**
	 * Return a List (ArrayList) of TAM Policies
	 * 
	 * @param mPDContext
	 *            The TAM Context
	 * 
	 * @return List (ArrayList)
	 * 
	 * @throws PDException
	 */
	public static List list(PDContext mPDContext) throws PDException {
		PDMessages msgs = new PDMessages();
		List ret;
		ret = PDUser.listUsers(mPDContext, PDUser.PDUSER_ALLPATTERN,
				PDUser.PDUSER_MAXRETURN, false, msgs);
		ret.add(PDPolicy.PDPOLICY_GLOBAL_POLICY);
		return ret;
	}
}
