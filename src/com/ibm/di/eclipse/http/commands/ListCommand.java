/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eclipse.http.commands;

import java.util.ArrayList;

import com.ibm.di.api.local.AssemblyLine;
import com.ibm.di.api.local.ConfigInstance;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.entry.Attribute;

public class ListCommand extends RestCommand {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public void execute() throws Exception {
		ArrayList arr = getSession().listAllConfigurations();
		Attribute a = getResponse().newAttribute(RES_CONFIGURATION);
		for(int i = 0; i < arr.size(); i++) {
			a.addValue((String)arr.get(i));
		}
		
		ConfigInstance[] runs = getSession().getConfigInstances();
		a = getResponse().newAttribute(RES_CONFIG_INSTANCE + "s");
		for(int i = 0; i < runs.length; i++) {
			Attribute inst = new Attribute(RES_CONFIG_INSTANCE);
			a.addValue(inst);
			inst.addValue(new Attribute("Name", runs[i].getConfigId()));
			AssemblyLine[] alruns = runs[i].getAssemblyLines();
			for(int j = 0; j < alruns.length; j++) {
				Attribute alinst = new Attribute(RES_CONFIG_RUNAL);
				alinst.addValue(new Attribute("Name", alruns[j].getName()));
				alinst.addValue(new Attribute("LogURL", "/log/" + runs[i].getGlobalUniqueID() + "/" + alruns[j].getGlobalUniqueID()));
				inst.addValue(alinst);
			}
			MetamergeFolder fld = runs[i].getConfiguration().getDefaultFolder(MetamergeConfig.ASSEMBLYLINE_FOLDER);
			Attribute als = new Attribute(RES_CONFIG_AL);
			inst.addValue(als);
			String[] names = fld.getNames();
			for(int k = 0; k < names.length; k++)
				als.addValue(new Attribute("Name", names[k]));
		}
	}
}
