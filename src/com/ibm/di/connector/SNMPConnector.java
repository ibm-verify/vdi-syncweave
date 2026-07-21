/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;

import java.util.Arrays;
import java.util.Vector;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;
import com.tivoli.snmp.data.IPAddress;
import com.tivoli.snmp.SnmpAPI;
import com.tivoli.snmp.SnmpMetaPDU;
import com.tivoli.snmp.SnmpPDU;
import com.tivoli.snmp.SnmpSession;
import com.tivoli.snmp.SnmpTrap;
import com.tivoli.snmp.SnmpTrapSession;
import com.tivoli.snmp.SnmpV1API;
import com.tivoli.snmp.SnmpVarBind;
import com.tivoli.snmp.TrapFilter;
import com.tivoli.snmp.TrapListener;
import com.tivoli.snmp.TrapReceiver;
import com.tivoli.snmp.data.Counter;
import com.tivoli.snmp.data.Null;
import com.tivoli.snmp.data.OID;
import com.tivoli.snmp.data.OctetString;
import com.tivoli.snmp.data.TimeTicks;

/**
 * The SNMPConnector implements get/set/walk and trap-receive operations by
 * means of the IBM Tivoli SNMP Stack.
 * <p>
 * The connector can operate in two modes: Client and Trap Receiver - Client
 * Mode
 * <p>
 * In client mode you can use it in iterator mode where the connector will send
 * a getnext request to the snmp agent and return one entry for each OID
 * returned by the agent. In Lookup mode the connector will perform a get
 * request returning the oid/value for the requested oid. The link criteria
 * specifies: oid and optionally server, port and version.
 * <p> - Trap Receiver Mode
 * <p>
 * In trap receiver mode the connector can only be used in Iterator mode. The
 * connector will listen for incoming snmp traps and return each as an entry to
 * the caller. Both V1, V2c and V3 traps are handled.
 */
public class SNMPConnector extends Connector implements TrapListener, TrapFilter {

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = 597542369339441385L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "snmpconnector";

	/**
	 * Component name.
	 */
	private static final String myName = "Metamerge SNMPConnector";

	/**
	 * Used for receiving traps
	 */
	private transient TrapReceiver trapReceiver;

	/**
	 * Vector of traps
	 */
	private Vector<Entry> trapList = new Vector<Entry>();

	/**
	 * Trap enabled flag.
	 */
	private boolean snmpTrapEnabled = false;

	/**
	 * Timeout in milliseconds ( 0 = forever )
	 */
	private long trapTimeout = 0;

	/**
	 * Used for iteration ( e.g. getnext requests )
	 */
	private SnmpPDU walkPdu;

	/**
	 * SNMP OID
	 */
	private String walkOID;

	/**
	 * SNMP session. Used in various contexts
	 */
	private transient SnmpSession session;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	private transient SnmpTrapSession snmpTrapSession;
	
	private final static String IBM_OID = "1.3.6.1.4.1.2";
	
	/**
	 * The community string to use
	 */
	private String community;
	
	/**"
	 * Constructor for the SNMPConnector object
	 */
	public SNMPConnector() {
		super();
		setName(myName);
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.ADDONLY_MODE, ConnectorConfig.LOOKUP_MODE, });
	}

	/**
	 * Version information.
	 * @return version information
	 */
	public String getVersion() {
		return "2.3-di7.1.1 %I%, 20%E%";
	}

	/**
	 * Gets the nextEntry attribute of the SNMPConnector object
	 * 
	 * @return The nextEntry value
	 * 
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	@Override
	public Entry getNextEntry() throws Exception {

		if (snmpTrapEnabled) {
			if (emptyQueue(trapList, trapTimeout))
				return null;
			return returnEntry(trapList.remove(0));
		} else if (walkPdu != null) {
			return getNextSNMP();
		} else {
			return sendGetRequest(session, getParam("snmpOID"));
		}
	}

	/**
	 * Checks the type of the given object. If it is an Entry, it casts it to an
	 * Entry and returns it.
	 * 
	 * @param obj String, Entry or Exception.
	 * @return returns null if a String is passed. Otherwise if an Entry is
	 *         passed returns the Object cast to Entry.
	 * @throws Exception
	 *             If the object is an Exception or is not an instance of the
	 *             above classes, an Exception is thrown.
	 */
	public Entry returnEntry(Object obj) throws Exception {
		Trace.entrymin(this, "returnEntry", obj);
		if (obj instanceof String) {
			Trace.exitmax(this, "returnEntry");
			return null;
		} else if (obj instanceof Exception)
			throw (Exception) obj;
		else if (obj instanceof Entry) {
			Trace.exitmax(this, "returnEntry", (Entry) obj);
			return (Entry) obj;
		} else {
			throw new Exception(sResHash.getString(
					"CONNECTOR.SNMP.UNKNOWNCLASS.EXCEPTION", obj));
		}
	}

	/**
	 * This methods initializes the SNMP API library, reads the Connector
	 * configurations parameters, subscribes for SNMP Traps and opens an SNMP
	 * session.
	 * 
	 * @param o
	 *            An object the AssemblyLine passes to the Connector on init
	 * 
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	@Override
	public void initialize(Object o) throws Exception {

		// Initialize SNMP stack
		SnmpV1API.initialize(-1);

		int trapPort = 162;
		if (hasConfigValue("snmpTrapPort"))
			trapPort = Integer.parseInt(getParam("snmpTrapPort"));

		// Check mode and create trap listener if requested
		if ("Trap Listener".equals(getParam("snmpMode"))) {
			snmpTrapEnabled = true;

			// Trap timeout
			if (hasConfigValue("snmpTrapTimeout"))
				trapTimeout = Integer.parseInt(getParam("snmpTrapTimeout"));

			trapReceiver = new TrapReceiver(trapPort);
			trapReceiver.subscribe(this, this);
			Thread t = new Thread(trapReceiver);
			t.start();
			return;
		}

		String host = getParam("snmpGetSetHost");

		community = getParam("snmpCommunity");
		if (community == null || community.length() == 0)
			community = "public";

		if ("Trap Sender".equals(getParam("snmpMode"))) {
			SnmpAPI.initialize(-1);
			snmpTrapSession = SnmpTrapSession.open(host, trapPort);
			return;
		}

		// Create contexts for V1 and V2C

		int port = 161;
		if (hasConfigValue("snmpGetSetPort"))
			port = Integer.parseInt(getParam("snmpGetSetPort"));
		int timeout = 5000;
		if (hasConfigValue("snmpWalkTimeout"))
			timeout = Integer.parseInt(getParam("snmpWalkTimeout"));

		session = SnmpSession.open(host, community, community, 4, timeout, port);

		// Detailed log?
		if (debugMode())
			SnmpV1API.startTrace(System.out);

		//

	}

	/**
	 * This method unsubscribes from SNMP Trap events, terminates the SNMP API
	 * library, i.e. this method cleans up.
	 */
	@Override
	public void terminate() {
		if (trapReceiver != null) {
			trapReceiver.unsubscribe(this, this);
			trapReceiver.terminate();
			trapReceiver = null;
			synchronized (this) {
				notify();
			}
		}

		if (snmpTrapSession != null) {
			snmpTrapSession.close();
			snmpTrapSession = null;
		}
		
		if (session != null) {
			session.close();
			session = null;
		}

		if (debugMode())
			try {
				SnmpV1API.terminate();
			} catch (Exception ignore) {
				SystemFunctions.doNothing();
			}
	}

	/**
	 * This method initializes the SNMP PDU (protocol data unit) to be sent on all
	 * subsequent getNextEntry calls as part of the get next request.
	 * 
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	@Override
	public void selectEntries() throws Exception {
		if (snmpTrapEnabled) {
			logmsg(sResHash.getString("CONNECTOR.SNMP.TRAPSONLY.INFO"));
			return;
		}

		walkPdu = session.makePDU();
		walkPdu.operation = SnmpPDU.GETNEXT;
		walkOID = getParam("snmpOID");
		if (walkOID == null || walkOID.equals("")) {
			throw new Exception(sResHash.getString("CONNECTOR.SNMP.SNMPOID.INFO"));
		}
		walkPdu.addVarBind(walkOID);
	}

	/**
	 * Retrieves next SNMP.
	 * @return the next SNMP
	 * @throws Exception
	 *             if an error occurs.
	 */
	private Entry getNextSNMP() throws Exception {
		SnmpPDU rsp = session.send(walkPdu);

		if (rsp.errorStatus == SnmpPDU.NOERROR) {
			// Check if we are at end of walk list
			OID rspOID = rsp.varBindAt(0).getOID();
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.SNMP.GETNEXTSNMP.INFO",
						new Object[] { rspOID.toString(), walkOID }));
			}
			if (!rspOID.toString().startsWith(walkOID))
				return null;

			Entry e = new Entry();
			add2entry(e, rsp);
			walkPdu = rsp;
			walkPdu.operation = SnmpPDU.GETNEXT;
			return e;
		}
		throw new Exception(sResHash.getString(
				"CONNECTOR.SNMP.BADPDU.EXCEPTION", rsp.getErrorStatusString()));
	}

	/**
	 * Searches for an entry matching the specified search criteria.
	 * 
	 * @param search
	 *            the search criteria
	 * 
	 * @return the entry found
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	@Override
	public Entry findEntry(SearchCriteria search) throws Exception {

		String host = (String) getParam("snmpGetSetHost");
		int port = 161;
		String oid = null;
		String version = "0";

		for (int i = 0; i < search.getCriteria().size(); i++) {
			SearchCriteria.rscSearch s = search.getCriteria(i);
			if (s.name.equals("oid")) {
				oid = s.value.toString();
			} else if (s.name.equals("host")) {
				host = s.value.toString();
			} else if (s.name.equals("port")) {
				port = Integer.parseInt(s.value.toString());
			} else if (s.name.equals("version")) {
				version = s.value.toString();
			} else {
				logmsg(sResHash.getString(
						"CONNECTOR.SNMP.UNSUPPORTEDLINK.INFO", s.name));
			}
		}

		SnmpSession tmp;
		if (community != null)
			tmp = SnmpSession.open(host, community);
		else
			tmp = SnmpSession.open(host);

		tmp.changeRemotePort(port);

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.SNMP.SEARCHCRIT.INFO",
					new Object[] { oid, version, host, String.valueOf(port) }));
		}

		try {
			Entry e = sendGetRequest(tmp, oid);
			session.close();
			return e;
		} catch (Exception error) {
			session.close();
			throw error;
		}
	}

	/**
	 * Send SNMP data.
	 * 
	 * @param entry
	 *            an entry containing the SNMP data to send
	 * 
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	@Override
	public void putEntry(Entry entry) throws Exception {

		if (snmpTrapSession != null) {
			sendTrap(entry);
			return;
		}
		
		SnmpPDU set = session.makePDU();
		set.operation = SnmpPDU.SET;

		for (String attribute: entry.getAttributeNames()) {
			Object value = entry.getObject(attribute);
			set.addVarBind(attribute, convertValue(value));
		}

		com.tivoli.snmp.utils.Queue rspQ = new com.tivoli.snmp.utils.Queue(true); // create
		// a
		// blocking
		// response
		// queue
		session.send(set, rspQ); // send the PDU asynchronously
		SnmpPDU rsp = (SnmpPDU) rspQ.dequeue(); // receive the response
		// asynchronously

		session.close();

		if (rsp.errorStatus != SnmpPDU.NOERROR) {
			throw new Exception(sResHash.getString(
					"CONNECTOR.SNMP.SETFAILED.EXCEPTION", rsp
							.getErrorStatusString()));
		}
	}

	private Serializable convertValue(Object value) {
		if (value == null)
			return new Null();
		if (value instanceof Number)
			return new Counter(((Number) value).longValue());
		if (value instanceof byte[])
			return new OctetString((byte[]) value);
		if (value instanceof String)
			return new OctetString((String)value, true);
		if (value instanceof Serializable)
			return (Serializable) value;
		return new OctetString(value.toString(), true);
	}

	/**
	 * Send a SNMP trap using the given Entry
	 * @param entry
	 * @throws IOException 
	 */
	public void sendTrap(Entry entry) throws IOException {

		SnmpTrap pdu = new SnmpTrap();
		
		pdu.enterprise = new OID(getString(entry, "snmp.enterprise", IBM_OID));
		pdu.genericTrap = getInt(entry, "snmp.genericID", 6);
		pdu.specificTrap = getInt(entry, "snmp.specificID", 1);
		
		pdu.communityString = new OctetString(community, true);
		pdu.agentAddr = new IPAddress(getString(entry, "snmp.sourceIP", InetAddress.getLocalHost().getHostAddress()));
		pdu.timeStamp = new TimeTicks(System.currentTimeMillis());

		String [] names = entry.getAttributeNames();
		Arrays.sort(names);
		for (String oid: names) {
			Attribute a = entry.getAttribute(oid);

			if (a.size() == 0) {
				pdu.addVarBind(oid);
			} else {
				for (int j = 0; j < a.size(); j++)
					pdu.addVarBind(oid, convertValue(a.getValue(j)));
			}
		}

		snmpTrapSession.send(pdu);
	}

	private String getString(Entry entry, String name, String defValue) {
		Object value = entry.getObject(name);
		if (value == null)
			value = defValue;
		else
			entry.removeAttribute(name);
		
		return value.toString();
	}

	private int getInt(Entry entry, String name, Integer defValue) {
		Object value = entry.getObject(name);
		if (value == null)
			value = defValue;
		else
			entry.removeAttribute(name);
		
		if (value instanceof Integer)
			return (Integer)value;
		else
			return Integer.parseInt(value.toString());
	}

	/**
	 * Sends an SNMP GET request.
	 * 
	 * @param session
	 *            the SNMP session object
	 * @param oid
	 *            the SNMP OID value
	 * @return an entry object containing the SNMP response data
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	private Entry sendGetRequest(SnmpSession session, String oid)
			throws Exception {

		Entry e = new Entry();

		SnmpPDU pdu = session.makePDU();
		pdu.operation = SnmpPDU.GET;
		pdu.addVarBind(oid);

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.SNMP.GETOID.INFO", oid));
		}

		SnmpPDU rsp = session.send(pdu);

		if (rsp.errorStatus == SnmpPDU.NOERROR) {

			add2entry(e, rsp);

		} else {
			throw new Exception(sResHash.getString(
					"CONNECTOR.SNMP.BADPDU2.EXCEPTION", rsp
							.getErrorStatusString()));
		}

		e.setProperty("$snmp.pdu", rsp);

		return e;
	}

	/**
	 * Adds pdu detail to the entry.
	 * 
	 * @param e
	 *            Entry
	 * @param pdu
	 *            SnmpMetaPDU
	 */
	private void add2entry(Entry e, SnmpMetaPDU pdu) {
		for (int i = 0; i < pdu.varBindListSize(); i++) {
			SnmpVarBind vb = pdu.varBindAt(i); // extract the variable binding
			OID oid = vb.getOID();
			Serializable var = vb.getVar();
			if (var instanceof OctetString) {
				if (((OctetString) var).isDisplayString())
					e.addAttributeValue(oid.toString(), ((OctetString) var)
							.toDisplayString());
				else
					e.addAttributeValue(oid.toString(), var);
			} else {
				e.addAttributeValue(oid.toString(), var);
			}
		}
	}

	/**
	 * Checks if the queue is empty and waits for the specified timeout for
	 * input.
	 * 
	 * @param queue
	 *            queue vectoro
	 * @param timeout
	 *            timeout in milliseconds
	 * @return <code>true</code> if empty , <code>false</code> otherwise
	 * @throws Exception
	 */
	private synchronized boolean emptyQueue(Vector<Entry> queue, long timeout)
			throws Exception {

		if (queue.size() > 0)
			return false;

		if (timeout > 0) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.SNMP.WAITTIME.INFO", String
						.valueOf(timeout)));
			}
			wait(timeout);
		} else {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.SNMP.WAITFOREVER.INFO"));
			}
			wait();
		}
		return queue.size() == 0;
	}

	/**
	 * Indicates that trap should be sent to this connector.
	 * 
	 * @param trap
	 *            the {@link SnmpTrap} object.
	 * 
	 * @return true 
	 * 
	 */
	public boolean filter(SnmpTrap trap) {
		return true;
	}

	/**
	 * Trap listener
	 * 
	 * @param trap
	 *            The SNMP trap to be handled
	 */
	public void handle(SnmpTrap trap) {
		Entry e = new Entry();

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.SNMP.TRAP.RECEIVED", trap));
		}

		e.setAttribute("snmp.genericID", Integer.valueOf(trap.get_generic_id()));
		e.setAttribute("snmp.specificID", Integer.valueOf(trap.get_specific_id()));
		e.setAttribute("snmp.timestamp", trap.get_notify_timestamp());
		e.setAttribute("snmp.agentIP", trap.agentAddr);

		if (trap.communityString != null) {
			if (trap.communityString.isDisplayString())
				e.setAttribute("snmp.community", trap.communityString.toDisplayString());
			else
				e.setAttribute("snmp.community", trap.communityString);
		}

		if (trap.get_notify_enterprise() != null)
			e.setAttribute("snmp.notify.enterprise", trap.get_notify_enterprise());

		/*
		 * Defect 3441 if ( trap.get_notify_id() != null ) e.setAttribute (
		 * "snmp.notifyID", trap.get_notify_id() );
		 */

		add2entry(e, trap);

		synchronized (this) {
			trapList.add(e);
			notify();
		}

	}

	/**
	 * Reconnect to the underlying data source.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	@Override
	public void reconnect() throws Exception {
		terminate();
		initialize(this);
		if (((ConnectorConfig) getConfiguration()).getMode().equals(
				ConnectorConfig.ITERATOR_MODE)) {
			selectEntries();
		}
	}

}
