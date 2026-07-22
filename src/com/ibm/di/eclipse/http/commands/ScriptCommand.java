/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eclipse.http.commands;

import java.io.File;

import com.ibm.di.api.APIEngine;
import com.ibm.di.script.ScriptEngine;

public class ScriptCommand extends RestCommand {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public static boolean hasHandler(String path) {
		return new File("rest/" + path).exists();
	}

	public void execute() throws Exception {
		File file = new File("rest/" + getCommand());
		String script = readFile(file);
		ScriptEngine se = new ScriptEngine(null);
		se.declareBean("cmd", this);
		se.declareBean("session", getApi().getSession());
		getResponse().setAttribute(HTTP_CONTENT_TYPE, "text/html");
		se.exec(script);
	}

}
