/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.tivoli.snmp.SnmpAsn1;
import com.tivoli.snmp.SnmpPDU;
import com.tivoli.snmp.SnmpVarBind;
import com.tivoli.snmp.data.BadValue;
import com.tivoli.snmp.data.Counter;
import com.tivoli.snmp.data.Counter64;
import com.tivoli.snmp.data.FixedLengthOctetString;
import com.tivoli.snmp.data.FullCounter64;
import com.tivoli.snmp.data.Gauge;
import com.tivoli.snmp.data.IPAddress;
import com.tivoli.snmp.data.NoChange;
import com.tivoli.snmp.data.NoSuchInstance;
import com.tivoli.snmp.data.NoSuchObject;
import com.tivoli.snmp.data.NotSupported;
import com.tivoli.snmp.data.Null;
import com.tivoli.snmp.data.OID;
import com.tivoli.snmp.data.OctetString;
import com.tivoli.snmp.data.Opaque;
import com.tivoli.snmp.data.TimeTicks;

/**
 * The SNMP Server Connector is used by a monitoring console (an SNMP Manager).
 * The SNMP Server Connector receives SNMP packets on a specified port, and
 * returns appropriate SNMP response packets.
 */
public class SNMPServerConnector extends Connector {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "snmpserverconnector";

	/**
	 * The maximum packet size of UDP protocol
	 */
	private final static int UDP_MAX_PACKET_SIZE = 65535;

	/**
	 * Parameter name in the configuration for udp.port.
	 */
	private final static String PARAM_SNMP_PORT = "udp.port";

	/**
	 * Parameter name in the configuration for snmp.community.
	 */
	private final static String PARAM_SNMP_COMMUNITY = "snmp.community";

	/**
	 * community used in Connector
	 */
	private String mCommunity;

	/**
	 * port used in Connector
	 */
	private int mPort;

	/**
	 * New SNMPServerConnector object created for every connected client.
	 */
	private SNMPServerConnector mServerConnector;

	/**
	 * DatagramSocket opened and accepting clients.
	 */
	private DatagramSocket mDatagramSocket;

	/**
	 * DatagramPacket object received.
	 */
	private DatagramPacket mPacket;

	/**
	 * Bytes received in the SNMP Packet.
	 */
	private byte[] mPacketData;

	/**
	 * Length of the SNMP packet.
	 */
	private int mPacketLength;

	/**
	 * Inet Address where SNMP packet come from.
	 */
	private InetAddress mPacketInetAddress;

	/**
	 * Inet port where SNMP packet came from.
	 */
	private int mPacketPort;

	/**
	 * If true, the Connector has started terminating or has already been
	 * terminated.
	 */
	private boolean mTerminationRequested;

	/**
	 * If true the Connector is accepting client requests.
	 */
	private boolean mIsAccepting;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Default constructor.
	 */
	public SNMPServerConnector() {
		super();
		setModes(new String[] { ConnectorConfig.SERVER_MODE });
	}

	/**
	 * Returns the server Connector if this Connector is handling a client
	 * session.
	 * 
	 * @return SNMPServerConnector The parent SNMPServerConnector
	 */
	public SNMPServerConnector getServerConnector() {
		return mServerConnector;
	}

	/**
	 * Sets community parameter
	 * 
	 * @param aCommunity
	 *            The community
	 */
	public void setCommunity(String aCommunity) {
		this.mCommunity = aCommunity;
	}

	/**
	 * Sets the server Connector for this Connector.
	 * 
	 * @param aServerConnector
	 *            The serverConnector.
	 */
	public void setServerConnector(SNMPServerConnector aServerConnector) {
		this.mServerConnector = aServerConnector;
	}

	/**
	 * Checks if this Connector is currently waiting for a client connection.
	 * 
	 * @return true if this Connector is currently waiting for a client
	 *         connection.
	 */
	public boolean isAccepting() {
		return mIsAccepting;
	}

	/**
	 * Checks if termination is requested.
	 * @return true if this Connector has the termination flag set.
	 */
	public boolean isTerminating() {
		return mTerminationRequested;
	}

	/**
	 * Initialize the Connector. To initialize this Connector with a
	 * DatagramPacket object for the obj parameter. In all other cases, the
	 * Connector will initialize an SNMP server(Agent) session.
	 * 
	 * @param aObj
	 *            Null, DatagramPacket or ConnectorMode class
	 * @throws Exception
	 *             If invalid Connector parameter values are supplied.
	 */
	@Override
	public void initialize(Object aObj) throws Exception {
		if (aObj instanceof DatagramPacket) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.SNMPSRV.DATAGRAM.INFO"));
			}
			mPacket = (DatagramPacket) aObj;
			return;
		}
		String strPort = getParam(PARAM_SNMP_PORT);
		if (strPort == null || strPort.trim().length() == 0) {
			throw new Exception(sResHash
					.getString("CONNECTOR.SNMPSRV.MISSING.UDPPORT.EXCEP"));
		}
		try {
			mPort = Integer.valueOf(strPort).intValue();
		} catch (NumberFormatException e) {
			throw new Exception(sResHash.getString(
					"CONNECTOR.SNMPSRV.BADUDPPORT.EXCEP", strPort));
		}

		mTerminationRequested = false;

		mCommunity = getParam(PARAM_SNMP_COMMUNITY);

		mDatagramSocket = new DatagramSocket(mPort);

		logmsg(sResHash.getString("CONNECTOR.SNMPSRV.WAITING.INFO", "" + mPort));
	}

	/**
	 * Server mode - returns a new instance of the Connector for each client
	 * connection.
	 * 
	 * @return ConnectorInterface child SNMPServerConnector object that process
	 *         client request
	 */
	@Override
	public ConnectorInterface getNextClient() throws Exception {
		DatagramPacket packet;
		if (!isTerminating()) {
			packet = new DatagramPacket(new byte[UDP_MAX_PACKET_SIZE],
					UDP_MAX_PACKET_SIZE);
			mIsAccepting = true;
			mDatagramSocket.receive(packet);
			mIsAccepting = false;
		} else {
			logmsg(sResHash.getString("CONNECTOR.SNMPSRV.TERMINATE1.INFO"));
			return null;
		}

		if (isTerminating()) {
			logmsg(sResHash.getString("CONNECTOR.SNMPSRV.TERMINATE2.INFO"));
			mDatagramSocket.close();
			mDatagramSocket = null;
			return null;
		}

		if (packet.getAddress() != null) {
			logmsg(sResHash.getString("CONNECTOR.SNMPSRV.CONNECTFROM.INFO",
					packet.getAddress().getCanonicalHostName()));
		}
		SNMPServerConnector clientSession = new SNMPServerConnector();
		clientSession.setServerConnector(this);
		clientSession.setConfiguration(getConfiguration());
		clientSession.setName(getName());
		clientSession.setLog(getLog());
		clientSession.setCommunity(mCommunity);
		clientSession.initialize(packet);
		return clientSession;
	}

	/**
	 * Returns the next Entry from the SNMP client(Manager).
	 * 
	 * @return - the next Entry, or null if the connection has been closed.
	 * @throws Exception
	 *             If retrieving the next Entry fails.
	 */
	@Override
	public Entry getNextEntry() throws Exception {

		if (mPacket == null) {
			return null;
		}
		mPacketData = mPacket.getData();
		mPacketLength = mPacket.getLength();
		mPacketInetAddress = mPacket.getAddress();
		mPacketPort = mPacket.getPort();

		if (mPacketInetAddress != null) {
			logmsg(sResHash.getString("CONNECTOR.SNMPSRV.CONNECTFROM2.INFO",
					mPacketInetAddress.getCanonicalHostName()));
		}

		Entry entry = new Entry();

		if (!addAttributes(entry)) {
			entry = null;
		}
		mPacket = null;
		return entry;
	}

	/**
	 * Adds all needed attributes to the Entry
	 * 
	 * @param aEntry
	 *            The entry.
	 * @return <code>true</code> if operation is successful
	 */
	private boolean addAttributes(Entry aEntry) {
		boolean result = true;
		try {
			SnmpPDU pdu = SnmpAsn1.decodePDU(mPacketData, mPacketLength);

			if ((mCommunity != null)
					&& (mCommunity.length() > 0)
					&& (!mCommunity.equals(pdu.communityString
							.toDisplayString()))) {
				logmsg(sResHash.getString("CONNECTOR.SNMPSRV.IGNOREREQ.INFO",
						pdu.communityString.toDisplayString()));
				result = false;
			}
			if (result && (pdu != null)) {
				aEntry.setAttribute("snmp.operation", pdu.getOperationString());
				if (pdu.communityString != null) {
					aEntry.setAttribute("snmp.community", pdu.communityString
							.toDisplayString());
				}

				if (mPacketInetAddress != null) {
					aEntry.setAttribute("snmp.remoteip", mPacketInetAddress
							.getHostAddress());
				}
				aEntry.setAttribute("snmp.errorcode", Integer.valueOf(2));
				aEntry.setAttribute("snmp.errorindex", Integer.valueOf(0));
				aEntry.setAttribute("snmp.request-id", Integer
						.valueOf(pdu.requestId));

				aEntry.setAttribute("snmp.PDU", pdu);
				addVarBind(aEntry, pdu);
			}
		} catch (Exception error) {
			logmsg(sResHash.getString("CONNECTOR.SNMPSRV.ADDATTR.EXC.WARN",
					error.getMessage()));
			result = false;
		}
		return result;
	}

	/**
	 * Add the varbind list to the entry attributes
	 * 
	 * @param aEntry
	 *            The entry.
	 * @param aPdu
	 *            The SnmpPDU object received
	 */
	private void addVarBind(Entry aEntry, SnmpPDU aPdu) {
		Attribute oid = aEntry.newAttribute("snmp.oid");
		Attribute oidvalue = aEntry.newAttribute("snmp.oidvalue");
		Attribute oidvalueraw = aEntry.newAttribute("snmp.oidvalue.raw");
		for (int i = 0; i < aPdu.get_vb_count(); i++) {
			SnmpVarBind b = aPdu.get_vb(i);
			if (b != null) {
				Object val = b.get_value();
				oid.addValue(b.get_printable_oid());
				if (val != null) {

					if (val instanceof OctetString) {
						oidvalue
								.addValue(((OctetString) val).toDisplayString());
					} else {
						oidvalue.addValue(val);
					}
					oidvalueraw.addValue(val);
				} else {
					oidvalue.addValue(null);
					oidvalueraw.addValue(null);
				}
			}
		}
	}

	/**
	 * Send response to the SNMP client (Manager).
	 * 
	 * @param aEntry
	 *            The entry.
	 * 
	 * @throws Exception
	 *             If sending response fails.
	 */
	@Override
	public void replyEntry(Entry aEntry) throws Exception {
		putEntry(aEntry);
	}

	/**
	 * Send response to the SNMP client (Manager).
	 * 
	 * @param aEntry
	 *            The entry.
	 * 
	 * @throws Exception
	 *             If sending response fails.
	 */
	@Override
	public void putEntry(Entry aEntry) throws Exception {
		byte[] responsePacketData = null;

		SnmpPDU pdu = CreateResponsePDU(aEntry);
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.SNMPSRV.PDUINFO.INFO", pdu));
		}

		responsePacketData = SnmpAsn1.encode(pdu);
		DatagramPacket packet = new DatagramPacket(responsePacketData,
				responsePacketData.length, mPacketInetAddress, mPacketPort);
		mServerConnector.sendPacket(packet);
	}

	/**
	 * Create PDU for the response packet to be send to the SNMP client(Manager)
	 * 
	 * @param aEntry
	 *            The entry.
	 * 
	 * @return SnmpPDU The PDU
	 */
	private SnmpPDU CreateResponsePDU(Entry aEntry) {
		SnmpPDU pdu = SnmpAsn1.decodePDU(mPacketData, mPacketLength);

		Attribute oid = aEntry.getAttribute("snmp.oid");
		Attribute oidvalue = aEntry.getAttribute("snmp.oidvalue");

		// Clear current list of varbinds
		pdu.deleteVarBindList();

		// Add new variables
		if (oid != null && oidvalue != null) {
			for (int i = 0; i < oid.size(); i++) {
				if (i >= oidvalue.size()
						|| oidvalue.getValue(i) instanceof EmptyValue) {
					pdu.addVarBind(oid.getValue(i).toString());
				} else {
					pdu.addVarBind(oid.getValue(i).toString(),
							(Serializable) oidvalue.getValue(i));
				}
			}
		}

		// Update error status/index
		int errorcode = 5;
		Object obj = aEntry.getObject("snmp.errorcode");
		if (obj instanceof Integer) {
			errorcode = ((Integer) obj).intValue();
		} else if (obj != null) {
			errorcode = Integer.parseInt(obj.toString());
		}
		if (errorcode < 0 || errorcode > 5) {
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.SNMPSRV.ERRORVALIDITY.INFO", "" + errorcode));
			}
			errorcode = 5;
		}
		pdu.set_error_status(errorcode);

		obj = aEntry.getObject("snmp.errorindex");
		if (obj instanceof Integer) {
			pdu.set_error_index(((Integer) obj).intValue());
		} else if (obj != null) {
			pdu.set_error_index(Integer.parseInt(obj.toString()));
		}
		pdu.operation = SnmpPDU.GETRESPONSE;
		return pdu;
	}

	/**
	 * Sends response packet
	 * 
	 * @param aPacket
	 *            {@link DatagramPacket} to send
	 * 
	 * @throws IOException
	 *             If sending packet fails.
	 */
	private void sendPacket(DatagramPacket aPacket) throws IOException {

		if (mDatagramSocket != null) {

			/*
			 * no need of synchronization here because DatagramSocket is
			 * thread-safe as-is for more information see:
			 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4035771
			 */
			mDatagramSocket.send(aPacket);
		}
	}

	/**
	 * This method tries to terminate the server by setting the termination flag
	 * for the Connector returned by getServerConnector and immediatly
	 * connecting to its port.
	 */
	@Override
	public void terminateServer() throws Exception {
		if (getServerConnector() == null) {
			mTerminationRequested = true;
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.SNMPSRV.TERMINATERCV.INFO"));
			}
			// At this point we are the server
			InetAddress address = InetAddress.getLocalHost();
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.SNMPSRV.DUMMY.INFO",
						new Object[] { "" + address, "" + mPort }));
			}
			try {
				DatagramSocket sock = new DatagramSocket();
				DatagramPacket packet = new DatagramPacket(new byte[] { 4 }, 1,
						address, mPort);
				sock.send(packet);
			} catch (IOException e) {
				logmsg(sResHash.getString(
						"CONNECTOR.SNMPSRV.MISSING.TCPPORT.WARN", e.toString()));
			}

		} else {
			// At this point we are a client sending a message to our server
			if (getServerConnector().isTerminating()) {
				if (debugMode()) {
					debug(sResHash
							.getString("CONNECTOR.SNMPSRV.ALREADYTERM.INFO"));
				}
				return;
			}

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.SNMPSRV.SENDINGTERM.INFO"));
			}
			getServerConnector().terminateServer();
		}
	}

	/**
	 * Terminate the connector.
	 */
	@Override
	public void terminate() throws Exception {
		super.terminate();

		if (mDatagramSocket != null) {
			mDatagramSocket.close();
			mDatagramSocket = null;
		}
	}

	/**
	 * Convenient methods to create SNMP object types.
	 * 
	 * @return the {@link BadValue} instance.
	 */
	public BadValue createBadValue() {
		return new BadValue();
	}

	/**
	 * Creates boolean object.
	 * 
	 * @param aValue
	 *            The boolean value to set.
	 * @return the {@link com.tivoli.snmp.data.Boolean} instance.
	 */
	public com.tivoli.snmp.data.Boolean createBoolean(boolean aValue) {
		return new com.tivoli.snmp.data.Boolean(aValue);
	}

	/**
	 * Creates {@link Counter} object.
	 * 
	 * @param aValue
	 *            The long value to set.
	 * @return the {@link Counter} instance.
	 */
	public Counter createCounter(long aValue) {
		return new Counter(aValue);
	}

	/**
	 * Creates {@link Counter64} object.
	 * 
	 * @param aValue
	 *            The long value to set.
	 * @return the {@link Counter64} instance.
	 */
	public Counter64 createCounter64(long aValue) {
		return new Counter64(aValue);
	}

	/**
	 * Creates {@link FixedLengthOctetString} object.
	 * 
	 * @param aValue
	 *            The String value to set.
	 * @return the {@link FixedLengthOctetString} instance.
	 */
	public FixedLengthOctetString createFixedLengthOctetString(String aValue) {
		return new FixedLengthOctetString(aValue);
	}

	/**
	 * Creates {@link FullCounter64} object.
	 * 
	 * @param aValue
	 *            The java.math.BigInteger value to set.
	 * @return the {@link FullCounter64} instance.
	 */
	public FullCounter64 createFullCounter64(java.math.BigInteger aValue) {
		return new FullCounter64(aValue);
	}

	/**
	 * Creates {@link Gauge} object.
	 * 
	 * @param aValue
	 *            The long value to set.
	 * @return the {@link Gauge} instance.
	 */
	public Gauge createGauge(long aValue) {
		return new Gauge(aValue);
	}

	/**
	 * Creates {@link IPAddress} object.
	 * 
	 * @param aValue
	 *            The String value to set.
	 * @return the {@link IPAddress} instance.
	 */
	public IPAddress createIPAddress(String aValue) {
		return new IPAddress(aValue);
	}

	/**
	 * Creates {@link NoChange} object.
	 * 
	 * @return the{@link NoChange} instance.
	 */
	public NoChange createNoChange() {
		return new NoChange();
	}

	/**
	 * Creates {@link NoSuchInstance} object.
	 * 
	 * @return the {@link NoSuchInstance} instance.
	 */
	public NoSuchInstance createNoSuchInstance() {
		return new NoSuchInstance();
	}

	/**
	 * Creates {@link NoSuchObject} object.
	 * 
	 * @return the {@link NoSuchObject} instance.
	 */
	public NoSuchObject createNoSuchObject() {
		return new NoSuchObject();
	}

	/**
	 * Creates {@link NotSupported} object.
	 * 
	 * @return the{@link NotSupported} instance.
	 */
	public NotSupported createNotSupported() {
		return new NotSupported();
	}

	/**
	 * Creates {@link Null} object.
	 * 
	 * @return the {@link Null} instance.
	 */
	public Null createNull() {
		return new Null();
	}

	/**
	 * Creates {@link OctetString} object.
	 * 
	 * @param aValue
	 *            The String value to set.
	 * @return the {@link OctetString} instance.
	 */
	public OctetString createOctetString(String aValue) {
		return new OctetString(aValue);
	}

	/**
	 * Creates {@link OID} object.
	 * 
	 * @param aValue
	 *            The String value to set.
	 * @return the {@link OID} instance.
	 */
	public OID createOID(String aValue) {
		return new OID(aValue);
	}

	/**
	 * Creates {@link Opaque} object.
	 * 
	 * @param aValue
	 *            The byte[] value to set.
	 * @return the {@link Opaque} instance.
	 */
	public Opaque createOpaque(byte[] aValue) {
		return new Opaque(aValue);
	}

	/**
	 * Creates {@link TimeTicks} object.
	 * 
	 * @param aValue
	 *            The long value to set.
	 * @return the {@link TimeTicks} instance.
	 */
	public TimeTicks createTimeTicks(long aValue) {
		return new TimeTicks(aValue);
	}

	/**
	 * An Empty Value used in internal code.
	 * 
	 */
	public static class EmptyValue extends Object implements Serializable {

		/**
		 * Uniqe ID used for deserialization.
		 */
		private static final long serialVersionUID = 4367070107057595068L;

		/**
		 * @return null
		 */
		@Override
		public String toString() {
			return "";
		}
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I%, 20%E%";
	}
}
