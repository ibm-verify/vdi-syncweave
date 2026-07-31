/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.user;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.connector.sapr3.user.ConfigurationNames;
import com.ibm.di.fc.Function;
import com.ibm.di.fc.sapr3rfc.SapR3RfcFC;
import com.ibm.di.function.SystemFunctions;

/**
 * Factory methods for creating the RFC Function Component.
 * 
 */
final class RfcFunctionFactory {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String TDI_SYSTEM_FUNCTIONS_NAME_PREFIX = "system:/Functions/";

	/**
	 * Disabled.
	 */
	private RfcFunctionFactory() {
		super();
	}

	/**
	 * Instantiates an TDI Function Component that enables communication with
	 * SAP R/3.
	 * 
	 * @param config
	 *            The configuration information as defined for the TDI
	 *            connector.
	 * @return A new Function Component
	 * @throws RfcFunctionFactoryException
	 *             If TDI UserFunctions fails to create the FC. Possible causes
	 *             include, jar containing FC not installed correctly,
	 *             configured name is incorrect.
	 * @throws IllegalArgumentException
	 *             if config == null.
	 */
	static Function createFC(Configuration config)
			throws RfcFunctionFactoryException {
		if (config == null) {
			throw new IllegalArgumentException();
		}

		// Dynamically load FC using the TDI utility class SystemFunctions.
		Function fc = null;
		try {
			fc = (Function) SystemFunctions.loadFunction(config
					.getParamAsString(ConfigurationNames.PARAM_RFC_FC));
		} catch (Exception e) {
			// TDIs method SystemFunctions.getFunction declares to
			// throw Exception.

			// ok try an alternaitve method to load the FC. The preferred method
			// probably failed because the connector is performing a schmea
			// query
			// while running under the config GUI.
			// From Bjorn Stadheim:
			// Right, this has to do with the runtime environment and multiple
			// config
			// instance support we've introduced in 6.0. When the
			// loadFunction(string) is
			// executed, it needs to find the current configuration object that
			// the caller is using.
			// We do this by using the current thread (plus thread groups and
			// some more) which is
			// what probably fails in this context. Anyway, there is a simple
			// work around to your
			// problem and that is to use the loadFunction(FunctionConfig fc)
			// version:
			//
			// fconfig = (FunctionConfig) config.getMetamergeConfig().lookup(
			// config.getParamAsString(ConfigurationNames.PARAM_RFC_FC) ) );
			// fc = (Function)SystemFunctions.loadFunction(fconfig);
			//
			// I'll look into it and see if I can fix it.
			//

			try {
				ConnectorConfig conCfg = (ConnectorConfig) config
						.getRawConfig();
				FunctionConfig fCfg = conCfg
						.getMetamergeConfig()
						.getFunction(
								config
										.getParamAsString(ConfigurationNames.PARAM_RFC_FC));
				fc = (Function) SystemFunctions.loadFunction(fCfg);
			} catch (Exception x) {
				try {
					ConnectorConfig conCfg = (ConnectorConfig) config
							.getRawConfig();
					FunctionConfig fCfg = conCfg
							.getMetamergeConfig()
							.getFunction(
									RfcFunctionFactory.TDI_SYSTEM_FUNCTIONS_NAME_PREFIX
											+ config
													.getParamAsString(ConfigurationNames.PARAM_RFC_FC));
					fc = (Function) SystemFunctions.loadFunction(fCfg);
				} catch (Exception xx) {
					Object[] msgArgs = new Object[] { config
							.getParamAsString(ConfigurationNames.PARAM_RFC_FC) };
					String msg = LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_UR_0001, msgArgs);
					config.getLog().logwarn(msg);
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					xx.printStackTrace(pw);
					config.getLog().warn(sw.toString());
					throw new RfcFunctionFactoryException(msg, xx);
				}
			}

			if (fc == null) {
				Object[] msgArgs = new Object[] { config
						.getParamAsString(ConfigurationNames.PARAM_RFC_FC) };
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_UR_0001, msgArgs);
				config.getLog().logwarn(msg);
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				config.getLog().warn(sw.toString());
				throw new RfcFunctionFactoryException(msg, e);
			}
		}

		if (fc == null) {
			Object[] msgArgs = new Object[] { ConfigurationNames.PARAM_RFC_FC };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_UR_0001, msgArgs);
			config.getLog().logwarn(msg);
			throw new RfcFunctionFactoryException(msg);
		}

		initFC(fc, config);

		return fc;

	}

	private static void initFC(Function fc, Configuration config)
			throws RfcFunctionFactoryException {
		//
		// Set the SAP R/3 connection information for the FC to use.
		//
		fc.setParam(ConfigurationNames.SAP_FC_PARAM_CLIENT, config
				.getParamAsString(ConfigurationNames.SAP_FC_PARAM_CLIENT));
		fc.setParam(ConfigurationNames.SAP_FC_PARAM_USER, config
				.getParamAsString(ConfigurationNames.SAP_FC_PARAM_USER));
		fc.setParam(ConfigurationNames.SAP_FC_PARAM_PASSWD, config
				.getParamAsString(ConfigurationNames.SAP_FC_PARAM_PASSWD));
		fc.setParam(ConfigurationNames.SAP_FC_PARAM_SYSNR, config
				.getParamAsString(ConfigurationNames.SAP_FC_PARAM_SYSNR));
		fc.setParam(ConfigurationNames.SAP_FC_PARAM_ASHOST, config
				.getParamAsString(ConfigurationNames.SAP_FC_PARAM_ASHOST));
		fc.setParam(ConfigurationNames.SAP_FC_PARAM_GWHOST, config
				.getParamAsString(ConfigurationNames.SAP_FC_PARAM_GWHOST));
		fc.setParam(ConfigurationNames.SAP_FC_PARAM_TRACE, config
				.getParamAsString(ConfigurationNames.SAP_FC_PARAM_TRACE));
		if (config.getParamAsString(ConfigurationNames.SAP_FC_PARAM_OPTIONAL)
				.length() > 0) {
			Properties optionalConnProps = new Properties();
			String[] props = config.getParamAsString(
					ConfigurationNames.SAP_FC_PARAM_OPTIONAL).split(" ");
			for (int i = 0; i < props.length; i++) {
				if ((props[i].length() > 0) && (props[i].indexOf('=') != -1)
						&& (props[i].length() > props[i].indexOf('='))) {
					String key = props[i].substring(0, props[i].indexOf('='));
					if (key.indexOf(SapR3RfcFC.PARAM_JCO_CLIENT_OPTIONS_PREFIX) != -1) {
						key = key
								.substring(key
										.indexOf(SapR3RfcFC.PARAM_JCO_CLIENT_OPTIONS_PREFIX) + 1);
					}
					String value = props[i]
							.substring(props[i].indexOf('=') + 1);
					if (key.length() > 0 && value.length() > 0) {
						// config.getLog().logdebug(
						// "Found Optional property " + key + "=" + value);
						optionalConnProps.put(key, value);
					}
					// else
					// config.getLog().logdebug("Bad Parameter Format '" +
					// props[i] + "'");
				}
			}
			String[] possibleOptions = SapR3RfcFC.getJCOClientOptions();
			for (int i = 0; i < possibleOptions.length; i++) {
				String paramVal = optionalConnProps
						.getProperty(possibleOptions[i]);
				if (paramVal != null) {
					fc.setParam(possibleOptions[i], paramVal);
				}
				// else
				// config.getLog().logdebug(
				// "No value specified for Optional RFC Logon Parameter "
				// + possibleOptions[i]);
			}
		}
		fc.setLog(config.getLog());
		if (config.getLog() != null)
			fc.setDebug(config.getLog().getDebug());

		try {
			fc.initialize(null);
		} catch (Exception e) {
			// 
			// TDIs method Function.initialize declares to
			// throw Exception.
			Object[] msgArgs = new Object[] { config
					.getParamAsString(ConfigurationNames.PARAM_RFC_FC) };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_UR_0002, msgArgs);
			config.getLog().logwarn(msg);
			throw new RfcFunctionFactoryException(msg);
		}
	}

}
