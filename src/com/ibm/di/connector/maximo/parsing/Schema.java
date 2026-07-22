/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.parsing;

import static java.util.Collections.synchronizedMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.w3c.dom.NodeList;

import com.ibm.di.connector.maximo.core.SimpleTpaeIFConnector;
import com.ibm.di.connector.maximo.exception.MxConnConfigException;
import com.ibm.di.connector.maximo.exception.MxConnIOException;
import com.ibm.di.connector.maximo.exception.MxConnXmlParsingException;
import com.ibm.di.connector.maximo.util.Dom;
import com.ibm.di.connector.maximo.util.HttpClient;
import com.ibm.di.connector.maximo.util.TemplateLoader;
import com.ibm.di.server.Log;

/**
 * This class provides access to descriptive information about Maximo Objects
 * Structures (MOS) based on its XML Schema Descriptor (XSD), which is generated
 * by the Maximo Object Structure Application. Every element is exposed through
 * {@link SchemaElement schema element} objects, organized in a hierarchical
 * way.
 * 
 * @since 7.1
 * @see SchemaConfiguration
 * @see SchemaElement
 */
public final class Schema {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String DOC_ROOT = "DocRoot";

	private static final String DOCUMENT_ROOT = "DocumentRoot";

	private static final String IMPLICIT_ATTR = ":0";

	private static final String MAXIMO_NAMESPACE = "@com.ibm.maximo";

	private static final String KIND_DETAIL = "kind";

	private static final String DOCUMENTATION_DETAIL = "documentation";

	private static final String UNIQUE_KEY_DOC = "Unique Key Component";

	private static final String ATTRIBUTE_KIND = "attribute";

	/**
	 * Maps the relative paths of the MBOs of the selected object structure to
	 * the the created SchemaElement.
	 * 
	 * For example for the "ASSET" object structure the map may look like this:
	 * 
	 * <pre>
	 * ...
	 * &quot;ASSET&quot;               : SchemaElement for ASSET
	 * &quot;ASSET@GLACCOUNT&quot;     : SchemaElement for GLACCOUNT
	 * &quot;ASSET@ASSETUSERCUST&quot; : SchemaElement for ASSETUSERCUST
	 * ...
	 * </pre>
	 */
	private final Map<String, SchemaElement> mbos;

	private final SchemaElement mos;

	/**
	 * Maps MOS service XSD definition's path to the generated Schema object.
	 * 
	 * For example:
	 * 
	 * <pre>
	 *  	...
	 *  	http://9.156.6.118/meaweb/schema/service/MXASSETService.xsd : Schema for MXASSET
	 *  	http://9.156.6.118/meaweb/schema/service/MXITEMService.xsd  : Schema for MXITEM
	 *  	...
	 * </pre>
	 */
	private static final Map<List<String>, Schema> schemaCache = synchronizedMap(new HashMap<List<String>, Schema>());

	private static Log logger;

	private Schema(final SchemaElement root, final String mosName) throws MxConnConfigException {
		mos = findFirstElement(root, DOC_ROOT + MAXIMO_NAMESPACE + "@" + mosName);

		if (mos == null) {
			throw new MxConnConfigException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CAN.NOT.FIND.MOS.SCHEMA", mosName));
		}

		// Add attributes form other definitions of the root MBO
		complementRootMBO(root, mos.getFirstChild());

		mbos = new TreeMap<String, SchemaElement>();
		collectMbos(mos, mbos);
	}

	/**
	 * This method adds additional attributes to the root MBO. This is needed
	 * because the definition in "DocRoot@com.ibm.maximo@<OS_NAME>@<root_MBO>"
	 * is incomplete. Therefore we complement the schema of the root MBO by
	 * using the attributes from
	 * "DocRoot@com.ibm.maximo@<root_MBO>MboQuery@<root_MBO>" definition.
	 * 
	 * @param root
	 * @param rootMBO
	 */
	private static void complementRootMBO(SchemaElement root, SchemaElement rootMBO) {
		SchemaElement rootMBOQuery = findFirstElement(root, DOC_ROOT + MAXIMO_NAMESPACE + "@" + rootMBO.getName() + "MboQuery@"
				+ rootMBO.getName());

		if (rootMBOQuery != null) {
			SchemaElement newChild = null;

			for (SchemaElement e : rootMBOQuery.getChildren()) {
				if (rootMBO.getChild(e.getName()) == null) {
					newChild = SchemaElement.buildElement(rootMBO, e.getName(), e.isAttribute(), e.isUniqueKey());
					newChild.setRequired(e.isRequired());
					newChild.setClassName(e.getClassName());
					rootMBO.addChild(newChild);
				}
			}
		}
	}

	/**
	 * Builds a {@link Schema schema} object based on the Maximo Object
	 * Structure and its XML Schema Descriptor's URL (XSD).
	 * 
	 * @param cfg
	 *            configuration parameter required to build the schema object
	 * @return {@link Schema} based on the Maximo Object Structure and its XML
	 *         Schema Descriptor's URL (XSD)
	 * @throws MxConnConfigException
	 *             if the configuration provided is not valid
	 * @throws MxConnIOException
	 *             if any sort of communication problem occurs when trying to
	 *             retrieve the XSD
	 * @throws MxConnXmlParsingException
	 *             if the XSD content can not be parsed
	 * @see Schema#clearSchemaCache()
	 */
	public static synchronized Schema getInstance(final SchemaConfiguration cfg, Log log) throws MxConnConfigException,
			MxConnIOException, MxConnXmlParsingException {

		logger = log;
		final List<String> urls = cfg.getXsdUrlList();

		if (!schemaCache.containsKey(urls)) {
			final Schema schema = new Schema(loadSchemaElement(cfg), cfg.getMosName());
			complementSchema(schema, cfg);
			schemaCache.put(urls, schema);
		}

		return schemaCache.get(urls);
	}

	/**
	 * Clears every schema object cached.
	 */
	public static synchronized void clearSchemaCache() {
		schemaCache.clear();
	}

	/**
	 * This method retrieves and sets the size for each child (except for
	 * attributes) of a specified MBO (only for MBOs of the selected MOS).
	 * 
	 * @param mbo
	 *            SchemaElelemt object for particular MBO
	 * @param xml
	 *            XML response to the QueryMXOBJECTCFG for this MBO
	 * @throws MxConnXmlParsingException
	 */
	private static void complementMBO(final SchemaElement mbo, final String xml) throws MxConnXmlParsingException {
		final NodeList maxAttrNodes = Dom.getElements("MAXATTRIBUTECFG", xml);

		for (int i = 0; i < maxAttrNodes.getLength(); i++) {
			final Map<String, String> attrMap = Dom.getAttributes(maxAttrNodes.item(i));
			final String attributeName = attrMap.get("ATTRIBUTENAME");
			final SchemaElement child = mbo.getChild(attributeName);

			if (child != null) {
				child.setSize(Integer.valueOf(attrMap.get("LENGTH")));
			}
		}
	}

	/**
	 * This method iterates trough all MBOs of the selected MOS and posts a
	 * query for their schema.
	 * 
	 * @param schema
	 * @param cfg
	 * @throws MxConnIOException
	 * @throws MxConnXmlParsingException
	 * @see #complementMBO(SchemaElement, String)
	 */
	private static void complementSchema(final Schema schema, final SchemaConfiguration cfg) throws MxConnIOException,
			MxConnXmlParsingException {

		final HttpClient client = new HttpClient(logger);
		client.setTimeout(cfg.getTimeout());
		client.setTargetUrlList(cfg.getMaxObjUrlList());
		client.setAuthenticationRequired(cfg.isAuthenticationRequired());
		client.setUserId(cfg.getUserId());
		client.setPassword(cfg.getPassword());

		final TemplateLoader tpl = new TemplateLoader(TemplateLoader.TYPE_QUERY, logger);

		for (final String mboName : schema.getMboNameList()) {

			// mboName contains the full path (e.g. "ASSET@ASSETSPEC")
			final SchemaElement mbo = schema.getMboByName(mboName);

			// mbo.getName() returns only the name (e.g. "ASSETSPEC")
			tpl.setProperty(TemplateLoader.MOS_HOLDER, cfg.getMaxobjObjectStructure());
			tpl.setProperty(TemplateLoader.MBO_HOLDER, "<MAXOBJECTCFG><OBJECTNAME operator=\"=\">" + mbo.getName()
					+ "</OBJECTNAME></MAXOBJECTCFG>");
			tpl.setProperty(TemplateLoader.UNIQUERES_HOLDER, "true");
			tpl.setProperty(TemplateLoader.MAXITEMS_HOLDER, "1");
			tpl.setProperty(TemplateLoader.RSSTART_HOLDER, "0");
			tpl.setProperty(TemplateLoader.LANG_HOLDER, cfg.getTransactionLang());

			// post a request for the configuration of each MBO to get only its
			// attributes' size
			complementMBO(mbo, client.post(tpl.toString()));
		}
	}

	/**
	 * @param element
	 *            to start the search
	 * @param regexp
	 *            name to search
	 * @return SchemaElement with specified name or <code>null</code> if not
	 *         found.
	 */
	private static SchemaElement findFirstElement(final SchemaElement element, final String regexp) {
		if (element.getPath().matches(regexp)) {
			return element;
		}

		for (final SchemaElement child : element.getChildren()) {
			final SchemaElement e;

			e = findFirstElement(child, regexp);

			if (e != null) {
				return e;
			}
		}
		return null;
	}

	private static boolean isAttribute(final EStructuralFeature esf) {
		if (esf.getEAnnotations().isEmpty()) {
			return false;
		}

		final EAnnotation eAnnotation = (EAnnotation) esf.getEAnnotations().get(0);
		final EMap<String, String> eMap = eAnnotation.getDetails();
		final String kind = eMap.get(KIND_DETAIL);

		return ATTRIBUTE_KIND.equals(kind);
	}

	private static boolean isUniqueKey(final EStructuralFeature esf) {
		if (esf.getEAnnotations().isEmpty()) {
			return false;
		}

		final EAnnotation eAnnotation = (EAnnotation) esf.getEAnnotations().get(0);
		final EMap<String, String> eMap = eAnnotation.getDetails();
		final String doc = eMap.get(DOCUMENTATION_DETAIL);

		return UNIQUE_KEY_DOC.equalsIgnoreCase(doc);
	}

	private static SchemaElement loadSchemaElement(final SchemaConfiguration cfg) throws MxConnIOException {
		MxConnXSDEcoreBuilder builder = new MxConnXSDEcoreBuilder(cfg, logger);
		builder.setValidate(true);

		final Collection<EObject> packages = builder.generate();
		final SchemaElement root = SchemaElement.buildRootElement(DOC_ROOT);

		for (final Iterator<EObject> i = packages.iterator(); i.hasNext();) {
			final EPackage ePck = (EPackage) i.next();

			// represents the meta objects - classes and data types, defined in
			// this package
			final EClass eClass = (EClass) ePck.getEClassifier(DOCUMENT_ROOT);
			final SchemaElement child = SchemaElement.buildElement(root, ePck.getName(), false, false);
			root.addChild(child);
			traverseClass(eClass, child);
		}
		return root;
	}

	private static void traverseClass(final EClass eClass, final SchemaElement parent) {
		final EList<EStructuralFeature> eList = eClass.getEAllStructuralFeatures();

		for (final Iterator<EStructuralFeature> i = eList.iterator(); i.hasNext();) {
			final EStructuralFeature esf = i.next();
			final EClassifier eClassifier = esf.getEType();
			final String name = ExtendedMetaData.INSTANCE.getName(esf);

			if (IMPLICIT_ATTR.equals(name)) {
				parent.setClassName(eClassifier.getInstanceClassName());
				continue;
			}

			final SchemaElement child = SchemaElement.buildElement(parent, name, isAttribute(esf), isUniqueKey(esf));
			child.setRequired(esf.isRequired());
			child.setClassName(eClassifier.getInstanceClassName());
			parent.addChild(child);

			if (eClassifier instanceof EClass) {
				traverseClass((EClass) eClassifier, child);
			}
		}
	}

	/**
	 * Returns the schema element that represents the specified MBO.
	 * 
	 * @param mboName
	 *            name of the MBO to be returned
	 * @return schema element that represents the specified MBO
	 * @throws MxConnConfigException
	 *             if <code>mboName</code> does not exist
	 */
	public SchemaElement getMboByName(final String mboName) throws MxConnConfigException {
		if (!mbos.containsKey(mboName)) {
			throw new MxConnConfigException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.MBO.NOT.FOUND.IN.SCHEMA", mboName));
		}
		return mbos.get(mboName);
	}

	/**
	 * Returns a list of all valid MBO names.
	 * 
	 * @return list of all valid MBO names
	 */
	public Set<String> getMboNameList() {
		return mbos.keySet();
	}

	/**
	 * Returns a schema element that describes the Maximo Object Structure
	 * (MOS).
	 * 
	 * @return schema element that describes the Maximo Object Structure
	 */
	public SchemaElement getMos() {
		return mos;
	}

	/**
	 * Returns the definition of the root or top-level MBO.
	 * 
	 * @return the definition of the root or top-level MBO
	 */
	public SchemaElement getRootMbo() {
		return mos.getFirstChild();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(Schema.class.getName());
		sb.append('{').append('\n');
		mos.dumpElements(sb);
		sb.append('}');
		return sb.toString();
	}

	private void collectMbos(final SchemaElement root, final Map<String, SchemaElement> mbos) {
		for (final SchemaElement e : root.getChildren()) {
			if (e.isMboDefinition()) {
				mbos.put(e.getPathRelativeTo(mos), e);
			}
			collectMbos(e, mbos);
		}
	}
}
