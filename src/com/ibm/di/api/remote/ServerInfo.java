/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import com.ibm.di.api.DIException;
import com.ibm.di.model.descriptor.ComponentDescriptor;

/**
 * 
 * This interface provides various methods for getting server information.
 * 
 */
public interface ServerInfo extends Remote {

	/**
	 * Returns Server version.
	 * 
	 * @return the version of the server.
	 * @throws DIException
	 *             if an error occurs while retrieving server's data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getServerVersion() throws DIException, RemoteException;

	/**
	 * Returns Server machine IP address.
	 * 
	 * @return the IP address of the machine where the server is running.
	 * @throws DIException
	 *             if an error occurs while retrieving server's data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getIPAddress() throws DIException, RemoteException;

	/**
	 * Returns Server machine host name.
	 * 
	 * @return the host name of the machine where the server is running.
	 * @throws DIException
	 *             if an error occurs while retrieving server's data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getHostName() throws DIException, RemoteException;

	/**
	 * Returns the name of the operating system where the Server is running.
	 * 
	 * @return the operating system of the machine where the server is running.
	 * @throws DIException
	 *             if an error occurs while retrieving server's data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getOperatingSystem() throws DIException, RemoteException;

	/**
	 * Returns the Server boot time.
	 * 
	 * @return a <code>java.util.Date</code> specifying the date and time the
	 *         server was started.
	 * @throws DIException
	 *             if an error occurs while retrieving server's boot time.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Date getServerBootTime() throws DIException, RemoteException;

	/**
	 * Returns the server unique identifier.
	 * 
	 * @return String - the value of the property com.ibm.di.server.id
	 * @throws DIException
	 *             if an error occurs while retrieving server's ID.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getServerID() throws DIException, RemoteException;

	// Connectors information

	/**
	 * Retrieves information for all Connectors installed on the Server. For
	 * each Connector a Hashtable object is constructed, containing the
	 * following elements:
	 * <ul>
	 * <li>key "Name" - the value is the name of the Connector
	 * <li>key "Description" - the value is the description of the Connector
	 * <li>key "Version" - the value is the Connector's version
	 * </ul>
	 * 
	 * @return a <code>java.util.Hashtable</code> array, each of its elements
	 *         corresponding to a single Connector.
	 * @throws DIException
	 *             if an error occurs while obtaining installed Connectors'
	 *             data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Hashtable<?, ?>[] getInstalledConnectors() throws DIException, RemoteException;

	/**
	 * Retrieves the names of all Connectors installed on the Server.
	 * 
	 * @return a <code>String</code> array, each of its elements specifying a
	 *         Connector name.
	 * @throws DIException
	 *             if an error occurs while obtaining installed Connectors'
	 *             data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String[] getInstalledConnectorsNames() throws DIException, RemoteException;

	/**
	 * Retrieves the description of a Connector installed on the Server.
	 * 
	 * @param aConnectorName
	 *            the name of the Connector whose description will be retrieved.
	 * @return the description of the specified Connector.
	 * @throws DIException
	 *             if an error occurs while obtaining Connector data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getConnectorDescription(String aConnectorName) throws DIException, RemoteException;

	/**
	 * Retrieves the version of a Connector installed on the Server.
	 * 
	 * @param aConnectorName
	 *            the name of the Connector whose version will be retrieved.
	 * @return the version of the specified Connector.
	 * @throws DIException
	 *             if an error occurs while obtaining Connector data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getConnectorVersionInfo(String aConnectorName) throws DIException, RemoteException;

	// Parsers information

	/**
	 * Retrieves information for all Parsers installed on the Server. For each
	 * Parser a Hashtable object is constructed, containing the following
	 * elements:
	 * <ul>
	 * <li>key "Name" - the value is the name of the Parser
	 * <li>key "Description" - the value is the description of the Parser
	 * <li>key "Version" - the value is the Parser's version
	 * </ul>
	 * 
	 * @return a <code>java.util.Hashtable</code> array, each of its elements
	 *         corresponding to a single Parser.
	 * @throws DIException
	 *             if an error occurs while obtaining installed Parsers' data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Hashtable<?, ?>[] getInstalledParsers() throws DIException, RemoteException;

	/**
	 * Retrieves the names of all Parsers installed on the Server.
	 * 
	 * @return a <code>String</code> array, each of its elements specifying a
	 *         Parser name.
	 * @throws DIException
	 *             if an error occurs while obtaining installed Parsers' data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String[] getInstalledParsersNames() throws DIException, RemoteException;

	/**
	 * Retrieves the description of a Parser installed on the Server.
	 * 
	 * @param aParserName
	 *            the name of the Parser whose description will be retrieved.
	 * @return the description of the specified Parser.
	 * @throws DIException
	 *             if an error occurs while obtaining Parser data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getParserDescription(String aParserName) throws DIException, RemoteException;

	/**
	 * Retrieves the version of a Parser installed on the Server.
	 * 
	 * @param aParserName
	 *            the name of the Parser whose version will be retrieved.
	 * @return the version of the specified Parser.
	 * @throws DIException
	 *             if an error occurs while obtaining Parser data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getParserVersionInfo(String aParserName) throws DIException, RemoteException;

	// Function Components information

	/**
	 * Retrieves information for all Function Components installed on the
	 * Server. For each Function Component a Hashtable object is constructed,
	 * containing the following elements:
	 * <ul>
	 * <li>key "Name" - the name of the Function Component
	 * <li>key "Description" - the description of the Function Component
	 * <li>key "Version" - the Function Component's version
	 * </ul>
	 * 
	 * @return a <code>java.util.Hashtable</code> array, each of its elements
	 *         corresponding to a single Function Component.
	 * @throws DIException
	 *             if an error occurs while obtaining installed Function
	 *             Components' data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Hashtable<?, ?>[] getInstalledFunctionComponents() throws DIException, RemoteException;

	/**
	 * Retrieves the names of all Function Components installed on the Server.
	 * 
	 * @return a <code>String</code> array, each of its elements specifying a
	 *         Function Component's name.
	 * @throws DIException
	 *             if an error occurs while obtaining installed Function
	 *             Components' data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String[] getInstalledFunctionComponentsNames() throws DIException, RemoteException;

	/**
	 * Retrieves the description of a Function Component installed on the
	 * Server.
	 * 
	 * @param aFunctionComponentName
	 *            the name of the Function Component whose description will be
	 *            retrieved.
	 * @return the description of the specified Function Component.
	 * @throws DIException
	 *             if an error occurs while obtaining Function Component data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getFunctionComponentDescription(String aFunctionComponentName) throws DIException, RemoteException;

	/**
	 * Retrieves the version of a Function Component installed on the Server.
	 * 
	 * @param aFunctionComponentName
	 *            the name of the Function Component whose version will be
	 *            retrieved.
	 * @return the version of the specified Function Component.
	 * @throws DIException
	 *             if an error occurs while obtaining Function Component data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getFunctionComponentVersionInfo(String aFunctionComponentName) throws DIException, RemoteException;

	/**
	 * Returns password parameters names for specified class.
	 * 
	 * @param aJavaClassName
	 *            Java class name from which password parameters names will be
	 *            taken.
	 * @return Vector object with elements the password parameters names of the
	 *         given class.
	 * @throws DIException
	 *             if an error occurs while retrieving password parameters
	 *             names.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Vector<String> getPasswordParameterNames(String aJavaClassName) throws DIException, RemoteException;

	/**
	 * Returns the localized form information about a specific component as
	 * component descriptor object.
	 * 
	 * <p>
	 * <b>This method is experimental. It is intended for internal use only.
	 * Backward compatibility is not guaranteed.</b>
	 * 
	 * @param componentName
	 *            The name of the component (e.g. ibmdi.LDAP or
	 *            system:/Connectors/ibmdi.LDAP)
	 * 
	 * @return the corresponding component descriptor object.
	 * 
	 * @throws DIException
	 *             if an error occurs while obtaining component data.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public ComponentDescriptor getInstalledComponentDescriptor(String componentName) throws DIException, RemoteException;
}
