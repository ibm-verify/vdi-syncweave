/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eclipse.http.commands;

import java.rmi.RemoteException;

import com.ibm.di.api.DIException;
import com.ibm.di.api.local.AssemblyLine;
import com.ibm.di.api.local.ConfigInstance;
import com.ibm.di.eclipse.http.XML;
import com.ibm.di.server.TaskCallBlock;

public class StartCommand extends RestCommand {

	public void execute() throws Exception {
		ConfigInstance cci;
		AssemblyLine al = null;
		String id = getPath(0);
		String alname = getPath(1);
		String data = getRequestBody();
		LogCommand listener = null;
		
		getApi().debugMsg("Body: " + data);
		if(id == null)
			throw new Exception(sRes.getString("Config.ID.required"));
		
		cci = startConfigInstance(id);
		getApi().debugMsg("Got " + cci);
		
		if(alname != null) {
			TaskCallBlock inputData = new TaskCallBlock();
			if(data != null) {
				 inputData.merge(XML.fromXML(data));
			}
			if(isParamTrue("log")) {
				listener = new LogCommand();
			}
			getApi().debugMsg(sRes.getString("startcommand.call.startal"));
			al = cci.startAssemblyLine(alname, inputData, listener, (listener!=null), false);
		}
		
		appendResult(RES_CONFIG_INSTANCE, cci.getConfigId());
		if(al != null) {
			appendResult(RES_ASSEMBLY_LINE, al.getName());
			String logurl = "log/" + cci.getConfigId() + "/" + al.getGlobalUniqueID();
			if(listener != null) {
				addPendingCommand(logurl, listener);
				appendResult(RES_ASSEMBLY_LINE_LOG, logurl);
			}
		}
	}

	public ConfigInstance startConfigInstance(String ci) throws RemoteException, DIException {
		ConfigInstance cci = null;
		if((cci = getSession().getConfigInstance(ci)) == null) {
			cci = getSession().startConfigInstance(ci);
		}
		return cci;
	}
	
}
