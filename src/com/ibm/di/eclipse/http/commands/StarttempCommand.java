/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eclipse.http.commands;

import java.io.File;
import java.util.Hashtable;

import com.ibm.di.api.local.ConfigInstance;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.xml.MetamergeConfigXML;

/**
 * This command expects a post with a single XML config to start a temporary config instance.
 * If a config is posted it is set on the config instance.
 */
public class StarttempCommand extends RestCommand {
	
	public void execute() throws Exception {
		ConfigInstance cci;
		String id = getPath(0);
		String data = getRequestBody();
		
		if(id == null)
			throw new Exception(sRes.getString("Config.ID.required"));
		
		// -- Get current config instance
		cci = getSession().getConfigInstance(id);
		if(cci == null && (data == null || data.length() == 0)) {
			cci = getSession().createNewConfigurationAndLoad(id, true);
		}

		if(data == null || data.length() == 0) {
			appendResult(RES_CONFIG_INSTANCE, cci.getConfigId());
			return;
		}

		// -- Update the metamergeconfig of the temporary instance
		File tmpfile = File.createTempFile(id, ".xml", new File(getSession().getConfigFolderPath()));
		try {
			Hashtable<String, Object> env = new Hashtable<String, Object>();
			env.put(MetamergeConfigFactory.MC_URL, data.getBytes());
			MetamergeConfigXML mc = new MetamergeConfigXML(env);
			mc.getSolutionInterface().setInstanceID(id);
			
			if(cci == null)
				cci = getSession().createNewConfigurationAndLoad(tmpfile.getName(), true);
			cci.setConfiguration(mc);
			appendResult(RES_CONFIG_INSTANCE, cci.getConfigId());
		} finally {
			tmpfile.delete();
		}
	}
	
}
