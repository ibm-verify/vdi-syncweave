/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.taddm;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.collation.platform.model.Guid;
import com.collation.platform.model.ModelObject;
import com.collation.proxy.api.client.ApiException;
import com.collation.proxy.api.client.DataApi;
import com.collation.reports.data.ChangeHistory;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.ChangelogInterface;
import com.ibm.di.entry.Entry;
import com.ibm.di.store.PropertyStore;
import com.ibm.di.store.StoreFactory;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

/**
 * This Connector permits reading changes from TADDM.
 */
public class TADDMChangeDetectionWorkerConnector extends TADDMWorkerConnector implements ChangelogInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * The parameters for the start date from which to start detecting changes.
	 */
	private static final String PARAM_START_AT = "startAt";

	/**
	 * The parameter for enabling detecting created model objects.
	 */
	private static final String PARAM_DETECT_CREATED = "created";

	/**
	 * The parameter for enabling detecting updated model objects.
	 */
	private static final String PARAM_DETECT_UPDATED = "updated";

	/**
	 * The parameter for enabling detecting deleted model objects.
	 */
	private static final String PARAM_DETECT_DELETED = "deleted";

	/**
	 * The parameter setting the interval between change requests to TADDM.
	 */
	private static final String PARAM_POLL_INTERVAL = "pollInterval";

	/**
	 * The parameter determining the key of the property used for persisting the
	 * current state.
	 */
	private static final String PARAM_ITERATOR_STATE_KEY = "iteratorStateKey";

	/**
	 * The parameter determining the key of the property used for persisting the
	 * current state.
	 */
	private static final String PARAM_TIMEOUT = "maximumWaitTime";

	/**
	 * Whether to detect created model objects.
	 */
	private boolean detectCreated;

	/**
	 * Whether to detect updated model objects.
	 */
	private boolean detectUpdated;

	/**
	 * Whether to detect deleted model objects.
	 */
	private boolean detectDeleted;

	/**
	 * A date formatter used for parsing the start date used for detection.
	 */
	private final DateFormat dateFormatter;

	/**
	 * The start of the current interval where changes will be detected.
	 */
	private Long startDate;

	/**
	 * The end of the current interval where changes will be detected.
	 */
	private Long endDate;

	/**
	 * A label denoting the current moment in time.
	 */
	private static final String START_NOW = "EOD";

	/**
	 * PropertyStore key used in Connector to store current marker state.
	 */
	private String iteratorStateKey;

	/**
	 * PropertyStore object used in the Connector to store current marker state.
	 */
	private PropertyStore store;

	/**
	 * If true, the Connector will store current marker state after read.
	 */
	private boolean saveAfterRead = true;

	/**
	 * Variable that holds the method used to store the current marker state.
	 */
	private int stateKeySaveMethod = SAVE_STATE_AFTER_READ;

	/**
	 * This class is used for driving the discovery process.
	 */
	private final TADDMChangeDetectionIterator iterator;

	/**
	 * Iterator for accessing the class types for which changes will be
	 * detected.
	 */
	private Iterator<String> classTypesIterator;

	/**
	 * Constructor.
	 */
	public TADDMChangeDetectionWorkerConnector() {
		setName("TADDM Change Detection Connector");
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE });
		dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
		iterator = new TADDMChangeDetectionIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize(final Object sessionObject) throws Exception {
		super.initialize(sessionObject);

		store = StoreFactory.getDefaultPropertyStore();

		detectCreated = getBooleanParameter(PARAM_DETECT_CREATED);
		detectUpdated = getBooleanParameter(PARAM_DETECT_UPDATED);
		detectDeleted = getBooleanParameter(PARAM_DETECT_DELETED);

		if (!detectCreated && !detectUpdated && !detectDeleted) {
			throw new Exception(getMessage("TADDM.CD.CONN.NO.CHANGE.TYPE.SELECTED"));
		}

		iteratorStateKey = getStringParameter(PARAM_ITERATOR_STATE_KEY);
		if (isSet(iteratorStateKey)) {
			super.printDebugMessage("TADDM.CONN.PARAMETER.INITIALIZED", PARAM_ITERATOR_STATE_KEY, iteratorStateKey);

			Object state = store.getProperty(iteratorStateKey);
			if (state instanceof Long) {
				startDate = (Long) state;
				debug(TADDMConnector.L10N.getString("TADDM.CD.CONN.STATE.FOUND", startDate.toString()));
			} else {
				debug(TADDMConnector.L10N.getString("TADDM.CD.CONN.NO.STATE.FOUND"));
			}
		} else {
			iteratorStateKey = null;
		}

		if (startDate == null) {
			String startString = getStringParameter(PARAM_START_AT);
			if (isSet(startString)) {
				super.printDebugMessage("TADDM.CONN.PARAMETER.INITIALIZED", PARAM_START_AT, startString);
				if (START_NOW.equalsIgnoreCase(startString)) {
					// get current time
					startDate = api.getServerTime();
				} else {
					try {
						startDate = dateFormatter.parse(startString).getTime();
					} catch (ParseException pe) {
						throw new Exception(getMessage("TADDM.CD.CONN.UNSUPPORTED.DATE.FORMAT", startString), pe);
					}
				}
			} else {
				// get the earlier date supported
				startDate = new Date(0).getTime();
			}
			debug(TADDMConnector.L10N.getString("TADDM.CD.CONN.START.DATE", new Date(startDate)));
		}

		String stateKeyPersistence = getStringParameter(CONN_PARAM_STATE_KEY_PERSISTENCE);
		if (isSet(stateKeyPersistence)) {
			if (PARAM_VAL_END_OF_CYCLE.equals(stateKeyPersistence)) {
				saveAfterRead = false;
				stateKeySaveMethod = SAVE_STATE_END_OF_CYCLE;
			} else if (PARAM_VAL_MANUAL.equals(stateKeyPersistence)) {
				saveAfterRead = false;
				stateKeySaveMethod = SAVE_STATE_MANUAL;
			} else {
				saveAfterRead = true;
			}
		}

		int interval = getIntegerParameter(PARAM_POLL_INTERVAL, true);
		if (interval != UNKNOWN) {
			iterator.setPollInterval(interval);
		}

		int timeout = getIntegerParameter(PARAM_TIMEOUT, false);
		if (timeout != UNKNOWN) {
			iterator.setTimeout(timeout);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectEntries() throws Exception {
		super.selectEntries();
		endDate = api.getServerTime();
		classTypesIterator = allPersistableClasses.iterator();
	}

	/**
	 * Finds the TADDM changes up to the provided point in time. This method
	 * checks each of the available class types for changes and returns as soon
	 * as it finds changes (any or all of the three types - added, deleted,
	 * modified). This means the subsequent call will start from the next class
	 * type in the list. When all types are iterated, the method will start from
	 * the first and will push the time interval for changes ahead.
	 * 
	 * @return a list of changes.
	 * @throws Exception
	 *             if a problem occurs when finding changes.
	 */
	private List<ModelObjectChange> findChanges() throws Exception {
		if (!classTypesIterator.hasNext()) {
			startDate = endDate;
			endDate = api.getServerTime();
			classTypesIterator = allPersistableClasses.iterator();
		}

		List<ModelObjectChange> modelObjectChanges = new LinkedList<ModelObjectChange>();
		while (modelObjectChanges.isEmpty() && classTypesIterator.hasNext()) {
			queryBuilder.setClassType(getMetaData().getClassType(classTypesIterator.next()));
			String query = queryBuilder.buildQuery();
			debug(getMessage("TADDM.CD.CONN.QUERYING.TADDM.FOR.CHANGES", new Date(startDate).toString(), new Date(endDate)
					.toString(), query));

			// NOTE: Order should not be changed!
			// If in the same time interval a resource is modified several
			// times, TADDM will combine them and report its state at the end.
			// This means that sequence create-update-update will be translated
			// into two events - create and update. However, a sequence like
			// create-delete will become a single delete event (because after a
			// resource is deleted in TADDM no reference of its content is
			// kept). This way the create-delete-create sequence will transform
			// into delete-create.
			// Therefore, when reading changes we first take the deletes,
			// then creates and finally updates. We do not use the
			// DataApi.ANYCHANGE option because it does not report the type of
			// change and also scrambles the change events.
			if (detectDeleted) {
				modelObjectChanges.addAll(findChanges(DataApi.DELETED, query, depth, startDate, endDate));
			}
			if (detectCreated) {
				modelObjectChanges.addAll(findChanges(DataApi.CREATED, query, depth, startDate, endDate));
			}
			if (detectUpdated) {
				modelObjectChanges.addAll(findChanges(DataApi.UPDATED, query, depth, startDate, endDate));
			}
		}

		return modelObjectChanges;
	}

	/**
	 * Checks TADDM for changes with the provided set of characteristics.
	 * 
	 * @param type
	 *            the type of change. See {@link DataApi} for details.
	 * @param query
	 *            the query determining which class type will be checked.
	 * @param depth
	 *            the depth of the returned resource.
	 * @param start
	 *            the start of the time interval where changes will be detected.
	 * @param end
	 *            the end of the time interval where changes will be detected.
	 * @return a list of the discovered modifications.
	 * @throws Exception
	 *             if a problem occurs when finding changes.
	 */
	private List<ModelObjectChange> findChanges(int type, String query, int depth, long start, long end) throws Exception {
		List<ModelObjectChange> modelObjectChanges = new ArrayList<ModelObjectChange>();
		// IMPORTANT: we can use the following call instead. However,
		// this will require reworking the logic of the objectConverter because
		// it only gives us the GUIDs of the related items. ModelObject[]
		// modelObjects = api.findChanges(queryBuilder.buildQuery(), true,
		// startDate, endDate, type);
		try {
			ModelObject[] modelObjects = api.findChanges(query, depth, start, end, type);
			for (ModelObject mo : modelObjects) {
				if (type != DataApi.DELETED || !filterDeletedItem(mo.getGuid(), start, end)) {
					modelObjectChanges.add(new ModelObjectChange(type, mo));
				}
			}
		} catch (ApiException ae) {
			if (queryMultipleClasses()) {
				// re-execute the query with the next class type
				printDebugMessage("TADDM.CD.CONN.ERROR.FINDING.CHANGE", convertDeltaType(type), query);
			} else {
				throw new Exception(getMessage("TADDM.CD.CONN.ERROR.FINDING.CHANGE", convertDeltaType(type), query), ae);
			}
		}
		return modelObjectChanges;
	}

	/**
	 * Checks if the item with the provided {@link Guid} was both created and
	 * deleted in the specified interval. This will mean it did not exist at all
	 * when looked at from the perspective of the interval's ends.
	 * 
	 * @param guid
	 *            the {@link Guid} of the item.
	 * @param start
	 *            the starting point of the interval (in ms).
	 * @param end
	 *            the end of the interval (in ms).
	 * @return <code>true</code> if the resource was both created and deleted
	 *         and thus can be ignored as a change event; otherwise
	 *         <code>false</code>.
	 * @throws ApiException
	 *             if a problem when extracting change history occurs.
	 */
	private boolean filterDeletedItem(Guid guid, long start, long end) throws ApiException {
		ChangeHistory[] changes = api.getChangeHistory(guid, start, end);
		// sort the change types in time
		Map<Long, String> changeTypes = new TreeMap<Long, String>();
		for (ChangeHistory c : changes) {
			changeTypes.put(c.getPrimaryKey(), c.getWhatHappened());
		}

		boolean filter = true;
		int count = 0;
		for (String change : changeTypes.values()) {
			if (ChangeHistory.CREATED.equals(change)) {
				count++;
			} else if (ChangeHistory.DELETED.equals(change)) {
				count--;
			}
			if (count < 0) {
				filter = false;
				break;
			}
		}
		return filter;
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getNextEntry() throws Exception {
		Entry entry = null;
		boolean success = false;
		ModelObjectChange change = null;
		while (!success && (change = iterator.next()) != null) {
			entry = objectConverter.convert(change.getModelObject());
			success = entry.getAttributeNames().length > 0;
			entry.setOperation(convertDeltaType(change.getType()));
			if (!success) {
				debug(getMessage("TADDM.CD.CONN.SKIPPING.CHANGE"));
			}
		}

		// Save change token
		if (saveAfterRead && iteratorStateKey != null) {
			store.updateProperty(iteratorStateKey, endDate, true);
		}

		if (success) {
			return entry;
		}
		return null;
	}

	/**
	 * Converts the TADDM change types to TDI delta operations.
	 * 
	 * @param type
	 *            TADDM change type.
	 * @return TDI operation string.
	 */
	private String convertDeltaType(int type) {
		switch (type) {
		case DataApi.CREATED:
			return Entry.OP_ADD2;
		case DataApi.UPDATED:
			return Entry.OP_MOD2;
		case DataApi.DELETED:
			return Entry.OP_DEL2;
		default:
			return Entry.OP_GEN2;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() throws Exception {
		iterator.terminate();
		super.terminate();
	}

	/**
	 * Retrieves the TADDM server time for design time configuration.
	 * 
	 * @return the server time.
	 * @throws Exception
	 *             if a problem with TADDM occurs.
	 */
	public Date getServerTime() throws Exception {
		Date serverTime = null;
		initialize(null);
		try {
			serverTime = new Date(api.getServerTime());
		} catch (Exception ex) {
			throw new Exception(getMessage("TADDM.CD.CONN.ERROR.GETTING.SERVER.TIME"), ex);
		} finally {
			terminate();
		}
		return serverTime;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersion() {
		return "1.0-di7.1.1 %I%, 20%E%";
	}

	/**
	 * Gets a localized message using the provided key and adding the available
	 * values.
	 * 
	 * @param key
	 *            the message's key.
	 * @param values
	 *            the values to be added to the message.
	 * @return the formatted localized string.
	 */
	private static String getMessage(String key, Object... values) {
		return TADDMConnector.L10N.getString(key, values);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getStateKeyObject() {
		return endDate != null ? endDate : startDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getStateKeySaveMethod() {
		return stateKeySaveMethod;
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveStateKey() throws Exception {
		if (!saveAfterRead && iteratorStateKey != null) {
			store.updateProperty(iteratorStateKey, getStateKeyObject(), true);
		}
	}

	/**
	 * Encapsulates a single change of a TADDM model object.
	 */
	private static class ModelObjectChange {

		/**
		 * The type of change. See {@link DataApi} for details.
		 */
		private final int type;

		/**
		 * The changed model object.
		 */
		private final ModelObject modelObject;

		/**
		 * Constructor.
		 * 
		 * @param type
		 *            change type. See {@link DataApi} for details.
		 * @param modelObject
		 *            changed model object.
		 */
		ModelObjectChange(int type, ModelObject modelObject) {
			this.type = type;
			this.modelObject = modelObject;
		}

		/**
		 * Returns the type of the change.
		 * 
		 * @return change type.
		 */
		public int getType() {
			return type;
		}

		/**
		 * Returns the changed model object.
		 * 
		 * @return changed model object.
		 */
		public ModelObject getModelObject() {
			return modelObject;
		}
	}

	/**
	 * A basic class used for iterating all model objects changes. If
	 * configured, it pulls for new changes at a set period of time and .
	 */
	private class TADDMChangeDetectionIterator implements Iterator<ModelObjectChange> {

		/**
		 * The poll interval used to query TADDM (in seconds). By default it is
		 * set to 180.
		 */
		private long pollInterval = 180; // seconds

		/**
		 * Timeout for that the iterator will wait for getting a result. If by
		 * the end of this period nothing is detected, <code>null</code> is
		 * returned. By default it waits forever (=0).
		 */
		private long timeout = 0;

		/**
		 * The start marker for the timeout check.
		 */
		private long timeoutStart;

		/**
		 * The end marker for the timeout check.
		 */
		private long timeoutEnd;

		/**
		 * Whether a termination request has been received.
		 */
		private AtomicBoolean terminationRequested;

		/**
		 * Contains the detected modifications.
		 */
		private List<ModelObjectChange> changes;

		/**
		 * Constructor.
		 */
		public TADDMChangeDetectionIterator() {
			changes = new LinkedList<ModelObjectChange>();
			terminationRequested = new AtomicBoolean(false);
		}

		/**
		 * Determines the amount of time the Iterator will wait before pulling
		 * TADDM for more changes.
		 * 
		 * @param pollInterval
		 *            the amount of time in seconds.
		 */
		public void setPollInterval(int pollInterval) {
			this.pollInterval = pollInterval;
		}

		/**
		 * The timeout which the iterator will wait for new changes after the
		 * last one was detected.
		 * 
		 * @param timeout
		 *            the required timeout in seconds. By default it is 0 (wait
		 *            indefinitely).
		 */
		public void setTimeout(int timeout) {
			this.timeout = timeout;
		}

		/**
		 * Checks TADDM for changes.
		 */
		private void reselect() {
			try {
				changes.addAll(findChanges());
				if (changes.isEmpty()) {
					debug(getMessage("TADDM.CD.CONN.NO.CHANGES.FOUND"));
				} else {
					debug(getMessage("TADDM.CD.CONN.CHANGES.FOUND", changes.size()));
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		/**
		 * Returns the next change in TADDM. If there are no more changes the
		 * method waits a set amount of time and rechecks (and so on) until the
		 * timeout expires.
		 * 
		 * @return the discovered change or <code>null</code> if the timeout has
		 *         expired.
		 */
		public ModelObjectChange next() {
			if (changes.isEmpty() && classTypesIterator.hasNext()) {
				reselect();
			}

			// sleep
			while (changes.isEmpty() && !terminationRequested.get() //
					&& !hasTimeoutExpired()) {
				debug(getMessage("TADDM.CD.CONN.WAITING", pollInterval));
				if (timeoutStart == 0) {
					timeoutStart = System.currentTimeMillis();
				}
				try {
					Thread.sleep(pollInterval * 1000);
				} catch (InterruptedException ie) {
					terminationRequested.set(true);
					Thread.currentThread().interrupt();
					break;
				}
				timeoutEnd = System.currentTimeMillis();
				reselect();
			}

			// return the next change
			ModelObjectChange change = null;
			if (!changes.isEmpty()) {
				change = changes.remove(0);
				// reset timeout
				timeoutStart = 0;
				timeoutEnd = 0;
			} else if (hasTimeoutExpired()) {
				debug(getMessage("TADDM.CD.CONN.TIMEOUT.EXPIRED", changes.size()));
			}
			return change;
		}

		/**
		 * Whether the timeout for not reading a change has expired or not.
		 * 
		 * @return <code>true</code> if the timeout has expired and
		 *         <code>false</code> otherwise.
		 */
		private boolean hasTimeoutExpired() {
			return timeout > 0 && timeoutEnd - timeoutStart > timeout;
		}

		/**
		 * Checks if there MAY be more elements.
		 * 
		 * <b>Important:</b> Due to the fact that this iterator periodically
		 * pulls changes from the underlying system the existence of more data
		 * cannot be determines definitely. Therefore, this method will return
		 * <code>false</code> only when its timeout has expired.
		 */
		public boolean hasNext() {
			return !hasTimeoutExpired();
		}

		/**
		 * Throws an UnsupportedOperationException.
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}

		/**
		 * Closes the Iterator, forcing it to stop waiting for changes.
		 */
		public void terminate() {
			terminationRequested.set(true);
			changes.clear();
		}

	}

}