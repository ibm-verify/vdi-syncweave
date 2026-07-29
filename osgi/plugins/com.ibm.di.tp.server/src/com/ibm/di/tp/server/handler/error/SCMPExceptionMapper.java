/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.handler.error;

import java.util.GregorianCalendar;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import javax.inject.Singleton;

import com.ibm.di.tp.server.handler.error.data.Error;
import com.ibm.di.tp.server.handler.error.data.Error.Details.Detail;
import com.ibm.di.tp.server.model.exception.SCMPException;

/**
 * Provides the means for a {@link SCMPException} to be presented in an SCMP
 * specific format. This mapper checks the message, if it starts with the known
 * "CTG" then it is considered to be the native message and will be put
 * separately in the final response. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
@Provider
@Singleton
@Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "text/javascript" })
public class SCMPExceptionMapper implements ExceptionMapper<SCMPException> {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	private static final DatatypeFactory datatypeFactory;
	static {
		DatatypeFactory df = null;
		try {
			df = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}

		datatypeFactory = df;
	}

	private String getNativeMsgId(String message) {
		return message != null && message.startsWith("CTG") ? message.substring(0, message.indexOf(' ')) : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
	 */
	public Response toResponse(SCMPException ex) {
		Error e = new Error();
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(ex.getCreationDate());
		e.setCreationTime(datatypeFactory.newXMLGregorianCalendar(calendar));
		e.setCode(Integer.toString(ex.getCode().getCode()));
		e.setNativeMsgid(getNativeMsgId(ex.getMessage()));
		e.setSummary(ex.getMessage());

		Detail d = null;
		for (String name : ex.getDetailsNames()) {
			d = new Detail();
			d.setName(name);
			d.setValue(ex.getDetail(name));
			e.getDetails().add(d);
		}

		return Response.status(ex.getHttpStatus() < 400 || ex.getHttpStatus() > 599 ? 500 : ex.getHttpStatus()).entity(e).build();
	}
}
