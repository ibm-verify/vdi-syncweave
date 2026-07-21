/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.handler.inst;

import java.net.URI;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
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
import org.apache.wink.common.model.synd.SyndCategory;
import org.apache.wink.common.model.synd.SyndEntry;
import org.w3c.dom.Element;

import com.ibm.di.jaxrs.storage.atom.AtomStorage;
import com.ibm.di.jaxrs.storage.atom.StorageException;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.ServerActivator;
import com.ibm.di.tp.server.TPServerApplication;
import com.ibm.di.tp.server.handler.base.PersistableEntry;
import com.ibm.di.tp.server.model.TouchpointInstance;
import com.ibm.di.tp.server.model.TouchpointRole;
import com.ibm.di.tp.server.model.TouchpointType;
import com.ibm.di.tp.server.model.config.InstanceData;
import com.ibm.di.tp.server.model.config.ObjectFactory;
import com.ibm.di.tp.server.model.exception.ErrorCode;
import com.ibm.di.tp.server.model.exception.SCMPException;
import com.ibm.di.tp.server.util.AtomUtils;
import com.ibm.di.tp.server.util.SCMPUtils;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @param <TPInstFeed>
 * 
 * @since 7.1
 */
public class TPInstEntry extends PersistableEntry {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private final TPInstFeed tpInstFeed;

	private TPStatusEntry statusEntry;

	private TPDestFeed destFeed;

	private TouchpointInstance ti;

	/** only used when restoring an instance out of the persistence store */
	private TouchpointType tt;

	@Context
	private ServletContext sctx;

	public TPInstEntry(TouchpointInstance ti, AtomStorage storage, SyndEntry template, TPInstFeed tpInstFeed) {
		super(storage, template);
		this.ti = ti;
		this.tpInstFeed = tpInstFeed;
		setRelativeLocation(tpInstFeed.getRelativeLocation() + "/" + ti.getId());

		statusEntry = new TPStatusEntry(ti);
		if (hasDestFeed()) {
			destFeed = new TPDestFeed(ti, getStorage(), this);
		}
	}

	/**
	 * @param storage
	 * @param relativeLocation
	 * @param tpInstFeed2
	 * @throws InvalidNameException
	 * @throws StorageException
	 */
	public TPInstEntry(TouchpointType tt, AtomStorage storage, String relativeLocation, TPInstFeed tpInstFeed)
			throws StorageException, InvalidNameException, IllegalStateException {
		super(storage);
		this.tt = tt;
		this.tpInstFeed = tpInstFeed;
		setRelativeLocation(relativeLocation);

		// restore...
		if (!retrieveEntry()) {
			throw new IllegalStateException(ServerActivator.L10N.getString("TP.PERSISTENCE.MISSING.ENTRY.STATE",
					relativeLocation));
		}

		// the "ti" reference will be restored in a little bit during the
		// restoreState() method's execution.
	}

	@DELETE
	public Response deleteRepresentation(@Context Request request) throws Exception {
		ResponseBuilder builder = request.evaluatePreconditions(getETag());

		// delete content
		if (builder == null) {
			tpInstFeed.deleteEntry(getEscapedId());
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
			setEntryTemplate(extractTemplateEntry(payload, ti));

			InstanceData instData = ObjectFactory.createInstanceData(elem);
			ti.setConfiguration(instData);
			setETag(AtomUtils.increaseIntegerValue(getETag()));

			// store this entry only.
			storeEntry(false);
			builder = Response.ok().tag(getETag());
		}

		return builder.build();
	}

	@Path(TPStatusEntry.URL)
	public TPStatusEntry getStatusEntry() {
		return statusEntry;
	}

	@Path(TPDestFeed.URL)
	public TPDestFeed getDestFeed() {
		return destFeed;
	}

	static TPInstEntry create(TouchpointInstance ti, AtomEntry payload, TPInstFeed tpInstFeed, URI feedUri) {
		// make sure the id don't contain forward slashes.
		SyndEntry template = extractTemplateEntry(payload, ti);
		template.setId(feedUri + "/" + payload.getId());

		TPInstEntry instEntry = new TPInstEntry(ti, tpInstFeed.getStorage(), template, tpInstFeed);
		return instEntry;
	}

	/**
	 * @param payload
	 * @return
	 */
	private static SyndEntry extractTemplateEntry(AtomEntry payload, TouchpointInstance ti) {
		purgeKnownLinks(payload);

		// remove static categories
		payload.getCategories().removeAll(
				AtomUtils.findCategoriesByTermAndScheme(payload.getCategories(), Constants.CAT_TOUCHPOINT));

		payload.getCategories().removeAll(AtomUtils.findCategoriesByScheme(payload.getCategories(), Constants.SCHEME_TP_TYPE));

		SyndEntry template = payload.toSynd(new SyndEntry());
		template.getCategories().add(Constants.CAT_TOUCHPOINT_SYND);
		template.addCategory(new SyndCategory(Constants.SCHEME_TP_TYPE, ti.getTouchpointType().getId(), null));

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

		// check for incorrect "Constants.RESOURCE_TYPE_REL_URL" links
		payload.getLinks().removeAll(AtomUtils.findLinksByLitteralRelValue(payload.getLinks(), Constants.REL_RESOURCE_TYPE));

		// check for incorrect "Constants.STATUS_REL_URL" links
		payload.getLinks().removeAll(AtomUtils.findLinksByLitteralRelValue(payload.getLinks(), Constants.REL_STATUS));

		// check for incorrect "Constants.REL_DESTINATION_FEED" links
		payload.getLinks().removeAll(AtomUtils.findLinksByLitteralRelValue(payload.getLinks(), Constants.REL_DESTINATION_FEED));
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

		String resourceURIString = resourceURI.toString();

		// instFeed
		URI typeEntry = AtomUtils.getParentURI(resourceURI);

		// typeEntry
		typeEntry = AtomUtils.getParentURI(typeEntry);

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
		editLink.setType(Constants.TYPE_APPLICATION_ATOM_XML_ENTRY);
		editLink.setHref(resourceURIString);
		payload.getLinks().add(editLink);

		// create the valid "Constants.REL_RESOURCE_TYPE" link if it does
		// not exist
		AtomLink tempLink = new AtomLink();
		tempLink.setRel(Constants.REL_RESOURCE_TYPE);
		tempLink.setType(Constants.TYPE_APPLICATION_ATOM_XML_ENTRY);
		tempLink.setHref(typeEntry.toString());
		payload.getLinks().add(tempLink);

		// create the valid "Constants.REL_STATUS" link if it does not exist
		tempLink = new AtomLink();
		tempLink.setRel(Constants.REL_STATUS);
		tempLink.setType(Constants.TYPE_APPLICATION_ATOM_XML_ENTRY);
		tempLink.setHref(resourceURIString + "/" + TPStatusEntry.URL);
		payload.getLinks().add(tempLink);

		if (destFeed != null) {
			// create the valid "Constants.REL_DESTINATION_FEED" link if it does
			// not exist
			tempLink = new AtomLink();
			tempLink.setRel(Constants.REL_DESTINATION_FEED);
			tempLink.setType(Constants.TYPE_APPLICATION_ATOM_XML_FEED);
			tempLink.setHref(resourceURIString + "/" + TPDestFeed.URL);
			payload.getLinks().add(tempLink);
		}

		super.expandLinks(payload, sctx, uriInfo);
	}

	private boolean hasDestFeed() {
		try {
			return ti.getDestinations() != null;
		} catch (SCMPException e) {
			return false;
		}
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
			entry.getAny().add(ObjectFactory.toElement(ti.getConfiguration()));
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
	 * com.ibm.di.tp.server.handler.ImmutableEntry#retrieveState(com.ibm.di.
	 * tp.server.storage.AtomStorage,
	 * org.apache.wink.common.model.atom.AtomEntry)
	 */
	@Override
	protected void retrieveState(AtomEntry entry) throws StorageException, InvalidNameException {
		if (tt == null) {
			throw new IllegalStateException();
		}

		if (entry.getId() == null) {
			throw new IllegalStateException(ServerActivator.L10N.getString("TP.PERSISTENCE.MISSING.ENTRY.ID",
					getRelativeLocation()));
		}

		Element elem = null;
		try {
			elem = SCMPUtils.getDataElement(entry.getAny());
		} catch (SCMPException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}

		int pos = entry.getId().lastIndexOf('/');
		String shortId = pos > -1 ? entry.getId().substring(pos + 1) : entry.getId();

		try {
			TouchpointRole tr = SCMPUtils.getTPRole(entry, tt);
			InstanceData instData = ObjectFactory.createInstanceData(elem);
			ti = tt.createInstance(shortId, tr, instData);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		// don't need tt any more... nullify it just in case
		tt = null;
		statusEntry = new TPStatusEntry(ti);

		if (hasDestFeed()) {
			// restore the destination feed all of its entries
			List<AtomLink> links = AtomUtils.findLinksByLitteralRelValue(entry.getLinks(), Constants.REL_DESTINATION_FEED);
			if (!links.isEmpty()) {
				try {
					AtomLink link = links.get(0);
					destFeed = new TPDestFeed(ti, getStorage(), link.getHref());
					entry.getLinks().remove(link);
				} catch (StorageException e) {
					TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"),
							e);
				} catch (InvalidNameException e) {
					TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"),
							e);
				} catch (IllegalStateException e) {
					TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"),
							e);
				}
			}
			if (destFeed == null) {
				destFeed = new TPDestFeed(ti, getStorage(), this);
			}
		}
		purgeKnownLinks(entry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.handler.ImmutableEntry#storeState(com.ibm.di.tp.
	 * server.storage.AtomStorage)
	 */
	@Override
	protected void storeState() throws StorageException, InvalidNameException {
		if (destFeed != null) {
			destFeed.storeFeed(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.handler.ImmutableEntry#destroyState(com.ibm.di.tp
	 * .server.storage.AtomStorage)
	 */
	@Override
	protected void purgeState() throws StorageException, InvalidNameException {
		if (destFeed != null) {
			destFeed.purgeFeed();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.handler.base.Entry#getShortId()
	 */
	@Override
	public String getEscapedId() {
		// we create the touchpoint instance id so it is an already escaped one.
		return ti.getId();
	}

	/**
	 * @return the ti
	 */
	TouchpointInstance getTouchpointInstance() {
		return ti;
	}
}
