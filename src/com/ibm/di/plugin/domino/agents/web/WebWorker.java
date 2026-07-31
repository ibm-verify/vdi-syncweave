/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.domino.agents.web;

import lotus.domino.AgentContext;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

import com.ibm.di.plugin.domino.agents.common.DominoCommandEmitter;
import com.ibm.di.plugin.log.PWSyncLog;
import com.ibm.di.server.ResourceHash;

public class WebWorker {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	private static final String PREFIX = "WebAgent";
	private static final String LOG_FILE_PROP = "web.logFile";

	private static final String FIELD_PASSWORD = "idi_pwsync_plain_pass";
	private static final String FIELD_UID_SOURCE = "FullName";
	private static final String FIELD_UID_LABEL = "User name";
	private static final String FIELD_WEB_FLAG = "idi_pwsync_change_src_flag";
	private static final String FIELD_SAVE_OPTIONS = "SaveOptions";
	private static final String VALUE_CANCEL_SAVE = "0";
	private static final String VALUE_UNKNOWN = "<unknown>";

	private PWSyncLog log = DominoCommandEmitter.getLog(LOG_FILE_PROP);

	private static final ResourceHash resHash = ResourceHash.getHash("domino");

	public void work() {

		boolean successfulStore = false;
		String uid = VALUE_UNKNOWN;
		Document doc = null;
		try {
			// get Person document
			try {
				Session session = NotesFactory.createSession();
				AgentContext agentContext = session.getAgentContext();
				doc = agentContext.getDocumentContext();
			} catch (NotesException e) {
				log.error(PREFIX, resHash
						.getString("DOMINO.WEB.UNABLE.TO.READ.DOCUMENT"), e);
				throw e;
			}

			// get plain password and user identifier
			String plainPassword = null;
			try {
				plainPassword = doc.getItemValueString(FIELD_PASSWORD);
				uid = doc.getItemValueString(FIELD_UID_SOURCE);
			} catch (NotesException e) {
				log.error(PREFIX, resHash
						.getString("DOMINO.WEB.UNABLE.TO.READ.DOCUMENT.DATA"),
						e);
				throw e;
			}

			// remove custom data we've added to the document
			try {
				doc.removeItem(FIELD_PASSWORD);
				doc.removeItem(FIELD_WEB_FLAG);
			} catch (NotesException e) {
				log
						.error(
								PREFIX,
								resHash
										.getString("DOMINO.WEB.UNABLE.TO.REMOVE.DOCUMENT.FIELDS"),
								e);
				throw e;
			}

			// check if we have user identifier
			if (uid == null || uid.trim().length() == 0) {
				uid = VALUE_UNKNOWN;
				throw new Exception(resHash.getString(
						"DOMINO.WEB.UNABLE.TO.SYNCHRONIZED.NULL.USER'",
						FIELD_UID_LABEL));
			}

			// pass uid and password to the Communication Library
			successfulStore = DominoCommandEmitter.syncPass(uid, plainPassword);
			if (successfulStore) {
				log.info(PREFIX, resHash.getString(
						"DOMINO.ADMIN.REQUEST.SUCCESS", uid));
			} else {
				log.warn(PREFIX, resHash.getString(
						"DOMINO.ADMIN.REQUEST.FAILURE", uid));
			}

		} catch (NotesException e) {
			log.error(PREFIX, resHash.getString("DOMINO.NOTES.EXCEPTION"), e);
		} catch (Exception e) {
			log.error(PREFIX, resHash.getString("DOMINO.JAVA.EXCEPTION"), e);
		} finally {
			if (!successfulStore) {
				log.warn(PREFIX, resHash.getString(
						"DOMINO.CLIENT.REJECTING.CHANGES", uid));

				try {
					doc.replaceItemValue(FIELD_SAVE_OPTIONS, VALUE_CANCEL_SAVE);
				} catch (NotesException e) {
					log.error(PREFIX, resHash
							.getString("DOMINO.NOTES.EXCEPTION"), e);
				}
			}
		}
	}
}
