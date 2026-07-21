/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.util.HookTree;
import com.ibm.di.util.ParameterSubstitutionCache;
import com.ibm.icu.util.StringTokenizer;
import com.ibm.jscript.IValue;
import com.ibm.jscript.types.FBSNull;

public class AttributeMapping {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PROPERTIES_FILE = "miserver";

	// Private properties
	private String name;

	private Map<String, SingleAttributeMap> samTable = new HashMap<String, SingleAttributeMap>();

	private List<String> advancedAttrs = new ArrayList<String>();

	private AssemblyLine assemblyLine;

	private Log log;

	private ScriptEngine se;

	private boolean mapAllAttributes = false;

	private boolean mapStar = false;

	private String defaultNullBehavior = null;

	private String defaultNullBehaviorValue = "";

	private String nullException = "input.attribute.missing";

	private static int NVD_ABSENT = 0;

	private static int NVD_EMPTY_ATTRIBUTE = 1;

	private static int NVD_EMPTY_STRING = 2;

	private static int NVD_VALUE = 3;

	private String defaultNullDefinition = null;

	private String defaultNullDefinitionValue = "";

	private SingleAttributeMap anonymous;

	private ParameterSubstitutionCache psc = new ParameterSubstitutionCache();

	private boolean isInputMap;

	private String mapName;

	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	// Public Properties
	public Object value;

	public final static String SCRIPT_OBJECT = "thisScriptObject";

	private Entry scriptObject = new Entry();

	//
	// Constructor
	//
	public AttributeMapping(String name, TaskInterface context, Log log,
			ScriptEngine se) {
		Trace.entrymax(this, "AttributeMapping");
		this.name = name;
		this.log = log;
		this.se = se;
		if (context instanceof AssemblyLine) {
			assemblyLine = (AssemblyLine) context;
			psc.put("op-entry", assemblyLine.getOpEntry());
			psc.put("task", assemblyLine);
			scriptObject.setAttribute("AssemblyLine", assemblyLine.getName());
		}
		scriptObject.setAttribute("Component", name);
		Trace.exitmax(this, "AttributeMapping");
	}

	public void setAutomap(boolean automap) {
		mapAllAttributes = automap;
	}

	public boolean getAutomap() {
		return mapAllAttributes;
	}
	
	public void declareStaticBean(String name, Object bean) throws Exception {
		se.declareStaticBean(name, bean);
	}

	public void declareBean(String name, Object bean) throws Exception {
		se.declareBean(name, bean);
	}

	public void releaseBeans() {
		se.clear();
	}

	public void undeclareBean(String name) throws Exception {
		se.undeclareBean(name);
	}

	public void undeclareStaticBean(String name) throws Exception {
		se.undeclareStaticBean(name);
	}

	public void pushStackFrame(AssemblyLineComponent tc) throws Exception {
		se.pushStackFrame();
		se.declareBean("thisConnector", tc);
		se.declareBean("thisComponent", tc);
	}

	public void popStackFrame() {
		se.popStackFrame();
	}

	public void unload() {
		se = null;
		assemblyLine = null;
	}

	public void loadMap(AttributeMapConfig map) throws Exception {
		Trace.entrymax(this, "loadMap", map);
		if (map == null) {
			String errorMessage = sResHash.getString("att.map.null.config");
			throw new Exception(errorMessage);
		}

		isInputMap = ConnectorConfig.INPUT_MAP_NAME.equals(map.getShortName());
		mapName = map.getShortName();
		scriptObject.setAttribute("Map", mapName);
		setDefaultNullValues(map);

		anonymous = new SingleAttributeMap();

		if (ConnectorConfig.OUTPUT_MAP_NAME.equals(map.getShortName()))
			nullException = "output.attribute.missing";

		for (String attr : map.getAttributeNames()) {

			AttributeMapItem ami = map.getAttributeMapItem(attr);

			if (attr.equals("*")) {
				anonymous = new SingleAttributeMap(attr, ami);
				mapStar = true;
				continue;
			}

			if (ami.getEnabled())
				samTable.put(attr, new SingleAttributeMap(attr, ami));

		}

		psc.put("config", map);
		Trace.exitmax(this, "loadMap");
	}

	public void loadEventMap(HooksConfig map) throws Exception {
		if (map == null) {
			String errorMessage = sResHash
					.getString("att.map.hooks.null.config");
			throw new Exception(errorMessage);
		}

		for (String attr : map.getKeys(BaseConfiguration.RECURSIVE_SUBTREE)) {
			HookConfig hc = map.getHook(attr);
			if (hc.getEnabled())
				samTable.put(attr, new SingleAttributeMap(attr, hc.getScript(),
						hc.getDebugBreak(false)));
		}
	}

	public String getName() {
		return name;
	}

	public boolean hasAttribute(String attr) {
		return samTable.containsKey(attr);
	}

	public boolean isDebugEnabled(String attr) {
		SingleAttributeMap sam = samTable.get(attr);
		if (sam == null)
			return false;
		else
			return sam.isDebugEnabled();
	}

	public boolean eval(String attr) throws Exception {
		SingleAttributeMap sam = samTable.get(attr);
		if (sam == null)
			return false;

		sam.logTrace("att.map.eval.trigger");

		value = null;

		try {
			sam.eval();
		} catch (Exception ex) {
			value = ex;
		}

		if (value == null)
			return true;

		if (value instanceof Exception) {
			if (!value.getClass().getName().startsWith("com.ibm.di"))
				log.error("while.evaluating",
						new Object[] { sResHash.getString("Hook." + attr),
								name, sam.getKey() }, (Exception) value);
			throw (Exception) value;
		}
		return false;
	}

	/**
	 * Returns either an Entry or an Attribute... FOR INTERNAL USE ONLY!!!
	 * 
	 * @param attr
	 *            the attribute name.
	 * @return either an Entry or an Attribute
	 * @throws Exception
	 */
	public Object mapAttribute(String attr) throws Exception {
		Trace.entrymax(this, "mapAttribute", attr);
		SingleAttributeMap sam = samTable.get(attr);
		if (sam == null)
			return null;

		Object resultObj = null;

		value = null;

		sam.logTrace("att.map.map.attribute");
		if (assemblyLine != null) {
			assemblyLine.debugBreak(sam.getKey());
		}
		try {
			sam.eval();
		} catch (Exception ex) {
			if (ex.getClass().getName().startsWith("com.ibm.di"))
				throw ex;
			String msg = log.getString("while.mapping", new Object[] { attr, mapName, name, sam.getKey() });
			log.error(msg, ex);
			throw new Exception(msg, ex);
		}

		if (value == null) {
			resultObj = sam.getNullValue();
		} else {

			log.debug("return", value.toString());
			log.debug("returned.object.class", value.getClass().getName());

			if (value instanceof Attribute) {

				/**
				 * Avoid cloning Attributes for optimization. The
				 * checking/cloning is handled in the Attribute#getValidNode().
				 * 
				 * @since 7.0
				 */
				// Attribute ca = (Attribute) ((Attribute) value).clone();
				resultObj = (Attribute) value;
				if (sam.isNull((Attribute) value)) {
					resultObj = sam.getNullValue();
				}
			} else if (value instanceof TaskCallBlock) {
				resultObj = new Attribute(attr, value);
			} else if (value instanceof Entry) {
				resultObj = value;
			} else if (value instanceof List) {
				Attribute temp = new Attribute(attr);
				if ("$tcb.accumulator".equals(attr)) {
					temp.addValue(value);
				} else {
					for (Object o : (List<?>) value) {
						if (o instanceof Attribute) {
							temp.addValues((Attribute) o);
						} else {
							temp.addValue(o);
						}
					}

					if (sam.isNull(temp)) {
						temp = sam.getNullValue();
					}
				}

				resultObj = temp;
			} else if (value instanceof NodeList && ! (value instanceof Document)) {
				Attribute temp = new Attribute(attr);
				NodeList nl = (NodeList)value;
				for (int i = 0; i < nl.getLength(); i++) {
					Object o = nl.item(i);
					if (o instanceof Attribute) {
						temp.addValues((Attribute) o);
					} else {
						temp.addValue(o);
					}
				}
				if (sam.isNull(temp)) {
					temp = sam.getNullValue();
				}
				resultObj = temp;
			} else {
				resultObj = new Attribute(attr, value);
				if (sam.isNull((Attribute) resultObj)) {
					resultObj = sam.getNullValue();
				}
			}
		}

		Trace.exitmax(this, "mapAttribute", resultObj);
		return resultObj;
	}

	public Entry mapEntry(Entry source, Entry newEntry) throws Exception {
		return mapEntry(source, newEntry, false);
	}

	/**
	 * Map one entry into another using the loaded attribute Mapping
	 * 
	 * @param source
	 *            The Entry containing the source attributes
	 * @param newEntry
	 *            The Entry that will contain the mapped attributes
	 * @param mergeValues
	 *            If true, do not remove values from newEntry
	 * @exception Exception
	 *                if an attribute is not present
	 */
	public Entry mapEntry(Entry source, Entry newEntry, boolean mergeValues)
			throws Exception {
		Trace.entrymax(this, "mapEntry");
		// Make sure we do not get a NullPointerException...
		if (source == null)
			source = new Entry();
		if (newEntry == null) {
			// if the source is hierarchical make the new entry hierarchical as
			// well.
			newEntry = new Entry(source.isDOMEnabled());
		}

		boolean newEntryIsEmpty = newEntry.size() == 0;

		// Copy delta operation code
		if (!mergeValues && newEntry.getOp() == Entry.OP_GEN)
			newEntry.setOp(source.getOp());

		// Copy all attributes ?
		if (mapStar || (mapAllAttributes && samTable.size() == 0)) {
			if (mapStar && assemblyLine != null) {
				assemblyLine.debugBreak(anonymous.getKey());
			}
			if (source.isDOMEnabled()) {
				// when mapping all of the attributes we need to iterate over a
				// hierarchical entry using the DOM API to avoid loosing
				// information using the flat (old) API.

				// Note: when the destination Entry is a flat one the mapped
				// hierarchical attribute children will not be expanded
				// correctly, meaning that only the top level attribute will be
				// resolvable from the destination entry but none of its
				// children. To quickly fix this behavior we are making the flat
				// entry a hierarchical one and thus we ensure the destination
				// entry handles its children correctly.
				newEntry.enableDOM();

				NodeList children = source.getChildNodes();
				Node child = null;
				for (int i = 0; i < children.getLength(); i++) {
					child = children.item(i);
					if (child instanceof Attribute
							&& !samTable.containsKey(((Attribute)child).getName())) {
						mapNonSimpleAttribute(newEntry, mergeValues,
								!newEntryIsEmpty, ((Attribute)child).getName(),
								(Attribute) child);
					}
				}
			} else {
				Collection<String> names = source.getAttributeCollection();
				for (String attr : names) {

					if (!samTable.containsKey(attr)) {
						mapNonSimpleAttribute(newEntry, mergeValues,
								!newEntryIsEmpty, attr, source
										.getAttribute(attr));
					}
				}
			}
		}

		SingleAttributeMap sam = null;
		String[] sv = null;
		boolean hasValue = false;

		// Do the simple mapping
		for (String attr : samTable.keySet()) {
			sam = samTable.get(attr);

			if (sam.isSimple()) {

				if (assemblyLine != null) {
					assemblyLine.debugBreak(sam.getKey());
				}

				// if we are merging we assume a value already exists.
				hasValue = mergeValues;

				sv = sam.getSimpleValues();

				for (int i = 0; i < sv.length; i++) {
					Attribute value = source.getAttribute(sv[i]);
					if (value != null) {
						if (!hasValue) {
							// doing set only for the first attribute (if
							// mergeValues is false)
							newEntry.setAttribute(attr, value);
							hasValue = true;
						} else {
							newEntry.mergeAttributeValue(attr, value);
						}
					}
				}

				if (hasValue) {
					// check whether the value is null
					if (sam.isNull(newEntry.getAttribute(attr))) {
						if (!mergeValues) {
							newEntry.setAttribute(attr, sam.getNullValue());
						} else {
							newEntry.mergeAttributeValue(attr, sam
									.getNullValue());
						}
					}
				} else {
					// if hasValue is false then mergeValuse is also false and
					// we need to replace the attribute in the newEntry
					Attribute nullAttr = sam.getNullValue();
					if (nullAttr == null) {
						if (!newEntryIsEmpty) {
							newEntry.removeAttribute(attr);
						}
					} else {
						newEntry.setAttribute(attr, nullAttr);
					}
				}
			}
		}

		// No advanced mapping wanted?
		if (advancedAttrs.size() == 0) {
			return newEntry;
		}

		// Do the advanced mapping

		declareBean("ret", this);

		psc.put((isInputMap ? "conn" : "work"), source);
		psc.put((isInputMap ? "work" : "conn"), newEntry);

		Attribute attr = null;
		Collection<String> names = null;
		for (String attName : advancedAttrs) {
			Object result = mapAttribute(attName);

			if (result instanceof Entry) {
				names = ((Entry) result).getAttributeCollection();
				for (String name : names) {
					attr = ((Entry) result).getAttribute(name);
					if (mergeValues) {
						newEntry.mergeAttributeValue(attr.getName(), attr);
					} else {
						newEntry.setAttribute(name, attr);
					}
				}
			} else if (result instanceof Attribute) {
				attr = (Attribute) result;
				if (mergeValues) {
					newEntry.mergeAttributeValue(attName, attr);
				} else {
					newEntry.setAttribute(attName, attr);
				}
			} else if (result == null && !mergeValues && !newEntryIsEmpty) {
				newEntry.removeAttribute(attName);
			}
			attr = null;
		}

		Trace.exitmax(this, "mapEntry", newEntry);
		return newEntry;
	}

	/**
	 * @param newEntry
	 *            this is the destination entry where the passed Attribute will
	 *            occur
	 * @param mergeValues
	 *            tell whether the passed Attribute's values will be merged with
	 *            the destination attribute.
	 * @param removeWhenNull
	 *            this shows whether to remove the destination attribute form
	 *            the newEntry if the passed Attribute resolves to null.
	 * @param attrName
	 *            the name of Attribute to set/merge/remove
	 * @param attr
	 *            the Attribute which will be set in the new entry.
	 * @throws Exception
	 *             if error occurs while getting the null value.
	 */
	private void mapNonSimpleAttribute(Entry newEntry, boolean mergeValues,
			boolean removeWhenNull, String attrName, Attribute attr)
			throws Exception {

		if (anonymous.isNull(attr)) {
			attr = anonymous.getNullValue();
			if (attr != null) {
				attr.setName(attrName);
			}
		}
		if (mergeValues) {
			newEntry.mergeAttributeValue(attrName, attr);
		} else if (attr != null) {
			newEntry.setAttribute(attrName, attr);
		} else if (removeWhenNull) {
			// no need to look for something that we know does not exist...
			newEntry.removeAttribute(attrName);
		}
	}

	private void setDefaultNullValues(AttributeMapConfig map) {
		defaultNullBehavior = map.getNullBehavior();
		defaultNullBehaviorValue = map.getNullBehaviorValue();
		if ((defaultNullBehavior == null || defaultNullBehavior.length() == 0 || defaultNullBehavior
				.equals("Default Behavior"))
				&& map.getParent() != null) {
			defaultNullBehavior = map.getParent().getNullBehavior();
			defaultNullBehaviorValue = map.getParent().getNullBehaviorValue();
		}
		if ((defaultNullBehavior == null || defaultNullBehavior.length() == 0 || defaultNullBehavior
				.equals("Default Behavior"))
				&& assemblyLine != null) {
			defaultNullBehavior = assemblyLine.getNullBehavior();
			defaultNullBehaviorValue = assemblyLine.getNullBehaviorValue();
		}

		defaultNullDefinition = map.getNullDefinition();
		defaultNullDefinitionValue = map.getNullDefinitionValue();
		if ((defaultNullDefinition == null
				|| defaultNullDefinition.length() == 0 || defaultNullDefinition
				.equals("Default"))
				&& map.getParent() != null) {
			defaultNullDefinition = map.getParent().getNullDefinition();
			defaultNullDefinitionValue = map.getParent()
					.getNullDefinitionValue();
		}
		if ((defaultNullDefinition == null
				|| defaultNullDefinition.length() == 0 || defaultNullDefinition
				.equals("Default"))
				&& assemblyLine != null) {
			defaultNullDefinition = assemblyLine.getNullDefinition();
			defaultNullDefinitionValue = assemblyLine.getNullDefinitionValue();
		}
	}

	/*
	 * Return true if no mapping will be done
	 */
	public boolean isEmpty() {
		return samTable.isEmpty() && !mapAllAttributes && !mapStar;
	}

	class SingleAttributeMap {

		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		private String attr;

		private String map;

		private boolean simple = false;

		private String[] simpleValues;

		private String key;

		private int callCounter = 0;

		private boolean debugBreak;

		private Attribute nullValue;

		private boolean nullIsError = false;

		private int nullDefinition;

		private String nullDefinitionValue;

		private boolean isProperty = false;

		private String item;
		private String translated = null;

		public SingleAttributeMap() {
			setNullValue(null, null);
			setNullDefinition(null, null);
			key = getName() + "." + mapName + ".*";
			item = "*";
		}

		public SingleAttributeMap(String attr, AttributeMapItem ami) {
			Trace.entrymax(this, "SingleAttributeMap", attr, ami);
			this.attr = attr;

			if (ami.isSimple()) {
				simple = true;

				StringTokenizer st = new StringTokenizer(ami.getSimple(),
						"\r\n");
				simpleValues = new String[st.countTokens()];

				for (int i = 0; st.hasMoreTokens(); i++) {
					simpleValues[i] = st.nextToken();
				}

			} else {
				simple = false;
				if (ami.isAdvanced())
					map = ami.getScript();
				else
					map = ami.getSubstitution();
				if (map == null || map.length() < 1) {
					log.warn("warn.emptyhook", attr);
					map = "";
				}

				advancedAttrs.add(attr);
				isProperty = ami.isSubstitution();
			}

			debugBreak = ami.getDebugBreak(false);
			key = getName() + "." + mapName + "." + attr;
			item = attr;

			setNullValue(ami.getNullBehavior(), ami.getNullBehaviorValue());
			setNullDefinition(ami.getNullDefinition(), ami
					.getNullDefinitionValue());
			Trace.exitmax(this, "SingleAttributeMap");
		}

		public SingleAttributeMap(String attr, String map, boolean debugBreak) {
			this.attr = attr;
			if (map == null || map.length() < 1) {
				log.warn("warn.emptyhook", attr);
				map = "";
			}
			this.map = map;
			this.debugBreak = debugBreak;
			key = name + "." + attr;
			item = attr;
			translated = HookTree.getHookLabel(item);
		}

		public void eval() throws Exception {
			if (map == null)
				return;

			if (isProperty) {
				value = psc.substitute(map, null, null);
				return;
			}

			try {
				value = FBSNull.nullValue;
				if (translated != null) {
					scriptObject.newAttribute("HookName").setValue(translated);
					scriptObject.newAttribute("InternalHookName")
							.setValue(item);
				} else {
					scriptObject.newAttribute("Attribute").setValue(item);
				}
				se.declareBean(SCRIPT_OBJECT, scriptObject);
				IValue v = se.interpret(map, false, getKey());
				if (value == FBSNull.nullValue) {
					if (v != null)
						value = v.toJavaObject();
					else
						value = null;
				}

			} catch (Exception err) {
				if (!(err instanceof com.ibm.di.exceptions.ReturnException))
					throw err;
			}

		}
		
		public String getMap() {
			return map;
		}

		public String getKey() {
			return key;
		}

		public boolean isSimple() {
			return simple;
		}

		public String[] getSimpleValues() {
			return simpleValues;
		}

		private void setNullDefinition(String definition, String value) {

			if (definition == null || definition.length() == 0
					|| definition.equals("Default")) {
				definition = defaultNullDefinition;
				value = defaultNullDefinitionValue;
			}

			if (definition == null || definition.equals("")
					|| definition.equals("AbsentAttribute"))
				nullDefinition = NVD_ABSENT;
			else if (definition.equals("EmptyAttribute"))
				nullDefinition = NVD_EMPTY_ATTRIBUTE;
			else if (definition.equals("EmptyString"))
				nullDefinition = NVD_EMPTY_STRING;
			else {
				nullDefinition = NVD_VALUE;
				nullDefinitionValue = value;
			}
		}

		public boolean isNull(Attribute attr) {
			if (attr == null) {
				return true;
			}
			if (nullDefinition == NVD_ABSENT) {
				return false;
			}

			if (attr.size() == 0
					&& (attr.getOwnerDocument() == null || (!attr
							.getOwnerDocument().isDOMEnabled() || attr
							.getChildNodes().getLength() == 0))) {
				return true;
			}
			if (nullDefinition == NVD_EMPTY_ATTRIBUTE || attr.size() != 1) {
				return false;
			}

			String value = attr.getValue();
			if (value == null || value.equals("")) {
				return true;
			}
			if (nullDefinition == NVD_EMPTY_STRING) {
				return false;
			}

			return value.equalsIgnoreCase(nullDefinitionValue);
		}

		public Attribute getNullValue() throws Exception {
			log.debug("att.map.use.null.behavior", attr);
			// Input or Output ??
			if (nullIsError)
				throw new com.ibm.di.exceptions.MissingAttributeException(log
						.getString(nullException, attr), attr);
			if (nullValue == null)
				return null;
			// Return a clone for safety, we do not want nullvalue modified
			return (Attribute) nullValue.clone();
		}

		private void setNullValue(String nullBehavior, String nullBehaviorValue) {

			nullValue = null;

			if (nullBehavior == null || nullBehavior.length() == 0
					|| nullBehavior.equals("Default Behavior")) {
				nullBehavior = defaultNullBehavior;
				nullBehaviorValue = defaultNullBehaviorValue;
			}

			if (nullBehavior == null || nullBehavior.equalsIgnoreCase("delete")
					|| nullBehavior.equals(""))
				return;

			if (nullBehavior.equals("Error")) {
				nullIsError = true;
				return;
			}

			nullValue = new Attribute(attr);

			if (nullBehavior.equalsIgnoreCase("null"))
				return;

			if (nullBehavior.equalsIgnoreCase("empty string")) {
				nullValue.setValue("");
				return;
			}

			if (nullBehaviorValue == null)
				return;

			StringTokenizer st = new StringTokenizer(nullBehaviorValue, "\r\n");
			while (st.hasMoreTokens()) {
				nullValue.addValue(st.nextToken());
			}
		}

		public boolean isDebugEnabled() {
			return debugBreak;
		}

		public void logTrace(String key) {
			callCounter++;

			log.debug(key, attr, Integer.toString(callCounter));

			if (callCounter == 1) {
				// first time
				log.debug("att.map.script.is", map);
			}
		}
	}
}
