/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.naming.NameNotFoundException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FormItemConfig;
import com.ibm.di.config.interfaces.FormSection;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.function.SystemFunctions;

public class FormConfigUtil {

	// -- Special form syntax entries
	private static final String DROPLIST_SYNTAX = "droplist";

	// -- Global prefix for form item names
	private static final String GLOBAL_PREFIX = "$GLOBAL.";

	// -- XSD Tags
	private static final String XSD_ENUMERATION = "xsd:enumeration";
	private static final String XSD_SIMPLETYPE = "xsd:simpleType";
	private static final String XSD_ANNOTATION = "xsd:annotation";
	private static final String XSD_RESTRICTIONS = "xsd:restrictions";
	private static final String XSD_APPINFO = "xsd:appinfo";
	private static final String XSD_COMPLEXTYPE = "xsd:complexType";
	private static final String XSD_ELEMENT = "xsd:element";
	private static final String XSD_SEQUENCE = "xsd:sequence";
	private static final String XSD_STRING = "xsd:string";
	private static final String XSD_BOOLEAN = "xsd:boolean";

	// -- XSD Attribute keywords
	private static final String XSD_ATTR_BASE = "base";
	private static final String XSD_ATTR_VALUE = "value";
	private static final String XSD_ATTR_NAME = "name";
	private static final String XSD_ATTR_TYPE = "type";
	private static final String XSD_ATTR_FIXED = "fixed";

	// -- SCMP Extension tags
	private static final String SCMP_DISPLAY = "scmp:display";
	private static final String SCMP_DESCRIPTION = "scmp:description";
	private static final String SCMP_SYNTAX = "scmp:syntax";
	private static final String SCMP_REQUIRED = "scmp:required";
	private static final String SCMP_READONLY = "scmp:readonly";
	private static final String SCMP_INDEXBASED = "scmp:indexbased";
	private static final String SCMP_SECTION = "scmp:section";
	private static final String SCMP_ITEM = "scmp:item";
	private static final String SCMP_LOCALIZEDVALUES = "scmp:localizedvalues";
	private static final String SCMP_VALUE = "scmp:value";
	private static final String SCMP_HELP_URL = "scmp:helpurl";

	// -- TDI elements
	private static final String COMPONENT_TYPE = "ComponentType";
	private static final String SUPPORTED_MODES = "Mode";
	private static final String CONNECTION = "Connection";
	private static final String SECTIONS = "Sections";

	private FormConfig global;
	private FormConfig form;
	private boolean expandValues;
	private String[] supportedModes;
	private String nspath;
	private BaseConfiguration config;

	/**
	 * Use this constructor to obtain the form that would be used to display
	 * settings for the provided config.
	 * 
	 * @param nspath
	 *            The complete path to the component (e.g.
	 *            system:/Connectors/ibmdi.LDAP)
	 * @param config
	 *            The configuration object (from which the form is derived)
	 * @param nlsCode
	 *            The NLS code for labels
	 * @param expandValues
	 *            true if values for drop-down lists etc should be expanded
	 * 
	 * @throws Exception
	 */
	public FormConfigUtil(String nspath, BaseConfiguration config, String nlsCode, boolean expandValues) throws Exception {
		String name = getFormName(config);
		if (name == null)
			throw new NameNotFoundException();
		form = (FormConfig) ((BaseConfiguration) MetamergeConfigFactory.lookup(null, "system:/Forms/" + name)).getClone();
		getGlobalForm();
		form.setTranslationLocale(nlsCode);
		this.expandValues = expandValues;
		if (config instanceof ConnectorConfig)
			supportedModes = getSupportedModes((ConnectorConfig) config);
		this.nspath = nspath;
		this.config = config;
	}

	/**
	 * Use this constructor to generate an XSD based on the assemblyline'
	 * schema.
	 * 
	 * @param alc
	 */
	public FormConfigUtil(AssemblyLineConfig alc) {
		this.config = alc;
	}

	/**
	 * Use this constructor when you already have a FormConfig object.
	 * 
	 * @param form
	 *            The form config (must not be null)
	 * @param nlsCode
	 *            The NLS code for labels
	 * @param expandValues
	 *            true if values for drop-down lists etc should be expanded
	 * 
	 * @throws Exception
	 */
	public FormConfigUtil(FormConfig form, String nlsCode, boolean expandValues) throws Exception {
		getGlobalForm();
		this.form = (FormConfig) form.getClone();
		this.form.setTranslationLocale(nlsCode);
		this.expandValues = expandValues;
	}

	/**
	 * Returns an XSD document that defines the contents of the form.
	 * 
	 * @return
	 * @throws Exception
	 */
	public Document toXSDDoc() throws Exception {

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

		// -- root element
		Element root = doc.createElement("xsd:schema");
		root.setAttribute("targetNamespace", "http://w3.ibm.com/xmlns/prod/scmp");
		root.setAttribute("xmlns:tns", "http://w3.ibm.com/xmlns/prod/scmp");
		root.setAttribute("xmlns:scmp", "http://w3.ibm.com/xmlns/prod/scmp");
		root.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");

		doc.appendChild(root);

		if (config instanceof AssemblyLineConfig) {
			generateSchemaFromAssemblyLine(root);
		} else {
			// -- Component type (a fixed value referencing the source
			// component)
			appendFixedType(root);

			// -- supported modes
			if (supportedModes != null)
				appendSupportedModes(root);

			// -- parameters
			appendFields(root);

			// -- add sections
			appendSections(root);
		}

		return doc;
	}

	/**
	 * Returns the XSD document as a string.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String toXSD() throws Exception {
		Document doc = toXSDDoc();
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		StringWriter output = new StringWriter();
		t.transform(new DOMSource(doc.getDocumentElement()), new StreamResult(output));
		return output.toString();
	}

	/**
	 * Generates the XSD based on the schema information in the assemblyline
	 * config
	 * 
	 * @param parent
	 * @throws Exception
	 */
	private void generateSchemaFromAssemblyLine(Element parent) throws Exception {
		AssemblyLineConfig alc = (AssemblyLineConfig) config;

		Element complexType = createComplexType(parent, CONNECTION);

		Element sequence = complexType.getOwnerDocument().createElement(XSD_SEQUENCE);
		complexType.appendChild(sequence);

		SchemaConfig sc = alc.getPublishedInitParams();
		for (String str : sc.getItemNames()) {

			Element item = createAppInfoBranch(sequence, str);
			Element simple = (Element) item.getParentNode().getParentNode();
			SchemaItemConfig sic = sc.getItem(str);
			String syntax = sic.getExternalSyntax();
			if ("boolean".equalsIgnoreCase(syntax))
				simple.setAttribute(XSD_ATTR_TYPE, XSD_BOOLEAN);
			else
				simple.setAttribute(XSD_ATTR_TYPE, XSD_STRING);

			if (sic.getUserComment() != null) {
				appendStringValue(item, SCMP_DISPLAY, sic.getUserComment());
			}

			if (sic.isRequired()) {
				appendStringValue(item, SCMP_REQUIRED, "true");
			}

		}
	}

	/**
	 * Adds the full path to the component as a fixed-valued element
	 * 
	 * @param parent
	 */
	private void appendFixedType(Element parent) throws Exception {
		Element top = parent.getOwnerDocument().createElement(XSD_ELEMENT);
		parent.appendChild(top);
		top.setAttribute(XSD_ATTR_NAME, COMPONENT_TYPE);
		top.setAttribute(XSD_ATTR_TYPE, XSD_STRING);
		top.setAttribute(XSD_ATTR_FIXED, nspath);
	}

	/**
	 * Adds the supported modes element with legal values
	 * 
	 * @param parent
	 */
	private void appendSupportedModes(Element parent) throws Exception {
		Element field = createSimpleType(parent, SUPPORTED_MODES);

		Element enumeration = field.getOwnerDocument().createElement(XSD_ENUMERATION);
		field.appendChild(enumeration);

		Element res = field.getOwnerDocument().createElement(XSD_RESTRICTIONS);
		enumeration.appendChild(res);
		res.setAttribute(XSD_ATTR_BASE, XSD_STRING);

		for (String str : supportedModes) {
			appendStringValue(res, XSD_ENUMERATION, null).setAttribute(XSD_ATTR_VALUE, str);
		}
	}

	/**
	 * Adds all form items from the current form to the root element
	 * 
	 * @param root
	 * @throws Exception
	 */
	private void appendFields(Element parent) throws Exception {
		Element complexType = createComplexType(parent, CONNECTION);
		Element sequence = complexType.getOwnerDocument().createElement(XSD_SEQUENCE);
		complexType.appendChild(sequence);

		for (String str : form.getFormItemNames()) {
			if (str.startsWith(GLOBAL_PREFIX)) {
				if (global != null)
					appendFieldElement(sequence, global.getFormItem(str.substring(GLOBAL_PREFIX.length())));
			} else {
				appendFieldElement(sequence, form.getFormItem(str));
			}
		}
	}

	/**
	 * Creates an xsd:element/xsd:simpleType branch in the parent element. The
	 * name of the xsd:element is set to name.
	 * 
	 * @param parent
	 *            The element to which the branch is appended
	 * @param name
	 *            The name of the xsd:element node
	 * @return The xsd:simpleType element
	 * @throws Exception
	 */
	private Element createSimpleType(Element parent, String name) throws Exception {
		Element element = parent.getOwnerDocument().createElement(XSD_ELEMENT);
		element.setAttribute(XSD_ATTR_NAME, name);
		parent.appendChild(element);

		Element field = parent.getOwnerDocument().createElement(XSD_SIMPLETYPE);
		element.appendChild(field);

		return field;
	}

	/**
	 * Creates an xsd:element/xsd:complexType branch in the parent element. The
	 * name of the xsd:element is set to name.
	 * 
	 * @param parent
	 *            The element to which the branch is appended
	 * @param name
	 *            The name of the xsd:element node
	 * @return The xsd:complexType element
	 * @throws Exception
	 */
	private Element createComplexType(Element parent, String name) throws Exception {
		Element element = parent.getOwnerDocument().createElement(XSD_ELEMENT);
		element.setAttribute(XSD_ATTR_NAME, name);
		parent.appendChild(element);

		Element field = parent.getOwnerDocument().createElement(XSD_COMPLEXTYPE);
		element.appendChild(field);

		return field;
	}

	/**
	 * Creates an xsd:element/xsd:simpleType/xsd:annotation/xsd:appinfo branch
	 * in the parent element. The name of the xsd:element is set to name.
	 * 
	 * @param parent
	 *            The element to which the branch is appended
	 * @param name
	 *            The name of the xsd:element node
	 * @return The xsd:appinfo element
	 * @throws Exception
	 */
	private Element createAppInfoBranch(Element parent, String name) throws Exception {
		Element field = createSimpleType(parent, name);

		Element annotation = parent.getOwnerDocument().createElement(XSD_ANNOTATION);
		field.appendChild(annotation);

		Element appinfo = parent.getOwnerDocument().createElement(XSD_APPINFO);
		annotation.appendChild(appinfo);

		return appinfo;
	}

	/**
	 * Creates an xsd:simpleType for the FormItemConfig
	 * 
	 * @param parent
	 * @param fic
	 * @return The element node containing the form item info
	 * @throws Exception
	 */
	private Element appendFieldElement(Element parent, FormItemConfig fic) throws Exception {
		// <xsd:element name="%PARAMETER%">
		// <xsd:simpleType>
		// <xsd:annotation>
		// <xsd:appinfo>
		// <fcm:display>%LABEL%</fcm:display>
		// <fcm:description>%TOOLTIP%</fcm:description>
		// <fcm:syntax>%SYNTAX%</fcm:syntax>
		// etc ....
		// </xsd:appinfo>
		// </xsd:annotation>
		// %RESTRICTIONS%
		// </xsd:simpleType>
		// </xsd:element>
		//
		if (fic == null)
			return null;

		// Element element =
		// parent.getOwnerDocument().createElement(XSD_ELEMENT);
		// element.setAttribute(XSD_ATTR_NAME, fic.getShortName());
		// parent.appendChild(element);
		//		
		// Element field =
		// parent.getOwnerDocument().createElement(XSD_SIMPLETYPE);
		// element.appendChild(field);
		//		
		// Element annotation =
		// parent.getOwnerDocument().createElement(XSD_ANNOTATION);
		// field.appendChild(annotation);
		//		
		// Element appinfo =
		// parent.getOwnerDocument().createElement(XSD_APPINFO);
		// annotation.appendChild(appinfo);

		Element appinfo = createAppInfoBranch(parent, fic.getShortName());
		Element field = (Element) appinfo.getParentNode().getParentNode();

		String syntax = getSyntax(fic);

		appendStringValue(appinfo, SCMP_DISPLAY, fic.getLabel());
		appendStringValue(appinfo, SCMP_DESCRIPTION, fic.getToolTip());
		appendStringValue(appinfo, SCMP_SYNTAX, syntax);
		appendStringValue(appinfo, SCMP_REQUIRED, "" + fic.isRequired());
		appendStringValue(appinfo, SCMP_READONLY, "" + fic.isReadOnly());
		appendStringValue(appinfo, SCMP_INDEXBASED, "" + fic.isIndexBased());

		// TODO add help location at component level
		appendStringValue(appinfo, SCMP_HELP_URL, System.getProperty("com.ibm.di.helpHost"));

		List<String> localizedValues = fic.getLocalizedValues();
		if (localizedValues != null) {
			Element values = appendStringValue(appinfo, SCMP_LOCALIZEDVALUES, null);
			for (String str : localizedValues) {
				appendStringValue(values, SCMP_VALUE, str);
			}
		}

		// -- add restrictions for read-only drop-down lists (e.g. value list)
		if (DROPLIST_SYNTAX.equals(syntax)) {
			appendRestrictions(field, fic);
		}
		return (Element) field.getParentNode();
	}

	/**
	 * Returns the syntax for the item or string if none is specified.
	 * 
	 * @param fic
	 * @return
	 */
	private String getSyntax(FormItemConfig fic) {
		String syntax = fic.getSyntax();
		if (syntax == null || syntax.equals(""))
			syntax = "string";
		return syntax.toLowerCase();
	}

	/**
	 * Adds a xsd:restrictions element to the field with the list of values
	 * taken from the form item config.
	 * 
	 * @param field
	 * @param fic
	 */
	private void appendRestrictions(Element field, FormItemConfig fic) {
		Element enumeration = field.getOwnerDocument().createElement(XSD_ENUMERATION);
		field.appendChild(enumeration);

		if (fic.getValues() != null && fic.getValues().size() > 0) {
			Element res = field.getOwnerDocument().createElement(XSD_RESTRICTIONS);
			enumeration.appendChild(res);
			res.setAttribute(XSD_ATTR_BASE, XSD_STRING);

			for (String str : fic.getValues()) {
				if (expandValues) {
					for (String s : expandValues(str))
						appendStringValue(res, XSD_ENUMERATION, null).setAttribute(XSD_ATTR_VALUE, s);
				} else {
					appendStringValue(res, XSD_ENUMERATION, null).setAttribute(XSD_ATTR_VALUE, str);
				}
			}
		}
	}

	/**
	 * Expand macro names
	 * 
	 * @param str
	 *            The macro to expand
	 * @return The List of Strings from the macro. If str was not a macro, the
	 *         list will contain only str.
	 */
	private List<String> expandValues(String str) {
		if ("@PARSERS@".equals(str))
			return getSystemFolderNames(MetamergeConfig.PARSER_FOLDER);

		if ("@CONNECTORS@".equals(str))
			return getSystemFolderNames(MetamergeConfig.CONNECTOR_FOLDER);

		if ("@FUNCTIONS@".equals(str))
			return getSystemFolderNames(MetamergeConfig.FUNCTION_FOLDER);

		List<String> list = new ArrayList<String>(1);
		list.add(str);
		return list;
	}

	/**
	 * Return all names in the specified system folder
	 * 
	 * @param type
	 * @return all names in the specified system folder
	 */
	public static List<String> getSystemFolderNames(int type) {
		MetamergeConfig system = MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE);

		List<String> ret = new ArrayList<String>();

		MetamergeFolder fld;
		List<String> names;
		try {
			fld = system.getDefaultFolder(type);
			names = Arrays.asList(fld.getNames());
		} catch (Exception err) {
			// Cannot happen
			return ret;
		}

		Collections.sort(names);
		for (String name : names) {
			ret.add(MetamergeConfigFactory.SYSTEM_NAMESPACE + ":/" + fld.getName() + "/" + name);
		}

		return ret;
	}

	/**
	 * Adds all sections into a complexType entry named "Sections".
	 * 
	 * @param parent
	 * @return
	 */
	private Element appendSections(Element parent) {
		// <xsd:complexType name="Sections">
		// <xsd:annotation>
		// <xsd:appinfo>
		// <fcm:section name="General">
		// <fcm:label>General Parameters</fcm:label>
		// <fcm:item>ldapUrl</fcm:item>
		// </fcm:section>
		// <fcm:section name="Advanced">
		// <fcm:label>Advanced Parameters</fcm:label>
		// <fcm:item>ldapUrl</fcm:item>
		// </fcm:section>
		// </xsd:appinfo>
		// </xsd:annotation>
		// </xsd:complexType>
		Element ct = parent.getOwnerDocument().createElement(XSD_COMPLEXTYPE);
		parent.appendChild(ct);
		ct.setAttribute(XSD_ATTR_NAME, SECTIONS);

		Element annotation = parent.getOwnerDocument().createElement(XSD_ANNOTATION);
		ct.appendChild(annotation);

		Element appinfo = parent.getOwnerDocument().createElement(XSD_APPINFO);
		annotation.appendChild(appinfo);

		for (String str : form.getSectionNames()) {
			FormSection section = form.getSection(str);
			appendSection(appinfo, section);
		}

		return ct;
	}

	/**
	 * Appends the section information to the parent.
	 * 
	 * @param appinfo
	 * @param section
	 * @return
	 */
	private Element appendSection(Element parent, FormSection section) {
		// <fcm:section name="Advanced">
		// <fcm:item>ldapUrl</fcm:item>
		// </fcm:section>
		Element sec = parent.getOwnerDocument().createElement(SCMP_SECTION);
		sec.setAttribute(XSD_ATTR_NAME, section.getTitle() == null ? "" : section.getTitle());
		sec.setAttribute("expanded", section.initiallyExpanded() ? "true" : "false");
		parent.appendChild(sec);

		for (String str : section.getNames()) {
			if (str.startsWith(GLOBAL_PREFIX))
				str = str.substring(GLOBAL_PREFIX.length());
			appendStringValue(sec, SCMP_ITEM, str);
		}

		return sec;
	}

	/**
	 * Appends a Text or CData section to the parent with the specified tagName.
	 * 
	 * @param parent
	 * @param tagName
	 * @param value
	 * @return
	 */
	private Element appendStringValue(Element parent, String tagName, String value) {
		Element elem = parent.getOwnerDocument().createElement(tagName);
		parent.appendChild(elem);
		if (value == null)
			return elem;

		if (value.indexOf("\r") != -1 || value.indexOf("\n") != -1) {
			elem.appendChild(parent.getOwnerDocument().createCDATASection(value));
		} else {
			elem.appendChild(parent.getOwnerDocument().createTextNode(value));
		}

		return elem;
	}

	private void getGlobalForm() throws Exception {
		try {
			global = (FormConfig) MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE).lookup(
					"/Forms/__GLOBAL__");
		} catch (NameNotFoundException nfn) {
			global = (FormConfig) MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.STDFORMS_NAMESPACE).lookup(
					"/Forms/__GLOBAL__");
		}
	}

	/**
	 * Find the name of the Form that should be used for this BaseConfiguration.
	 * 
	 * @param bc
	 *            The BaseConfiguration
	 * @return The name of the Form, or null if nothing found.
	 * @since 7.1
	 */
	public static String getFormName(BaseConfiguration bc) {
		String javaClass;
		if (bc instanceof RawConnectorConfig) {
			javaClass = ((RawConnectorConfig) bc).getJavaClass();
			bc = getParentConfig(bc, ConnectorConfig.class);
		} else if (bc instanceof ParserConfig) {
			javaClass = ((ParserConfig) bc).getJavaClass();
		} else if (bc instanceof ConnectorConfig) {
			javaClass = ((ConnectorConfig) bc).getConnectionConfig().getJavaClass();
		} else {
			bc = getParentConfig(bc, FunctionConfig.class);
			if (bc == null)
				return null;
			javaClass = ((FunctionConfig) bc).getJavaClass();
		}

		MetamergeConfig system = MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE);
		BaseConfiguration current = bc;
		while (current != null) {
			try {
				String name = javaClass + "." + current.getShortName();
				system.lookup("/Forms/" + name);
				return name;
			} catch (Exception notFound) {
				current = current.getInheritsFrom();
			}
		}

		String name = null;
		if (current == null) {
			// walked up the hierarchy and couldn't find anything.

			// Try first with a class name
			try {
				system.lookup("/Forms/" + javaClass);
				name = javaClass;
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}

			if (name == null) {
				// still nothing... try config short name
				current = bc;
				while (current != null) {
					try {
						name = current.getShortName();
						system.lookup("/Forms/" + name);
						break;
					} catch (Exception notFound) {
						current = current.getInheritsFrom();
					}
				}
			}
		}

		return name;
	}

	@SuppressWarnings("unchecked")
	/**
	 * Returns the parent configuration that implements cls.
	 * 
	 */
	public static <T> T getParentConfig(Object config, Class<T> cls) {
		Object b = config;
		while (b != null) {
			if (cls.isAssignableFrom(b.getClass()))
				return (T) b;

			for (Class<?> src : b.getClass().getInterfaces()) {
				if (cls.isAssignableFrom(src))
					return (T) b;
			}

			if (b instanceof BaseConfiguration)
				b = ((BaseConfiguration) b).getParent();
			else
				return null;
		}
		return null;
	}

	/**
	 * Returns the supported modes for the connector
	 * 
	 * @param config
	 * @return
	 */
	private String[] getSupportedModes(ConnectorConfig config) {
		try {
			String className = config.getConnectionConfig().getJavaClass();
			Class<?> cls = Class.forName(className);
			Object conn = cls.newInstance();
			if (conn instanceof Connector) {
				Vector<String> modes = ((Connector) conn).getModes(config);
				return modes.toArray(new String[modes.size()]);
			}
		} catch (Throwable e) {
			return Connector.ALL_MODES;
		}
		return Connector.ALL_MODES;
	}
}
