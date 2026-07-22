/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.emf;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.sdo.EDataObject;
import org.eclipse.emf.ecore.sdo.util.SDOUtil;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.w3c.dom.Element;

import com.ibm.di.entry.Entry;
import com.ibm.di.entry.Attribute;
import com.ibm.di.fc.Function;
import com.ibm.di.fc.webservice.axis2.WebServiceClient;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.config.interfaces.FunctionConfig;

/**
 * Function Component that using XML Schema converts an XML document to a tree
 * structure of Data Objects. It also implements a discover schema functionality
 * through its <code>updateSchema</code> method.
 * 
 * <br />
 * <br />
 * <b> THIS CLASS HAS BEEN DEPRECATED SINCE THE IBM Tivoli Directory Integrator 7.0
 * RELEASE AND WILL BE COMPLETELY REMOVED IN A FUTURE RELEASE! </b>
 */
@Deprecated
public class EmfXmlToSdo extends Function {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PARAM_XSD_FILE = "xsdFile";

	private static final String PARAM_NAMESPACE_MAP = "namespaceMap";

	private static final String PARAM_USE_NAMESPACES = "useNamespaces";

	private static final String PARAM_INPUT_XML_TYPE = "inputXMLType";

	private static final String PARAM_ENCODING = "encoding";

	private static final String INPUT_XML_STRING = "String";

	private static final String INPUT_XML_DOM = "DOMElement";

	private static final String IN_ATTRIBUTE_XML_STRING = "xmlString";

	private static final String IN_ATTRIBUTE_DOM_ELEMENT = "xmlDOMElement";

	private static final String PROPERTIES_FILE = "emfxmltosdo";

	private static ResourceHash sResHash = null;

	private static ModelParser sModelParser = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
		sModelParser = new ModelParser(sResHash);
	}

	private String mXSDFile = null;

	private String mInputXMLType = null;

	private String mEncoding = null;

	private Map mURIToPrefixMap = null;

	private boolean mUseNamespaces = false;

	private ResourceSet mResourceSet = null;

	private ExtendedMetaData mMetaData = null;

	/**
	 * Initializes the function component. Sets instance members corresponding
	 * to the function component paramters.
	 * 
	 * @throws Exception
	 *             if the <code>xsdFile</code> function component parameter is
	 *             not specified if the file specified by <code>xsdFile</code>
	 *             function component parameter is not found if the file
	 *             specified by <code>xsdFile</code> function component
	 *             parameter is not valid XML Schema file
	 */
	@Deprecated
	public void initialize(Object obj) throws Exception {

		mXSDFile = getRequiredStringParameter(PARAM_XSD_FILE);

		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_XSD_FILE, mXSDFile }));
		}

		mInputXMLType = getRequiredStringParameter(PARAM_INPUT_XML_TYPE);

		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_INPUT_XML_TYPE, mInputXMLType }));
		}

		mUseNamespaces = getBooleanParameter(PARAM_USE_NAMESPACES);

		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_USE_NAMESPACES,
							Boolean.valueOf(mUseNamespaces) }));
		}

		String namespaces = null;
		if (mUseNamespaces) {
			namespaces = (String) getParam(PARAM_NAMESPACE_MAP);
			if (getDebug()) {
				logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
						new Object[] { PARAM_NAMESPACE_MAP, namespaces }));
			}
		}
		mURIToPrefixMap = sModelParser.parseNamespaces(namespaces, false);

		mEncoding = (String) getParam(PARAM_ENCODING);

		if (mEncoding == null || mEncoding.trim().equals("")) {

			mEncoding = (new OutputStreamWriter(new ByteArrayOutputStream()))
					.getEncoding();
		}

		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_ENCODING, mEncoding }));
		}

		createMetaData();

		super.initialize(null);
	}

	/**
	 * Converts an XML document to Data Objects and sets the working entry
	 * attributes. The XML document is expected in <code>xmlString</code>
	 * Entry Attribute. The method creates Entry Attributes corresponding to the
	 * XML elements and attributes.
	 * 
	 * @param obj
	 *            the object is the working IBM Tivoli Directory Integrator
	 *            Entry and should be of type com.ibm.di.entry.Entry.
	 * 
	 * @return IBM Tivoli Directory Integrator Entry containing Entry Attributes
	 *         coresponding to the XML elements and attributes.
	 * 
	 * @throws Exception
	 *             if the IBM Tivoli Directory Integrator Entry does not contain
	 *             a <code>xmlString</code> attribute. if the
	 *             <code>xmlString</code> does not contain valid XML document.
	 */
	@Deprecated
	public Object perform(Object obj) throws Exception {
		verifyInitialized();

		if (!(obj instanceof Entry)) {
			String errorMessage = sResHash
					.getString("INVALID.PERFORM.PARAMETER.TYPE");
			logerror(errorMessage);
			throw new Exception(errorMessage);
		}
		Entry entry = (Entry) obj;

		String xmlString = null;
		if (mInputXMLType.equals(INPUT_XML_STRING)) {
			xmlString = (String) entry.getObject(IN_ATTRIBUTE_XML_STRING);
			if (xmlString == null) {
				String errorMessage = sResHash.getString(
						"ENTRY.ATTRIBUTE.NOT.PRESENT", IN_ATTRIBUTE_XML_STRING);
				logerror(errorMessage);
				throw new Exception(errorMessage);
			}
		} else {
			Object xmlDOMElement = entry.getObject(IN_ATTRIBUTE_DOM_ELEMENT);
			if (xmlDOMElement == null) {
				String errorMessage = sResHash.getString(
						"ENTRY.ATTRIBUTE.NOT.PRESENT2",
						IN_ATTRIBUTE_DOM_ELEMENT);
				logerror(errorMessage);
				throw new Exception(errorMessage);
			}
			xmlString = WebServiceClient.getAsString((Element) xmlDOMElement);
		}

		logdebug(sResHash.getString("XML.FROM.DOM.ELEMENT.TO.XML.INFO",
				xmlString));

		EDataObject root = convertToDataObjects(xmlString);

		if (getDebug()) {
			logdebug(sResHash.getString("XML.PARSING.COMPLETED"));
		}

		GraphObjects graphObjects = new GraphObjects(sResHash);
		List attributeList = graphObjects.getAttributes(root, mURIToPrefixMap,
				mUseNamespaces);

		if (getDebug()) {
			logdebug(sResHash.getString("ENTRY.ATTRIBUTES.CREATED"));
		}

		for (Iterator iter = attributeList.iterator(); iter.hasNext();) {
			Attribute attribute = (Attribute) iter.next();
			entry.setAttribute(attribute);
		}

		return entry;
	}

	/**
	 * Implements the discovery schema functionality. The method reads the
	 * specified XML Schema File and creates Entry Attribute names based on the
	 * nesting of the XML elements.
	 * 
	 * @param config
	 *            contains the function configuration parameter.
	 * 
	 * @return returns true on success
	 * 
	 * @throws Exception
	 *             if the the <code>xsdFile</code> parameter is not specified
	 *             if the file specified by <code>xsdFile</code> parameter is
	 *             not found if the file specified by <code>xsdFile</code>
	 *             parameter is not valid XML Schema file
	 */
	@Deprecated
	public boolean updateSchema(FunctionConfig config) throws Exception {
		setConfiguration(config.getFunctionConfig());

		String xsdFile = getRequiredStringParameter(PARAM_XSD_FILE);

		boolean useNamespaces = getBooleanParameter(PARAM_USE_NAMESPACES);
		String namespaces = null;
		if (useNamespaces) {
			namespaces = (String) getParam(PARAM_NAMESPACE_MAP);
		}
		Map uriToPrefixMap = sModelParser.parseNamespaces(namespaces, false);

		UpdateSchema updater = new UpdateSchema(xsdFile, uriToPrefixMap,
				useNamespaces, false, sResHash);
		updater.updateSchema(config);

		return true;
	}

	/**
	 * Returns function component's version
	 */
	@Deprecated
	public String getVersion() {
		return "2.0-di7.1.1 %I% 20%E%";

	}

	/**
	 * Converts XML document to Data Objects. A tree-like structure of Data
	 * Objects is created. The structure corresponds to the structure fo the XML
	 * document. If the XML document is not valid according to the XML Schema
	 * file an exception is thrown.
	 * 
	 * @param xmlString
	 *            string object containing the XML document to be converted.
	 * 
	 * @return the Data Object corresponding to the XML root.
	 * @throws Exception
	 *             if the <code>xmlString</code> parameter do not contain
	 *             valid XML document.
	 */
	private EDataObject convertToDataObjects(String xmlString) throws Exception {
		byte[] xmlBytes = xmlString.getBytes(mEncoding);
		ByteArrayInputStream xmlByteInputStream = new ByteArrayInputStream(
				xmlBytes);

		HashMap loadOptions = new HashMap();
		loadOptions.put("EXTENDED_META_DATA", mMetaData);

		Resource resource = mResourceSet.createResource(URI.createURI("*.xml"));
		try {
			resource.load(xmlByteInputStream, loadOptions);
		} catch (Exception e) {
			String errorMessage = sResHash.getString("ERROR.PROCESSING.XML", e
					.getMessage());
			logerror(errorMessage);
			throw new Exception(errorMessage);
		}

		EDataObject root = (EDataObject) resource.getContents().get(0);

		return root;
	}

	/**
	 * Creates a Ecore Meta Data for the XML Schema file specified by the
	 * <code>mXSDFile</code> instance member variable
	 */
	private void createMetaData() throws Exception {
		Collection packageCollection = null;
		try {
			packageCollection = sModelParser.getPackageCollection(mXSDFile);

			if (getDebug()) {
				logdebug(sResHash.getString("XML.SCHEMA.PARSED"));
			}

			sModelParser.checkPackagesForConformity(packageCollection,
					mUseNamespaces);

			if (getDebug()) {
				logdebug(sResHash.getString("ECORE.MODEL.CHECKED"));
			}
		} catch (Exception e) {
			logerror(e.getMessage());
			throw e;
		}

		mResourceSet = SDOUtil.createResourceSet();
		Registry packageRegistry = mResourceSet.getPackageRegistry();
		ModelParser.updatePackageRegistry(packageCollection, packageRegistry);
		ModelParser.updateFactoryInstances(packageCollection);

		mMetaData = new BasicExtendedMetaData(packageRegistry);
	}

	/**
	 * Gets a required function component parameter and returns it as string. If
	 * the parameter is not present or is empty the method throws an exception.
	 * 
	 * @param parameterName
	 *            specifies the required parameter's name
	 * 
	 * @return the parameter as String object
	 * @throws Exception
	 *             if the parameter does not exist or is empty
	 */
	private String getRequiredStringParameter(String parameterName)
			throws Exception {
		String parameter = (String) getParam(parameterName);
		if (parameter != null && !parameter.equals("")) {
			parameter = parameter.trim();
		} else {
			String errorMessage = sResHash.getString(
					"REQUIRED.PARAMETER.NOT.SET", parameterName);
			logerror(errorMessage);
			throw new Exception(errorMessage);
		}

		return parameter;
	}

	/**
	 * Gets a string parameter and converts it to a primitive boolean value.
	 */
	private boolean getBooleanParameter(String parameterName) {
		return Boolean.valueOf((String) getParam(parameterName)).booleanValue();
	}

	/**
	 * Logs an error message if the <code>logger</code> object is present.
	 * 
	 * @param errorMessage
	 *            the error message to be logged.
	 */
	private void logerror(String errorMessage) {
		if (logger != null) {
			logger.logerror(errorMessage);
		}
	}

	/**
	 * Logs a debug information if the <code>logger</code> object is present.
	 * 
	 * @param debugMessage
	 *            the debug information to be logged.
	 */
	private void logdebug(String debugMessage) {
		if (logger != null) {
			logger.logdebug(debugMessage);
		}
	}
}
