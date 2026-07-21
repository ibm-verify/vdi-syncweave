/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eclipse.http.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import com.ibm.di.api.local.ConfigInstance;

public class GetCommand extends RestCommand {

	public void execute() throws Exception {
		String id = getPath(0);
		if(id == null)
			throw new Exception(sRes.getString("Config.name.required"));
		
		String path = getSession().getConfigFolderPath() + File.separator + id;
		File file = new File(path);
		if(!file.exists())
			file = new File(path + ".xml");
		
		if(file.exists()) {
			appendResult("ConfigurationData", readFile(file));
			return;
		}
		
		// Try getting the config from a running CI
		ConfigInstance cci = getSession().getConfigInstance(id);
		if(cci != null) {
			StringWriter sw = new StringWriter();
			cci.getConfiguration().commitChanges(sw, false);
			appendResult("ConfigurationData", sw.toString());
		} else {
			throw new Exception(sRes.getString("File.not.found", id));
		}
	}

}
