/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.bind;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.XMLConstants;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.ibm.di.config.bind.AssemblyLineBinding;
import com.ibm.di.config.bind.ContainerBinding;
import com.ibm.di.config.bind.SolutionBinding;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.entry.Attribute;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class BindUtil {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final int ATTR_NODE_DOM = 0;
	private static final int ATTR_NODE_VALS_ONLY = 1;
	private static final int ATTR_NODE_CHILDREN_ONLY = 2;
	private static final int ATTR_NODE_EMPTY = 3;

	/**
	 * Maps from TDI Entry to the JAX-B representation. TDI Entries are always
	 * represented as hierarchical, even if the input entry is flat.
	 */
	public static Entry fromEntry(com.ibm.di.entry.Entry e) {
		if (e == null) {
			return null;
		}

		if (!e.isDOMEnabled()) {
			// better clone it as you don't know where that entry is coming from
			// and who will use it next
			e = e.clone();
		}

		Entry res = new Entry();
		for (String prop : e.getPropertyNames()) {
			EntryProperty p = new EntryProperty();
			p.setName(prop);
			Object val = e.getProperty(prop);
			p.setValue(val == null ? null : val.toString());
			res.getProperties().add(p);
		}

		NodeList children = e.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
				EntryAttribute ea = getEntryAttribute((Element) children.item(i));
				res.getAttributes().add(ea);
			}
		}

		return res;
	}

	private static EntryAttribute getEntryAttribute(Element item) {
		EntryAttribute res = new EntryAttribute();
		res.setName(item.getNodeName());
		res.setNamespace(XMLConstants.DEFAULT_NS_PREFIX.equals(item.getNamespaceURI()) ? null : item.getNamespaceURI());
		res.setProtect(((Attribute) item).getProtected());

		NamedNodeMap props = item.getAttributes();
		for (int i = 0; i < props.getLength(); i++) {
			Attr prop = (Attr) props.item(i);
			AttributeProperty p = new AttributeProperty();
			p.setName(prop.getNodeName());
			p.setNamespace(XMLConstants.DEFAULT_NS_PREFIX.equals(prop.getNamespaceURI()) ? null : prop.getNamespaceURI());
			p.setValue(prop.getValue());
			res.getProperties().add(p);
		}

		NodeList children = item.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
				res.getChildren().add(getEntryAttribute((Element) children.item(i)));
			} else if (children.item(i).getNodeType() == Node.CDATA_SECTION_NODE
					|| children.item(i).getNodeType() == Node.TEXT_NODE) {
				AttributeValue av = new AttributeValue();
				if(children.item(i) instanceof com.ibm.di.entry.AttributeValue) {
					com.ibm.di.entry.AttributeValue nav = (com.ibm.di.entry.AttributeValue) children.item(i);
					
					Object jsonValue = convertValue2JSON(nav.getValue());
					if(jsonValue != null) {
						res.getChildren().add(jsonValue);
						continue;
					} else if (nav.getValue() instanceof List) {
						List lst = (List)nav.getValue();
						for(Object obj : ((List)nav.getValue())) {
							jsonValue = convertValue2JSON(obj);
							if(jsonValue != null)
								res.getChildren().add(jsonValue);
							else
								res.getChildren().add(obj);
						}
						continue;
					} else if (nav.getValue() instanceof Date) {
						av.setValue(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").format((Date)nav.getValue()));
						res.getChildren().add(av);
						continue;
					}
				}
				av.setValue(children.item(i).getNodeValue());
				res.getChildren().add(av);
			}
		}

		return res;
	}
	
	public static Object convertValue2JSON(Object value) {
		if(value instanceof com.ibm.di.entry.Entry)
			return fromEntry((com.ibm.di.entry.Entry) value);
		else if (value instanceof Integer || value instanceof Boolean || value instanceof Double || value instanceof Float) {
			return value;
		} else if (value instanceof BigDecimal) {
			return ((BigDecimal)value).doubleValue();
		} else if (value instanceof BigInteger) {
			return ((BigInteger)value).longValue();
		}
		return null;
	}
	
	public static TaskStatistics fromTaskStatistics(com.ibm.di.server.TaskStatistics stats) {
		TaskStatistics ts = new TaskStatistics();
		addStat(ts, "add", stats.add);
		addStat(ts, "mod", stats.mod);
		addStat(ts, "del", stats.del);
		addStat(ts, "get", stats.get);
		addStat(ts, "getTries", stats.getTries);
		addStat(ts, "getclient", stats.getclient);
		addStat(ts, "getclientTries", stats.getclientTries);
		addStat(ts, "callreply", stats.callreply);
		addStat(ts, "err", stats.err);
		addStat(ts, "nochange", stats.nochange);
		addStat(ts, "lookup", stats.lookup);
		addStat(ts, "skip", stats.skip);
		addStat(ts, "ignore", stats.ignore);
		addStat(ts, "reply", stats.reply);
		addStat(ts, "branchtrue", stats.branchtrue);
		addStat(ts, "branchfalse", stats.branchfalse);
		addStat(ts, "switches", stats.switches);
		addStat(ts, "loopstart", stats.loopstart);
		addStat(ts, "loopcycles", stats.loopcycles);
		addStat(ts, "start", stats.start);
		addStat(ts, "end", stats.end);
		addStat(ts, "reconnect", stats.reconnect);
		addStat(ts, "reconnectTime", stats.reconnectTime);
		java.lang.Exception e = stats.getError();
		if (e != null) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			addException(ts, e.getMessage(), sw.toString());
		}
		return ts;
	}

	private static void addStat(TaskStatistics ts, String name, long value) {
		if (value > 0) {
			Stat s = new Stat();
			s.setName(name);
			s.setValue(value);
			ts.getStats().add(s);
		}
	}

	private static void addException(TaskStatistics ts, String msg, String stack) {
		if (msg == null) {
			return;
		}
		Exception e = new Exception();
		e.setMessage(msg);
		e.setStack(stack);
		ts.setError(e);
	}

	public static com.ibm.di.entry.Entry toEntry(Entry e) {
		if (e == null) {
			return null;
		}

		com.ibm.di.entry.Entry res = new com.ibm.di.entry.Entry(false);
		toEntry(e, res);

		return res;
	}

	private static void toEntry(Entry e, com.ibm.di.entry.Entry res) {
		for (EntryProperty p : e.getProperties()) {
			res.setProperty(p.getName(), p.getValue());
		}

		for (EntryAttribute att : e.getAttributes()) {
			if (res.isDOMEnabled()) {
				setAttribute(res, res, att);
			} else {
				setAttribute(res, (String) null, att);
			}
		}
	}

	/**
	 * Sets a potentially flat attribute on an entry. In case the attribute
	 * turns out to comply with a hierarchical model the entry is made DOM
	 * enabled.
	 */
	private static void setAttribute(com.ibm.di.entry.Entry res, String parentsEscName, EntryAttribute att) {

		// model:
		// __<a>
		// ____<b>val1, val2</b>
		// __</a>
		// is presented as flat attribute: "a.b = [val1, val2]"
		// where the model:
		// __<a>
		// ____<b>val1</b>
		// ____<b>val2</b>
		// __</a>
		// is represented as DOM model

		int attType = ATTR_NODE_EMPTY;
		List<String> sibNames = new LinkedList<String>();

		List<Object> children = att.getChildren();
		for (int i = 0; i < children.size() && attType != ATTR_NODE_DOM; i++) {
			if (children.get(i) instanceof AttributeValue) {
				attType = attType == ATTR_NODE_CHILDREN_ONLY ? ATTR_NODE_DOM : ATTR_NODE_VALS_ONLY;
			} else if (children.get(i) instanceof EntryAttribute) {
				attType = attType == ATTR_NODE_VALS_ONLY || ((EntryAttribute) children.get(i)).getProperties().size() > 0
						|| sibNames.contains(((EntryAttribute) children.get(i)).getName()) ? ATTR_NODE_DOM
						: ATTR_NODE_CHILDREN_ONLY;
				sibNames.add(((EntryAttribute) children.get(i)).getName());
			} else if (children.get(i) instanceof Entry) {
				attType = ATTR_NODE_VALS_ONLY;
			}
		}
		sibNames = null;

		Attribute newAttr = null;
		String attEscName = concateNamesAndEscape(parentsEscName, att.getName());
		switch (attType) {
		case ATTR_NODE_EMPTY:
			// empty attribute element
			// this is usually caused by entry.newAttribute();
			res.newAttribute(attEscName);
			break;
		case ATTR_NODE_VALS_ONLY:
			// reached the bottom of the recursion this is a flat attribute for
			// sure
			newAttr = res.newAttribute(attEscName);
			for(Object val : att.getChildren()) {
				if(val instanceof EntryAttribute) {
					newAttr.setValues( ((EntryAttribute)val).getChildren() );
				} else if(val instanceof AttributeValue) {
					newAttr.addValue(((AttributeValue)val).getValue());
				} else {
					newAttr.addValue(val instanceof Entry ? toEntry((Entry)val) : val);
				}
			}
//			newAttr.setValues(att.getChildren());
			break;
		case ATTR_NODE_CHILDREN_ONLY:
			// need to represent this empty node as part of a flat attribute.
			for (Object child : att.getChildren()) {
				// some of the setAttribute(Entry, String, EntryAttribute) might
				// enable dom so check
				if (res.isDOMEnabled()) {
					newAttr = res.newAttribute(attEscName);
					setAttribute(res, newAttr, (EntryAttribute) child);
				} else {
					setAttribute(res, attEscName, (EntryAttribute) child);
				}
			}

			if (newAttr != null) {
				// enabled DOM, so check for props
				for (AttributeProperty p : att.getProperties()) {
					if (p.getNamespace() != null) {
						newAttr.setAttributeNS(p.getNamespace(), p.getName(), p.getValue());
					} else {
						newAttr.setAttribute(p.getName(), p.getValue());
					}
				}
			}
			break;
		case ATTR_NODE_DOM:
			if (parentsEscName == null) {
				// entry level
				setAttribute(res, res, att);
			} else {
				// get parent
				newAttr = res.newAttribute(attEscName);
				setAttribute(res, newAttr, att);
			}
			break;
		}
	}

	private static String concateNamesAndEscape(String parentsEscName, String attUnescName) {
		return parentsEscName != null ? parentsEscName + com.ibm.di.entry.Entry.PATH_SEPARATOR_CHAR
				+ Attribute.escapeName(attUnescName) : Attribute.escapeName(attUnescName);
	}

	/**
	 * Sets a DOM attribute on a DOM parent
	 */
	private static void setAttribute(Document res, Node parent, EntryAttribute att) {
		Element e = att.getNamespace() != null ? res.createElementNS(att.getNamespace(), att.getName()) : res.createElement(att
				.getName());
		parent.appendChild(e);

		for (AttributeProperty p : att.getProperties()) {
			if (p.getNamespace() != null) {
				e.setAttributeNS(p.getNamespace(), p.getName(), p.getValue());
			} else {
				e.setAttribute(p.getName(), p.getValue());
			}
		}

		for (Object child : att.getChildren()) {
			if (child instanceof AttributeValue) {
				Text text = res.createTextNode(((AttributeValue) child).getValue());
				e.appendChild(text);
			} else if (child instanceof EntryAttribute) {
				setAttribute(res, e, (EntryAttribute) child);
			}
		}
	}

	public static com.ibm.di.server.TaskCallBlock toTCB(TaskCallBlock tcb) throws java.lang.Exception {
		if (tcb == null) {
			return null;
		}

		com.ibm.di.server.TaskCallBlock res = new com.ibm.di.server.TaskCallBlock();
		AssemblyLineConfig al = toAL(tcb.getAssemblyLine());
		if (al != null) {
			res.applyALSettings(al);
		}

		toEntry(tcb, res);

		com.ibm.di.entry.Entry iwe = toEntry(tcb.getIwe());
		if (iwe != null) {
			res.setInitialWorkEntry(iwe);
		}

		TcbRuntime rt = tcb.getRuntime();
		if (rt != null) {
			String mode = rt.getMode();
			if (mode != null) {
				res.setRunMode(mode);
			}

			String op = rt.getOperation();
			if (op != null) {
				res.setOperation(op);
			}

			Entry e = rt.getInitParam();
			if(e != null) {
				res.setOperationInitParams(toEntry(e));
			}
//			for (TcbInitParam p : rt.getInitParam()) {
//				res.setOperationInitParam(p.getName(), p.getValue());
//			}

			if (rt.getComponents() != null) {
				for (TcbComponent c : rt.getComponents().getComponent()) {
					res.setComponentEnabled(c.getName(), c.isEnabled());

					for (TcbParameter p : c.getParameter()) {
						if (p.isProtect()) {
							res.setComponentParameter(c.getName(), p.getName(), p.getValue(), true);
						} else {
							res.setComponentParameter(c.getName(), p.getName(), p.getValue());
						}
					}
				}
			}
		}

		return res;
	}

	private static AssemblyLineConfig toAL(AssemblyLineBinding al) throws java.lang.Exception {
		if (al == null) {
			return null;
		}

		SolutionBinding sb = new SolutionBinding();
		ContainerBinding cb = new ContainerBinding();
		cb.setName(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER);
		cb.getConfigs().add(al);
		sb.getContainers().add(cb);

		MetamergeConfig mc = com.ibm.di.config.bind.BindUtil.toMetamergeConfig(sb);
		Object alc = mc.lookup(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/" + al.getName());

		return alc instanceof AssemblyLineConfig ? (AssemblyLineConfig) alc : null;
	}

	public static Tombstone fromTombstone(com.ibm.di.api.Tombstone ts) {
		if (ts == null) {
			return null;
		}
		Tombstone newTs = new Tombstone();
		newTs.setGuid(ts.getGUID());
		newTs.setExitCode(ts.getExitCode());
		newTs.setErrorDescription("".equals(ts.getErrorDescription()) ? null : ts.getErrorDescription());
		newTs.setCreatedOn(ts.getTombstoneCreateTime().getTime());

		if (ts.getComponentName() != null && ts.getComponentName().startsWith("AssemblyLines/")) {
			ALTombstoneData ald = new ALTombstoneData();
			// skip the "AssemblyLines/" string
			ald.setAlName(ts.getComponentName().substring(14));
			ald.setConfigInstanceId(ts.getConfiguration());
			ald.setUserMessage(ts.getUserMessage());
			ald.setStartedOn(ts.getStartTime().getTime());
			ald.setStatistics(fromEntryTaskStatistics(ts.getStatistics()));
			newTs.setData(ald);
		} else {
			CITombstoneData cid = new CITombstoneData();
			cid.setConfigInstanceId(ts.getComponentName());
			cid.setStartedOn(ts.getStartTime().getTime());
			newTs.setData(cid);
		}

		return newTs;
	}

	/**
	 * @param statistics
	 * @return
	 */
	private static TaskStatistics fromEntryTaskStatistics(com.ibm.di.entry.Entry statistics) {
		if (statistics == null) {
			return null;
		}
		TaskStatistics ts = new TaskStatistics();
		for (String stat : statistics.getAttributeCollection()) {
			if (!"exception".equals(stat)) {
				addStat(ts, stat, Long.parseLong(statistics.getString(stat)));
			}
		}
		addException(ts, statistics.getString("exception"), null);

		return null;
	}
}
