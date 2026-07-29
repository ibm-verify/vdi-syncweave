/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// SNMP.java
//
//
// $Header $
//
package com.ibm.di.protocols;

import java.io.*;
import java.net.*;
import java.util.*;

import com.ibm.di.entry.*;

// IBM Tivoli SNMP Stack
import com.tivoli.snmp.*;
import com.tivoli.snmp.data.*;

/**
 * This class offers the functionality of the Simple Network Management.
 * Typically SNMP is used, when there are a number of systems to be managed, and
 * one or more systems managing them. The managed system that report information
 * via SNMP to the managing systems.
 * 
 */
public class SNMP {

	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The default value for the manage object's type.
	 */
	public static final String SNMP_ENTERPRISE = "1.2.3.4.5.6.7.8.9";

	/**
	 * SNMP community string.
	 */
	public static final String SNMP_COMMUNITY = "public";

	/**
	 * A timeout parameter.
	 */
	public static int timeout = 5000;

	/**
	 * A parameter determining the number of retries to be made.
	 */
	public static int retries = 4;

	/**
	 * Sends an unsolicited SNMP TRAP message for an occurred event. Uses the
	 * local host address as agent IP and default values for community string,
	 * managed object's type, generic and specific trap types.
	 * 
	 * @param host
	 *            the host where the management system is.
	 * @param port
	 *            the TCP port to connect to.
	 * @param oid
	 *            the object identifier. If oid is null, value must be an Entry
	 *            where the Attribute names will be used as OIDs.
	 * @param value
	 *            the value(s).
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public static void sendTrap(String host, int port, String oid, Object value)
			throws Exception {
		sendTrap(host, port, SNMP_COMMUNITY, SNMP_ENTERPRISE, 6, 1, oid, value);
	}

	/**
	 * Sends an unsolicited SNMP TRAP message for an occurred event. Uses the
	 * local host address as agent IP.
	 * 
	 * @param host
	 *            the host where the management system is.
	 * @param port
	 *            the TCP port to connect to.
	 * @param community
	 *            the SNMP community string.
	 * @param enterprise
	 *            the type of managed object that generates the trap.
	 * @param genericTrap
	 *            indicates one of a number of generic trap types (coldStart,
	 *            warmStart, linkUp, linkDown, ...).
	 * @param specificTrap
	 *            indicates one of a number of enterprise specific trap codes.
	 * @param oid
	 *            the object identifier. If oid is null, value must be an Entry
	 *            where the Attribute names will be used as OIDs.
	 * @param value
	 *            the value(s).
	 * @throws Exception
	 *             if a problem occurs.
	 * 
	 * 
	 */
	public static void sendTrap(String host, int port, String community,
			String enterprise, int genericTrap, int specificTrap, String oid,
			Object value) throws Exception {
		sendTrap(InetAddress.getLocalHost().getHostAddress(), host, port,
				community, enterprise, genericTrap, specificTrap, oid, value);
	}

	/**
	 * Sends an unsolicited SNMP TRAP message for an occurred event. It is
	 * initialized by the network element and sent to its management system.
	 * This way the management system does not have to check all the time the
	 * state of each agent's objects. Thus the effectiveness is significantly
	 * improved.
	 * 
	 * @param agentIP
	 *            the IP of the agent sending the message.
	 * @param host
	 *            the host where the management system is.
	 * @param port
	 *            the TCP port to connect to.
	 * @param community
	 *            the SNMP community string.
	 * @param enterprise
	 *            the type of managed object that generates the trap.
	 * @param genericTrap
	 *            indicates one of a number of generic trap types (coldStart,
	 *            warmStart, linkUp, linkDown, ...).
	 * @param specificTrap
	 *            indicates one of a number of enterprise specific trap codes.
	 * @param oid
	 *            the object identifier. If oid is null, value must be an Entry
	 *            where the Attribute names will be used as OIDs.
	 * @param value
	 *            the value(s).
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public static void sendTrap(String agentIP, String host, int port,
			String community, String enterprise, int genericTrap,
			int specificTrap, String oid, Object value) throws Exception {
		SnmpAPI.initialize(-1);

		String agentHost = agentIP;
		if (agentHost == null)
			agentHost = InetAddress.getLocalHost().getHostAddress();

		SnmpTrapSession session = SnmpTrapSession.open(host, port);
		SnmpTrap pdu = new SnmpTrap();

		pdu.enterprise = new OID(enterprise);
		pdu.communityString = new OctetString(community, true);
		pdu.agentAddr = new IPAddress(agentHost);
		pdu.genericTrap = genericTrap;
		pdu.specificTrap = specificTrap;
		pdu.timeStamp = new TimeTicks(System.currentTimeMillis());

		if (oid != null && value == null) {
			pdu.addVarBind(oid);

		} else if (oid != null && value != null) {
			Vector values = new Vector();

			if (value instanceof List) {
				List v = (List) value;
				for (int i = 0; i < v.size(); i++) {
					values.add(convertValue(v.get(i)));
				}

			} else {
				values.add(convertValue(value));
			}

			for (int i = 0; i < values.size(); i++) {
				// System.out.println("--addVarBind: " + oid + ": " +
				// values.get(i));
				pdu.addVarBind(oid, (Serializable) values.get(i));
			}
		} else if (value instanceof Entry) {

			Entry entry = (Entry) value;
			String[] oids = entry.getAttributeNames();

			for (int i = 0; i < oids.length; i++) {
				Attribute a = entry.getAttribute(oids[i]);

				if (a.size() == 0) {
					pdu.addVarBind(oids[i]);
				} else {
					for (int j = 0; j < a.size(); j++)
						pdu.addVarBind(oids[i], convertValue(a.getValue(j)));
				}
			}
		}

		session.send(pdu);
		session.close();
	}

	/**
	 * Converts the given value to a SNMP object type.
	 * 
	 * @param value
	 *            the value to be converted.
	 * @return the result of the conversion.
	 */
	private static Serializable convertValue(Object value) {
		if (value == null)
			return new Null();
		else if (value.getClass().getName().indexOf("com.tivoli.snmp.data") != -1)
			return (Serializable) value;
		if (value instanceof Integer) {
			return new Counter(((Integer) value).intValue());
		} else if (value instanceof byte[]) {
			return new OctetString((byte[]) value);
		} else {
			return new OctetString(value.toString(), true);
		}
	}

	/**
	 * Creates a SNMP BadValue object.
	 * 
	 * @return the created object.
	 */
	public static BadValue createBadValue() {
		return new BadValue();
	}

	/**
	 * Creates a SNMP Boolean object.
	 * 
	 * @param value
	 *            the <b>boolean</b> parameter to be converted.
	 * @return the created object.
	 */
	public static com.tivoli.snmp.data.Boolean createBoolean(boolean value) {
		return new com.tivoli.snmp.data.Boolean(value);
	}

	/**
	 * Creates a SNMP Counter object.
	 * 
	 * @param value
	 *            the <b>long</b> parameter to be converted.
	 * @return the created object.
	 */
	public static Counter createCounter(long value) {
		return new Counter(value);
	}

	/**
	 * Creates a 64-bit SNMP Counter object.
	 * 
	 * @param value
	 *            the <b>long</b> parameter to be converted.
	 * @return the created object.
	 */
	public static Counter64 createCounter64(long value) {
		return new Counter64(value);
	}

	/**
	 * Creates a SNMP OctetString object with fixed length.
	 * 
	 * @param value
	 *            the <b>String</b> parameter to be converted.
	 * @return the created object.
	 */
	public static FixedLengthOctetString createFixedLengthOctetString(
			String value) {
		return new FixedLengthOctetString(value);
	}

	/**
	 * Creates a SNMP FullCounter64 object.
	 * 
	 * @param value
	 *            the <b>BigInteger</b> parameter to be converted.
	 * @return the created object.
	 */
	public static FullCounter64 createFullCounter64(java.math.BigInteger value) {
		return new FullCounter64(value);
	}

	/**
	 * Creates a SNMP Gauge object.
	 * 
	 * @param value
	 *            the <b>long</b> parameter to be converted.
	 * @return the created object.
	 */
	public static Gauge createGauge(long value) {
		return new Gauge(value);
	}

	/**
	 * Creates a SNMP IPAddress object.
	 * 
	 * @param value
	 *            the <b>String</b> parameter to be converted.
	 * @return the created object.
	 */
	public static IPAddress createIPAddress(String value) {
		return new IPAddress(value);
	}

	/**
	 * Creates a SNMP NoChange object.
	 * 
	 * @return the created object.
	 */
	public static NoChange createNoChange() {
		return new NoChange();
	}

	/**
	 * Creates a SNMP NoSuchInstance object.
	 * 
	 * @return the created object.
	 */
	public static NoSuchInstance createNoSuchInstance() {
		return new NoSuchInstance();
	}

	/**
	 * Creates a SNMP NoSuchObject object.
	 * 
	 * @return the created object.
	 */
	public static NoSuchObject createNoSuchObject() {
		return new NoSuchObject();
	}

	/**
	 * Creates a SNMP NotSupported object.
	 * 
	 * @return the created object.
	 */
	public static NotSupported createNotSupported() {
		return new NotSupported();
	}

	/**
	 * Creates a SNMP Null object.
	 * 
	 * @return the created object.
	 */
	public static Null createNull() {
		return new Null();
	}

	/**
	 * Creates a SNMP OctetString object.
	 * 
	 * @param value
	 *            the <b>String</b> parameter to be converted.
	 * @return the created object.
	 */
	public static OctetString createOctetString(String value) {
		return new OctetString(value);
	}

	/**
	 * Creates a SNMP OID (object identifier) object.
	 * 
	 * @param value
	 *            the <b>String</b> parameter to be converted.
	 * @return the created object.
	 */
	public static OID createOID(String value) {
		return new OID(value);
	}

	/**
	 * Creates a SNMP Opaque object.
	 * 
	 * @param value
	 *            the <b>byte</b> array parameter to be converted.
	 * @return the created object.
	 */
	public static Opaque createOpaque(byte[] value) {
		return new Opaque(value);
	}

	/**
	 * Creates a SNMP TimeTicks object.
	 * 
	 * @param value
	 *            the <b>long</b> parameter to be converted.
	 * @return the created object.
	 */
	public static TimeTicks createTimeTicks(long value) {
		return new TimeTicks(value);
	}

}
