/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.StringTokenizer;
import com.tivoli.tec.event_delivery.EDException;
import com.tivoli.tec.event_delivery.IEventProcessing;
import com.tivoli.tec.event_delivery.TECAgent;
import com.tivoli.tec.event_delivery.TECEvent;

/**
 * A connector for sending and receiving messages from/to IBM Tivoli Enterprise
 * Console.
 * 
 * @since 7.0
 */
public class EIFConnector extends Connector {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static final ResourceHash resHash = ResourceHash
			.getHash("eifconnector");

	/**
	 * Frontend for the TEC framework.
	 */
	private TECAgent agent = null;

	/**
	 * The helper class used for asynchronous messages handling.
	 */
	private EventProcessing processor = null;

	/**
	 * The time to wait for an event to be delivered. (-1 - wait forever, 0 -
	 * get next and don't wait, N - number of seconds to wait before returning
	 * null)
	 */
	private long waitTimeout = 0;

	/**
	 * The time to wait on closure.
	 */
	private int waitOnClose = 120;

	/**
	 * Holds the type the TECAgent is initialized into.
	 */
	private int agentMode = TECAgent.RECEIVER_MODE;

	/**
	 * List of EIF Configuration parameters or path to the configuration file.
	 */
	private StringBuilder eifConfig = null;

	/**
	 * Constructs an instance of this connector.
	 */
	public EIFConnector() {
		super();
		setName("EIFConnector");
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.ADDONLY_MODE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(Object o) throws Exception {
		eifConfig = new StringBuilder();

		String param = getParam("eifConfFilePath");
		if (param != null && param.trim().length() > 0) {
			eifConfig.append(param);
		}

		if (o != null && ConnectorConfig.ITERATOR_MODE.equals(o.toString())) {
			agentMode = TECAgent.RECEIVER_MODE;
		} else {
			agentMode = TECAgent.SENDER_MODE;
		}

		agent = new TECAgent(getConfigReader(), agentMode, Boolean
				.parseBoolean(getParam("eif.breakOnError")));

		param = getParam("eifWaitTimeout");
		if (param != null && param.trim().length() > 0) {
			waitTimeout = Long.parseLong(param);
		}

		param = getParam("eifWaitOnClose");
		if (param != null && param.trim().length() > 0) {
			waitOnClose = Integer.parseInt(param);
		}

		debug(resHash.getString("EIF.CONN.SUCCESS.INIT"));
	}

	/**
	 * Creates a java.io.Reader object based on the provided configuration
	 * (string reader or file reader)
	 * 
	 * @return Reader object for configuration
	 * @throws FileNotFoundException
	 */
	private Reader getConfigReader() throws FileNotFoundException {
		if (eifConfig.charAt(0) == '!')
			return new StringReader(eifConfig.substring(1));
		else
			return new FileReader(eifConfig.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectEntries() throws Exception {

		processor = new EventProcessing(getLog(), debugMode());

		if (agent != null) {
			agent.registerListener(processor);
		}
	}

	/**
	 * The returned entry has the following structure:
	 * 
	 * <pre>
	 * Entry
	 *    +--&gt;className:String
	 *    +--&gt;{slotName}:String
	 *    +--&gt;{slotName}:String
	 *    ...{moreSlots}...
	 * </pre>
	 * 
	 * @return an entry with the specified above structure or null.
	 */
	@Override
	public Entry getNextEntry() throws Exception {
		Entry result = null;

		try {
			if (waitTimeout > 0) {
				debug(resHash.getString("EIF.CONN.WAITING.FOR.EVENT.SEC",
						waitTimeout));
				result = processor.poll(waitTimeout, TimeUnit.SECONDS);
			} else if (waitTimeout == 0) {
				debug(resHash.getString("EIF.CONN.WAITING.FOR.EVENT.NO"));
				result = processor.poll();
			} else {
				debug(resHash.getString("EIF.CONN.WAITING.FOR.EVENT.FOREVER"));
				result = processor.take();
			}
		} catch (InterruptedException ie) {
			// Someone interrupted the listener...
			// we are probably shutting down...
			result = null;
		}

		if (processor.getErrorMsg() != null) {	
			terminate();
			if (result instanceof EndEntry) {
				throw new EDException(processor.getErrorMsg());
			}
		}
		return result;
	}

	/**
	 * Sends an Entry to the remote server.
	 * 
	 * @param entry
	 *            the event to send. The entry should comply with the following
	 *            structure. If the attribute event is present (not null) then
	 *            it will be sent to the remote server, otherwise the className
	 *            and slots attributes will be used to compile an event.
	 * 
	 *            <pre>
	 * Entry
	 *    +--&gt;className:String
	 *    +--&gt;{slotName}:String
	 *    +--&gt;{slotName}:String
	 *    ...{moreSlots}...
	 * </pre>
	 * 
	 */
	@Override
	public void putEntry(Entry entry) throws Exception {
		if (entry == null) {
			return;
		}
		TECEvent event = new TECEvent();
		Collection<String> attrNames = entry.getAttributeCollection();

		if (entry.getString("className") != null) {
			event.setClassName(entry.getString("className"));
			attrNames.remove("className");
		}

		for (String attr : attrNames) {
			event.setSlot(attr, entry.getString(attr));
		}

		String serializedEvent = event.toString(false);
		int errCode = 0;

		debug(resHash.getString("EIF.CONN.SEND.RAW.MSG", serializedEvent));

		if ((errCode = agent.sendEvent(serializedEvent)) < 0) {
			throw new Exception(resHash.getString("EIF.CONN.ERROR.SEND.EVENT",
					errCode));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void terminate() throws Exception {

		if (agent != null) {
			try {
				agent.disconnect(waitOnClose);
			} catch (NullPointerException npe) {
				// For some reason the agent throws a Null Pointer when
				// disconnecting so just ignore that.
			}
			agent = null;
		}
		debug(resHash.getString("EIF.CONN.SUCCESS.TERM"));
	}

	/**
	 * Query the Schema.
	 * 
	 * @return If the <code>eifSchemaFile</code> file is <code>null</code>,
	 *         returns the schema for the current Entry; else returns schema
	 *         containing destination field names from the mapping file used by
	 *         the Gateway for Tivoli EIF.
	 */
	public Object querySchema(Object source) {
		Entry msgEntry = new Entry();
		msgEntry.setAttribute("name", "msg");
		msgEntry.setAttribute("syntax", "java.lang.String");

		Vector<Object> schema = new Vector<Object>();
		schema.add(msgEntry);

		// For old configs still using this parameter
		if (getParam("eifSchemaFile") != null
				&& getParam("eifSchemaFile").length() > 0) {
			try {
				updateSchemaFromMap(getParam("eifSchemaFile"), schema);
			} catch (Exception e) {
				logmsg(resHash.getString("EIF.CONN.CANNOT.RETRIEVE.SCHEMA", e
						.getMessage()));
			}
		}
		return (schema);
	}

	/**
	 * This method updates the schema vector based on a mapping file.
	 * 
	 *<pre>
	 * CREATE MAPPING StatusMap
	 * 	(
	 * 		'identifier'	   = 	'@Identifier',
	 * 		'server_identifier'=	'@ServerName' + &quot; &quot; + TO_STRING('@ServerSerial'),
	 * 		'sub_source'	   =	'@AlertKey'		ON INSERT ONLY,
	 * 		'sub_origin'	   =	'@AlertGroup'		ON INSERT ONLY,
	 * 		'msg'		   =	'@Summary'		ON INSERT ONLY,
	 * 		'origin'	   =	'@Node'			ON INSERT ONLY,
	 * 		'node_alias'	   =	'@NodeAlias'		ON INSERT ONLY
	 * 									NOTNULL '@Node',
	 * 		'manager'	   =	'@Manager'		ON INSERT ONLY,
	 * 		'source'	   =	'@Agent'		ON INSERT ONLY,
	 * 		'severity'	   =	'@Severity',
	 * 		'date'		   =	'@LastOccurrence'	ON INSERT ONLY,
	 * 		'omnibus_last_modified_time' = '@InternalLast'	ON INSERT ONLY );
	 * </pre>
	 * 
	 * @param path
	 *            path to the file
	 * @throws Exception
	 *             if file could not be found, read or closed.
	 */
	public void updateSchemaFromMap(String path, Vector<Object> schema)
			throws Exception {

		int result = -1;
		char[] buf = new char[1024];
		StringBuilder str = new StringBuilder();
		FileReader fr = new FileReader(path);
		
		try {
			while( (result = fr.read(buf)) != -1 ) {
				str.append(buf, 0, result);
			}	
		} finally {
			fr.close();
		}

		Entry e = null;
		
		// matches all strings looking like this one:
		// 'one_or_more_non_white_space_characters'<zero or more white
		// characters>=
		Pattern p = Pattern.compile("'(\\S+)'\\s*=", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(str);
		while (m.find()) {
			e = new Entry();
			e.setAttribute("name", m.group(1));
			e.setAttribute("syntax", "java.lang.String");
			schema.add(e);
		}
	}

	/**
	 * @return the version of this connector as String.
	 */
	public String getVersion() {
		return "1.0-di7.1.1 %I%, 20%E%";
	}

	/**
	 * This class implements the interface IEventProcessing used for receiving
	 * event messages asynchronously.
	 * 
	 * @since 7.0
	 */
	private static class EventProcessing implements IEventProcessing {
		/**
		 * Copyright.
		 */
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		/**
		 * Holds the last error that has occurred.
		 */
		private String errorMsg = null;

		/**
		 * The queue holding the received entries and implementing the blocking
		 * functionality.
		 */
		private BlockingQueue<Entry> entries = null;

		/**
		 * The place to log to.
		 */
		private Log log;

		/**
		 * switch debugging on/off.
		 */
		private boolean debug;

		/**
		 * Constructs the an event processor.
		 * 
		 * @param log
		 *            the place where any massages will be output
		 * @param debug
		 *            switch the debugging on/off.
		 */
		public EventProcessing(Log log, boolean debug) {
			// This queue is a second message buffer... it is also useful for
			// synchronization/delaying purposes.
			entries = new ArrayBlockingQueue<Entry>(100);
			this.log = log;
			this.debug = debug;
		}

		/**
		 * See {@link BlockingQueue#take()}
		 * 
		 * @return the next element from the queue.
		 * @throws InterruptedException
		 *             if interpreted while waiting.
		 */
		public Entry take() throws InterruptedException {
			return entries.take();
		}

		/**
		 * See {@link BlockingQueue#poll()}
		 * 
		 * @return the next element from the queue.
		 */
		public Entry poll() {
			return entries.poll();
		}

		/**
		 * See {@link BlockingQueue#poll(long, TimeUnit)}
		 * 
		 * @return the next element from the queue.
		 * @throws InterruptedException
		 *             if interpreted while waiting.
		 */
		public Entry poll(long timeout, TimeUnit unit)
				throws InterruptedException {
			return entries.poll(timeout, unit);
		}

		/**
		 * The call-back method that receives notifications when an event is
		 * available to the underlying framework.
		 */
		@SuppressWarnings("unchecked")
		public boolean onMessage(String msg) {
			StringTokenizer tokens = new StringTokenizer(msg, "" + (char) 1);
			long tokenCount = 0;

			debug(resHash.getString("EIF.CONN.GET.RAW.MSG", msg));
			debug(resHash.getString("EIF.CONN.RAW.MSG.TOKENS", tokens
					.countTokens()));

			while (tokens.hasMoreTokens()) {
				tokenCount++;
				Entry entry = new Entry();
				String token = tokens.nextToken();
				TECEvent event = new TECEvent();

				if (!event.init(token)) {
					errorMsg = resHash.getString("EIF.CONN.ERROR.PARSING.MSG",
							token.replaceAll("\\n", ""));
					// put a dummy entry to wake the connector up.
					try {
						entries.put(new EndEntry());
					} catch (InterruptedException ie) {
						return false;
					}
					return false;
				}

				entry.setAttribute("className", event.className());

				for (Enumeration<String> e = event.slots(); e.hasMoreElements();) {
					String slot = e.nextElement();
					entry.setAttribute(slot, event.getSlot(slot));
				}
				try {
					entries.put(entry);
				} catch (InterruptedException e) {
					return false;
				}
			}
			return true;
		}

		/**
		 * Provide access to the error massage explaining the error that has
		 * occurred in the background thread.
		 * 
		 * @return the error message.
		 */
		protected String getErrorMsg() {
			return errorMsg;
		}

		/**
		 * Logs a debug message to provided Log object. If the debug flag is not
		 * raised then the method returns.
		 * 
		 * @param msg
		 *            the message to log.
		 */
		private void debug(String msg) {
			if (debug && log != null) {
				log.debug(msg);
			}
		}
	}
	
	/**
	 * Marker class for last entry in the blocking queue.
	 */
	private static class EndEntry extends Entry {

		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 6298979363068011953L;
	}
}

