/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import java.io.File;
import java.net.URL;

import org.w3c.dom.Element;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.RawFunctionConfig;

/**
 * Read/write {@link FunctionConfig} elements in XML format.
 *
 */
public class FunctionFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String FUNCTION_TAG = "Function";

	public final static String CONFIGURATION = ConnectorFactory.CONFIGURATION;

	private final static String STATE = ConnectorFactory.STATE;

	private final static String INIT_OPTION = ConnectorFactory.INIT_OPTION;

	private final static String MAPPING_FILE = "mappingFile";

	private final static String OLD_VALUE1 = "jars/functions/di_castor_mapping.xml";

	private final static String NEW_VALUE1 = "etc/di_castor_mapping.xml";

	private final static String OLD_VALUE2 = "jars\\functions\\di_castor_mapping.xml";

	private final static String NEW_VALUE2 = "etc\\di_castor_mapping.xml";

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration config, Element elem) throws Exception {
		ConnectorFactory cf = (ConnectorFactory) getFactory(ConnectorFactory.CONNECTOR_TAG);
		FunctionConfig fc = (FunctionConfig) config;
		fc.init();

		Element e;
		String str;

		getBaseName(config, elem);

		// For backwards compatibility
		getParameters(elem, config);

		// No need to get mode, always Function

		// Connector state
		if ((str = getNodeTextByName(elem, STATE)) != null)
			fc.setState(str);

		// Schema
		cf.getSchemas(elem, fc);

		// Hooks
		if ((e = getSingleElement(elem, HookFactory.HOOK_TAG)) != null)
			getFactory(HookFactory.HOOK_TAG).parse(fc.getHooks(), e);

		// Add Raw Function
		RawFunctionConfig rawFunction = fc.getFunctionConfig();
		if ((e = getSingleElement(elem, CONFIGURATION)) != null) {
			getBaseName(rawFunction, e);
			getParameters(e, rawFunction);
		}

		// Backwards compatibility
		str = rawFunction.getStringParameter("parser");
		if (str != null) {
			fc.getParserConfig().setInheritsFromRef(str);
			rawFunction.removeParameter("parser");
		}

		if("7.1".compareTo(config.getMetamergeConfig().getConfigVersion()) > 0) {
					// Migration code for Caster Function Components
					Object o = rawFunction.getParameterRaw(MAPPING_FILE);
					if ( o != null )
						migrateMappingFile(rawFunction, o.toString());
				}

		// Add Parser settings
		if ((e = getSingleElement(elem, ParserFactory.PARSER_TAG)) != null)
			getFactory(ParserFactory.PARSER_TAG).parse(fc.getParserConfig(), e);

		// Sandbox config
		if ((e = getSingleElement(elem, SandboxFactory.SANDBOX_TAG)) != null)
			getFactory(SandboxFactory.SANDBOX_TAG).parse(fc.getSandboxConfig(),
					e);

		// Function attribute maps
		cf.getAttributeMaps(elem, fc);

		// Init Option
		str = getNodeTextByName(elem, INIT_OPTION);
		if (str != null && str.length() > 0)
			fc.setInitializeOption(Integer.parseInt(str));

	}

	private void migrateMappingFile(RawFunctionConfig config, String param) {
				String fileName = param;
				if (fileName.startsWith("file:")) {
					try {
						URL u = new URL(fileName);
						fileName = u.getPath();
					} catch (Exception e) {
						// Cannot parse, do not change
						return;
					}
				}

				if (fileName.length() == 0 || new File(fileName).exists())
					return;

				int i = param.lastIndexOf(OLD_VALUE1);
				if (i >=0) {
					config.setParameter(MAPPING_FILE, param.substring(0, i) + NEW_VALUE1);
					return;
				}

				i = param.lastIndexOf(OLD_VALUE2);
				if (i >=0) {
					config.setParameter(MAPPING_FILE, param.substring(0, i) + NEW_VALUE2);
					return;
				}

			}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration config, Element elem) throws Exception {
		ConnectorFactory cf = (ConnectorFactory) getFactory(ConnectorFactory.CONNECTOR_TAG);
		FunctionConfig fc = (FunctionConfig) config;

		setBaseName(config, elem);

		// No need to save Mode, always Function

		// State
		setSingleElement(elem, STATE, fc, InternalSchema.CONNECTOR_STATE);

		// Schema
		cf.setSchemas(elem, fc);

		// Hooks
		getFactory(HookFactory.HOOK_TAG).build(fc.getHooks(), elem);

		// Raw Connector
		Element rc = elem.getOwnerDocument().createElement(CONFIGURATION);
		elem.appendChild(rc);
		setBaseName(fc.getFunctionConfig(), rc);
		setParameters(rc, fc.getFunctionConfig(), null);

		// Parser
		if ("com.ibm.di.fc.ParserFC".equals(fc.getJavaClass())) {
			Element parserElement = elem.getOwnerDocument().createElement(
					ParserFactory.PARSER_TAG);
			elem.appendChild(parserElement);
			getFactory(ParserFactory.PARSER_TAG).build(fc.getParserConfig(),
					parserElement);
		}

		// Sandbox config
		rc = elem.getOwnerDocument().createElement(SandboxFactory.SANDBOX_TAG);
		elem.appendChild(rc);
		getFactory(SandboxFactory.SANDBOX_TAG).build(fc.getSandboxConfig(), rc);

		cf.setAttributeMaps(elem, fc);

		// Initialize Option
		setSingleElement(elem, INIT_OPTION, fc,
				InternalSchema.CONNECTOR_INIT_OPTION);

	}

}
