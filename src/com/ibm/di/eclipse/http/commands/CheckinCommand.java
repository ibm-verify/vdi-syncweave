/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eclipse.http.commands;

import java.util.ArrayList;
import java.util.Hashtable;

import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.xml.MetamergeConfigXML;

public class CheckinCommand extends RestCommand {

	public void execute() throws Exception {
		String name = getPath(0);
		if(name == null)
			throw new Exception(sRes.getString("Config.name.required"));
		String file = name + ".xml";
		
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(MetamergeConfigFactory.MC_URL, getRequest().getString(HTTP_BODY).getBytes());
		MetamergeConfigXML mc = new MetamergeConfigXML(env);
		
		ArrayList list = getSession().listAllConfigurations();
		if(!list.contains(name) && !list.contains(file))
			getSession().createNewConfiguration(file, false);

		getSession().checkInConfiguration(mc, file);
	}

}
