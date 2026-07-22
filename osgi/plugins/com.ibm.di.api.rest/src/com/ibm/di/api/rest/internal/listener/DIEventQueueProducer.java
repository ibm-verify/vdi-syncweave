/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.listener;

import java.rmi.RemoteException;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.ServletContext;

import com.ibm.di.api.DIEvent;
import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.PollChannel;
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
public class DIEventQueueProducer extends QueueProducerBase implements DIEventListener {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * @param channel
	 * @throws DIException
	 */
	public DIEventQueueProducer(PollChannel channel, ServletContext sctx) throws DIException {
		super(channel, sctx);
	}

	public void handleEvent(DIEvent event) throws DIException, RemoteException {
		publishMessage(EventMapper.mapDIEvent(event));
	}
}
