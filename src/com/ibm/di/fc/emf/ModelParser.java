/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.emf;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.sdo.EDataObject;
import org.eclipse.emf.ecore.sdo.impl.DynamicEDataObjectImpl;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.xsd.XSDDiagnostic;
import org.eclipse.xsd.ecore.XSDEcoreBuilder;

import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.StringTokenizer;

/**
 * Utility class providing a set functions related to working with the Ecore
 * model used by EMF XMLToSDO and SDOToXML Function Components.
 * 
 * <br />
 * <br />
 * <b> THIS CLASS HAS BEEN DEPRECATED SINCE THE SyncWeave 7.0
 * RELEASE AND WILL BE COMPLETELY REMOVED IN A FUTURE RELEASE! </b>
 */
@Deprecated
public class ModelParser {

	/**
	 * The name of the XML root object. The name is used in the path
	 * expressions.
	 */
	public final static String ROOT_NAME = "DocRoot";

	/**
	 * The name of the feature used in the Ecore models for the namespaces maps.
	 */
	public final static String XMLNSPREFIX_FEATURE_NAME = "xMLNSPrefixMap";

	/**
	 * The name of the classes used in the Ecore models for the document root
	 * classes.
	 */
	public final static String DOCROOT_CLASSNAME = "DocumentRoot";

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final static String MIXED_ATTRIBUTE_NAME = ":mixed";

	private final static String XMLNSPREFIX_ATTRIBUTE_NAME = "xmlns:prefix";

	private final static String SCHEMALOCATION_ATTRIBUTE_NAME = "xsi:schemaLocation";

	private final static String FEATURE_KIND = "kind";

	private final static String FEATURE_KIND_ATTRIBUTE = "attribute";

	private final static String FEATURE_KIND_ELEMENT = "element";

	private final static String MAPPING_NAMESPACE_DELIMITER = "\n";

	private final static String MAPPING_PREFIX_DELIMITER = "=";

	private final static String NAMESPACE_SEPARATOR = ":";

	private final static char ELEMENT_SEPARATOR = '@';

	private final static char ATTRIBUTE_SEPARATOR = '#';

	/**
	 * Set with ignored element or attribute names
	 */
	private static Set sIgnoreSet = null;

	static {
		sIgnoreSet = new HashSet();

		sIgnoreSet.add(MIXED_ATTRIBUTE_NAME);
		sIgnoreSet.add(XMLNSPREFIX_ATTRIBUTE_NAME);
		sIgnoreSet.add(SCHEMALOCATION_ATTRIBUTE_NAME);
	}

	private ResourceHash mResHash = null;

	/**
	 * Checks if the specified string is a ignored feature name.
	 */
	@Deprecated
	public static boolean isIgnored(String name) {
		return sIgnoreSet.contains(name);
	}

	/**
	 * Checks if the specified character is a feature path separator.
	 */
	@Deprecated
	public static boolean isSeparator(char c) {
		return c == ATTRIBUTE_SEPARATOR || c == ELEMENT_SEPARATOR;
	}

	/**
	 * Returns the name as specified in the XML Schema of an XML element or
	 * attribute represented by the <code>feature</code> object.
	 */
	@Deprecated
	public static String getFeatureName(EStructuralFeature feature) {
		return ExtendedMetaData.INSTANCE.getName(feature);
	}

	/**
	 * Returns the namespace URI of the XML element or attribute represented by
	 * the <code>feature</code> object.
	 */
	@Deprecated
	public static String getFeatureNamespace(EStructuralFeature feature) {
		return feature.getEContainingClass().getEPackage().getNsURI();
	}

	/**
	 * Checks if the specified <code>feature</code> object represents an XML
	 * attribute.
	 */
	@Deprecated
	public static boolean isAttribute(EStructuralFeature feature) {
		EAnnotation annotation = (EAnnotation) feature.getEAnnotations().get(0);
		EMap details = annotation.getDetails();
		String featureKind = (String) details.get(FEATURE_KIND);

		return featureKind.equals(FEATURE_KIND_ATTRIBUTE);
	}

	/**
	 * Checks if the specified <code>feature</code> object represents an XML
	 * element.
	 */
	@Deprecated
	public static boolean isElement(EStructuralFeature feature) {
		EAnnotation annotation = (EAnnotation) feature.getEAnnotations().get(0);
		EMap details = annotation.getDetails();
		String featureKind = (String) details.get(FEATURE_KIND);

		return featureKind.equals(FEATURE_KIND_ELEMENT);
	}

	/**
	 * Appends an XML element or attribute name to the current path. The method
	 * qualifies the name with namespace prefix if such exists in the
	 * <code>uriToPrefixMap</code> parameter, or with a namespace URI if a
	 * prefix does not exist and the <code>alwaysPrefix</code> parameter is
	 * <code>true</code>.
	 * 
	 * @param feature
	 *            EStructuralFeature object that represents an XML element or
	 *            attribute.
	 * @param path
	 *            the path expression from the root till the current XML element
	 *            or attribute.
	 * @param uriToPrefixMap
	 *            namespace URI to prefix map.
	 * @param alwaysPrefix
	 *            if <code>true</code> and no prefix is defined for the
	 *            namespace of the XML element or attribute, for prefix is used
	 *            the namespace URI.
	 */
	@Deprecated
	public static String makeFeaturePath(EStructuralFeature feature,
			String path, Map uriToPrefixMap, boolean alwaysPrefix) {
		String featureName = getFeatureName(feature);
		String namespacePrefix = getNamespacePrefix(feature, uriToPrefixMap,
				alwaysPrefix);
		if (namespacePrefix != null && !namespacePrefix.equals("")) {
			featureName = namespacePrefix + ":" + featureName;
		}
		char separator = (isAttribute(feature) ? ATTRIBUTE_SEPARATOR
				: ELEMENT_SEPARATOR);
		String featurePath = path + separator + featureName;

		return featurePath;
	}

	/**
	 * Searches for a separator in the feature path and returns the position of
	 * the first separator or -1 if there are no separators.
	 */
	@Deprecated
	public static int getSeparatorPosition(String path) {
		int separatorIndex = -1;
		int pathLength = path.length();
		for (int pos = 0; pos < pathLength; pos++) {
			char ch = path.charAt(pos);
			if (isSeparator(ch)) {
				separatorIndex = pos;
				break;
			}
		}

		return separatorIndex;
	}

	/**
	 * Searches the package collection for a package that defines a root class.
	 * 
	 * @param packageCollection
	 *            collection of EPackage objects
	 * @return a package that defines a root class
	 */
	@Deprecated
	public static EPackage getRootPackage(Collection packageCollection) {
		EPackage rootPackage = null;

		for (Iterator i = packageCollection.iterator(); i.hasNext();) {
			EPackage epackage = (EPackage) i.next();
			EClass rootClass = getRootClass(epackage);
			if (rootClass != null) {
				rootPackage = epackage;
				break;
			}
		}

		return rootPackage;
	}

	/**
	 * Creates a Data Object of type the root class of the <code>epackage</code>
	 * package.
	 * 
	 * @param epackage
	 *            specifies the package which root class is used in the object
	 *            creation
	 * @return Data Object of type the root class of the <code>epackage</code>
	 *         package. If the package has no root class <code>null</code> is
	 *         returned.
	 */
	@Deprecated
	public static EDataObject createRootObject(EPackage epackage) {
		EDataObject root = null;

		EClass rootClass = getRootClass(epackage);
		if (rootClass != null) {
			EFactory factory = epackage.getEFactoryInstance();
			root = (EDataObject) factory.create(rootClass);

			EStructuralFeature feature = rootClass
					.getEStructuralFeature(XMLNSPREFIX_FEATURE_NAME);
			EMap xmlnsMap = (EMap) root.eGet(feature);
			xmlnsMap.put("", epackage.getNsURI());
		}

		return root;
	}

	/**
	 * Traverses a collection of EPackages and adds them to a package registry.
	 * 
	 * @param packageCollection
	 *            a collection of EPackages
	 * @param packageRegistry
	 *            a package registry
	 */
	@Deprecated
	public static void updatePackageRegistry(Collection packageCollection,
			Registry packageRegistry) {
		for (Iterator i = packageCollection.iterator(); i.hasNext();) {
			EPackage epackage = (EPackage) i.next();
			String nsURI = epackage.getNsURI();
			packageRegistry.put(nsURI, epackage);
		}
	}

	/**
	 * Sets the factory instances of all EPackages in the collection to factory
	 * instances compliant to the EDataObject interface.
	 * 
	 * @param packageCollection
	 *            collection of EPackage objects
	 */
	@Deprecated
	public static void updateFactoryInstances(Collection packageCollection) {
		for (Iterator i = packageCollection.iterator(); i.hasNext();) {
			EPackage epackage = (EPackage) i.next();
			epackage
					.setEFactoryInstance(new DynamicEDataObjectImpl.FactoryImpl());
		}
	}

	/**
	 * Returns the root class of the specified package.
	 */
	@Deprecated
	public static EClass getRootClass(EPackage epackage) {
		return (EClass) epackage.getEClassifier(ModelParser.DOCROOT_CLASSNAME);
	}

	/**
	 * Returns a namespace prefix for an XML element or attribute represented by
	 * the <code>feature</code> object. The feature's namespace URI is
	 * determined and a lookup in the namespace URI to prefix map is made. In
	 * case of lookup failure and the <code>alwaysPrefix</code> parameter is
	 * <code>true</code>, the namespace URI is returned as prefix.
	 * 
	 * @param feature
	 *            feature object representing an XML element or attribute.
	 * @param uriToPrefixMap
	 *            a namespace URI to prefix map.
	 * @param alwaysPrefix
	 *            specifies if no prefix is found the namespace URI to be
	 *            returned as prefix value.
	 * @return a prefix corresponding to the namespace of the XML element or
	 *         attribute represented by the <code>feature</code> object. If no
	 *         prefix is found in the <code>namespaceMap</code> map and the
	 *         <code>alwaysPrefix</code> parameter is true the namespace URI
	 *         is returned. In all other cases the return value is
	 *         <code>null</code>.
	 */
	private static String getNamespacePrefix(EStructuralFeature feature,
			Map uriToPrefixMap, boolean alwaysPrefix) {
		String featureNamespace = getFeatureNamespace(feature);
		String prefix = (String) uriToPrefixMap.get(featureNamespace);
		if (prefix == null && alwaysPrefix) {
			prefix = featureNamespace;
		}

		return prefix;
	}

	/**
	 * Checks if the namespace of the XML element or attribute represented by
	 * the <code>feature</code> object is the same as the namespace
	 * corresponding to the namespace prefix <code>nsPrefix</code> according
	 * to the prefix to namespace URI map. If the map does not contain an entry
	 * for the <code>nsPrefix</code> prefix, the parameter is interpreted as a
	 * namespace URI instead of prefix.
	 * 
	 * @param feature
	 *            feature object representing XML element or attribute, which
	 *            namespace is compared.
	 * @param nsPrefix
	 *            namespace prefix or namespace URI.
	 * @param prefixToURIMap
	 *            namespace prefix to namespace URI map.
	 * @return <code>true</code> if the namespace prefix (or URI)
	 *         <code>nsPrefix</code> refers to the namespace of the
	 *         <code>feature</code> object.
	 */
	private static boolean compareNamespaces(EStructuralFeature feature,
			String nsPrefix, Map prefixToURIMap) {
		String namespaceURI = (String) prefixToURIMap.get(nsPrefix);
		if (namespaceURI == null) {
			namespaceURI = nsPrefix;
		}

		String featureNamespace = getFeatureNamespace(feature);

		return featureNamespace.equals(namespaceURI);
	}

	/**
	 * Initializing the class instances.
	 */
	@Deprecated
	public ModelParser(ResourceHash resHash) {
		mResHash = resHash;
	}

	/**
	 * Checks if the packages conform to the restrictions set by the EMF
	 * XMLToSDO and SDOToXML Function Components. At this moment the only
	 * restriction checked is for case-insensitive equality in names of sibling
	 * XML elements or attributes.
	 * 
	 * @param packageCollection
	 *            the Ecore model package collection.
	 * @param isNamespaceAware
	 *            if <code>true</code> siblings with case-insensitive equality
	 *            in names but from different namespaces will be considered
	 *            different.
	 * @throws Exception
	 *             if there are siblings with case-insenstive equal names and
	 *             the XML elements or attributes are in the same namespace.
	 * @throws Exception
	 *             if there are siblings with case-insensitive equal names and
	 *             the XML elements or attributes are in different namespaces
	 *             but the <code>isNamespaceAware</code> is false.
	 */
	@Deprecated
	public void checkPackagesForConformity(Collection packageCollection,
			boolean isNamespaceAware) throws Exception {
		Set classSet = new HashSet();

		for (Iterator i = packageCollection.iterator(); i.hasNext();) {
			EPackage epackage = (EPackage) i.next();
			EClass rootClass = getRootClass(epackage);
			if (rootClass != null) {
				classSet.clear();
				checkClassConformity(rootClass, classSet, isNamespaceAware);
			}
		}
	}

	/**
	 * Checks the conformity of the <code>eclass</code> class and its
	 * properties' classes.
	 */
	private void checkClassConformity(EClass eclass, Set classSet,
			boolean isNamespaceAware) throws Exception {
		String className = eclass.getName();
		if (!classSet.contains(className)) {
			classSet.add(className);

			checkFeatureNamesEquality(eclass, isNamespaceAware);

			List featureList = eclass.getEStructuralFeatures();
			for (Iterator iter = featureList.iterator(); iter.hasNext();) {
				EStructuralFeature feature = (EStructuralFeature) iter.next();
				String featureName = getFeatureName(feature);
				EClassifier featureType = feature.getEType();

				if (!ModelParser.isIgnored(featureName)
						&& featureType instanceof EClass) {
					checkClassConformity((EClass) featureType, classSet,
							isNamespaceAware);
				}
			}
		}
	}

	/**
	 * Checks if the <code>eclass</code> contains XML elements or attributes
	 * with case-insensitive equality in names
	 */
	private void checkFeatureNamesEquality(EClass eclass,
			boolean isNamespaceAware) throws Exception {
		Map elementMap = new HashMap();
		List featureList = eclass.getEAllStructuralFeatures();
		for (Iterator iter = featureList.iterator(); iter.hasNext();) {
			EStructuralFeature feature = (EStructuralFeature) iter.next();
			String featureName = getFeatureName(feature);
			String lowerCaseFeatureName = featureName
					.toLowerCase(Locale.ENGLISH);

			Set namespaceSet = (Set) elementMap.get(lowerCaseFeatureName);
			if (namespaceSet != null) {
				String featureNamespace = getFeatureNamespace(feature);
				if (namespaceSet.contains(featureNamespace)) {
					String errorMessage = mResHash.getString(
							"SIBLINGS.EQUAL.NAMES.AND.NAMESPACES", featureName);
					throw new Exception(errorMessage);
				} else {
					if (!isNamespaceAware) {
						String errorMessage = mResHash.getString(
								"SIBLINGS.EQUAL.NAMES.NOT.PREFIXED",
								featureName);
						throw new Exception(errorMessage);
					}
					namespaceSet.add(featureNamespace);
				}
			} else {
				namespaceSet = new HashSet();
				String featureNamespace = getFeatureNamespace(feature);
				namespaceSet.add(featureNamespace);
				elementMap.put(lowerCaseFeatureName, namespaceSet);
			}
		}
	}

	/**
	 * Parses a string that describes a mapping between prefixes and namespace
	 * URIs. Then creates a prefix to URI or URI to prefix map, depending on the
	 * <code>isPrefixToURIMap</code> parameter.
	 * 
	 * @param namespaces
	 *            string object describing a mapping between prefixes and
	 *            namespace URIs. Each mapping is delimited by an end-of-line
	 *            character. On each line the prefix is delimited from the
	 *            namespace URI by an equal sign.
	 * @param isPrefixToURIMap
	 *            if <code>true</code> the created map is prefix to URI map,
	 *            otherwise it is a URI to prefix map.
	 * @return prefix to URI map or URI to prefix map created by parsing the
	 *         <code>namespaces</code> attribute.
	 * @throws Exception
	 *             if the namespace mapping format (prefix=namespaceURI) is not
	 *             obeyed.
	 */
	@Deprecated
	public Map parseNamespaces(String namespaces, boolean isPrefixToURIMap)
			throws Exception {
		Map namespaceMap = new HashMap();

		if (namespaces != null) {
			StringTokenizer tokenizer = new StringTokenizer(namespaces,
					MAPPING_NAMESPACE_DELIMITER);

			int lineIndex = 1;
			while (tokenizer.hasMoreTokens()) {
				String namespaceMapping = tokenizer.nextToken();
				String[] namespaceProps = namespaceMapping.split(
						MAPPING_PREFIX_DELIMITER, 2);
				if (namespaceProps.length == 2) {
					String namespacePrefix = namespaceProps[0].trim();
					String namespaceURI = namespaceProps[1].trim();
					if (isPrefixToURIMap) {
						namespaceMap.put(namespacePrefix, namespaceURI);
					} else {
						namespaceMap.put(namespaceURI, namespacePrefix);
					}
				} else {
					String errorMessage = mResHash.getString(
							"INVALID.MAPPING.FORMAT", Integer
									.valueOf(lineIndex));
					throw new Exception(errorMessage);
				}

				lineIndex++;
			}
		}

		return namespaceMap;
	}

	/**
	 * Reads an XML Schema file and creates an Ecore model corresponding to it.
	 * Then returns a <code>Collection</code> containing all packages in the
	 * model.
	 * 
	 * @param xsdFile
	 *            specifies the location of the XML Schema File.
	 * @throws Exception
	 *             if the XML Schema file is not found or is not valid XML
	 *             Schema.
	 */
	@Deprecated
	public Collection getPackageCollection(String xsdFile) throws Exception {
		XSDEcoreBuilder xsdBuild = new XSDEcoreBuilder();
		xsdBuild.setValidate(true);

		Collection packageList = null;
		try {
			packageList = xsdBuild.generate(URI.createURI(xsdFile));
		} catch (Exception e) {
			String exceptionMessage = e.getMessage();
			if (exceptionMessage != null) {
				String errorMessage = mResHash.getString(
						"XML.SCHEMA.FILE.ERROR", new Object[] { xsdFile,
								exceptionMessage });
				throw new Exception(errorMessage);
			} else {
				String diagnosticString = getDiagnosticString(xsdBuild);
				if (!diagnosticString.equals("")) {
					String errorMessage = mResHash.getString(
							"XML.SCHEMA.FILE.ERROR2", new Object[] { xsdFile,
									diagnosticString });
					throw new Exception(errorMessage);
				} else {
					String errorMessage = mResHash.getString(
							"XML.SCHEMA.FILE.ERROR3", xsdFile);
					throw new Exception(errorMessage);
				}
			}
		}

		String diagnosticString = getDiagnosticString(xsdBuild);
		if (!diagnosticString.equals("")) {
			String errorMessage = mResHash.getString("XML.SCHEMA.FILE.ERROR4",
					new Object[] { xsdFile, diagnosticString });
			throw new Exception(errorMessage);
		}

		return packageList;
	}

	/**
	 * Checks if there are any fatal or error diagnostics in the XSDEcoreBuilder
	 * object and returns the first one found.
	 */
	private String getDiagnosticString(XSDEcoreBuilder xsdBuild) {
		List diagnosticsList = xsdBuild.getDiagnostics();
		StringBuffer diagnosticsString = new StringBuffer();
		for (Iterator iter = diagnosticsList.iterator(); iter.hasNext();) {
			XSDDiagnostic diag = (XSDDiagnostic) iter.next();
			diagnosticsString.append("\n" + diag.getLocationURI() + ":"
					+ diag.getLine() + " - " + diag.getMessage());
		}

		return diagnosticsString.toString();
	}

	/**
	 * Creates a root object according to the XML Schema specified by the
	 * <code>xsdFile</code> file.
	 * 
	 * @param xsdFile
	 *            specifies an XML Schema file.
	 * @return Data Object corresponding to the root object defined in the XML
	 *         Schema
	 * @throws Exception
	 *             if the specified file does not exist or contains an invalid
	 *             XML Schema.
	 * @throws Exception
	 *             if the XML Schema does not define a root element
	 */
	@Deprecated
	public EDataObject createRootObject(String xsdFile) throws Exception {
		Collection packageCollection = getPackageCollection(xsdFile);
		EPackage rootPackage = getRootPackage(packageCollection);
		EDataObject rootObject = null;

		if (rootPackage != null) {
			rootPackage
					.setEFactoryInstance(new DynamicEDataObjectImpl.FactoryImpl());
			rootObject = createRootObject(rootPackage);
		} else {
			String errorMessage = mResHash.getString("NO.ROOT.IN.SCHEMA");
			throw new Exception(errorMessage);
		}

		return rootObject;
	}

	/**
	 * Searches an EClass features for a feature which represents an XML element
	 * or attribute with specified name. The function searches for a feature
	 * with matching name and namespace and if found it is returned. If only a
	 * feature with matching name is found it is also returned. If no feature
	 * matches the specified name an exception is thrown. Note: XML attributes
	 * are not considered namespace qualified, the prefix is regarded as part of
	 * the name.
	 * 
	 * @param eclass
	 *            the EClass object which features are searched.
	 * @param name
	 *            the name of the XML element or attribute, probably with
	 *            namespace prefix.
	 * @param prefixToURIMap
	 *            prefix to namespace URI map.
	 * @return feature representing the XML element or attribute with the
	 *         specified name and namespace by the <code>name</code>
	 *         parameter.
	 * @throws Exception
	 *             if no feature matches the specified name.
	 */
	@Deprecated
	public EStructuralFeature getFeature(EClass eclass, String name,
			Map prefixToURIMap) throws Exception {
		String nsPrefix = "";
		String localName = name;

		String[] nameParts = name.split(NAMESPACE_SEPARATOR, 2);
		if (nameParts.length == 2) {
			nsPrefix = nameParts[0];
			localName = nameParts[1];
		}

		EStructuralFeature nameMatchedFeature = null;
		boolean namespaceMatched = false;

		List featureList = eclass.getEAllStructuralFeatures();
		for (Iterator iter = featureList.iterator(); iter.hasNext();) {
			EStructuralFeature feature = (EStructuralFeature) iter.next();
			String featureName = getFeatureName(feature);

			if (featureName.equals(":0"))
				featureName = "value";

			if (featureName.equals(localName)) {
				nameMatchedFeature = feature;
				if (compareNamespaces(feature, nsPrefix, prefixToURIMap)) {
					namespaceMatched = true;
					break;
				}
			}
		}

		/*
		 * if there is no feature matching the name or there is some but a
		 * namespace is specified and the feature does not match it
		 */
		if (nameMatchedFeature == null || (!namespaceMatched && nsPrefix != "")) {
			String errorMessage = mResHash.getString("INVALID.FEATURE.NAME",
					name);
			throw new Exception(errorMessage);
		}

		return nameMatchedFeature;
	}
}
