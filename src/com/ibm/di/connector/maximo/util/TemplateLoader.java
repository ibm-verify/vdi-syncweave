/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.ibm.di.connector.maximo.core.SimpleTpaeIFConnector;
import com.ibm.di.connector.maximo.exception.MxConnectorRuntimeException;
import com.ibm.di.server.Log;

/**
 * This class loads a text file and replaces tokens defined inside it.
 * <p>
 * Let's suppose the file <code>/com/ibm/di/maximo/query.xml.template</code>
 * defines the following template:
 * </p>
 * 
 * <pre>
 * &lt;?xml version='1.0' encoding='UTF-8'?&gt;
 * &lt;Query&lt;b&gt;{mos.name}&lt;/b&gt;
 *     xmlns=&quot;http://www.ibm.com/maximo&quot;
 *     creationDateTime=&quot;&lt;b&gt;{creation.date.time}&lt;/b&gt;&quot;
 *     baseLanguage=&quot;EN&quot;
 *     transLanguage=&quot;EN&quot;
 *     messageID=&quot;&lt;b&gt;{message.id}&lt;/b&gt;&quot;
 *     maximoVersion=&quot;&lt;b&gt;{maximo.version}&lt;/b&gt;&quot;
 *     uniqueResult=&quot;false&quot;
 *     maxItems=&quot;&lt;b&gt;{max.items}&lt;/b&gt;&quot;
 *     rsStart=&quot;&lt;b&gt;{rs.start}&lt;/b&gt;&quot;&gt;
 *   &lt;&lt;b&gt;{mos.name}&lt;/b&gt;Query&gt;
 *       &lt;b&gt;{criteria}&lt;/b&gt;
 *   &lt;/&lt;b&gt;{mos.name}&lt;/b&gt;Query&gt;
 * &lt;/Query&lt;b&gt;{mos.name}&lt;/b&gt;&gt;
 * </pre>
 * 
 * <p>
 * The code bellow can be used to load and replace the tokens:
 * </p>
 * 
 * <pre>
 * TemplateLoader tl = new TemplateLoader(&quot;/com/ibm/di/maximo/query.xml.template&quot;);
 * tl.setProperty(&quot;mos.name&quot;, &quot;MXASSET&quot;);
 * tl.setProperty(&quot;maximo.version&quot;, &quot;MX_VERSION&quot;);
 * tl.setProperty(&quot;creation.date.time&quot;, &quot;2007-07-25T10:00:00-03:00&quot;);
 * tl.setProperty(&quot;message.id&quot;, 123);
 * tl.setProperty(&quot;max.items&quot;, &quot;10&quot;);
 * tl.setProperty(&quot;rs.start&quot;, &quot;1&quot;);
 * tl.setProperty(&quot;criteria&quot;, &quot;&lt;ASSET&gt;&lt;ASSETNUM operator=\&quot;=\&quot;&gt;5000&lt;/ASSETNUM&gt;&lt;/ASSET&gt;&quot;);
 * 
 * System.out.println(tl.toString());
 * </pre>
 * 
 * <p>
 * The result should be something like:
 * </p>
 * 
 * <pre>
 * &lt;QueryMXASSET
 *     xmlns=&quot;http://www.ibm.com/maximo&quot;
 *     creationDateTime=&quot;2007-07-25T10:00:00-03:00&quot;
 *     baseLanguage=&quot;EN&quot;
 *     transLanguage=&quot;EN&quot;
 *     messageID=&quot;123&quot;
 *     maximoVersion=&quot;MX_VERSION&quot;
 *     uniqueResult=&quot;false&quot;
 *     maxItems=&quot;10&quot;
 *     rsStart=&quot;1&quot;&gt;
 *   &lt;MXASSETQuery&gt;
 *     &lt;ASSET&gt;
 *       &lt;SOME CRITERIA /&gt;
 *     &lt;/ASSET&gt;
 *   &lt;/MXASSETQuery&gt;
 * &lt;/QueryMXASSET&gt;
 * </pre>
 * 
 * @since 7.1
 */
public final class TemplateLoader {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// Placeholders names for common attributes for all operations
	public static final String CREATION_HOLDER = "creation.date.time";
	public static final String MSGID_HOLDER = "message.id";
	public static final String VERSION_HOLDER = "maximo.version";
	public static final String LANG_HOLDER = "trans.language";

	// Placeholders for Query specific attributes
	public static final String UNIQUERES_HOLDER = "unique.result";
	public static final String MAXITEMS_HOLDER = "max.items";
	public static final String RSSTART_HOLDER = "rs.start";

	// Placeholders used for customizing template
	public static final String OPER_HOLDER = "oper";
	public static final String ARG_HOLDER = "arg";
	public static final String MBO_HOLDER = "mbo";
	public static final String MOS_HOLDER = "mos.name";
	public static final String QUERY_ARGS = "QueryArgs";

	// Template types
	public static final int TYPE_QUERY = 0;
	public static final int TYPE_CREATE = 1;
	public static final int TYPE_UPDATE = 2;
	public static final int TYPE_DELETE = 3;
	public static final int TYPE_SYNC = 4;

	private static final String XML_BEGIN = "<?xml version='1.0' encoding='UTF-8'?>\r\n" + 
											"<{oper}{mos.name}\r\n" + 
											"	xmlns=\"http://www.ibm.com/maximo\"\r\n" + 
											"	creationDateTime=\"{creation.date.time}\"\r\n" + 
											"	transLanguage=\"{trans.language}\"\r\n" + 											
											"	messageID=\"{message.id}\"\r\n" + 
											"	maximoVersion=\"{maximo.version}\"";
	
	private static final String QUERY_ATTR= "	uniqueResult=\"{unique.result}\"\r\n" + 
											"	maxItems=\"{max.items}\"\r\n" + 
											"	rsStart=\"{rs.start}\""; 
	
	private static final String XML_END =   ">\r\n" +
											"  <{mos.name}{arg}>\r\n" + 
											"    {mbo}\r\n" + 
											"  </{mos.name}{arg}>\r\n" + 
											"</{oper}{mos.name}>";

	private static final String XML_QUERY_END =   ">\r\n" +
	"  <{mos.name}{arg} {QueryArgs}>\r\n" + 
	"    {mbo}\r\n" + 
	"  </{mos.name}{arg}>\r\n" + 
	"</{oper}{mos.name}>";

	private final Map<String, String> props;

	private String template;

	/**
	 * Logger used by the TPAE IF Connector.
	 */
	private Log logger = null;

	/**
	 * Constructs a {@link TemplateLoader} with the specified file.
	 * 
	 * @param templateName
	 *            name of the file that contains the template definition
	 */
	public TemplateLoader(final String templateName) {
		this.template = loadTemplate(templateName);
		this.props = new HashMap<String, String>();
	}

	/**
	 * Constructs a {@link TemplateLoader} for the specified type.
	 * 
	 * @param templateType
	 *            type of XML to create
	 */
	public TemplateLoader(final int templateType, Log log) {
		logger = log;
		this.props = new HashMap<String, String>();
		initTemplate(templateType);
	}

	/**
	 * Defines the value associated with the given token.
	 * 
	 * @param token
	 *            token defined inside the template
	 * @param value
	 *            value to be associated with the given token
	 */
	public void setProperty(final String token, final long value) {
		props.put("{" + token + "}", String.valueOf(value));
	}

	/**
	 * Defines the value associated with the given token.
	 * 
	 * @param token
	 *            token defined inside the template
	 * @param value
	 *            value to be associated with the given token
	 */
	public void setProperty(final String token, final String value) {
		props.put("{" + token + "}", value);
	}

	/**
	 * Returns the template content with all tokens replaced.
	 * 
	 * @return template content with all tokens replaced
	 */
	@Override
	public String toString() {
		return replaceTokens();
	}

	private String loadTemplate(final String templateName) {
		final InputStream in = TemplateLoader.class.getResourceAsStream(templateName);
		final StringWriter sw = new StringWriter();

		try {
			for (int c = in.read(); c != -1; c = in.read()) {
				sw.append((char) c);
			}
		} catch (final IOException e) {
			throw new MxConnectorRuntimeException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.LOAD.TEMPLATE", e));
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.debug(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.CLOSE.INPUT.STREAM", e));
				}
			}
		}
		return sw.toString();
	}

	private void initTemplate(final int type) {
		template = XML_BEGIN + XML_END;

		switch (type) {
		case TYPE_QUERY:
			template = XML_BEGIN + "\r\n" + QUERY_ATTR + XML_QUERY_END;
			setProperty("oper", "Query");
			setProperty("arg", "Query");
			setProperty(QUERY_ARGS, "");
			break;
		case TYPE_CREATE:
			setProperty("oper", "Create");
			setProperty("arg", "Set");
			break;
		case TYPE_UPDATE:
			setProperty("oper", "Update");
			setProperty("arg", "Set");
			break;
		case TYPE_SYNC:
			setProperty("oper", "Sync");
			setProperty("arg", "Set");
			break;
		case TYPE_DELETE:
			setProperty("oper", "Delete");
			setProperty("arg", "Delete");
			break;
		}
	}

	private String replaceTokens() {
		final StringBuilder sb = new StringBuilder(template);

		for (final String s : props.keySet()) {
			int i = sb.indexOf(s);
			while (i != -1) {
				sb.replace(i, i + s.length(), props.get(s));
				i = sb.indexOf(s, i);
			}
		}
		return sb.toString();
	}
}
