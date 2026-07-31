/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.domino.agents.client;

import lotus.domino.Agent;
import lotus.domino.AgentContext;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

import com.ibm.di.plugin.domino.agents.common.DominoCommandEmitter;
import com.ibm.di.plugin.log.PWSyncLog;
import com.ibm.di.server.ResourceHash;

public class ClientWorker {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	private static final String PREFIX = "ClientAgent";
	private static final String LOG_FILE_PROP = "client.logFile";

	private static final String HELPER_DATABASE_FILE_NAME = "idipwsync.nsf";
	private static final String FIELD_PASSWORD = "idi_pwsync_client_password";
	private static final String FIELD_UID = "user_id";
	private static final String FIELD_STORE_SUCCESS = "store_success";

	private PWSyncLog log = DominoCommandEmitter.getLog(LOG_FILE_PROP);

	private static final ResourceHash resHash = ResourceHash.getHash("domino");

	public void work() {

		boolean willSavePersonDocument = false;
		String uid = "<unknown>";
		Document doc = null;

		try {
			// get pasword document from helper database
			try {
				Session session = NotesFactory.createSession();
				AgentContext agentContext = session.getAgentContext();
				Agent agent = agentContext.getCurrentAgent();

				Database db = session.getDatabase(null,
						HELPER_DATABASE_FILE_NAME);
				doc = db.getDocumentByID(agent.getParameterDocID());
			} catch (NotesException e) {
				log.error(PREFIX, resHash
						.getString("DOMINO.CLIENT.UNABLE.TO.READ.DOCUMENT"), e);
				throw e;
			}

			if (!doc.hasItem(FIELD_UID)) {
				String errorMessage = resHash.getString(
						"DOMINO.CLIENT.MISSING.FIELD", new Object[] {
								FIELD_UID, HELPER_DATABASE_FILE_NAME });

				log.error(PREFIX, errorMessage);
				throw new Exception(errorMessage);
			}

			// read plain password and user identifier and remove the
			// corresponding fields
			String plainPassword = null;
			try {
				plainPassword = doc.getItemValueString(FIELD_PASSWORD);
				uid = doc.getItemValueString(FIELD_UID);

				doc.removeItem(FIELD_PASSWORD);
				doc.removeItem(FIELD_UID);
				doc.save(true);
			} catch (NotesException e) {
				log.error(PREFIX, resHash.getString("DOMINO.NOTES.EXCEPTION"),
						e);
				throw e;
			}

			// pass uid and password to the Communication Library
			boolean successfulStore = DominoCommandEmitter.syncPass(uid,
					plainPassword);

			if (!successfulStore) {
				log.warn(PREFIX, resHash.getString(
						"DOMINO.ADMIN.REQUEST.FAILURE", uid));
			} else {
				log.info(PREFIX, resHash.getString(
						"DOMINO.ADMIN.REQUEST.SUCCESS", uid));
				try {
					doc.replaceItemValue(FIELD_STORE_SUCCESS, "1");
					willSavePersonDocument = doc.save(true);
					if (!willSavePersonDocument) {
						log.warn(PREFIX, resHash
								.getString("DOMINO.CLIENT.SAVE.FAILED"));
					}
				} catch (NotesException e) {
					log.error(PREFIX, resHash
							.getString("DOMINO.NOTES.EXCEPTION"), e);
				}
			}
		} catch (NotesException e) {
			log.error(PREFIX, resHash.getString("DOMINO.NOTES.EXCEPTION"), e);
		} catch (Exception e) {
			log.error(PREFIX, resHash.getString("DOMINO.JAVA.EXCEPTION"), e);
		} finally {
			if (!willSavePersonDocument) {
				log.warn(PREFIX, resHash.getString(
						"DOMINO.CLIENT.REJECTING.CHANGES", uid));
			}
		}
	}
}
