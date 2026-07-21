/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser.xml;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;

/**
 * 
 * This class is used for parsing the configuration parameters of the XML
 * Parser2. After it compiles everything needed into an array it is used for
 * matching elements found in the XML being parsed. Also this class keeps track
 * of the elements that the XML Parser2 have entered in. Since XML Parser2 could
 * be in only one mode (Input or Output) the
 * 
 * THIS CLASS IS FOR INTERNAL USAGE ONLY! MAY CHANGE IN THE FUTURE!
 * 
 * @since 7.0
 */
public class SimpleXPathEvaluator {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static final ResourceHash resHash = new ResourceHash("xmlparser2");

	/**
	 * returned by the match(String, String, String) method and means that no
	 * match is found for the specified path
	 */
	public static final byte NO_MATCH_FOUND = 0;

	/**
	 * returned by the match(String, String, String) method and means that an
	 * exact match is found for the specified path
	 */
	public static final byte EXACT_MATCH_FOUND = 1;

	/**
	 * returned by the match(String, String, String) method and means that the
	 * provided element is part of the specified path
	 */
	public static final byte PARTIAL_MATCH_FOUND = 2;

	/**
	 * The character that represents a QualifiedElementName or just an
	 * ElementName in the provided xpath string.
	 */
	public static final String WILDCARD = "*";

	/**
	 * The character that separates each element in a path.
	 */
	public static final String ELEMENT_SEPARATOR = "/";

	/**
	 * The character that separates the prefix from the local name in a regular
	 * element.
	 */
	public static final String PREFIX_LOCALNAME_SEPARATOR = ":";

	/**
	 * Represents not compiled value - '0'
	 */
	private static final byte NOT_COMPILED = 0;

	/**
	 * Represents compiled for reading value -'1'
	 */
	private static final byte COMPILED_FOR_READING = 1;

	/**
	 * Represents compiled for writing value - '-1'
	 */
	private static final byte COMPILED_FOR_WRITING = -1;

	/** raw XPath string */
	private String xPath = null;

	/** raw prefix to namespaceURI map string */
	private String nsMap = null;

	/** raw attributes declaration string */
	private String attrsDeclaration = null;

	/**
	 * <b>example: /root/prefix:name/entry/r/cont/p:entry </b><br />
	 * compiled[0] -> first path <br />
	 * compiled[0][1] -> first child under the root element (prefix:name) <br />
	 * compiled[0][1][0] -> the prefix of the first child of the root or
	 * <code>null</code> if none <br />
	 * compiled[0][1][1] -> the localName of the element <br />
	 * compiled[0][1][2] -> the namespaceURI of the element defined in the
	 * ns.map configuration parameter, if non is defined then this would be
	 * <code>null</code> <br />
	 */
	private String[][][] compiled = null;

	/** used to track the path the XML Parser2 is passing through */
	private ArrayList<String[]> level = null;

	/**
	 * used to keep information of the prefixes declared for the static roots
	 * (used in Output mode).
	 */
	private ArrayList<HashMap<String, String>> decl = null;

	/**
	 * used to keep information of the attributes declared for the static roots
	 * (used in Output mode).
	 */
	private ArrayList<HashMap<AttrEntityKey, String>> attr = null;

	/**
	 * shows how the parameters are compiled in the <code>compiled</code> array
	 */
	private byte compiledFlag = NOT_COMPILED;

	/**
	 * Holding log messages.
	 */
	private Log log = null;

	private boolean wrapUnwrapEntry = false;

	private String[] entryTag = null;

	private int numberOfDeclElems;

	/**
	 * Constructor for the class.
	 * 
	 * @param simpleXPath
	 *            - the Simple XPath String to compile.
	 * @param namespaceMap
	 *            - the namespaceMap String to compile (e.g.
	 *            prefix=namespaceURI).
	 * @param attributesDeclaration
	 *            - the attributes declaration string.
	 * @param entryTag
	 *            - the element that will be used to wrap each entry when
	 *            writing. Used only for reference when parsing the Static
	 *            Attribute Declarations. This parameter should be in one of the
	 *            following forms: [null, "localName"], ["prefix", "localName"].
	 *            If null is passed then no wrapping will be done...
	 * @param log
	 *            - the log object to use for logging.
	 * 
	 */
	public SimpleXPathEvaluator(String simpleXPath, String namespaceMap,
			String attributesDeclaration, String[] entryTag, Log log) {

		xPath = simpleXPath;
		nsMap = namespaceMap;
		attrsDeclaration = attributesDeclaration;

		this.wrapUnwrapEntry = entryTag != null;
		this.entryTag = wrapUnwrapEntry ? entryTag.clone() : null;

		this.log = log;
	}

	/**
	 * Retrieves elements for the specified path.
	 * 
	 * @param pathNum
	 *            number of the path
	 * @return all elements under this path
	 */
	String[][] getPath(int pathNum) {
		return compiled[pathNum];
	}

	/**
	 * Retrieves the prefixes declaration for the specified path
	 * 
	 * @param pathNum
	 *            path number from the root
	 * @return the prefix declaration
	 */
	HashMap<String, String> getDecl(int pathNum) {
		return decl.get(pathNum);
	}

	/**
	 * Retrieves the attributes declaration for the specified path
	 * 
	 * @param pathNum
	 *            path number from the root
	 * @return the attribute declaration
	 */
	HashMap<AttrEntityKey, String> getAttr(int pathNum) {
		return attr.get(pathNum);
	}

	/**
	 * Retrieves path length.
	 * 
	 * @return path length.
	 */
	int pathsSize() {
		return compiled.length;
	}

	/**
	 * Retrieves the QName list of current xPath elements parser is working on
	 * 
	 * @return QName List of current xPath elements parser is working on
	 */
	List<QName> getCurrentXPath() {

		List<QName> path = new ArrayList<QName>();

		QName elementQ = null;

		for (int i = 0; i < level.size(); i++) {

			String[] element = level.get(i);

			if (element[0] != null && element[1] != null && element[2] != null) {
				elementQ = new QName(element[2], element[1], element[0]);
			} else if (element[1] != null && element[0] == null
					&& element[2] == null) {
				elementQ = new QName(element[1]);
			} else if (element[1] != null && element[2] != null
					&& element[0] == null) {
				elementQ = new QName(element[2], element[1]);
			}

			path.add(i, elementQ);
		}

		return path;
	}

	/**
	 * This method is called by the XML Parser2 when it encounters a start
	 * element. Using this method the XML Parser2 decides whether the element
	 * belongs to the path the user is specified in as a parameter.
	 * 
	 * @param prefix
	 *            - the prefix of the start element that was just found
	 * @param localName
	 *            - the localName of the start element that was just found
	 * @param namespaceURI
	 *            - the namespaceURI of the start element that was just found
	 * @return SimpleXPathEvaluator.EXACT_MATCH_FOUND - if the passed element
	 *         completes any of the paths the user have specified<br>
	 *         SimpleXPathEvaluator.PARTIAL_MATCH_FOUND - if the passed element
	 *         belongs to (a) path(s) from the configuration but might contain
	 *         (a) child(s) that should be also checked. <br>
	 *         SimpleXPathEvaluator.NO_MATCH_FOUND - if the passed element does
	 *         not belong to any of the specified paths and thus the XML Parser2
	 *         should take a step back.
	 * 
	 */
	byte match(String prefix, String localName, String namespaceURI) {

		byte result = NO_MATCH_FOUND;

		level.add(new String[] { prefix, localName, namespaceURI });

		path: for (int path = 0; path < compiled.length; path++) {

			if (compiled[path].length < (level.size())) {
				// the path is not the one we need, check the next one
				continue;
			}

			for (int elem = 0; elem < level.size(); elem++) {

				if (!internalCheckEquality(level.get(elem),
						compiled[path][elem])) {
					continue path;
				}
			}

			if (compiled[path].length == level.size()) {
				result = EXACT_MATCH_FOUND;
				break;
			} else if (compiled[path].length > level.size()) {
				result = PARTIAL_MATCH_FOUND;
			}
		}

		if (result == NO_MATCH_FOUND) {
			decreaseCurrentLevel();
		}

		return result;
	}

	/**
	 * Retrieves level size.
	 * 
	 * @return level size.
	 */
	int getLevelSize() {
		return level.size();
	}

	/**
	 * Retrieves current element.
	 * 
	 * @return the last element that was acknowledged and was left in the List
	 */
	String[] getCurrentLevel() {
		if (level != null && level.size() > 0) {
			return level.get(level.size() - 1);
		} else
			return null;
	}

	/**
	 * removes the last entered element from the List
	 */
	void decreaseCurrentLevel() {
		if (level != null && level.size() > 0) {
			level.remove(level.size() - 1);
		}
	}

	/**
	 * This method parse the xPath and the nsMap strings to create the compiled
	 * array which contains the information needed for the navigation through
	 * the XML elements when the XML Parser2 is in Input mode.
	 * 
	 * Warning: if this method is called the <code>compiled</code> array will be
	 * reinitialized.
	 * 
	 * @see #isCompiledForReading()
	 * @see #compileForWriting()
	 * @see #isCompiledForWriting()
	 * 
	 * @throws Exception
	 *             - in case any of the parameters is not correctly filled out
	 *             or an error occurs after the parameters are parsed
	 */
	void compileForReading() throws Exception {

		// xPath could be null if external class is using this class so just
		// prepare for that kind of usage
		xPath = (xPath == null || xPath.trim().length() == 0) ? WILDCARD
				: xPath;

		// we are separating xPaths with the pipe char
		String[] paths = xPath.trim().split("\\|");

		// we are separating prefix:namespace definitions with the pipe char
		String[] nameSpaces = null;

		// make sure we initialize the nameSpaces array
		nsMap = nsMap == null ? "" : nsMap;

		nameSpaces = nsMap.trim().split("\\|");

		// boolean array full of false values.
		boolean[] invalidPath = new boolean[paths.length];

		int invalidPathsCount = 0;

		for (int i = 0; i < paths.length; i++) {

			// remove white space
			paths[i] = paths[i].trim();

			// do this before slashes removal subroutine for optimization
			if (paths[i].contains("//")) {
				logmsg(resHash
						.getString("XML.PARSER.2.HELPER.UNSUPPORTED.NOTATION"));
				// just log it - we do not support the "//" notation and
				++invalidPathsCount;
				invalidPath[i] = true;
				// ignore this path
				continue;
			}

			paths[i] = trimChars(paths[i], ELEMENT_SEPARATOR);
			paths[i] = trimChars(paths[i], "\"");

			// check if we have ended up with an empty string
			if (paths[i].trim().length() == 0) {
				// we treat the single forward slash as the name of the root(s)
				// element since we do not know it we replace it with the
				// wildcard *
				paths[i] = WILDCARD;
			}
		}

		int correctPaths = paths.length - invalidPathsCount;

		if (correctPaths > 0) {
			compiled = new String[correctPaths][][];
		} else {
			throw new Exception(resHash
					.getString("XML.PARSER.2.HELPER.NO.VALID.PATH.LEFT"));
		}

		// before filling the compiled array prepare the namespaceURI map
		HashMap<String, String> prefixToNSMap = new HashMap<String, String>();
		String currEntry = null;
		String currPrefix = null;
		String currNS = null;

		for (int i = 0; nameSpaces != null && i < nameSpaces.length; i++) {
			currEntry = nameSpaces[i].trim();
			// we are separating prefixes and namespacesURI with "=" char
			int eqPos = currEntry.indexOf('=');
			if (eqPos > 0) {
				currPrefix = currEntry.substring(0, eqPos).trim();
				currPrefix = currPrefix.trim().length() == 0 ? null
						: currPrefix;

				currNS = currEntry.substring(eqPos + 1).trim();
				currNS = trimChars(currNS, "\"");
				currNS = currNS.length() == 0 ? null : currNS;

				if (currNS == null) {
					logmsg(resHash.getString(
							"XML.PARSER.2.HELPER.IGNORE.NS.MAP", currEntry));
				}

				// we have all we need
				prefixToNSMap.put(currPrefix, currNS);
			} else {
				logmsg(resHash.getString("XML.PARSER.2.HELPER.IGNORE.NS.MAP",
						currEntry));
			}
		}

		String[] elements = null;
		String[] name = null;
		int longestPath = 0;
		boolean invalidElement = false;

		// filling the array in
		for (int i = 0, j = 0; i < paths.length; i++) {

			if (!invalidPath[i]) {

				elements = paths[i].split(ELEMENT_SEPARATOR);

				// if we have a wrapper tag we increase the length of each array
				compiled[j] = new String[wrapUnwrapEntry ? elements.length + 1
						: elements.length][];

				for (int k = 0; k < elements.length; k++) {
					name = elements[k].split(":");

					if (name.length == 1) {

						// set the prefix to null
						compiled[j][k] = new String[] { null, name[0].trim(),
								null };

						if (name[0].trim().length() > 1
								&& name[0].contains(WILDCARD)) {
							invalidElement = true;
						}

					} else if (name.length == 2) {
						String prefix = name[0].trim();

						if (prefix.contains(WILDCARD)
								|| (name[1].trim().length() > 1 && name[1]
										.contains(WILDCARD))) {

							invalidElement = true;
						} else {

							compiled[j][k] = new String[] {
									// check for default namespaceURI
									prefix.startsWith("$") ? null : prefix,
									name[1].trim(), prefixToNSMap.get(prefix) };
						}

					}

					if (invalidElement) {
						throw new Exception(resHash.getString(
								"XML.PARSER.2.HELPER.INVALID.PATH", paths[i]));
					}
				}

				if (wrapUnwrapEntry) {
					compiled[j][elements.length] = new String[] { entryTag[0],
							entryTag[1], prefixToNSMap.get(entryTag[0]) };
				}

				// compute the longest path
				if (longestPath < compiled[j].length) {
					longestPath = compiled[j].length;
				}

				j++;
			}
		}

		// setting initial size increases the performance of the ArrayList
		level = new ArrayList<String[]>(longestPath + 1);

		compiledFlag = COMPILED_FOR_READING;
	}

	/**
	 * This method parse the xPath and the attrsDeclaration strings to create
	 * the compiled array which contains the information needed for writing the
	 * static root tags when the XML Parser2 is in Output mode.
	 * 
	 * Warning: if this method is called the <code>compiled</code> array will be
	 * reinitialized.
	 * 
	 * @see #isCompiledForWriting()
	 * @see #compileForReading()
	 * @see #isCompiledForReading()
	 * 
	 * @throws Exception
	 *             - in case any of the parameters is not correctly filled out
	 *             or an error occurs after the parameters are parsed
	 */
	void compileForWriting() throws Exception {

		// xPath could be null if external class is using this class so just
		// prepare for that kind of usage
		xPath = xPath == null ? "" : xPath;

		// working only with the last path
		String path = xPath.trim().split("\\|")[0].trim();

		// handle the wildcard character correctly.
		int wildCardPos = path.indexOf(WILDCARD);
		if (wildCardPos > -1) {
			path = path.substring(0, wildCardPos).trim();

			if (trimChars(path, ELEMENT_SEPARATOR).length() == 0) {
				// this means that the user have specified a wild card or we are
				// running with default configuration. Just set the default root
				// name.
				path = "DocRoot";
			}
		}

		compiled = new String[1][][];

		path = trimChars(path, ELEMENT_SEPARATOR);

		if (path.length() == 0) {
			// this means that no root is specified and the content of the entry
			// will be output as is (no wrapping)
			compiled[0] = null;
		} else {
			// make sure we do not have empty elements.
			String[] elements = path.split(ELEMENT_SEPARATOR);
			String[] buffer = new String[elements.length];
			int length = 0;

			for (int i = 0; i < elements.length; i++)
				if (elements[i].trim().length() > 0) {
					buffer[i] = elements[i];
					length++;
				} else {
					buffer[i] = null;
				}

			elements = new String[length];
			length = 0;

			for (int i = 0; i < buffer.length; i++)
				if (buffer[i] != null) {
					elements[length++] = buffer[i];
				}

			// we have a path specified so break it down
			compiled[0] = new String[elements.length][];

			// the level ArrayList will store the attribute information for each
			// path
			for (int i = 0; i < elements.length; i++) {

				compiled[0][i] = new String[2];
				separatePrefixAndLocalName(elements[i], compiled[0][i]);
			}
		}

		// Prefixes and Attributes parsing section
		// ********************************************************************
		int numberOfOtherElems = wrapUnwrapEntry ? 1 : 0;
		numberOfDeclElems = (compiled[0] == null ? 0 : compiled[0].length)
				+ numberOfOtherElems;

		decl = new ArrayList<HashMap<String, String>>(numberOfDeclElems);
		attr = new ArrayList<HashMap<AttrEntityKey, String>>(numberOfDeclElems);

		// initialize the level list of attributes/prefixes
		for (int i = 0; i < numberOfDeclElems; i++) {
			decl.add(new HashMap<String, String>(4));
			attr.add(new HashMap<AttrEntityKey, String>(4));
		}

		// if attrsDeclaration was initialized with null then think of it as an
		// empty string
		attrsDeclaration = attrsDeclaration == null ? "" : attrsDeclaration;

		// parse the section using the parser itself.
		Reader paramXML = new StringReader(attrsDeclaration);
		XMLParser2 parser = new XMLParser2();
		parser.setInputStream(paramXML);
		parser.initParser();

		Document attrsDecl = parser.readEntry();
		if (attrsDecl != null) {
			Element current = attrsDecl.getDocumentElement();

			int currentLevel = 0;
			parseElement(current, currentLevel);
		}

		compiledFlag = COMPILED_FOR_WRITING;
	}

	/**
	 * Recursively cycle through all the valid paths starting from the current
	 * element. All the found attributes declarations will be added to the attr
	 * list the namespace declarations will be added to the decl list.
	 * 
	 * @param current
	 *            the parent node which children will be traversed.
	 * @param currentLevel
	 *            the level of depth we are into. Useful to know which element
	 *            from the compiled[0] to use for the matching process.
	 */
	private void parseElement(Element current, int currentLevel) {
		boolean matchingElement = false;

		NodeList children = null;

		if (current != null && currentLevel < numberOfDeclElems) {

			if (wrapUnwrapEntry && (currentLevel == numberOfDeclElems - 1)) {
				matchingElement = elementsMatch(current, entryTag);
			} else if (compiled[0] != null && currentLevel < compiled[0].length) {
				matchingElement = elementsMatch(current,
						compiled[0][currentLevel]);
			} else {
				matchingElement = false;
			}

			if (matchingElement) {
				NamedNodeMap attrs = current.getAttributes();
				Attr attrNode = null;
				for (int i = 0; i < attrs.getLength(); i++) {
					attrNode = (Attr) attrs.item(i);
					if ("xmlns".equals(attrNode.getLocalName())
							&& attrNode.getPrefix() == null) {
						// default xml declaration
						decl.get(currentLevel).put(null,
								attrNode.getNodeValue());
					} else if ("xmlns".equals(attrNode.getPrefix())) {
						// prefix to namespace binding
						decl.get(currentLevel).put(attrNode.getLocalName(),
								attrNode.getNodeValue());
					} else {
						// regular attribute declaration
						attr.get(currentLevel).put(
								new AttrEntityKey(attrNode.getPrefix(),
										attrNode.getLocalName()),
								attrNode.getNodeValue());
					}
				}

				// recursively check the children of the current node...
				children = current.getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
						parseElement((Element) children.item(i),
								currentLevel + 1);
					}
				}
			}
		}
	}

	private static boolean elementsMatch(Element xmlElem, String[] element) {

		return ((element[0] == null && xmlElem.getPrefix() == null) || (element[0] != null && element[0]
				.equals(xmlElem.getPrefix())))
				&& ((element[1].equals(xmlElem.getLocalName())));
	}

	/**
	 * This method separates the passed as first argument string to a prefix and
	 * local name.
	 * 
	 * @param fullElement
	 *            - this is the string to be separated. It could be of the
	 *            following form: "prefix:localName" or "localName". Cannot be a
	 *            multi-element string!
	 * @param putHere
	 *            - this is an array with at least two elements in length. After
	 *            this method completes the first element will be set to the
	 *            value of the found prefix or to null if no such value is
	 *            found. The second element will be set to the value of the
	 *            localName found in the <code>fullName</code> argument.
	 * @throws Exception
	 *             if the provided first argument is not "well-formed".
	 */
	static void separatePrefixAndLocalName(String fullElement, String[] putHere)
			throws Exception {

		if (fullElement.indexOf(ELEMENT_SEPARATOR) != -1) {
			throw new Exception(resHash.getString(
					"XML.PARSER.2.HELPER.INVALID.PATH", fullElement));
		}

		String[] temp = fullElement.split(PREFIX_LOCALNAME_SEPARATOR);

		if (temp.length == 1) {
			// set the prefix to null
			putHere[0] = null;
			putHere[1] = temp[0].trim();

		} else if (temp.length == 2) {
			// we have a prefix
			putHere[0] = temp[0].trim();
			putHere[1] = temp[1].trim();

		} else {
			throw new Exception(resHash.getString(
					"XML.PARSER.2.HELPER.INVALID.PATH", fullElement));
		}
	}

	/**
	 * Check if the element have the same features (i.e. the prefixes, the
	 * localNames and the namespaceURIs match) as the element on top of our
	 * stack.
	 * 
	 * @param externalElement
	 *            - an array of Strings where the first element is the prefix of
	 *            the element or null, the second is the localName (must not be
	 *            null) and the third is the namespaceURI (could be null)
	 * 
	 * @return true if the externalElement's prefix, localName and namespaceURI
	 *         matches literally the last element in the level tracker.
	 */
	boolean checkEquality(String[] externalElement) {

		String[] levelElement = null;
		if (level == null || (levelElement = getCurrentLevel()) == null) {
			return false;
		}

		if (
		// check localNames
		(externalElement[1].equals(levelElement[1]))

		//
				// check prefixes
				// if we have prefixes defined and they are the same
				&& ((externalElement[0] != null && levelElement[0] != null && externalElement[0]
						.equals(levelElement[0]))

				// or if both elements do not have prefixes defined
				|| (externalElement[0] == null && levelElement[0] == null))

				// check namespaceURIs
				// if we have namespacesURI defined and they are the same
				&& ((externalElement[2] != null && levelElement[2] != null && externalElement[2]
						.equals(levelElement[2]))

				// or if both elements do not have prefixes defined
				|| (externalElement[2] == null && levelElement[2] == null))) {

			return true;
		}
		return false;
	}

	/**
	 * Makes checks between the compiled array elements and the level tracker
	 * List. <br>
	 * <br>
	 * For both parameters: <br>
	 * zero position contains the prefix of the element<br>
	 * first position contains the localName of the element <br>
	 * second position contains the namespaceURI of the element<br>
	 * 
	 * @param currentElement
	 *            - this is the element that will be matched against a
	 *            predefined in the configuration element
	 * @param userElement
	 *            - this is the element that has been predefined by the user in
	 *            the configuration
	 * 
	 * @return - true if both elements comply with the internal rules and logic
	 */
	static boolean internalCheckEquality(String[] currentElement,
			String[] userElement) {
		boolean match = false;

		// check localNames and if they do not match then check whether the user
		// have specified a wildcard for elements on this level.
		if (currentElement[1].equals(userElement[1])
				|| WILDCARD.equals(userElement[1])) {
			if (userElement[0] != null) {
				// the user have defined a prefix for this element
				if (userElement[2] != null
						&& userElement[2].equals(currentElement[2])) {
					// the prefix:namespace relation is present
					// at this point we know that the user wants to match
					// localNames and namespacesURIs
					match = true;
				} else if (userElement[0].equals(currentElement[0])) {
					// no namespaceURI for the prefix so we are matching only
					// prefixes and localNames. The currentElement must have a
					// namespaceURI but we are ignoring it
					match = true;
				}
			} else {
				// the user have not specified a XML prefix. He/she may be
				// specified a default namespaceURI so check for that
				if (userElement[2] != null) {
					// yes a default namespaceURI is specified so check against
					// the currentElement. Make sure the currentElement does not
					// have a prefix either
					if (currentElement[0] == null
							&& userElement[2].equals(currentElement[2])) {
						// default namespacesURI match
						match = true;
					}
				} else {
					// at this point we know that the user have specified only a
					// localName without any prefix or namespaceURI relation so
					// return true since we know that the localNames already
					// match
					match = true;
				}
			}
		}

		return match;
	}

	/**
	 * Removes the specified chars from the beginning and the end of the string.
	 * 
	 * @param str
	 *            String to be manipulated
	 * @param ch
	 *            character to be removed
	 * @return the trimmed string
	 */
	String trimChars(String str, String ch) {

		while (str.startsWith(ch)) {
			str = str.substring(1).trim();
		}
		while (str.endsWith(ch)) {
			str = str.substring(0, str.length() - 1).trim();
		}

		return str;
	}

	/**
	 * lookup the namespaceURI for the specified level or if not found on that
	 * level then the above levels are checked.
	 * 
	 * @param level
	 *            - the position of the element in the level tracker List
	 * @param prefix
	 *            - the prefix string
	 * @return - the namespaceURI if it is found on that level or above
	 */
	String getNSForLevelUP(int level, String prefix) {
		String namespaceURI = null;

		for (int i = level; i >= 0 && i < decl.size(); i--) {
			namespaceURI = decl.get(i).get(prefix);
			if (namespaceURI != null) {
				return namespaceURI;
			}
		}
		return null;
	}

	/**
	 * Checks whether the parameters were compiled for reading.
	 * 
	 * @return true if the parameters were compiled for reading
	 */
	boolean isCompiledForReading() {
		return compiledFlag == COMPILED_FOR_READING;
	}

	/**
	 * Checks whether the parameters were compiled for writing.
	 * 
	 * @return true if the parameters were compiled for writing
	 */
	boolean isCompiledForWriting() {
		return compiledFlag == COMPILED_FOR_WRITING;
	}

	/**
	 * Logs message to the log used when the object was created.
	 * 
	 * @param msg
	 *            - the message which will be logged.
	 */
	void logmsg(String msg) {
		if (log != null) {
			log.logdebug(msg);
		}
	}

	/**
	 * This class represents the prefix-localName pair used in the attr
	 * structure.
	 * 
	 * @since 7.0
	 */
	static class AttrEntityKey {
		/**
		 * private field holding the prefix of the attribute.
		 */
		private String prefix = null;

		/**
		 * private field holding the localName of the attribute.
		 */
		private String localName = null;

		/**
		 * creates an instance of this object with the specified prefix and
		 * localName
		 * 
		 * @param prefix
		 * @param localName
		 */
		public AttrEntityKey(String prefix, String localName) {
			this.prefix = prefix;
			this.localName = localName;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object o) {
			if (o instanceof AttrEntityKey) {
				AttrEntityKey entityKey = (AttrEntityKey) o;

				return (prefix == entityKey.prefix || (prefix != null && prefix
						.equals(entityKey.prefix))
						&& (localName == entityKey.localName || (localName != null && localName
								.equals(entityKey.localName))));
			}

			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return (prefix == null ? 0 : prefix.hashCode())
					^ (localName == null ? 0 : localName.hashCode());
		}

		/**
		 * @return the prefix of the attribute
		 */
		public String getPrefix() {
			return prefix;
		}

		/**
		 * @return the localName of the attribute
		 */
		public String getLocalName() {
			return localName;
		}
	}
}
