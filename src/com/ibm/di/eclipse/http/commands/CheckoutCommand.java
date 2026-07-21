/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eclipse.http.commands;

import java.io.ByteArrayOutputStream;

import com.ibm.di.config.interfaces.MetamergeConfig;

public class CheckoutCommand extends RestCommand {

	public void execute() throws Exception {
		String name = getPath(0);
		if(name == null)
			throw new Exception(sRes.getString("Config.name.required"));
		String file = name + ".xml";

		MetamergeConfig mc = getSession().checkOutConfiguration(file);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		mc.commitChanges(bos, false);
		appendResult("Configuration", name);
		appendResult("ConfigurationData", new String(bos.toByteArray()));
	}

}
