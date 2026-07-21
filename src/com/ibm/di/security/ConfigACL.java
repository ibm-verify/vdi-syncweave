/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.security;

import java.util.*;

public class ConfigACL {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final int DONT_CARE = 0;

	private static final int DENY = 1;

	private static final int GRANT = 2;

	private static final String ACL_USER = "user";

	private static final String ACL_PATH = "path";

	private static final String ACL_VERB = "verb";

	private static final String ACL_VERB_DENY = "deny";

	private static final String ACL_VERB_GRANT = "grant";

	private static final String ANONYMOUS = "anonymous";

	private String user;

	private Vector acl;

	public ConfigACL(String user) {
		if (user == null)
			this.user = ANONYMOUS;
		else
			this.user = user;

		acl = new Vector();
	}

	public boolean checkAccess(String path) {

		boolean deny = true;

		for (int i = 0; i < acl.size(); i++) {
			switch (checkACL(i, path)) {
			case DONT_CARE:
				break;
			case DENY:
				deny = true;
				break;
			case GRANT:
				deny = false;
			}
		}

		return deny;

	}

	public int checkACL(int i, String cfg) {

		TreeMap tm = (TreeMap) acl.elementAt(i);
		String user = (String) tm.get(ACL_USER);
		String verb = (String) tm.get(ACL_VERB);
		String path = (String) tm.get(ACL_PATH);

		// Check if path applies to this ACL
		if (cfg != null && !cfg.startsWith(path)) {
			return DONT_CARE;
		}

		boolean verbMatch = verb.equals(ACL_VERB_GRANT);
		boolean userMatch;

		if (user.equals("*"))
			userMatch = true;
		else
			userMatch = user.equals(this.user);

		if (userMatch) {
			if (verbMatch)
				return GRANT;
			else
				return DENY;
		} else {
			return DONT_CARE;
		}
	}

}
