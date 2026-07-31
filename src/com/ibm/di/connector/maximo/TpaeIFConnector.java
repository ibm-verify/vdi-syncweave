/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.maximo.core.MxConnConfiguration;
import com.ibm.di.connector.maximo.core.MxConnIterator;
import com.ibm.di.connector.maximo.core.MxConnLookup;
import com.ibm.di.connector.maximo.core.MxConnSync;
import com.ibm.di.connector.maximo.core.SimpleTpaeIFConnector;
import com.ibm.di.connector.maximo.exception.MxConnectorException;
import com.ibm.di.connector.maximo.exception.MxConnectorRuntimeException;
import com.ibm.di.connector.maximo.parsing.EntryConverter;
import com.ibm.di.connector.maximo.parsing.Schema;
import com.ibm.di.connector.maximo.parsing.SchemaElement;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;
import com.ibm.di.util.SchemaUtils;

/**
 * Tpae IF Connector is able to work with hierarchical entries and is based on
 * the SimpleTpaeIFConnector.
 */
public class TpaeIFConnector extends SimpleTpaeIFConnector {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "tpaeifconnector";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static final ResourceHash resHash = new ResourceHash(PROPERTIES_FILE);

	/**
	 * The name of the Connector
	 */
	private static final String myName = "Tpae IF Connector";

	/**
	 * The {@link MxConnSync} object that will handle add, update and
	 * delete operations.
	 */
	private MxConnSync connSync;

	/**
	 * Default constructor.
	 */
	public TpaeIFConnector() {
		setName(myName);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE, ConnectorConfig.DELETE_MODE, ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.LOOKUP_MODE, ConnectorConfig.UPDATE_MODE });
	}

	/**
	 * This method makes sure that
	 * {@link InternalSchema#CONNECTOR_COMPUTE_CHANGES} is set to
	 * <code>false</code> since compute changes logic is not supported for
	 * hierarchical entries yet.
	 */
	@Override
	public void setConfiguration(Object config) {
		((ConnectorConfig) config).setParameter(InternalSchema.CONNECTOR_COMPUTE_CHANGES, false);
		super.setConfiguration(config);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(Object obj) throws Exception {
		super.initialize(obj);

		String user = getParam(MxConnConfiguration.PARAM_USER_ID);
		String pass = getParam(MxConnConfiguration.PARAM_PASSWORD);

		// We have user and pass so set authenticationRequired to true.
		// Here setParam is override by our parent and it also updates cfg
		if (user != null && pass != null && user.trim().length() > 0 && pass.trim().length() > 0) {
			setParam(MxConnConfiguration.PARAM_AUTHENTICATION_REQUIRED, "true");
		} else {
			setParam(MxConnConfiguration.PARAM_AUTHENTICATION_REQUIRED, "false");
		}

		setParam("mbo", cfg.getSchema().getRootMbo().getName());
		setParam(MxConnConfiguration.PARAM_ES_SYNC, getParam(MxConnConfiguration.PARAM_ES_SYNC));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectEntries() throws MxConnectorException {
		Trace.entrymin(this, "selectEntries");

		if (connIterator == null) {
			connIterator = new MxConnIterator(cfg, proxyLog, true);
			debug(resHash.getString("ADVTPAE.NEW.MXCONNITERATOR.CREATED"));
		}
		super.selectEntries();

		Trace.exitmin(this, "selectEntries");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Entry getNextEntry() throws MxConnectorException {
		Trace.entrymin(this, "getNextEntry");

		Entry e = super.getNextEntry();

		Trace.exitmin(this, "getNextEntry", e);

		return e;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putEntry(final Entry newEntry) throws MxConnectorException {
		Trace.entrymin(this, "putEntry", newEntry);

		checkRootMBO(newEntry);
		initSyncConn();

		newEntry.setOp(Entry.OP_ADD);
		connSync.sync(newEntry, null);

		Trace.exitmin(this, "putEntry");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Entry findEntry(final SearchCriteria searchCriteria) throws Exception {
		Trace.entrymin(this, "findEntry", searchCriteria);

		if (connLookup == null) {
			connLookup = new MxConnLookup(cfg, proxyLog, true);
			debug(resHash.getString("ADVTPAE.NEW.MXCONNLOOKUP.CREATED"));
		}

		logmsg(resHash.getString("ADVTPAE.EXECUTE.SEARCH"));
		connLookup.setSearchCriteria(searchCriteria);
		connLookup.executeQuery();

		clearFindEntries();

		Entry e = null;
		do {
			e = connLookup.getNext();
			if (e != null && !addFindEntry(e)) {
				break;
			}
		} while (e != null);

		Entry entry = getFindEntryCount() == 1 ? getFirstFindEntry() : null;

		Trace.exitmin(this, "findEntry", entry);

		return entry;
	}

	/**
	 * Initialize the {@link MxConnSync} object that will handle add, update and
	 * delete operations.
	 */
	private void initSyncConn() {
		if (connSync == null) {
			connSync = new MxConnSync(cfg, proxyLog);
			debug(resHash.getString("ADVTPAE.NEW.MXCONNSYNC.CREATED"));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modEntry(final Entry newEntry, final SearchCriteria searchCriteria, final Entry oldEntry)
			throws MxConnectorException {
		Trace.entrymin(this, "modEntry", newEntry, oldEntry);

		checkRootMBO(newEntry);
		if (oldEntry != null) {
			checkRootMBO(oldEntry);
		}
		initSyncConn();

		ConnectorConfig cc = (ConnectorConfig) getConfiguration();
		if (cc.getSkipLookup()) {
			// let Maximo determine if add or modify
			newEntry.setOp(Entry.OP_GEN);
		} else {
			newEntry.setOp(Entry.OP_MOD);
		}

		connSync.sync(newEntry, oldEntry);

		Trace.exitmin(this, "modEntry");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteEntry(Entry entry, final SearchCriteria sc) throws MxConnectorException {
		Trace.entrymin(this, "deleteEntry", entry, sc);

		// In Delta mode null is supplied and we must use the criteria
		if (entry == null) {
			entry = EntryConverter.searchCriteriaToEntry(sc, cfg.getSchema(), true);
		}
		checkRootMBO(entry);
		initSyncConn();

		entry.setOp(Entry.OP_DEL);
		connSync.sync(entry, null);

		Trace.exitmin(this, "deleteEntry");
	}

	/**
	 * This method checks whether attribute representing the root MBO is present
	 * in the Entry passed to {@link #putEntry(Entry)},
	 * {@link #modEntry(Entry, SearchCriteria, Entry)} or
	 * {@link #deleteEntry(Entry, SearchCriteria)} methods.
	 * 
	 * @param e
	 *            Entry object to check
	 * @throws MxConnectorException
	 *             if Entry object does not contain an Attribute for the root
	 *             MBO of the specified Object structure
	 */
	private void checkRootMBO(Entry e) throws MxConnectorException {
		String rootMBOName = cfg.getSchema().getRootMbo().getName();

		String[] attrs = e.getAttributeNames();
		for(int i = 0; i < attrs.length; i++){
			if(attrs[i].equalsIgnoreCase(rootMBOName))
				continue;
			if(!attrs[i].startsWith(rootMBOName+".")){
				throw new MxConnectorRuntimeException(resHash.getString("ADVTPAE.ROOT.MBO.ATTRIBUTE.MISSING", rootMBOName));
			}					
		}
		
		// There must be exactly one root MBO attribute in an entry
		if (e.getAttribute(rootMBOName) == null) {
			//throw new MxConnectorRuntimeException(resHash.getString("ADVTPAE.ROOT.MBO.ATTRIBUTE.MISSING", rootMBOName));
			//if user not setting rootMBO attribute,Instead of directly throwing exception, we are setting it here
			e.setAttribute(rootMBOName, "");
		}
	}

	/**
	 * This method populates the input/output map of the connector with the
	 * hierarchical schema of the specified Object structure.
	 * <p>
	 * <b>Note</b>: This method uses
	 * {@link SchemaUtils#convertEntryToSchemaHier(Entry, ConnectorConfig, boolean)}
	 * method to display a hierarchical schema. However currently this method is
	 * no converting correctly the hierarchical entry to a hierarchical schema.
	 * This will be addressed later in the release.
	 */
	@Override
	public Object querySchema(Object obj) throws Exception {
		Trace.entrymin(this, "querySchema", obj);

		Entry schema = getHierSchemaEntry();
		ConnectorConfig config = (ConnectorConfig) getConfiguration();
		SchemaUtils.convertEntryToSchemaHier(schema, config, hasInputMap());

		Trace.exitmin(this, "querySchema");

		return null;
	}

	/**
	 * Checks if the Connector has Input Map.
	 * 
	 * @return whether the Connector has Input Map.
	 */
	private boolean hasInputMap() {
		String mode = ((ConnectorConfig) this.getConfiguration()).getMode();
		return !(mode.equals(ConnectorConfig.UPDATE_MODE) || mode.equals(ConnectorConfig.ADDONLY_MODE));
	}

	/**
	 * @return a hierarchical entry representing the schema of the specified
	 *         Object structure.
	 * @throws MxConnectorException
	 *             if any of the mandatory parameters is missing or if it is not
	 *             possible build the schema object
	 */
	private Entry getHierSchemaEntry() throws MxConnectorException {
		Entry result = new Entry();

		Schema schema = cfg.getSchema();
		SchemaElement mos = schema.getMos();
		Attribute childAttr = null;
		SchemaElement mbo = null;

		for (String mboName : schema.getMboNameList()) {
			mbo = schema.getMboByName(mboName);
			result.newAttribute(mbo.getEntryPathRelativeTo(mos));

			for (final SchemaElement child : mbo.getChildren()) {

				// Skip MBO elements and XML attributes from the schema
				if (!child.isMboDefinition() && !child.isAttribute()) {
					childAttr = result.newAttribute(child.getEntryPathRelativeTo(mos));

					// Pass syntax and required as Entry properties. These
					// should be later read by the
					// SchemaUtils.convertEntryToSchemaHier method.
					if (child.isUniqueKey()) {
						childAttr.setAttribute("presenceFlag", "Required");
					} else {
						childAttr.setAttribute("presenceFlag", "Optional");
					}
					childAttr.setAttribute("externalSyntax", child.getClassName());
				}
			}
		}

		return result;
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I%, 20%E%";
	}
}