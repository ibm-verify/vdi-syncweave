/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eclipse.http.commands;

import java.util.Date;

import com.ibm.di.eclipse.http.XML;
import com.ibm.di.entry.Attribute;

public class PingCommand extends RestCommand {

	public void execute() throws Exception {
		Attribute attr = new Attribute("Date");
		attr.addValue(new Date().toString());
		Attribute top = new Attribute("Ping");
		top.addValue(attr);
		
		setBody(XML.toXML(top), false);
	}

}
