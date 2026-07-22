/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.listener;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import com.ibm.di.web.common.atom.AtomText;
import com.ibm.di.api.DIEvent;
import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.PushChannel;
import com.ibm.di.api.remote.DIEventListener;
import com.ibm.di.api.rest.internal.util.EventMapper;

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
public class DIEventHttpForwarder extends HttpForwarderBase implements DIEventListener {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public DIEventHttpForwarder(PushChannel channel) throws MalformedURLException {
		super(channel);
	}

	public void handleEvent(DIEvent event) throws DIException, RemoteException {
		sendMessage(EventMapper.mapDIEvent(event));
	}
}
