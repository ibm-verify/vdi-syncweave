/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors;

import org.eclipse.core.resources.IFile;

import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.server.RestServerAPI;

public class RunRemoteAssemblyLineInput extends RunAssemblyLineInput {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IFile server;
	private String cid;
	private String alid;
	private RestServerAPI api;

	public RunRemoteAssemblyLineInput(IFile server, String cid, String alid, boolean debug) throws Exception {
		super();
		this.server = server;
		this.cid = cid;
		this.alid = alid;
		this.api = RestServerAPI.createInstance(server);
		setAddress(api.getAddress());
		setStepMode(RUNMODE_NORMAL);
		setDebug(debug);
		setConfiguration(api.getAssemblyLineConfiguration(getCid(), getAlid()));
	}

	public IFile getServer() {
		return server;
	}

	public String getCid() {
		return cid;
	}

	public String getAlid() {
		return alid;
	}

	public RestServerAPI getApi() {
		return api;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RunRemoteAssemblyLineInput) {
			RunRemoteAssemblyLineInput other = (RunRemoteAssemblyLineInput) obj;
			return getAlid().equals(other.getAlid()) && getCid().equals(other.getCid());
		}
		return false;
	}

	@Override
	public int hashCode() {
		int h = 42;
		if (alid != null)
			h ^= alid.hashCode();
		if (cid != null)
			h ^= cid.hashCode();
		return h;
	}
	
	public String getToolTipText() {
		return Messages.getMessage("RunRemoteAssemblyLineInput.tooltip", getName());
	}
}
