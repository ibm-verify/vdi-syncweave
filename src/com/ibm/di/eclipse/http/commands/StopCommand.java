/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eclipse.http.commands;

import com.ibm.di.api.local.AssemblyLine;
import com.ibm.di.api.local.ConfigInstance;

public class StopCommand extends RestCommand {

	public void execute() throws Exception {

		String id = getPath(0);
		String al = getPath(1);
		if(id == null)
			throw new Exception(sRes.getString("Config.ID.required"));
		
		ConfigInstance cci = getSession().getConfigInstance(id);
		if (cci == null)
			return;
		if(al != null) {
			AssemblyLine[] arr = cci.getAssemblyLines();
			for(int i = 0; i < arr.length; i++) {
				if(arr[i].getName().equals(al)) {
					arr[i].stop();
					appendResult(RES_ASSEMBLY_LINE, al);
				}
			}
		} else {
			cci.stop();
			appendResult(RES_CONFIG_INSTANCE, id);
		}
	}

}
