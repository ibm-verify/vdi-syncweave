/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.handler.inst;

import java.net.URI;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomLink;

import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.TPServerApplication;
import com.ibm.di.tp.server.handler.base.Entry;
import com.ibm.di.tp.server.model.TouchpointInstance;
import com.ibm.di.tp.server.model.config.ObjectFactory;
import com.ibm.di.tp.server.model.exception.ErrorCode;
import com.ibm.di.tp.server.model.exception.SCMPException;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class TPStatusEntry extends Entry {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	static final String URL = "tp-status";

	private final TouchpointInstance ti;

	@Context
	private ServletContext sctx;

	public TPStatusEntry(TouchpointInstance ti) {
		this.ti = ti;

		getEntryTemplate().getCategories().add(Constants.CAT_TOUCHPOINT_SYND);
		getEntryTemplate().getCategories().add(Constants.CAT_STATUS_ENTRY_SYND);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.handler.base.Entry#getEntryFromTemplate()
	 */
	@Override
	protected AtomEntry constructAtomEntry() throws SCMPException {
		AtomEntry entry = super.constructAtomEntry();
		// get the server status.
		try {
			entry.getAny().add(ObjectFactory.toElement(ti.getStatus()));
		} catch (JAXBException je) {
			TPServerApplication.getLog().error(je.getMessage(), je);
			throw new SCMPException(ErrorCode.CONNECTIVITY_UNKNOWN, je.getMessage(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		return entry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.handler.ImmutableEntry#expandLinks(org.apache.wink
	 * .common.model.atom.AtomEntry, java.net.URI, java.net.URI)
	 */
	@Override
	protected void expandLinks(AtomEntry payload, UriInfo uriInfo) {
		URI resourceURI = uriInfo.getAbsolutePath();

		// create the valid "self" link if it does not exist
		// at least a single valid link was not found
		AtomLink selfLink = new AtomLink();
		selfLink.setRel(Constants.REL_SELF);
		selfLink.setType(Constants.TYPE_APPLICATION_ATOM_XML_ENTRY);
		selfLink.setHref(resourceURI.toString());
		payload.getLinks().add(selfLink);

		super.expandLinks(payload, sctx, uriInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.handler.base.Entry#getShortId()
	 */
	@Override
	public String getEscapedId() {
		return URL;
	}
}
