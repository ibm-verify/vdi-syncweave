/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.emf;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.sdo.EDataObject;
import org.eclipse.emf.ecore.sdo.util.SDOUtil;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.w3c.dom.Element;

import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;
import com.ibm.di.fc.webservice.axis2.WebServiceClient;
import com.ibm.di.server.ResourceHash;
import commonj.sdo.DataObject;

/**
 * Function Component that using XML Schema converts Data Objects to an XML
 * document. It also implements a discover schema functionality through its
 * <code>updateSchema</code> method.
 * 
 * <br />
 * <br />
 * <b> THIS CLASS IS DEPRECATED FOR THE IBM Tivoli Directory Integrator 7.0 or 7.1
 * RELEASES AND WILL BE COMPLETELY REMOVED IN A FUTURE RELEASE! </b>
 */
@Deprecated
public class EmfSdoToXml extends Function {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PARAM_XSD_FILE = "xsdFile";

	private static final String PARAM_NAMESPACE_MAP = "namespaceMap";

	private static final String PARAM_USE_NAMESPACES = "useNamespaces";

	private static final String PARAM_RETURN_XML_TYPE = "returnXMLType";

	private static final String PARAM_ENCODING = "encoding";

	private static final String RETURN_XML_STRING = "String";

	private static final String RETURN_XML_DOM = "DOMElement";

	private static final String OUT_ATTRIBUTE_XML_STRING = "xmlString";

	private static final String OUT_ATTRIBUTE_DOM_ELEMENT = "xmlDOMElement";

	private static final String PROPERTIES_FILE = "emfsdotoxml";

	private static class Node implements Comparable<Node> {

		private Attribute mAttribute;

		private int mLevel;

		public Node(Attribute newAttribute) {
			mAttribute = newAttribute;

			String name = mAttribute.getName();
			mLevel = 0;
			for (int i = 0; i < name.length(); i++) {
				if (ModelParser.isSeparator(name.charAt(i))) {
					mLevel++;
				}
			}
		}

		public Attribute getAttribute() {
			return mAttribute;
		}

		public boolean isComposite() {
			return mLevel > 0;
		}

		public int compareTo(Node obj) {
			return mLevel - obj.mLevel;
		}

		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof Node) {
				return compareTo((Node)obj) == 0;
			}
			return false;
		}

		public int hashCode() {
			return mLevel;
		}
	}

	private static ResourceHash sResHash = null;

	private static ModelParser sModelParser = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
		sModelParser = new ModelParser(sResHash);
	}

	private String mXSDFile = null;

	private String mReturnXMLType = null;

	private String mEncoding = null;

	private Map mPrefixToURIMap = null;

	/**
	 * Creates and returns a Data Object corresponding to the root object of a
	 * document compliant to the XML Schema specified in the
	 * <code>xsdFile</code> file.
	 * 
	 * @param xsdFile
	 *            location of a XML Schema file.
	 * 
	 * @return Data Object corresponding to the root object of a document
	 *         compliant to the XML Schema specified
	 * 
	 * @throws Exception
	 *             if the specified file cannot be found or is not valid XML
	 *             Schema. if the XML Schema does not define a root element.
	 */
	@Deprecated
	public static DataObject createRootObject(String xsdFile) throws Exception {
		return sModelParser.createRootObject(xsdFile);
	}

	/**
	 * Initializes the function component. Sets instance members corresponding
	 * to the function component paramters.
	 * 
	 * @throws Exception
	 *             if the xsdFile parameter is not specified
	 */
	@Deprecated
	public void initialize(Object obj) throws Exception {

		mXSDFile = getRequiredStringParameter(PARAM_XSD_FILE);

		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_XSD_FILE, mXSDFile }));
		}

		mReturnXMLType = getRequiredStringParameter(PARAM_RETURN_XML_TYPE);

		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_RETURN_XML_TYPE, mReturnXMLType }));
		}

		String namespaces = (String) getParam(PARAM_NAMESPACE_MAP);

		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_NAMESPACE_MAP, namespaces }));
		}

		mEncoding = (String) getParam(PARAM_ENCODING);

		if (mEncoding == null || mEncoding.trim().equals("")) {

			mEncoding = (new OutputStreamWriter(new ByteArrayOutputStream()))
					.getEncoding();
		}

		if (getDebug()) {
			logdebug(sResHash.getString("COMPONENT.PARAMETER.INITIALIZED",
					new Object[] { PARAM_ENCODING, mEncoding }));
		}

		mPrefixToURIMap = sModelParser.parseNamespaces(namespaces, true);

		super.initialize(null);
	}

	/**
	 * Serializes Data Objects to XML document according to the XML Schema
	 * specified by the <code>xsdFile</code> function component parameter. The
	 * Data Objects are stored in the Entry Attributes of the IBM Tivoli
	 * Directory Integrator Entry passed as parameter to the function.
	 * 
	 * @param obj
	 *            the object is the working IBM Tivoli Directory Integrator
	 *            Entry and should be of type com.ibm.di.entry.Entry.
	 * 
	 * @return IBM Tivoli Directory Integrator Entry containing Entry Attributes
	 *         coresponding to the XML elements and attributes.
	 * @throws Exception
	 *             if the names of the Entry Attributes do not represent valid
	 *             XML elements. if an Entry Attribute for the root element does
	 *             not exist in the entry and the XML Schema does not define a
	 *             root element.
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

		ResourceSet resourceSet = SDOUtil.createResourceSet();

		Registry packageRegistry = resourceSet.getPackageRegistry();
		EPackage rootPackage = processPackages(packageRegistry);

		if (getDebug()) {
			logdebug(sResHash.getString("XML.SCHEMA.PARSED"));
		}

		EDataObject root = (EDataObject) entry.getObject(ModelParser.ROOT_NAME);
		if (root == null) {
			if (rootPackage != null) {
				root = ModelParser.createRootObject(rootPackage);
			} else {
				String errorMessage = sResHash.getString("NO.ROOT.IN.SCHEMA");
				logerror(errorMessage);
				throw new Exception(errorMessage);
			}
		}

		applyAttributes(root, entry);

		if (getDebug()) {
			logdebug(sResHash.getString("ENTRY.ATTRIBUTES.APPLIED"));
		}

		String xmlString = convertToXML(root, resourceSet);
		logdebug(sResHash.getString("CONVERT.TO.XML.INFO", xmlString));

		if (mReturnXMLType.equals(RETURN_XML_STRING)) {
			entry.setAttribute(OUT_ATTRIBUTE_XML_STRING, xmlString);
		} else {
			Element xmlDOMElement = WebServiceClient.getAsDOM(xmlString);
			entry.setAttribute(OUT_ATTRIBUTE_DOM_ELEMENT, xmlDOMElement);
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
	 * @throws Exception
	 *             If the the xsdFile parameter is not specified. If the the
	 *             specified file extension is not .xsd If the target file is
	 *             not found If the target file is not valid XML Schema file
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
				useNamespaces, true, sResHash);
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
	 * Adds the Data Objects from the Entry Attribute to the tree structure
	 * rooted at <code>root</code>
	 * 
	 * @param root
	 *            the root of the tree structure
	 * @param entry
	 *            IBM Tivoli Directory Integrator Entry containing the
	 *            attributes to be added to the structure
	 * 
	 * @throws Exception
	 *             if the Entry Attribute names or values are not valid
	 *             according to the XML Schema.
	 */
	private void applyAttributes(EDataObject root, Entry entry)
			throws Exception {
		String[] attributeNames = entry.getAttributeNames();
		int attributeCount = attributeNames.length;

		Node[] nodes = new Node[attributeCount];
		for (int i = 0; i < attributeCount; i++) {
			Attribute attribute = entry.getAttribute(attributeNames[i]);
			nodes[i] = new Node(attribute);
		}
		Arrays.sort(nodes);

		try {
			GraphObjects graphObjects = new GraphObjects(sResHash);
			for (int i = 0; i < attributeCount; i++) {
				if (nodes[i].isComposite()) {
					Attribute attribute = nodes[i].getAttribute();
					Object value = attribute.getValue(0);
					if (value instanceof EDataObject) {
						EClass rootClass = root.eClass();
						String attributeName = attribute.getName();
						EClass objectClass = ((EDataObject) value).eClass();
						graphObjects.applyClassToModel(rootClass,
								attributeName, objectClass, mPrefixToURIMap);
					}
				}
			}

			for (int i = 0; i < attributeCount; i++) {
				if (nodes[i].isComposite()) {
					Attribute attribute = nodes[i].getAttribute();
					graphObjects.applyAttribute(root, attribute,
							mPrefixToURIMap);
				}
			}
		} catch (Exception e) {
			logerror(e.getMessage());
			throw e;
		}
	}

	/**
	 * Converts the tree structure of Data Objects rooted at <code>root</code>
	 * to a XML and returns it as a String object.
	 */
	private String convertToXML(EDataObject root, ResourceSet resourceSet)
			throws Exception {
		Registry packageRegistry = resourceSet.getPackageRegistry();
		ExtendedMetaData metaData = new BasicExtendedMetaData(packageRegistry);
		HashMap saveOptions = new HashMap();
		saveOptions.put("EXTENDED_META_DATA", metaData);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Resource resource = resourceSet.createResource(URI.createURI("*.xml"));

		if (resource instanceof XMIResource) {
			((XMIResource) resource).setEncoding(mEncoding);
		}
		if (resource instanceof XMLResource) {
			((XMLResource) resource).setEncoding(mEncoding);
		}

		resource.getContents().add(root);

		try {
			resource.save(outputStream, saveOptions);
		} catch (Exception e) {
			String errorMessage = sResHash.getString(
					"ERROR.CONVERTING.DATA.OBJECTS", e.getMessage());
			logerror(errorMessage);
			throw new Exception(errorMessage);
		}

		String xmlString = outputStream.toString(mEncoding);

		return xmlString;
	}

	/**
	 * Creates package collection according to the XML Schema file, adds
	 * packages to the package registry, updates the factory instances of the
	 * packages and returns the package which describes the root element.
	 * 
	 * @param packageRegistry
	 * 
	 * @return Retures the processed package.
	 * 
	 * @throws Exception
	 *             if the XML Schema file specified by the <code>mXSDFile</code>
	 *             instance member variable is not found or is not valid XML
	 *             Schema file.
	 */
	private EPackage processPackages(Registry packageRegistry) throws Exception {
		Collection packageCollection = null;
		try {
			packageCollection = sModelParser.getPackageCollection(mXSDFile);
		} catch (Exception e) {
			logerror(e.getMessage());
			throw e;
		}

		ModelParser.updatePackageRegistry(packageCollection, packageRegistry);
		ModelParser.updateFactoryInstances(packageCollection);
		EPackage rootPackage = ModelParser.getRootPackage(packageCollection);

		return rootPackage;
	}

	/**
	 * Gets required function component parameter and returns it as string. If
	 * the parameter is not present or is empty throws an exception.
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
