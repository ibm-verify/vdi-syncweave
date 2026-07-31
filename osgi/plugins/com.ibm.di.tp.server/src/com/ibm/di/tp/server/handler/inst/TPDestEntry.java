/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.handler.inst;

import java.net.URI;

import javax.naming.InvalidNameException;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomLink;
import org.apache.wink.common.model.synd.SyndEntry;
import org.w3c.dom.Element;

import com.ibm.di.jaxrs.storage.atom.AtomStorage;
import com.ibm.di.jaxrs.storage.atom.StorageException;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.ServerActivator;
import com.ibm.di.tp.server.TPServerApplication;
import com.ibm.di.tp.server.handler.base.PersistableEntry;
import com.ibm.di.tp.server.model.TouchpointDestination;
import com.ibm.di.tp.server.model.TouchpointInstance;
import com.ibm.di.tp.server.model.config.DestinationData;
import com.ibm.di.tp.server.model.config.ObjectFactory;
import com.ibm.di.tp.server.model.exception.ErrorCode;
import com.ibm.di.tp.server.model.exception.SCMPException;
import com.ibm.di.tp.server.util.AtomUtils;
import com.ibm.di.tp.server.util.SCMPUtils;

/**
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class TPDestEntry extends PersistableEntry {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private TPDestFeed tpDestFeed;

	private String shortId;

	private TouchpointDestination td;

	/** used when restoring state only */
	private TouchpointInstance ti;

	@Context
	private ServletContext sctx;

	/**
	 * @param ti
	 * @param storage
	 * @param relativeLocation
	 * @param tpDestFeed
	 * @throws InvalidNameException
	 * @throws StorageException
	 */
	public TPDestEntry(TouchpointInstance ti, AtomStorage storage, String relativeLocation, TPDestFeed tpDestFeed)
			throws StorageException, InvalidNameException {
		super(storage);
		setRelativeLocation(relativeLocation);
		this.ti = ti;
		this.tpDestFeed = tpDestFeed;

		// restore...
		if (!retrieveEntry()) {
			throw new IllegalStateException(ServerActivator.L10N.getString("TP.PERSISTENCE.MISSING.ENTRY.STATE", relativeLocation));
		}
		// the dest config will be restored in a little bit during the
		// restoreState() method's execution.
	}

	/**
	 * @param shortId
	 * @param dest
	 * @param storage
	 * @param template
	 * @param tpDestFeed2
	 */
	public TPDestEntry(String shortId, TouchpointDestination dest, AtomStorage storage, SyndEntry template, TPDestFeed tpDestFeed) {
		super(storage, template);
		this.shortId = shortId;
		this.td = dest;
		this.tpDestFeed = tpDestFeed;
		setRelativeLocation(tpDestFeed.getRelativeLocation() + "/" + shortId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.handler.base.PersistableEntry#purgeState()
	 */
	@Override
	protected void purgeState() throws StorageException, InvalidNameException {
		// no children to purge
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.handler.base.PersistableEntry#retrieveState(org.
	 * apache.wink.common.model.atom.AtomEntry)
	 */
	@Override
	protected void retrieveState(AtomEntry entry) throws StorageException, InvalidNameException {
		if (ti == null) {
			throw new IllegalStateException();
		}

		Element elem = null;
		try {
			elem = SCMPUtils.getDataElement(entry.getAny());
		} catch (SCMPException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}

		if (entry.getId() == null) {
			throw new IllegalStateException(ServerActivator.L10N
					.getString("TP.PERSISTENCE.MISSING.ENTRY.ID", getRelativeLocation()));
		}

		int pos = entry.getId().lastIndexOf('/');
		shortId = pos > -1 ? entry.getId().substring(pos + 1) : entry.getId();

		DestinationData destData = null;
		try {
			destData = ObjectFactory.createDestinationData(elem);
			td = ti.createDestination(destData);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		// don't need ti any more... nullify it just in case
		ti = null;

		purgeKnownLinks(entry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.handler.base.PersistableEntry#storeState()
	 */
	@Override
	protected void storeState() throws StorageException, InvalidNameException {
		// no children to store
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.handler.base.Entry#expandLinks(org.apache.wink.common
	 * .model.atom.AtomEntry, java.net.URI, java.net.URI)
	 */
	@Override
	protected void expandLinks(AtomEntry payload, UriInfo uriInfo) {
		URI resourceURI = uriInfo.getAbsolutePath();

		String resourceURIString = resourceURI.toString();

		// create the valid "self" link if it does not exist
		// at least a single valid link was not found
		AtomLink selfLink = new AtomLink();
		selfLink.setRel(Constants.REL_SELF);
		selfLink.setType(Constants.TYPE_APPLICATION_ATOM_XML_ENTRY);
		selfLink.setHref(resourceURIString);
		payload.getLinks().add(selfLink);

		// create the valid "edit" link if it does not exist
		// at least a single valid link was not found
		AtomLink editLink = new AtomLink();
		editLink.setRel(Constants.REL_EDIT);
		selfLink.setType(Constants.TYPE_APPLICATION_ATOM_XML_ENTRY);
		editLink.setHref(resourceURIString);
		payload.getLinks().add(editLink);

		super.expandLinks(payload, sctx, uriInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.handler.base.Entry#getEscapedId()
	 */
	@Override
	public String getEscapedId() {
		return shortId;
	}

	@DELETE
	public Response deleteRepresentation(@Context Request request) throws Exception {
		ResponseBuilder builder = request.evaluatePreconditions(getETag());

		// delete content
		if (builder == null) {
			td.getTouchpointInstance().deleteDestination(td);
			tpDestFeed.deleteEntry(getEscapedId());
			return Response.ok().build();
		} else {
			return builder.build();
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_ATOM_XML)
	public Response putRepresentation(@Context Request request, AtomEntry payload) throws Exception {
		ResponseBuilder builder = request.evaluatePreconditions(getETag());
		// if builder is not null then the precondition failed... return 412
		// with the valid eTag.

		// change the config
		if (builder == null) {
			Element elem = SCMPUtils.getDataElement(payload.getAny());

			payload.setId(getEntryTemplate().getId());
			payload.setUpdated(System.currentTimeMillis());
			setEntryTemplate(extractTemplateEntry(payload));

			DestinationData destData = ObjectFactory.createDestinationData(elem);
			td.setConfiguration(destData);
			setETag(AtomUtils.increaseIntegerValue(getETag()));

			// store this entry only.
			storeEntry(false);
			builder = Response.ok().tag(getETag());
		}

		return builder.build();
	}

	static TPDestEntry create(String shortId, TouchpointDestination dest, AtomEntry payload, TPDestFeed tpDestFeed, URI feedUri) {
		// make sure the id don't contain forward slashes.
		SyndEntry template = extractTemplateEntry(payload);

		template.setId(feedUri + "/" + shortId);

		TPDestEntry destEntry = new TPDestEntry(shortId, dest, tpDestFeed.getStorage(), template, tpDestFeed);
		return destEntry;
	}

	private static SyndEntry extractTemplateEntry(AtomEntry payload) {
		purgeKnownLinks(payload);

		// remove static categories
		payload.getCategories().removeAll(
				AtomUtils.findCategoriesByTermAndScheme(payload.getCategories(), Constants.CAT_DESTINATION_ENTRY));

		SyndEntry template = payload.toSynd(new SyndEntry());
		template.getCategories().add(Constants.CAT_DESTINATION_ENTRY_SYND);
		return template;
	}

	/**
	 * @param payload
	 */
	private static void purgeKnownLinks(AtomEntry payload) {
		// make sure the payload does not contain some links in conflict
		// with the protocol

		// check for incorrect "self" links
		payload.getLinks().removeAll(AtomUtils.findLinksByLitteralRelValue(payload.getLinks(), Constants.REL_SELF));

		// check for incorrect "edit" links
		payload.getLinks().removeAll(AtomUtils.findLinksByLitteralRelValue(payload.getLinks(), Constants.REL_EDIT));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.handler.base.Entry#getEntryFromTemplate()
	 */
	@Override
	protected AtomEntry constructAtomEntry() throws SCMPException {
		AtomEntry entry = super.constructAtomEntry();
		try {
			entry.getAny().add(ObjectFactory.toElement(td.getConfiguration()));
		} catch (JAXBException je) {
			TPServerApplication.getLog().error(je.getMessage(), je);
			throw new SCMPException(ErrorCode.CONNECTIVITY_UNKNOWN, je.getMessage(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		return entry;
	}
}
