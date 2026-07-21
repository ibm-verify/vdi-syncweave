/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.domino.agents.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import lotus.domino.AgentContext;
import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

import com.ibm.di.plugin.domino.agents.common.DominoCommandEmitter;
import com.ibm.di.plugin.log.PWSyncLog;
import com.ibm.di.server.ResourceHash;

public class AdminWorker {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	private static final String PREFIX = "AdminAgent";
	private static final String LOG_FILE_PROP = "admin.logFile";

	// Database fields
	private static final String FIELD_DOCUMENT_PROCESSED = "$PWSyncAgentProcessed";
	private static final String FIELD_ACTION_END_TIME = "ActionEndTime";
	private static final String FIELD_PASSWORD = "ProxyNewHTPPPassword";
	private static final String FIELD_UID_SOURCE = "ProxyNameList";

	private static final String QUERY = "ProxyAction=\"127\" & Form=\"AdminLog\" & @IsUnAvailable(ErrorFlag) & @IsUnAvailable("
			+ FIELD_DOCUMENT_PROCESSED + ")";

	// maximum MAX_ADMIN_REQUESTS admin requests are processed on an agent run,
	// the next requests (if any) are processed on the next agent run(s)
	// we introduce this restriction because trying to process more than 5000
	// documents on one pass
	// results in OUT OF PRIVATE HANDLES exception in Domino
	private static final int MAX_ADMIN_REQUESTS = 5000;

	private Database mDb = null;

	private PWSyncLog log = DominoCommandEmitter.getLog(LOG_FILE_PROP);

	private static final ResourceHash resHash = ResourceHash.getHash("domino");

	public void work() {
		try {
			// general password store check...
			if (!DominoCommandEmitter.readyToSync("", null)) {
				log.error(PREFIX, resHash.getString("DOMINO.PWSTORE.NOT.AVAILABLE"));
				return;
			}

			Session session = NotesFactory.createSession();
			AgentContext agentContext = session.getAgentContext();
			mDb = agentContext.getCurrentDatabase();

			DocumentCollection dc = mDb.search(QUERY, null, MAX_ADMIN_REQUESTS);
			if (dc.getCount() == 0) {
				log.debug(PREFIX, resHash.getString("DOMINO.SEARCH.NO.RESULTS"));
				return;
			} else {
				log.debug(PREFIX, resHash.getString("DOMINO.SEARCH.RESULTS",
						dc.getCount()));
			}

			ArrayList<Document> documents = new ArrayList<Document>();
			Document doc = dc.getFirstDocument();
			int documentCounter = 0;
			while (doc != null && documentCounter < MAX_ADMIN_REQUESTS) {
				documentCounter++;
				documents.add(doc);
				doc = dc.getNextDocument();
			}

			Comparator<Document> requestComparator = new DocumentComparator();

			Collections.sort(documents, requestComparator);

			for (int i = 0; i < documents.size(); i++) {
				doc = documents.get(i);
				processDoc(doc);

				// recycle document
				documents.set(i, null);
				doc.recycle();
			}
			dc.recycle();
		} catch (NotesException e) {
			log.error(PREFIX, resHash.getString("DOMINO.NOTES.EXCEPTION"), e);
		} catch (Exception e) {
			log.error(PREFIX, resHash.getString("DOMINO.JAVA.EXCEPTION"), e);
		}
	}

	private void processDoc(Document aDoc) {
		boolean markDocument = true;
		String adminRequestUNID = "unknown";
		String userID = "unknown";
		Document parentDoc = null;

		try {
			adminRequestUNID = aDoc.getParentDocumentUNID();
			log.debug(PREFIX, resHash.getString(
					"DOMINO.ADMIN.PROCESSING.REQUEST", adminRequestUNID));
			parentDoc = mDb.getDocumentByUNID(adminRequestUNID);
			if (parentDoc == null || !parentDoc.isValid()) {
				log.warn(PREFIX, resHash.getString(
						"DOMINO.ADMIN.REQUEST.NO.UNID", adminRequestUNID));
				return;
			}

			// get admin request password data
			String plainPass = parentDoc.getItemValueString(FIELD_PASSWORD);
			if (plainPass == null || plainPass.startsWith("(")) {
				log.warn(resHash.getString("DOMINO.ADMIN.REQUEST.NO.PASSWD",
						adminRequestUNID));
				return;
			}
			userID = parentDoc.getItemValueString(FIELD_UID_SOURCE);
			if (userID == null || userID.trim().length() == 0) {
				log.warn(PREFIX, resHash
						.getString("DOMINO.ADMIN.REQUEST.NO.FULL.NAME"));
				return;
			}

			// invoke the Communication Library to store the intercepted
			// password

			markDocument = DominoCommandEmitter.syncPass(userID, plainPass);

			if (markDocument) {
				log.info(PREFIX, resHash.getString(
						"DOMINO.ADMIN.REQUEST.SUCCESS", userID));
			} else {
				log.info(resHash.getString("DOMINO.ADMIN.REQUEST.FAILURE",
						userID));
			}
		} catch (NotesException e) {
			log.error(PREFIX, resHash.getString("DOMINO.ADMIN.REQUEST.FAILURE",
					adminRequestUNID));
			log.error(PREFIX, resHash.getString("DOMINO.NOTES.EXCEPTION"), e);
		} catch (Exception e) {
			log.error(PREFIX, resHash.getString("DOMINO.ADMIN.REQUEST.FAILURE",
					adminRequestUNID));
			log.error(PREFIX, resHash.getString("DOMINO.JAVA.EXCEPTION"), e);
		} finally {
			if (markDocument) {
				try {
					markDoc(aDoc);
				} catch (NotesException e) {
					log.error(PREFIX, resHash.getString(
							"DOMINO.ADMIN.UNABLE.TO.MARK", adminRequestUNID));
					log.error(PREFIX, resHash
							.getString("DOMINO.NOTES.EXCEPTION"), e);
				}
			}

			// recycle parent document
			if (parentDoc != null) {
				try {
					parentDoc.recycle();
				} catch (NotesException e) {
					log.error(PREFIX, resHash
							.getString("DOMINO.NOTES.EXCEPTION"), e);
				}
			}
		}
	}

	private void markDoc(Document aDoc) throws NotesException {
		aDoc.replaceItemValue(FIELD_DOCUMENT_PROCESSED, "1");
		aDoc.save(true);
	}

	private class DocumentComparator implements Comparator<Document> {

		/**
		 * {@inheritDoc}
		 */
		public int compare(Document o1, Document o2) {
			int res = 0;

			try {
				Document doc1 = (Document) o1;
				Document doc2 = (Document) o2;
				Item item1 = doc1.getFirstItem(FIELD_ACTION_END_TIME);
				Item item2 = doc2.getFirstItem(FIELD_ACTION_END_TIME);
				DateTime dt1 = item1.getDateTimeValue();
				DateTime dt2 = item2.getDateTimeValue();

				// we are using the JavaDate representation, because the
				// "timeDifference" DateTime
				// method does not work correctly when the values are
				// close one to another (the difference
				// is less than a second)
				res = (dt1.toJavaDate()).compareTo(dt2.toJavaDate());
			} catch (Exception e) {
				if (e instanceof NotesException) {
					log.error(PREFIX, resHash
							.getString("DOMINO.NOTES.EXCEPTION"), e);
				}
				log.warn(PREFIX, resHash.getString(
						"DOMINO.COMPARATOR.ASSUMPTION", new Object[] {
								o1.toString(), o2.toString() }));
			}
			return res;
		}
	}
}
