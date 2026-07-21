/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.ibm.di.web.common.atom.AtomText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.di.api.ALEvent;
import com.ibm.di.api.CIEvent;
import com.ibm.di.api.ConfigEvent;
import com.ibm.di.api.DIEvent;
import com.ibm.di.api.bind.AssemblyLineEvent;
import com.ibm.di.api.bind.BindUtil;
import com.ibm.di.api.bind.ConfigFileEvent;
import com.ibm.di.api.bind.ConfigFileEventTypeEnum;
import com.ibm.di.api.bind.Data;
import com.ibm.di.api.bind.LogEvent;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.UserFunctions;

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
public class EventMapper {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static Logger log = LoggerFactory.getLogger(EventMapper.class);

	public static com.ibm.di.api.bind.DIEvent mapDIEvent(DIEvent event) {
		com.ibm.di.api.bind.DIEvent payload = null;
		if (event instanceof ALEvent) {
			payload = new com.ibm.di.api.bind.ALEvent();
			((com.ibm.di.api.bind.ALEvent) payload).setAlGuid(((ALEvent) event).getGUID());
			((com.ibm.di.api.bind.ALEvent) payload).setTaskStatistics(BindUtil
					.fromTaskStatistics(((ALEvent) event).getStatistics()));
		} else if (event instanceof CIEvent) {
			payload = new com.ibm.di.api.bind.CIEvent();
			((com.ibm.di.api.bind.CIEvent) payload).setCiGuid(((CIEvent) event).getGUID());
			((com.ibm.di.api.bind.CIEvent) payload).setCiStart(((CIEvent) event).getStarted());
			((com.ibm.di.api.bind.CIEvent) payload).setTombstoneCreated(((CIEvent) event).createTombstone());
		} else {
			payload = new com.ibm.di.api.bind.DIEvent();
		}

		payload.setCiId(event.getConfigInstanceId());
		payload.setId(event.getId());
		payload.setType(event.getType());
		payload.setCreated(event.getDateCreated() != null ? event.getDateCreated().getTime() : null);

		Object data = event.getData();
		Data d = null;
		if (data instanceof byte[]) {
			d = new Data();
			d.setType("application/octet-stream");
			d.setValue(UserFunctions.base64Encode((byte[]) data));
		} else if (data instanceof Serializable && !(data instanceof String) && !(data instanceof Boolean)
				&& !(data instanceof Character) && !(data instanceof Integer) && !(data instanceof Long)
				&& !(data instanceof Short) && !(data instanceof Byte) && !(data instanceof Float) && !(data instanceof Double)) {
			d = new Data();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				ObjectOutputStream oos = new ObjectOutputStream(out);
				oos.writeObject(data);
			} catch (IOException e) {
				log.error(AppConstants.L10N.getString("REST.API.SERIALIZATION.ERROR", data), e);
			}
			d.setType("application/octet-stream+object");
			d.setValue(UserFunctions.base64Encode(out.toByteArray()));
		} else if (data != null) {
			d = new Data();
			d.setType("text/plain");
			d.setValue(data.toString());
		}
		payload.setData(d);

		return payload;
	}

	public static AssemblyLineEvent mapALCycleDone(Entry payload) {
		AssemblyLineEvent evt = new AssemblyLineEvent();
		evt.setResultEntry(com.ibm.di.api.bind.BindUtil.fromEntry(payload));
		evt.setType("cycleDone"); // should probably have an enum for this...
		return evt;
	}

	public static AssemblyLineEvent mapALFinished() {
		AssemblyLineEvent evt = new AssemblyLineEvent();
		evt.setType("alStopped"); // should probably have an enum for this...
		return evt;
	}

	/**
	 * @param msg
	 * @return
	 */
	public static LogEvent mapLogMessage(String msg) {
		LogEvent evt = new LogEvent();
		evt.setMessage(msg);
		return evt;
	}

	public static ConfigFileEvent mapConfigEvent(ConfigEvent evt) {
		ConfigFileEvent e = new ConfigFileEvent();
		switch (evt.getType()) {
		case CREATE:
			e.setEventType(ConfigFileEventTypeEnum.CREATE);
			break;
		case CREATE_LOCKED:
			e.setEventType(ConfigFileEventTypeEnum.CREATE_LOCKED);
			break;
		case CHECK_IN:
			e.setEventType(ConfigFileEventTypeEnum.CHECK_IN);
			break;
		case CHECK_IN_LOCKED:
			e.setEventType(ConfigFileEventTypeEnum.CHECK_IN_LOCKED);
			break;
		case CHECK_OUT:
			e.setEventType(ConfigFileEventTypeEnum.CHECK_OUT);
			break;
		case UNLOCK:
			e.setEventType(ConfigFileEventTypeEnum.UNLOCK);
			break;
		case DELETE:
			e.setEventType(ConfigFileEventTypeEnum.DELETE);
			break;
		}

		e.setConfigFileId(evt.getIdentifier());
		e.setUserId(evt.getUser());

		return e;
	}
}
