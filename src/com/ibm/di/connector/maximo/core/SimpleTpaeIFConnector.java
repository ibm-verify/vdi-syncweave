/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.core;

import java.util.Vector;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.maximo.exception.MxConnExcedentSizeException;
import com.ibm.di.connector.maximo.exception.MxConnHttpException;
import com.ibm.di.connector.maximo.exception.MxConnIOException;
import com.ibm.di.connector.maximo.exception.MxConnTimeoutException;
import com.ibm.di.connector.maximo.exception.MxConnXmlParsingException;
import com.ibm.di.connector.maximo.exception.MxConnectorException;
import com.ibm.di.connector.maximo.parsing.Schema;
import com.ibm.di.connector.maximo.parsing.SchemaElement;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;

/**
 * IBM Tivoli Directory Integrator connector for TPAE IF.
 * <p>
 * Supported Modes:
 * </p>
 * <p>
 * <a href="#iterator">Iterator</a>, <a href="#lookup">Lookup</a>, <a
 * href="#addonly">AddOnly</a>, <a href="#delete">Delete</a>, <a
 * href="#update">Update</a>
 * </p>
 * <p>
 * <b>Note:</b> The basic functionality of this connector has been tested with
 * TPAE 7.1.1.5 and 7.1.1.7.
 * </p>
 * <hr/>
 * <h1>Overview</h1>
 * <p>
 * Maximo is a very powerful framework that allows the creation of rich
 * web-based applications, such as CCMDB and other IBM products. Inside Maximo,
 * every business rule is defined by a Maximo Business Object or MBO. A MBO
 * instance has its entire life cycle controlled by Maximo and to avoid
 * inconsistence, no external application should directly access it. Because of
 * that, Maximo has its own integration mechanism, called Maximo Enterprise
 * Adapter or MEA.
 * </p>
 * <p>
 * The TPAE IF Connector provides access to any MBO properly exposed by the MEA.
 * </p>
 * <hr/>
 * <h1>Maximo Enterprise Adapter</h1>
 * <p>
 * It is beyond the scope of this document an explanation about MEA, however,
 * the TPAE IF Connector relies entirely upon it, making a brief presentation of
 * its main concepts necessary.
 * </p>
 * <p>
 * The Maximo Enterprise Adapter or MEA is a set of applications that provide
 * integration between Maximo and other external systems. From the
 * connector&apos;s perspective, the Object Structure application, the
 * Enterprise Service application and the External System application are the
 * most important.
 * </p>
 * <h2>Object Structure Application</h2>
 * <p>
 * The Object Structure Application manages object structures. An object
 * structure defines which MBO and relationships will be exposed.
 * </p>
 * <p>
 * Maximo comes with several predefined object structures. One of them is
 * MXASSET, which is described below as an example:
 * </p>
 * <table border="1">
 * <tr>
 * <th>MBO</th>
 * <th>Parent Object</th>
 * <th>Location Path</th>
 * <th>Relationship</th>
 * </tr>
 * <tr>
 * <td>ASSET</td>
 * <td>&nbsp;</td>
 * <td>ASSET</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>ASSETMETER</td>
 * <td>ASSET</td>
 * <td>ASSET/ASSETMETER</td>
 * <td>INT_ASSETMETER</td>
 * </tr>
 * <tr>
 * <td>ASSETUSERCUST</td>
 * <td>ASSET</td>
 * <td>ASSET/ASSETUSERCUST</td>
 * <td>ASSETUSERCUST</td>
 * </tr>
 * <tr>
 * <td>ASSETSPEC</td>
 * <td>ASSET</td>
 * <td>ASSET/ASSETSPEC</td>
 * <td>ASSETSPECCLASS</td>
 * </tr>
 * </table>
 * <p>
 * The result is an XML structure where the relationships are represented as
 * nested elements:
 * </p>
 * 
 * <pre>
 * &lt;ASSET&gt;
 *    &lt;ASSETID&gt;1&lt;/ASSETID&gt;
 *    ...
 *    &lt;ASSETMETER&gt;
 *       &lt;ASSETMETERID&gt;1&lt;/ASSETMETERID&gt;
 *       ...
 *    &lt;/ASSETMETER&gt;
 *    &lt;ASSETMETER&gt;
 *       &lt;ASSETMETERID&gt;2&lt;/ASSETMETERID&gt;
 *       ...
 *    &lt;/ASSETMETER&gt;
 *    ...
 * &lt;/ASSET&gt;
 * </pre>
 * 
 * <h2>Enterprise Service Application</h2>
 * <p>
 * The Enterprise Service Application manages enterprise services. An enterprise
 * service associates an operation and an object structure, in other words, an
 * enterprise service defines which operations can be performed on a given
 * object structure. These operations are requested to Maximo as XML messages
 * over HTTP.
 * </p>
 * <p>
 * Maximo supports a set of operations, but just some are interesting from the
 * connector&apos;s perspective:
 * </p>
 * <table border="1">
 * <tr>
 * <th>Operation</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>Create</td>
 * <td>Create new objects</td>
 * </tr>
 * <tr>
 * <td>Delete</td>
 * <td>Delete existing objects</td>
 * </tr>
 * <tr>
 * <td>Query</td>
 * <td>Query existing objects</td>
 * </tr>
 * <tr>
 * <td>Update</td>
 * <td>Update existing objects</td>
 * </tr>
 * </table>
 * <p>
 * <b>Note:</b> Every operation is performed over the object structure&apos;s
 * top-level MBO and cascaded to the related child MBOs.
 * </p>
 * <h2>External System Application</h2>
 * <p>
 * The External System Application manages external systems. An external system
 * identifies a specific external application involved in outbound or inbound
 * data synchronization with Maximo. It defines all the enterprise services
 * available to the external application.
 * </p>
 * <hr/>
 * <h1>Modes</h1>
 * <h2><a name="iterator">Iterator Mode</a></h2>
 * <h3>Required Parameters</h3>
 * <p>
 * {@link MxConnConfiguration#PARAM_MX_BASE_URL Base URL},
 * {@link MxConnConfiguration#PARAM_EXTSYS External System},
 * {@link MxConnConfiguration#PARAM_MAXOBJECT_OS MAXOBJECT Object Structure},
 * {@link MxConnConfiguration#PARAM_MAXOBJECT_ES_QUERY MAXOBJECT QUERY
 * Enterprise Service}, {@link MxConnConfiguration#PARAM_OBJECT_STRUCTURE Object
 * Structure}, {@link MxConnConfiguration#PARAM_MBO MBO},
 * {@link MxConnConfiguration#PARAM_ES_QUERY QUERY Enterprise Service},
 * {@link MxConnConfiguration#PARAM_PAGE_SIZE Page Size}
 * </p>
 * <h3>Optional Parameters</h3>
 * <p>
 * {@link MxConnConfiguration#PARAM_QUERY_CRITERIA Query Criteria}
 * </p>
 * <h3>About the MBO Parameter</h3>
 * <p>
 * Suppose Maximo returns the following XML as a result of a query operation on
 * the predefined object structure MXASSET:
 * </p>
 * 
 * <pre>
 * &lt;ASSET&gt;
 *    &lt;ASSETID&gt;1&lt;/ASSETID&gt;
 *    &lt;ASSETNUM&gt;A1&lt;/ASSETNUM&gt;
 *    ...
 *    &lt;ASSETMETER&gt;
 *       &lt;ASSETMETERID&gt;11&lt;/ASSETMETERID&gt;
 *       ...
 *    &lt;/ASSETMETER&gt;
 *    &lt;ASSETMETER&gt;
 *       &lt;ASSETMETERID&gt;12&lt;/ASSETMETERID&gt;
 *       ...
 *    &lt;/ASSETMETER&gt;
 *    ...
 * &lt;/ASSET&gt;
 * &lt;ASSET&gt;
 *    &lt;ASSETID&gt;2&lt;/ASSETID&gt;
 *    &lt;ASSETNUM&gt;A2&lt;/ASSETNUM&gt;
 *    ...
 *    &lt;ASSETMETER&gt;
 *       &lt;ASSETMETERID&gt;21&lt;/ASSETMETERID&gt;
 *       ...
 *    &lt;/ASSETMETER&gt;
 *    &lt;ASSETMETER&gt;
 *       &lt;ASSETMETERID&gt;22&lt;/ASSETMETERID&gt;
 *       ...
 *    &lt;/ASSETMETER&gt;
 *    ...
 * &lt;/ASSET&gt;
 * </pre>
 * 
 * <p>
 * Although the query returns 2 assets, each one with 2 asset meters, the
 * resulting Entry objects will depend on the value defined for the MBO
 * parameter.
 * </p>
 * <p>
 * If the MBO parameter is <i>ASSET</i>, the result would be <b>2 Entry
 * objects</b> with the following attribute names and values:
 * </p>
 * <table border="1">
 * <tr>
 * <th>ASSETID</th>
 * <th>ASSETNUM</th>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>A1</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>A2</td>
 * </tr>
 * </table>
 * <p>
 * On the other hand, if the MBO parameter is <i>ASSET@ASSETMETER</i>, the
 * result would be <b>4 Entry objects</b> with the following attribute names and
 * values:
 * </p>
 * <table border="1">
 * <tr>
 * <th>ASSETID</th>
 * <th>ASSETNUM</th>
 * <th>ASSETMETER@ASSETMETERID</th>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>A1</td>
 * <td>11</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>A1</td>
 * <td>12</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>A2</td>
 * <td>21</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>A2</td>
 * <td>22</td>
 * </tr>
 * </table>
 * <h2><a name="lookup">Lookup Mode</a></h2>
 * <h3>Required Parameters</h3>
 * <p>
 * {@link MxConnConfiguration#PARAM_MX_BASE_URL Base URL},
 * {@link MxConnConfiguration#PARAM_EXTSYS External System},
 * {@link MxConnConfiguration#PARAM_MAXOBJECT_OS MAXOBJECT Object Structure},
 * {@link MxConnConfiguration#PARAM_MAXOBJECT_ES_QUERY MAXOBJECT QUERY
 * Enterprise Service}, {@link MxConnConfiguration#PARAM_OBJECT_STRUCTURE Object
 * Structure}, {@link MxConnConfiguration#PARAM_MBO MBO},
 * {@link MxConnConfiguration#PARAM_ES_QUERY QUERY Enterprise Service},
 * {@link MxConnConfiguration#PARAM_PAGE_SIZE Page Size}
 * </p>
 * <h3>About the Link Criteria</h3>
 * <p>
 * In order to find a specific record inside Maximo, the Link Criteria must be
 * provided with attributes that uniquely identify the record.
 * </p>
 * <p>
 * Examples
 * </p>
 * <p>
 * The following attributes uniquely identify an asset in Maximo.
 * </p>
 * <table border="1">
 * <tr>
 * <th>Attribute Name</th>
 * <th>Value</th>
 * </tr>
 * <tr>
 * <td>ASSETNUM</td>
 * <td>1001</td>
 * </tr>
 * <tr>
 * <td>SITEID</td>
 * <td>BEDFORD</td>
 * </tr>
 * </table>
 * <p>
 * The following attributes uniquely identify an asset&apos;s meter in Maximo.
 * </p>
 * <table border="1">
 * <tr>
 * <th>Attribute Name</th>
 * <th>Value</th>
 * </tr>
 * <tr>
 * <td>ASSETNUM</td>
 * <td>1001</td>
 * </tr>
 * <tr>
 * <td>SITEID</td>
 * <td>BEDFORD</td>
 * </tr>
 * <tr>
 * <td>ASSETMETER@METERNAME</td>
 * <td>RUNHOURS</td>
 * </tr>
 * </table>
 * <h2><a name="addonly">AddOnly Mode</a></h2>
 * <h3>Required Parameters</h3>
 * <p>
 * {@link MxConnConfiguration#PARAM_MX_BASE_URL Base URL},
 * {@link MxConnConfiguration#PARAM_EXTSYS External System},
 * {@link MxConnConfiguration#PARAM_MAXOBJECT_OS MAXOBJECT Object Structure},
 * {@link MxConnConfiguration#PARAM_MAXOBJECT_ES_QUERY MAXOBJECT QUERY
 * Enterprise Service}, {@link MxConnConfiguration#PARAM_OBJECT_STRUCTURE Object
 * Structure}, {@link MxConnConfiguration#PARAM_MBO MBO},
 * {@link MxConnConfiguration#PARAM_ES_CREATE CREATE Enterprise Service},
 * {@link MxConnConfiguration#PARAM_ES_UPDATE UPDATE Enterprise Service}
 * </p>
 * <h3>About the MBO Parameter</h3>
 * <p>
 * If the {@link MxConnConfiguration#PARAM_MBO MBO parameter} targets the object
 * structure&apos;s top-level MBO, then the connector will use the enterprise
 * service defined by the {@link MxConnConfiguration#PARAM_ES_CREATE CREATE
 * Enterprise Service parameter}.
 * </p>
 * <p>
 * Nonetheless, if the {@link MxConnConfiguration#PARAM_MBO MBO parameter}
 * targets a child MBO at any level of the object structure, then the connector
 * will use the enterprise service defined by the
 * {@link MxConnConfiguration#PARAM_ES_UPDATE UPDATE Enterprise Service
 * parameter}. Besides, the key attributes of all MBOs until the object
 * structure&apos;s top-level MBO must be provided and must reference existent
 * records, except the MBO target by the {@link MxConnConfiguration#PARAM_MBO
 * MBO parameter} (the one to be created).
 * </p>
 * <p>
 * For example, the predefined object structure MXASSET exposes the ASSET and
 * the ASSETMETER MBOs. So, to create a new meter for an asset, a work entry
 * with, at least, the attributes below must be provided:
 * </p>
 * <table border="1">
 * <tr>
 * <th>Attribute Name</th>
 * <th>Value</th>
 * </tr>
 * <tr>
 * <td>ASSETNUM</td>
 * <td>1001</td>
 * </tr>
 * <tr>
 * <td>SITEID</td>
 * <td>BEDFORD</td>
 * </tr>
 * <tr>
 * <td>ASSETMETER@METERNAME</td>
 * <td>RUNHOURS</td>
 * </tr>
 * </table>
 * <p>
 * ASSETNUM and SITEID identify an existent asset and ASSETMETER@METERNAME is
 * the name of the new meter.
 * </p>
 * <h2><a name="delete">Delete Mode</a></h2>
 * <h3>Required Parameters</h3>
 * <p>
 * {@link MxConnConfiguration#PARAM_MX_BASE_URL Base URL},
 * {@link MxConnConfiguration#PARAM_EXTSYS External System},
 * {@link MxConnConfiguration#PARAM_MAXOBJECT_OS MAXOBJECT Object Structure},
 * {@link MxConnConfiguration#PARAM_MAXOBJECT_ES_QUERY MAXOBJECT QUERY
 * Enterprise Service}, {@link MxConnConfiguration#PARAM_OBJECT_STRUCTURE Object
 * Structure}, {@link MxConnConfiguration#PARAM_ES_DELETE DELETE Enterprise
 * Service}, {@link MxConnConfiguration#PARAM_ES_UPDATE UPDATE Enterprise
 * Service}
 * </p>
 * <h3>About the MBO Parameter</h3>
 * <p>
 * If the {@link MxConnConfiguration#PARAM_MBO MBO parameter} targets the object
 * structure&apos;s top-level MBO, then the connector will use the enterprise
 * service defined by the {@link MxConnConfiguration#PARAM_ES_DELETE DELETE
 * Enterprise Service parameter}.
 * </p>
 * <p>
 * Nonetheless, if the {@link MxConnConfiguration#PARAM_MBO MBO parameter}
 * targets a child MBO at any level of the object structure, then the connector
 * will use the enterprise service defined by the
 * {@link MxConnConfiguration#PARAM_ES_UPDATE UPDATE Enterprise Service
 * parameter}. Besides, the key attributes of all MBOs until the object
 * structure&apos;s top-level MBO must be provided and must reference existent
 * records.
 * </p>
 * <p>
 * For example, the predefined object structure MXASSET exposes the ASSET and
 * the ASSETMETER MBOs. So, to delete an asset&apos;s meter, a work entry with
 * the attributes below must be provided:
 * </p>
 * <table border="1">
 * <tr>
 * <th>Attribute Name</th>
 * <th>Value</th>
 * </tr>
 * <tr>
 * <td>ASSETNUM</td>
 * <td>11430</td>
 * </tr>
 * <tr>
 * <td>SITEID</td>
 * <td>BEDFORD</td>
 * </tr>
 * <tr>
 * <td>ASSETMETER@METERNAME</td>
 * <td>RUNHOURS</td>
 * </tr>
 * </table>
 * <p>
 * ASSETNUM and SITEID identify an existent asset and ASSETMETER@METERNAME
 * identifies the meter to be deleted.
 * </p>
 * <h2><a name="update">Update Mode</a></h2>
 * <h3>Required Parameters</h3>
 * <p>
 * {@link MxConnConfiguration#PARAM_MX_BASE_URL Base URL},
 * {@link MxConnConfiguration#PARAM_EXTSYS External System},
 * {@link MxConnConfiguration#PARAM_MAXOBJECT_OS MAXOBJECT Object Structure},
 * {@link MxConnConfiguration#PARAM_MAXOBJECT_ES_QUERY MAXOBJECT QUERY
 * Enterprise Service}, {@link MxConnConfiguration#PARAM_OBJECT_STRUCTURE Object
 * Structure}, {@link MxConnConfiguration#PARAM_MBO MBO},
 * {@link MxConnConfiguration#PARAM_ES_QUERY QUERY Enterprise Service},
 * {@link MxConnConfiguration#PARAM_ES_CREATE CREATE Enterprise Service},
 * {@link MxConnConfiguration#PARAM_ES_UPDATE UPDATE Enterprise Service}
 * </p>
 * <p>
 * No further observation.
 * </p>
 * 
 * @since 7.1
 * @see MxConnConfiguration
 * @see AbstractMxConnMode
 * @see MxConnAddOnly
 * @see MxConnDelete
 * @see MxConnIterator
 * @see MxConnLookup
 * @see MxConnUpdate
 */
public class SimpleTpaeIFConnector extends Connector {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	public static final String PROPERTIES_FILE = "simpletpaeifconnector";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static final ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);

	protected MxConnConfiguration cfg;

	protected MxConnIterator connIterator;

	private MxConnAddOnly connAddOnly;

	private MxConnUpdate connUpdate;

	protected MxConnLookup connLookup;

	private MxConnDelete connDelete;

	private MxConnFunctions fc;

	/**
	 * The name of the Connector
	 */
	private static final String myName = "Simple TPAE IF Connector";

	/**
	 * Log object for logging in all classes used by the TPAE IF Connector.
	 */
	protected Log proxyLog;

	/**
	 * Default constructor.
	 */
	public SimpleTpaeIFConnector() {
		setName(myName);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE, ConnectorConfig.DELETE_MODE, ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.LOOKUP_MODE, ConnectorConfig.UPDATE_MODE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(final Object obj) throws Exception {
		Trace.entrymin(this, "initialize", obj);

		super.initialize(obj);

		proxyLog = getLog();
		if (proxyLog == null) {
			proxyLog = new Log(PROPERTIES_FILE);
		}

		cfg = new MxConnConfiguration(proxyLog);
		
		//We dont need to load parameters from external property files. 
		//User can provide all configuration parameters needed for the connector in Connector's configuration panel itself. Hence commenting out the below line.
		//cfg.loadParameters();

		for (final String key : MxConnConfiguration.TDI_PARAMETER_KEYS) {
			cfg.setParameter(key, getParam(key));
		}

		debug(resHash.getString("MXCONN.CONFIG.PARAMS", cfg.toString()));

		Trace.exitmin(this, "initialize");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectEntries() throws MxConnectorException {
		Trace.entrymin(this, "selectEntries");

		if (connIterator == null) {
			connIterator = new MxConnIterator(cfg, proxyLog);
			debug(resHash.getString("MXCONN.NEW.MXCONNITERATOR.CREATED"));
		}

		debug(resHash.getString("MXCONN.SELECT.ENTRIES"));
		connIterator.executeQuery();

		Trace.exitmin(this, "selectEntries");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Entry getNextEntry() throws MxConnectorException {
		Trace.entrymin(this, "getNextEntry");

		if (connIterator == null) {
			selectEntries();
		}
		Entry entry = connIterator.getNext();

		Trace.exitmin(this, "getNextEntry", entry);

		return entry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putEntry(final Entry newEntry) throws MxConnectorException {
		Trace.entrymin(this, "putEntry", newEntry);

		if (connAddOnly == null) {
			connAddOnly = new MxConnAddOnly(cfg, proxyLog);
			debug(resHash.getString("MXCONN.NEW.MXCONNADDONLY.CREATED"));
		}

		debug(resHash.getString("MXCONN.CREATE.ENTRY"));
		connAddOnly.create(newEntry);

		Trace.exitmin(this, "putEntry");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Entry findEntry(final SearchCriteria searchCriteria) throws Exception {
		Trace.entrymin(this, "findEntry", searchCriteria);

		if (connLookup == null) {
			connLookup = new MxConnLookup(cfg, proxyLog);
			debug(resHash.getString("MXCONN.NEW.MXCONNLOOKUP.CREATED"));
		}

		debug(resHash.getString("MXCONN.EXECUTE.SEARCH"));
		connLookup.setSearchCriteria(searchCriteria);
		connLookup.executeQuery();

		clearFindEntries();
		for (Entry e = connLookup.getNext(); e != null; e = connLookup.getNext()) {
			if (EntryFilter.match(searchCriteria, e)) {
				addFindEntry(e);
			}
		}
		Entry entry = getFindEntryCount() == 1 ? getFirstFindEntry() : null;

		Trace.exitmin(this, "findEntry", entry);

		return entry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modEntry(final Entry newEntry, final SearchCriteria searchCriteria, final Entry oldEntry)
			throws MxConnectorException {
		Trace.entrymin(this, "modEntry", newEntry, oldEntry);

		if (connUpdate == null) {
			connUpdate = new MxConnUpdate(cfg, proxyLog);
			debug(resHash.getString("MXCONN.NEW.MXCONNUPDATE.CREATED"));
		}

		debug(resHash.getString("MXCONN.UPDATE.ENTRY"));
		connUpdate.update(newEntry, oldEntry);

		Trace.exitmin(this, "modEntry");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteEntry(final Entry entry, final SearchCriteria sc) throws MxConnectorException {
		Trace.entrymin(this, "deleteEntry", entry, sc);

		if (connDelete == null) {
			connDelete = new MxConnDelete(cfg, proxyLog);
			debug(resHash.getString("MXCONN.NEW.MXCONNDELETE.CREATED"));
		}

		debug(resHash.getString("MXCONN.DELETE.ENTRY"));
		connDelete.delete(entry);

		Trace.exitmin(this, "deleteEntry");
	}

	/**
	 * This method populates the input and output map of the connector with the
	 * schema of the chosen MBO. All unique attributes from the schema are
	 * marked as "Required" in the CE.
	 * <p>
	 * Note: This method does not return a Vector of entries because the schema
	 * is directly updated.
	 */
	@Override
	public Object querySchema(Object obj) throws Exception {
		Trace.entrymin(this, "querySchema", obj);

		Schema schema = cfg.getSchema();
		SchemaElement mbo = schema.getMboByName(cfg.getMbo());
		SchemaElement parent = mbo.getParent();

		SchemaConfig inpSchema = ((ConnectorConfig) getConfiguration()).getSchema(true);
		SchemaConfig outSchema = ((ConnectorConfig) getConfiguration()).getSchema(false);

		while (!parent.equals(schema.getMos())) {
			for (final SchemaElement e : parent.getChildren()) {
				if (!e.isMboDefinition()) {
					addSchemaItem(inpSchema, e.getPathRelativeTo(schema.getRootMbo()), e);
					addSchemaItem(outSchema, e.getPathRelativeTo(schema.getRootMbo()), e);
				}
			}
			parent = parent.getParent();
		}

		for (final SchemaElement e : mbo.getChildren()) {
			if (!e.isMboDefinition()) {
				addSchemaItem(inpSchema, e.getPathRelativeTo(schema.getRootMbo()), e);
				addSchemaItem(outSchema, e.getPathRelativeTo(schema.getRootMbo()), e);
			}
		}

		Trace.exitmin(this, "querySchema");

		return null;
	}

	/**
	 * Returns the object that provides utility functions.
	 * 
	 * @return object that provides utility functions
	 */
	public MxConnFunctions getFc() {
		if (fc == null) {
			fc = new MxConnFunctions(cfg, proxyLog);
		}
		return fc;
	}

	/**
	 * Adds a SchemaItemConfig to an existing Schema object. This object can be
	 * either the input or the output map of the connector.
	 * 
	 * @param schema
	 *            Schema object
	 * @param name
	 *            name of item to add
	 * @param se
	 *            SchemaElement object
	 * @return the added SchemaItemConfig object or <code>null</code> if it
	 *         already exists
	 * @throws Exception
	 */
	private SchemaItemConfig addSchemaItem(SchemaConfig schema, String name, SchemaElement se) throws Exception {

		// do not add schema item twice
		if (schema.getItem(name) == null) {
			SchemaItemConfig sic = schema.newItem(name);
			sic.setExternalSyntax(se.getClassName());
			if (se.isUniqueKey()) {
				// When setting the presence flag the CE concatenates [1..1] to
				// the attribute name. These numbers represent the minimum and
				// maximum occurrence.
				sic.setPresenceFlag("Required");
			}
			return sic;
		}
		return null;
	}

	/**
	 * Clears the cache of the schema.
	 */
	public void clearSchemaCache() {
		Schema.clearSchemaCache();
	}

	/**
	 * Returns a list of the available MBOs in the object structure.
	 * 
	 * @return list of the available MBOs in the object structure
	 * @throws MxConnectorException
	 *             if any of the required parameters is missing (Base URL,
	 *             External System, MaxObject/MaxAttribute Object Structure,
	 *             MaxObject/MaxAttribute QUERY Enterprise Service, Object
	 *             Structure) or if it is not possible to obtain the schema data
	 * @see <a href="#mbo">MBO Parameter</a>
	 */
	@SuppressWarnings("unchecked")
	public Vector getMboList() throws MxConnectorException {
		final Schema s = cfg.getSchema();
		return new Vector(s.getMboNameList());
	}

	/**
	 * This method extracts additional information about a Maximo exception. It
	 * is particularly useful to call from an error hook since the
	 * predefined bean "error" contains an entry with the exception .<br>
	 * <b>For example:</b>
	 * 
	 * <pre>
	 * task.logmsg(&quot;ERROR&quot;, &quot;An exception occurred.&quot;);
	 * mxConn.connector.extractMaximoException(error);
	 * 
	 * // print detailed information about error
	 * task.dumpEntry(error);
	 * </pre>
	 * 
	 * @param error
	 *            an Entry object containing the exception in its "exception"
	 *            attribute.
	 */
	public void extractMaximoException(Entry error) {
		Object o = error.getObject("exception");
		Throwable ex = null;

		if (o instanceof Throwable) {
			ex = (Throwable) o;
		}

		while (ex != null && !(ex instanceof MxConnectorException)) {
			ex = ex.getCause();
		}

		if (ex == null) {
			proxyLog.debug(resHash.getString("MXCONN.MAXIMO.EXCEPION.NOT.FOUND"));
		}

		if (ex instanceof MxConnIOException) {
			MxConnIOException ioe = (MxConnIOException) ex;
			error.setAttribute("targetUrl", ioe.getTargetUrl());

			if (ioe instanceof MxConnHttpException) {
				MxConnHttpException httpe = (MxConnHttpException) ioe;

				error.setAttribute("responseCode", httpe.getResponseCode());
				error.setAttribute("responseMessage", httpe.getResponseMessage());
				error.setAttribute("body", httpe.getBody());

			} else if (ioe instanceof MxConnTimeoutException) {
				error.setAttribute("timeout", ((MxConnTimeoutException) ex).getTimeout());
			}

		} else if (ex instanceof MxConnExcedentSizeException) {
			MxConnExcedentSizeException ese = (MxConnExcedentSizeException) ex;

			error.setAttribute("attributeName", ese.getAttributeName());
			error.setAttribute("maxSize", ese.getMaxSize());
			error.setAttribute("size", ese.getSize());

		} else if (ex instanceof MxConnXmlParsingException) {
			error.setAttribute("xml", ((MxConnXmlParsingException) ex).getXml());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParam(final String key, final String value) {
		super.setParam(key, value);
		if (cfg != null)
			cfg.setParameter(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void terminate() throws Exception {
		Trace.entrymin(this, "terminate");

		super.terminate();

		connIterator = null;
		connAddOnly = null;
		connDelete = null;
		connLookup = null;
		connUpdate = null;
		fc = null;

		cfg.clear();

		Trace.exitmin(this, "terminate");
	}

	/**
	 * @return ResourceHash object holding messages for this connector.
	 */
	public static ResourceHash getResHash() {
		return resHash;
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I%, 20%E%";
	}
	
	@Override
	public void extractExceptionInformation(Entry error) {
	        extractMaximoException(error);	
    }
}
