/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.listener;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import com.ibm.di.web.common.atom.AtomText;
import com.ibm.di.api.ConfigEvent;
import com.ibm.di.api.bind.PushChannel;
import com.ibm.di.api.remote.ConfigurationFileListener;
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
public class ConfigFileEventHttpForwarder extends HttpForwarderBase implements ConfigurationFileListener {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * @param channel
	 * @throws MalformedURLException
	 */
	protected ConfigFileEventHttpForwarder(PushChannel channel) throws MalformedURLException {
		super(channel);
	}

	public void handleEvent(ConfigEvent evt) throws RemoteException {
		sendMessage(EventMapper.mapConfigEvent(evt));
	}
}
