/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.listener;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import com.ibm.di.web.common.atom.AtomText;
import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.PushChannel;
import com.ibm.di.api.remote.AssemblyLineListener;
import com.ibm.di.api.rest.internal.util.EventMapper;
import com.ibm.di.entry.Entry;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class ALHttpForwarder extends LogHttpForwarder implements AssemblyLineListener {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public ALHttpForwarder(PushChannel channel) throws MalformedURLException {
		super(channel);
	}

	public void assemblyLineCycleDone(Entry payload) throws DIException, RemoteException {
		sendMessage(EventMapper.mapALCycleDone(payload));
	}

	public void assemblyLineFinished() throws DIException, RemoteException {
		sendMessage(EventMapper.mapALFinished());
	}
}
