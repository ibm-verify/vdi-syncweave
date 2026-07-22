/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.dcs;

import java.util.HashMap;
import java.util.Map;

import com.ibm.tivoli.datacleanser.DataCleanser;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;
import com.ibm.di.server.ResourceHash;

/**
 * This class initializes Data Cleanser Service and
 * takes a string and gives a standard CDM String corresponding to the inout string.
 */
public class DataCleanserFC extends Function {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "datacleanser";
	
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);
	
	/**
	 * The name of the Attribute type parameter form FC's configuration.
	 */
	private static final String PARAM_CDMATTR_TYPE = "cdmAttributeType";
	
	/**
	 * This output map attribute is used to override the CDM Attribute name set in the
	 * Components configuration panel.
	 */
	public static final String CDM_ATTR_TYPE = "$cdmAttributeType";
	
	/**
	 * This output map attribute is for input string which need to be cleansed.
	 */
	public static final String INPUT_STRING = "$inputString";
	
	/**
	 * This input map attribute is the cleansed string of the given input string.
	 */
	public static final String CLEANSED_STRING = "$cleansedString";
	
	/**
	 * The name CDM Attribute Type for the String which needs to be cleansed
	 * 
	 */
	private String cdmAttributeType;
	
	/**
	 * The name the String which needs to be cleansed
	 * 
	 */
	private String inputString;
	
	/**
	 * An object of DataCleanser Service
	 */
	private static DataCleanser dcs = DataCleanser.getInstance();

	/**
	 * Called once to initialize the Function Component.
	 * 
	 * @param obj -
	 *            ignored
	 * @throws Exception
	 *             if an error occurs.
	 * 
	 */
		
	public void initialize(Object obj) throws Exception {
		
		super.initialize(obj);
		
		// read configuration parameters
		cdmAttributeType = getStringParameter(PARAM_CDMATTR_TYPE);
		
		if (cdmAttributeType != null) {
			printDebugMessage("DATA.CLEANSER.FC.CDMATTRTYPE.INITIALIZED", new Object[] { PARAM_CDMATTR_TYPE, cdmAttributeType });
		}
		
		//Call the init() method of NRS to initialize and set up 
		//the running environment of the naming rule plug in.
		dcs.init();
		
	}

	/**
	 * The FC takes the string which needs to be cleansed and a CDM Attribute.
	 * If the string has a corresponding CDM standard name it gives the standard
	 * name of the string otherwise gives the same string which was passed.
	 * 
	 * @param obj
	 *            the work entry passed to the FC.
	 * @return an empty Entry object.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Object perform(Object obj) throws Exception {
		
		if (!(obj instanceof Entry)) {
			throw new Exception(resHash.getString("DATA.CLEANSER.FC.EXPECTS.ENTRY"));
		}
		
		Entry work = (Entry) obj;
		
		String cdmAttrType = work.getString(CDM_ATTR_TYPE);
		
		//Check if the Attribute Type is set in Output map of FC.
		if (cdmAttrType != null) {
			cdmAttributeType = cdmAttrType;
		}
		
		if (cdmAttributeType == null || cdmAttributeType.equals("")){
			throw new Exception(resHash.getString("DATA.CLEANSER.FC.PARAMETER.NOT.PROVIDED", CDM_ATTR_TYPE));	
		}
		
		inputString = work.getString(INPUT_STRING);
		
		if (inputString == null || inputString.equals("")){
			throw new Exception(resHash.getString("DATA.CLEANSER.FC.PARAMETER.NOT.PROVIDED", INPUT_STRING));	
		}		
		
		//Pass the CDM Type and Input String to get the cleansed String 
		String outputString = dcs.getData(cdmAttributeType, inputString);
		
		Entry returnEntry = new Entry();
		if (outputString != null) {
			returnEntry.setAttribute(CLEANSED_STRING, outputString);
		}
		return returnEntry;
		
	}
		
	/**
	 * Retrieves a value, specified by the user.
	 * 
	 * @param parameterName
	 *            name of the parameter , String.
	 * @return the value of the parameter.
	 */
	private String getStringParameter(String parameterName) {
		String parameter = (String) getParam(parameterName);
		if (parameter != null) {
			parameter = parameter.trim();
		}
		return parameter;
	}
	
	/**
	 * Retrieves the Attribute Types which can be cleansed.
	 *  
	 * @return the Attribute Type which can be cleansed.
	 * 
	 * @throws Exception if error occurred.
	 */
	public Map<String, Object> getAttributeTypes() throws Exception {		
		
		String[] AtrTypes = dcs.getTypes();
		
		HashMap<String, Object> attributeTypes = new HashMap<String, Object>();
		
		for(int i=0; i< AtrTypes.length; i++){
			attributeTypes.put(AtrTypes[i], null);
		}
		
		return attributeTypes;		
	}

	
	/**
	 * Prints a debug message if debug mode for the Components is enabled.
	 * 
	 * @param msgKey
	 *            message key
	 * @param params
	 *            place holder for debug messages
	 */
	private void printDebugMessage(String msgKey, Object[] params) {
		if (params == null || params.length == 0) {
			debug(resHash.getString(msgKey));
		} else if (params.length == 1) {
			debug(resHash.getString(msgKey, params[0]));
		} else {
			debug(resHash.getString(msgKey, params));
		}
	}
	
	/**
	 * Version information.
	 * 
	 * @return version information.
	 */	
	public String getVersion() {
		return "1.0-di7.1.1 %I% 20%E%";
	}
		
}

