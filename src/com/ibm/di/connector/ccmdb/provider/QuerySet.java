/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.provider;

import java.io.IOException;
import java.net.URL;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import com.ibm.di.util.ResourceLocator;

/**
 * This class is responsible for reading of static SQL statements 
 * stored in a configurationXML file.
 * 
 * @author yavor.gologanov
 *
 */
public class QuerySet {
	
	private static final String QUERY_FILE = "queries.xml";
	
	public static final String ACTCIRELATION_SELECT_CLASSIFICATIONS = "ACTCIRELATION.SELECT.CLASSIFICATIONS";
	public static final String ACTCIRELATION_SELECT_NEXT_ACTCIRELID = "ACTCIRELATION.SELECT.NEXT_ACTCIRELID";
	public static final String ACTCIRELATION_SELECT_ALL = "ACTCIRELATION.SELECT.ALL";
	public static final String ACTCIRELATION_SELECT_BY_RST = "ACTCIRELATION.SELECT.BY_RST";	
	public static final String ACTCIRELATION_SELECT_BY_TRG = "ACTCIRELATION.SELECT.BY_TRG";
	public static final String ACTCIRELATION_SELECT_BY_SRC = "ACTCIRELATION.SELECT.BY_SRC";
	public static final String ACTCIRELATION_SELECT_BY_RELATIONNUM = "ACTCIRELATION.SELECT.BY_RELATIONNUM";
	public static final String ACTCIRELATION_SELECT_PROPERTIES_DEF = "ACTCIRELATION.SELECT.PROPERTIES.DEF";	
	public static final String ACTCIRELATION_DELETE = "ACTCIRELATION.DELETE";
	public static final String ACTCIRELATION_DELETE_BY_GUID = "ACTCIRELATION.DELETE.BY_GUID";
	
	public static final String ACTCI_SELECT_CLASSIFICATIONS = "ACTCI.SELECT.CLASSIFICATIONS";
	public static final String ACTCI_SELECT_CLASSIFICATIONS_BY_PARENT = "ACTCI.SELECT.CLASSIFICATIONS.BY_PARENT";
	public static final String ACTCI_SELECT_NEXT_ACTCIID = "ACTCI.SELECT.NEXT_ACTCIID";
	public static final String ACTCI_SELECT_BY_GUID = "ACTCI.SELECT.BY_GUID";
	public static final String ACTCI_SELECT_ALL = "ACTCI.SELECT.ALL";
	public static final String ACTCI_SELECT_BY_CLASSSTRUCTURE = "ACTCI.SELECT.BY_CLASSSTRUCTURE";
	public static final String ACTCI_SELECT_PROPERTIES_DEF = "ACTCI.SELECT.PROPERTIES.DEF";	
	public static final String ACTCI_DELETE_BY_GUID = "ACTCI.DELETE.BY_GUID";
	
	public static final String OMP_SELECT_BY_GUID = "OMP.SELECT.BY_GUID";		
	public static final String OMP_SELECT_PROPERTIES_DEF = "OMP.SELECT.PROPERTIES.DEF";
	public static final String OMPCIRLN_SELECT_BY_ACTCIGUID = "OMPCIRLN.SELECT.BY_ACTCIGUID";	
	public static final String OMPCIRLN_SELECT_PROPERTIES_DEF = "OMPCIRLN.SELECT.PROPERTIES.DEF";
	
	public static final String CCIDELETEDACTCI_SELECT_PROPERTIES_DEF = "CCIDELETEDACTCI.SELECT.PROPERTIES.DEF";	
	public static final String CCIDELETEDACTCI_SELECT_BY_SRC = "CCIDELETEDACTCI.SELECT.BY_SRC";	
	
	public static final String ACTCISPEC_SELECT_BY_ACTCINUM = "ACTCISPEC.SELECT.BY_ACTCINUM";	
	public static final String ACTCISPEC_SELECT_NEXT_ID = "ACTCISPEC.SELECT.NEXT_ID";
	public static final String ACTCISPEC_DELETE = "ACTCISPEC.DELETE";
	public static final String ACTCISPEC_DELETE_BY_ACTCINUM = "ACTCISPEC.DELETE.BY_ACTCINUM";
	
	public static final String RELATIONRULES_SELECT_ST = "RELATIONRULES.SELECT_ST";	
	public static final String RELATIONRULES_SELECT_BY_TRG = "RELATIONRULES.SELECT.BY_TRG";	
	public static final String RELATIONRULES_SELECT_BY_SRC = "RELATIONRULES.SELECT.BY_SRC";	
	
	public static final String CLASSSPEC_SELECT_BY_CLASSSTRUCTURE = "CLASSSPEC.SELECT.BY_CLASSSTRUCTURE";	
		
	public static final String ASSETATTRID_SELECT_MODEL_OBJECT = "ASSETATTRID.SELECT.MODEL_OBJECT";
	
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	
	private Properties queryProperties = null;	
	
	/**
	 * 
	 * @throws InvalidPropertiesFormatException
	 * @throws IOException
	 */
	public QuerySet() throws InvalidPropertiesFormatException, IOException {
		URL url = ResourceLocator.getResourceURL(QUERY_FILE);
		this.queryProperties = new Properties();
		this.queryProperties.loadFromXML(url.openStream());
	}
	
	/**
	 * 
	 * @param queryName
	 * @return String
	 */
	public String getSQLQuery(String queryName) {
		return queryProperties.getProperty(queryName);
	}	
	
}
