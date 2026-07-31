/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.apache.xml.serialize.*;

import com.ibm.di.entry.*;
import java.util.*;
import java.io.*;

/*
 import org.apache.soap.*;
 import org.apache.soap.rpc.*;
 import org.apache.soap.server.*;
 import org.apache.soap.encoding.*;
 import org.apache.soap.util.* ;
 import org.apache.soap.util.xml.*;
 */

//import org.xml.sax.SAXException;
//import org.apache.xalan.xslt.*;
public class XMLUtils {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public Exception lastError;

	private Vector loop;

	public String entry2XML(Entry e) throws Exception {
		Document doc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().newDocument();
		Element root = doc.createElement("Metamerge");
		Element entl = doc.createElement("Entry");
		entl.setAttribute("operation", "" + e.getOp());
		doc.appendChild(root);
		root.appendChild(entl);

		loop = new Vector();
		try {
			appendEntry(doc, entl, e);
		} catch (Exception err) {
			loop.removeAllElements();
			throw err;
		}
		loop.removeAllElements();

		/*
		 * StringWriter sw = new StringWriter (); doc.write(sw, "UTF-8");
		 * 
		 * ByteArrayOutputStream bos = new ByteArrayOutputStream();
		 * OutputStreamWriter osw = new OutputStreamWriter(bos, "UTF-8");
		 * osw.write (sw.getBuffer().toString().toCharArray()); osw.close();
		 * //return sw.toString();
		 */
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(bos, "UTF-8");

		OutputFormat format = new OutputFormat(doc); // Serialize DOM
		XMLSerializer serial = new XMLSerializer(osw, format);
		serial.asDOMSerializer(); // As a DOM Serializer
		serial.serialize(doc.getDocumentElement());
		// doc.write (osw, "UTF-8");
		osw.close();
		return bos.toString("UTF-8");
	}

	private void appendEntry(Document doc, Element entl, Entry e) {
		if (loop.contains(e))
			return;

		loop.add(e);

		String[] names = e.getAttributeNames();
		for (int i = 0; i < names.length; i++) {
			Element el = doc.createElement("attribute");
			Attribute attr = e.getAttribute(names[i]);
			el.setAttribute("name", names[i]);
			for (int j = 0; j < attr.size(); j++) {
				Object val = attr.getValue(j);
				if (val instanceof Entry) {
					appendEntry(doc, el, (Entry) val);
				} else {
					Element value = doc.createElement("value");
					value.appendChild(doc.createTextNode(attr.getValue(j)
							.toString()));
					el.appendChild(value);
				}
			}
			entl.appendChild(el);
		}
	}

	public Entry XML2Entry(String xml) throws Exception {
		Entry entry = new Entry();

		StringBufferInputStream sir = new StringBufferInputStream(xml);
		Document doc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().parse(sir);
		Element root = doc.getDocumentElement();

		for (int i = 0; i < root.getChildNodes().getLength(); i++) {
			Node nx = root.getChildNodes().item(i);
			// System.out.println ("Next Node: " + nx.getNodeName());
			if (nx.getNodeName().equalsIgnoreCase("entry")) {
				for (int j = 0; j < nx.getChildNodes().getLength(); j++) {
					Node e = nx.getChildNodes().item(j);
					// System.out.println (" ChildNode: " + e.getNodeName());
					if (e.getNodeName().equalsIgnoreCase("attribute")) {
						// System.out.println (" new attribute: " +
						// e.getAttributes().getNamedItem("name").getNodeValue());
						Attribute attr = new Attribute(e.getAttributes()
								.getNamedItem("name").getNodeValue());
						for (int k = 0; k < e.getChildNodes().getLength(); k++) {
							Node v = e.getChildNodes().item(k);
							if (v.getNodeName().equalsIgnoreCase("value")) {
								// System.out.println (" new value: " +
								// v.getChildNodes().item(0).getNodeValue());
								attr.addValue(v.getChildNodes().item(0)
										.getNodeValue());
							}
						}
						entry.setAttribute(attr);
					}
				}
			}
		}

		return entry;
	}

	/*
	 * public String xslTransformEntry (Entry entry, String xsl) { try {
	 * XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
	 * 
	 * //processor.setStylesheetParam("param1", processor.createXString("my
	 * parameter name"));
	 *  // Have the XSLTProcessor processor object transform "foo.xml" to //
	 * System.out, using the XSLT instructions found in "foo.xsl". StringReader
	 * inp = new StringReader (entry2XML(entry)); StringWriter out = new
	 * StringWriter (); XSLTInputSource stylesheet;
	 * 
	 * if (xsl.startsWith("$GS.")) { TreeMap tm =
	 * com.ibm.di.server.RS.gFileConfig.getScript(xsl.substring (4)); stylesheet =
	 * new XSLTInputSource (new StringReader ( (String) tm.get("script") )); }
	 * else { stylesheet = new XSLTInputSource (new FileReader (xsl)); }
	 * 
	 * processor.process(new XSLTInputSource(inp), new
	 * XSLTInputSource(stylesheet), new XSLTResultTarget(out));
	 * 
	 * return out.toString(); } catch (Exception e) { this.lastError = e; return
	 * null; } }
	 */

	/*
	 * public Object parseSoapRequest (String req) { System.out.println
	 * ("parseSoapRequest\n" + req + "\n***************"); try { RPCRouter
	 * rpcRouter = null; Document callDoc; Element payloadEl; Envelope callEnv;
	 * Call call; String targetID; org.apache.soap.util.xml.XMLParserLiaison xpl =
	 * new XercesParserLiaison();
	 * 
	 * System.out.println ("parseSoapRequest: xpl.read: " + xpl); callDoc =
	 * xpl.read ("- SOAP HTTP RPC Call Envelope -", new StringReader(req));
	 * System.out.println ("parseSoapRequest: callDoc.getdocumentelement()");
	 * payloadEl = callDoc.getDocumentElement (); System.out.println
	 * ("parseSoapRequest: Envelope.unmarshall()"); callEnv =
	 * Envelope.unmarshall (payloadEl); System.out.println ("parseSoapRequest:
	 * Extract call");
	 * 
	 * Vector v = callEnv.getEnvelopeEntries(); for (int i = 0; i < v.size();
	 * i++) { System.out.println (v.elementAt(i)); }
	 * 
	 * //* //call = rpcRouter.extractCallFromEnvelope (callEnv);
	 * //System.out.println ("parseSoapRequest: get targed ID"); //targetID =
	 * call.getTargetObjectURI ();
	 * 
	 * //System.out.println ("TargetID = " + targetID);
	 * 
	 * //return call; // return callEnv;
	 *  } catch (IllegalArgumentException iae) {
	 * 
	 * System.out.println ("IAE: " + iae); return null;
	 *  } catch (Exception e) { lastError = e; System.out.println ("EXCEPTION
	 * OCCURRED " + e); e.printStackTrace(); System.out.println (e); return e; }
	 * 
	 * //return null; }
	 */
	/*
	 * public String xslTransformEntry (Entry entry, String xsl) { try { String
	 * xml = entry2XML (entry); com.ibm.bsf.BSFManager mgr = new
	 * com.ibm.bsf.BSFManager(); mgr.registerScriptingEngine (
	 * 
	 * mgr.registerBean ("lotusxsl:src", new StringReader (xml)); Object result =
	 * mgr.eval ("lotusxsl", xsl, 0, 0,
	 * com.ibm.cs.util.IOUtils.getStringFromReader (new FileReader (xsl)));
	 * 
	 * System.out.println ("XSL: " + result); if (result != null) return
	 * result.toString(); else return null; } catch (Exception e) {
	 * this.lastError = e; return null; } }
	 */

	/*
	 * public String xslTransformEntry (Entry entry, String xsl) {
	 * 
	 * System.out.println ("xslTransformEntry"); System.out.flush(); try {
	 * StringReader xml = new StringReader( entry2XML(entry) ); StringWriter out =
	 * new StringWriter ();
	 * 
	 * System.out.println ("XSLTProcessorFactory.getProcessor");
	 * System.out.flush(); XSLTProcessor processor =
	 * XSLTProcessorFactory.getProcessor();
	 * 
	 * System.out.println ("processor.process"); System.out.flush();
	 * processor.process(new XSLTInputSource(xml), new XSLTInputSource(xsl), new
	 * XSLTResultTarget(out));
	 * 
	 * return out.toString();
	 *  } catch (Exception e) { System.out.println (e.toString()); lastError =
	 * e; return null; } }
	 */
}
