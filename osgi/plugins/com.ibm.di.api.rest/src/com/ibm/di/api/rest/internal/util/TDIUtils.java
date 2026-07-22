/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.util;

import java.util.regex.Pattern;

import com.ibm.di.web.common.atom.AtomText;
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
public class TDIUtils {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final Pattern CONFIG_ID_SEPARATORS = Pattern.compile("[\\\\:/]");

	/**
	 * we know that the server api don't tolerate colons and any kind of
	 * slashes, so we need to make sure we don't send a runName containing those
	 * chars.
	 */
	public static String escapeRunName(String rawRunName) {
		return CONFIG_ID_SEPARATORS.matcher(rawRunName).replaceAll("_");
	}

	public static String getConnectorName(String serverInfoConnectorName) {
		int start = 0;
		if (serverInfoConnectorName.startsWith("system:", start)) {
			start += "system:/".length();
		}
		if (serverInfoConnectorName.startsWith("/", start)) {
			start += 1;
		}
		if (serverInfoConnectorName.startsWith("Connectors", start)) {
			start += "Connectors/".length();
		}
		if (serverInfoConnectorName.startsWith("/", start)) {
			start += 1;
		}

		return serverInfoConnectorName.substring(start);
	}

	public static String getFunctionName(String serverInfoFunctionName) {
		int start = 0;
		if (serverInfoFunctionName.startsWith("system:", start)) {
			start += "system:".length();
		}
		if (serverInfoFunctionName.startsWith("/", start)) {
			start += 1;
		}
		if (serverInfoFunctionName.startsWith("Functions", start)) {
			start += "Functions".length();
		}
		if (serverInfoFunctionName.startsWith("/", start)) {
			start += 1;
		}
		return serverInfoFunctionName.substring(start);
	}
	
	public static String getParsernName(String serverInfoParserName) {
		int start = 0;
		if (serverInfoParserName.startsWith("system:", start)) {
			start += "system:".length();
		}
		if (serverInfoParserName.startsWith("/", start)) {
			start += 1;
		}
		if (serverInfoParserName.startsWith("Parsers", start)) {
			start += "Parsers".length();
		}
		if (serverInfoParserName.startsWith("/", start)) {
			start += 1;
		}
		return serverInfoParserName.substring(start);
	}
}
