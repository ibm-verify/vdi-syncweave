/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.security;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.RS;
import com.ibm.icu.util.StringTokenizer;

/**
 * The Registry class is used by the Server API to parse the User Registry file
 * and hold all the user identities with their corresponding permissions. After
 * initialization of the Registry the Identites are accessed through the
 * getIdentity(String aUserID) method. From 7.0 this method also returns
 * Identities for userIDs which are members of groups specified in the registry.
 */
public class Registry {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * This tag takes no arguments, and serves as an opening bracket for the
	 * tags below; a [USER] and [ENDUSER] pair of tags, each placed on a single
	 * line, provide the definition of a single user in the registry file. There
	 * can be multiple pairs of this type, each of which specify a user of the
	 * Server API.
	 */
	private static final String TAG_USER = "[USER]";

	/**
	 * a [USER] and [ENDUSER] pair of tags, each placed on a single line,
	 * provide the definition of a single user in the registry file. There can
	 * be multiple pairs of this type, each of which specify a user of the
	 * Server API.
	 */
	private static final String TAG_ENDUSER = "[ENDUSER]";

	/**
	 * This tag is the first tag after the [USER] tag and its argument
	 * <user_identifier> is the unique identifier of the user of the Server API.
	 * This ID value is the from the trust store file. The tag and the argument
	 * of the tag are placed on a single line, and there can be only one [ID]:
	 * tag included in a [USER] and [ENDUSER] pair.
	 */
	private static final String TAG_ID = "[ID]:";

	/**
	 * This tag specifies a role for the user. Possible roles are: read, execute
	 * or admin. Everything after the [ROLE]: tag and its argument and before
	 * another [ROLE]: tag or an [ENDUSER] tag (whichever comes first) specifies
	 * details of this user role. The tag and the argument of the tag are placed
	 * on a single line, and there can be multiple [ROLE]: tags included in a
	 * [USER] and [ENDUSER] pair, specifying multiple roles for that user.
	 */
	private static final String TAG_ROLE = "[ROLE]:";

	/**
	 * This tag specifies the identifier of a TDI configuration, the absolute
	 * file path of the configuration. Relative file paths are not recognized.
	 * This tag is subordinate to a [ROLE]: tag, and the tag specifies a
	 * configuration for the role given by the [ROLE]: tag. This tag and its
	 * argument are placed on a single line, and there can be multiple [CONFIG]:
	 * tags, all belonging to the superior [ROLE]: tag. If no [CONFIG]: tag is
	 * associated with a [ROLE]: tag, the list of configurations for the
	 * corresponding role definition is empty.
	 */
	private static final String TAG_CONFIG = "[CONFIG]:";

	/**
	 * This tag specifies an AssemblyLine name. This tag is subordinate to a
	 * [CONFIG]: tag. The tag and its argument are placed on a single line, and
	 * there can be multiple [AL]: tags, all belonging to the superior [CONFIG]:
	 * tag. If no [AL]: tag is associated with a [CONFIG]: tag, the list of
	 * AssemblyLines for the corresponding configuration ID is empty.
	 */
	private static final String TAG_AL = "[AL]:";

	/**
	 * tag comment
	 */
	private static final String TAG_COMMENT = "#";

	/**
	 * wild character
	 */
	private static final String WILD_CHAR = "*";

	/**
	 * delimiter
	 */
	private static final String DELIMITER = ";";

	/**
	 * A vector for all the identities loaded from the User Registry
	 */
	private Vector<Identity> mIdentities = null;

	/**
	 * Initially the file is split to lines and each line is loaded in this
	 * vector
	 */
	private Vector<String> mSpecs = null;

	/**
	 * Keeps the line number of each loaded line from the file
	 */
	private Vector<Integer> mNumbers = null;

	/**
	 * A cursor to the current line read
	 */
	private int mPos = 0;

	/**
	 * Keeps the current line number of each loaded line from the file
	 */
	private int mLineNumber = 0;

	/**
	 * this policy is used when parsing the tags that define which
	 * configurations the user is authorized access to
	 */
	private RS.ConfigInstanceNamingPolicy configInstanceNamingPolicy = null;

	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Parses the User Registry file and creates a vector with all Identities
	 * found in it with their corresponding permissions.
	 * 
	 * If the server API user registry encryption is turned on the file is
	 * decrypted using the CryptoUtils class.
	 * 
	 * All the tags are read line by line. This parsing traverses the Registry
	 * tags tree in depth and all authorization elements found are added as
	 * corresponding vectors to their parent object.
	 * 
	 * @param aRegistryFileName
	 *            the full path to the user registry file name
	 * 
	 * @param configInstanceNamingPolicy
	 *            this policy is used when parsing the tags that define which
	 *            configurations the user is authorized access to
	 * 
	 * @throws DIException
	 *             if an Exception occurs during the reading, decrypting or
	 *             parsing the file
	 */
	public synchronized void initialize(String aRegistryFileName,
			RS.ConfigInstanceNamingPolicy configInstanceNamingPolicy)
			throws DIException {
		if (mIdentities != null) {
			throw new DIException(sResHash
					.getString("SEVER.API.REGISTRY.ALREADY.INITIALIZED"));
		}

		if (aRegistryFileName == null || aRegistryFileName.trim().length() == 0) {
			throw new DIException(sResHash
					.getString("SEVER.API.REGISTRY.FILE.NAME.IS.NULL"));
		}

		File regFile = new File(aRegistryFileName);
		if (!regFile.exists()) {
			throw new DIException(sResHash
					.getString("SEVER.API.REGISTRY.FILE.DOES.NOT.EXIST",
							aRegistryFileName));
		}

		// read the file
		if (APIEngine.isDebugEnabled()) {
			APIEngine.logDebug(sResHash
					.getString("SEVER.API.READING.SECURITY.REGISTRY"));
		}

		this.configInstanceNamingPolicy = configInstanceNamingPolicy;

		InputStream inputStream = null;
		// decrypt the file
		if (Boolean.getBoolean(APIEngine.PROP_API_USER_REGISTRY_ENCRYPTION_ON)) {
			if (APIEngine.isDebugEnabled()) {
				APIEngine.logDebug(sResHash
						.getString("SEVER.API.DECRYPTING.SECURITY.REGISTRY"));
			}
			try {
				byte[] encrypted = CryptoUtils.readFile(aRegistryFileName);
				byte[] decrypted = CryptoUtils
						.decryptSecurityRegistry(encrypted);
				inputStream = new ByteArrayInputStream(decrypted);
			} catch (Exception e) {
				APIEngine
						.logErrorAndThrowException(
								sResHash
										.getString("SEVER.API.ERROR.WHILE.DECRYPTING.REGISTRY.FILE"),
								e);
			}
		} else {
			try {
				inputStream = new FileInputStream(aRegistryFileName);
			} catch (FileNotFoundException e) {
				APIEngine.logErrorAndThrowException(sResHash
						.getString("SEVER.API.REGISTRY.FILE.NOT.FOUND"), e);
			}
		}

		String inputLine;
		mSpecs = new Vector<String>();
		mNumbers = new Vector<Integer>();

		int lineNumber = 0;
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream));
			for (inputLine = bufferedReader.readLine(); inputLine != null; inputLine = bufferedReader
					.readLine()) {
				lineNumber++;
				inputLine = inputLine.trim();
				if (inputLine.length() > 0
						&& !inputLine.startsWith(TAG_COMMENT)) {
					mSpecs.add(inputLine);
					mNumbers.add(Integer.valueOf(lineNumber));
					if (APIEngine.isDebugEnabled()) {
						APIEngine
								.logDebug(sResHash.getString(
										"SEVER.API.COLON.1", new Object[] {
												String.valueOf(lineNumber),
												inputLine }));
					}
				}
			}

			bufferedReader.close();
		} catch (IOException e) {
			APIEngine.logErrorAndThrowException(sResHash
					.getString("SEVER.API.ERROR.WHILE.READING.REGISTRY.FILE"),
					e);
		}

		parseIdentities();

		if (APIEngine.isDebugEnabled()) {
			APIEngine.logDebug(sResHash
					.getString("SEVER.API.SECURITY.REGISTRY.SUCCESSFULLY"));
		}
	}

	/**
	 * Parses all the {@value #TAG_USER} regions and puts the received Identity
	 * objects in a vector
	 * 
	 * @throws DIException
	 */
	private void parseIdentities() throws DIException {
		mIdentities = new Vector<Identity>();
		Identity identity = null;
		while ((identity = parseUser()) != null) {
			mIdentities.add(identity);
		}
	}

	/**
	 * Parses a {@value #TAG_USER} region
	 * 
	 * @return an Identity object with the parsed information
	 * @throws DIException
	 */
	private Identity parseUser() throws DIException {
		String line = readLine();
		if (line == null) {
			return null;
		}

		if (!line.equals(TAG_USER)) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.INVALID.TAG.AT.LINE.1", new Object[] {
							String.valueOf(mLineNumber), line }));
		}

		String userId = readUserId();
		Vector<Role> userRoles = new Vector<Role>();
		Role role = null;
		while ((role = parseRole()) != null) {
			userRoles.add(role);
		}

		return new Identity(userId, userRoles);
	}

	/**
	 * Reads a {@value #TAG_ID} region and returns its value
	 * 
	 * @return the value of the region
	 * @throws DIException
	 */
	private String readUserId() throws DIException {
		String line = readLine();
		if (line == null) {
			APIEngine.logErrorAndThrowException(sResHash
					.getString("SEVER.API.NO.USER.ID.SPECIFIED"));
		}

		if (!line.startsWith(TAG_ID)) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.INVALID.TAG.AT.LINE.2", new Object[] {
							String.valueOf(mLineNumber), line }));
		}

		String userId = line.substring(TAG_ID.length());
		if (userId != null) {
			userId = userId.trim();
		}
		if (userId == null || (userId.length() == 0)) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.EMPTY.USER.ID.SPECIFIED.AT.LINE", String
							.valueOf(mLineNumber)));
		}

		if (getIdentity(userId) != null) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.INVALID.USER.DEFINITION.USER.ID", new Object[] {
							userId, String.valueOf(mLineNumber) }));
		}

		return userId;
	}

	/**
	 * Parses a role region
	 * 
	 * @return the parsed information into a Role object
	 * @throws DIException
	 */
	private Role parseRole() throws DIException {
		String line = readLine();
		if (line == null) {
			APIEngine
					.logErrorAndThrowException(sResHash
							.getString("SEVER.API.USER.DEFINITION.NOT.CLOSED.AT.END.OF.FILE"));
		}

		if (line.equals(TAG_ENDUSER)) {
			return null;
		}

		if (!line.startsWith(TAG_ROLE)) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.INVALID.TAG.AT.LINE.3", new Object[] {
							String.valueOf(mLineNumber), line }));
		}

		String roleName = line.substring(TAG_ROLE.length());
		if (roleName != null) {
			roleName = roleName.trim();
		}
		if (roleName == null || (roleName.length() == 0)) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.EMPTY.ROLE.SPECIFIED.AT.LINE", String
							.valueOf(mLineNumber)));
		}

		if (!(roleName.equals(Role.ROLE_NAMES[Role.ROLE_ADMIN])
				|| roleName.equals(Role.ROLE_NAMES[Role.ROLE_READ]) || roleName
				.equals(Role.ROLE_NAMES[Role.ROLE_EXECUTE]))) {

			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.INVALID.ROLE.SPECIFIED.AT.LINE", new Object[] {
							String.valueOf(mLineNumber), roleName }));
		}

		TreeMap<String, TreeMap<String, Vector<String>>> roleSpecs = null;
		if (!roleName.equals(Role.ROLE_NAMES[Role.ROLE_ADMIN])) {
			roleSpecs = new TreeMap<String, TreeMap<String, Vector<String>>>();
			boolean configParsed = false;
			do {
				configParsed = parseConfig(roleSpecs, roleName);
			} while (configParsed == true);
		}

		return new Role(roleName, roleSpecs);
	}

	/**
	 * Parses a {@value #TAG_CONFIG} region
	 * 
	 * @param aRoleSpecs
	 *            a map which is filled or updated with the parsed information
	 * @param aRoleName
	 *            if read, no config specs are put into the role specs map
	 * @return true if successful
	 * @throws DIException
	 */
	private boolean parseConfig(
			TreeMap<String, TreeMap<String, Vector<String>>> aRoleSpecs,
			String aRoleName) throws DIException {
		String line = readLine();
		if (line == null) {
			APIEngine
					.logErrorAndThrowException(sResHash
							.getString("SEVER.API.USER.DEFINITION.NOT.CLOSED.AT.END.OF.FILE.1"));
		}
		if (line.equals(TAG_ENDUSER) || line.startsWith(TAG_ROLE)) {
			mPos--;
			return false;
		}

		if (!line.startsWith(TAG_CONFIG)) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.INVALID.TAG.AT.LINE.4", new Object[] {
							String.valueOf(mLineNumber), line }));
		}

		String configId = line.substring(TAG_CONFIG.length());
		if (configId != null) {
			configId = configId.trim();
		}
		if (configId == null || (configId.length() == 0)) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.EMPTY.CONFIG.SPECIFIED.AT.LINE", String
							.valueOf(mLineNumber)));
		}

		if (!configId.equals(WILD_CHAR)) {

			// analyze the field to obtain its corresponding config instance id

			// configuration instance startup params
			Map<String, Object> params = new TreeMap<String, Object>();

			if (new File(configId).isAbsolute()) {
				// a configuration file name
				params.put(RS.CL_CONFIG, configId);
			} else {
				// a solution name or a run name
				params.put(RS.CL_INTERNAL_CONFIG_NSTANCE_NAME, configId);
			}

			try {
				configId = configInstanceNamingPolicy
						.getConfigInstanceName(params);
			} catch (Exception ex) {
				throw new DIException(
						sResHash
								.getString(
										"SERVER.API.REGISTRY.CANNOT.UNDERSTAND.CONFIG.IDENTIFIER",
										new Object[] { configId, ex }));
			}
		}

		if (configId.equals(WILD_CHAR)) {
			aRoleSpecs.clear();
			aRoleSpecs.put(configId, null);
		} else if (aRoleName.equals(Role.ROLE_NAMES[Role.ROLE_READ])) {
			aRoleSpecs.put(configId, null);
		} else {
			TreeMap<String, Vector<String>> configSpecs = new TreeMap<String, Vector<String>>();

			boolean componentParsed = false;
			do {
				componentParsed = parseComponent(configSpecs);
			} while (componentParsed == true);

			if (!aRoleSpecs.containsKey(WILD_CHAR)) {
				aRoleSpecs.put(configId, configSpecs);
			}
		}

		return true;
	}

	/**
	 * Parses an assembly line or event handler component description
	 * 
	 * @param aConfigSpecs
	 *            a map which is filled or updated with the parsed information
	 * @return true if successful
	 * @throws DIException
	 */
	private boolean parseComponent(TreeMap<String, Vector<String>> aConfigSpecs)
			throws DIException {
		String line = readLine();
		if (line == null) {
			APIEngine
					.logErrorAndThrowException(sResHash
							.getString("SEVER.API.USER.DEFINITION.NOT.CLOSED.AT.END.OF.FILE.2"));
		}
		if (line.equals(TAG_ENDUSER) || line.startsWith(TAG_ROLE)
				|| line.startsWith(TAG_CONFIG)) {

			mPos--;
			return false;
		}

		if (line.startsWith(TAG_AL)) {
			String alName = line.substring(TAG_AL.length());
			if (alName != null) {
				alName = alName.trim();
			}
			if (alName == null || (alName.length() == 0)) {
				APIEngine.logErrorAndThrowException(sResHash.getString(
						"SEVER.API.EMPTY.AL.SPECIFIED.AT.LINE", String
								.valueOf(mLineNumber)));
			}

			Vector<String> alList = aConfigSpecs.get(Role.ALLIST);
			if (alList == null) {
				alList = new Vector<String>();
				aConfigSpecs.put(Role.ALLIST, alList);
			}

			if (!alList.contains(WILD_CHAR)) {
				if (alName.equals(WILD_CHAR)) {
					alList.clear();
				}
				alList.add(alName);
			}
		} else {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.INVALID.TAG.AT.LINE.5", new Object[] {
							String.valueOf(mLineNumber), line }));
		}

		return true;
	}

	/**
	 * Reads next line.
	 * @return The next line read from the file
	 */
	private String readLine() {
		if (mPos >= mSpecs.size()) {
			return null;
		}
		mLineNumber = mNumbers.get(mPos).intValue();
		String line = mSpecs.get(mPos);
		mPos++;
		return line;
	}

	/**
	 * The method searches for Identities with userId corresponding to the
	 * supplied parameter. It distinguishes two cases: simple - when only user
	 * name is supplied. In this case the Identity with matching userId is
	 * returned. In the other case, when the LDAP authentication with group
	 * support is performed, as parameter is supplied not only the user, but
	 * also the users' groups separated by ";".The first in this sequience must
	 * be the authenticating user. In this case new Identity is created, which
	 * posseses all the roles assigned to the user as well to the groups in the
	 * user registry.
	 * 
	 * @param aUserID
	 *            String object containing the authenticating user or the user
	 *            and the users' groups each one separated by ";" by the others.
	 * @return Identity object
	 * @since 7.0
	 */
	public Identity getIdentity(String aUserID) {
		Identity identity = null;
		Vector<String> userAndGroups = new Vector<String>();
		Vector<Role> roles = new Vector<Role>();
		boolean groupSupport = false;
		StringTokenizer st = new StringTokenizer(aUserID, DELIMITER);
		if (st.countTokens() > 1) {
			while (st.hasMoreTokens()) {
				userAndGroups.add(st.nextToken());
			}
			groupSupport = true;
		}
		if (!groupSupport) {
			for (int i = 0; i < mIdentities.size(); i++) {
				if (aUserID.equalsIgnoreCase(mIdentities.get(i)
						.getUserId())) {
					identity = mIdentities.get(i);
					break;
				}
			}
		} else {
			for (int i = 0; i < userAndGroups.size(); i++)
				for (int j = 0; j < mIdentities.size(); j++) {
					if (userAndGroups.get(i).equalsIgnoreCase(
							mIdentities.get(j).getUserId())) {
						identity = mIdentities.get(j);
						roles.addAll(identity.getRoles());
					}
				}
			if (roles.size() == 0)
				return null;
			identity = new Identity(userAndGroups, roles);
		}
		return identity;
	}

}
