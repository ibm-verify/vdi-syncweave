/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.handler.inst;

import java.net.URI;
import java.util.Date;

import javax.naming.InvalidNameException;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.common.model.atom.AtomLink;
import org.apache.wink.common.model.synd.SyndText;
import org.w3c.dom.Element;

import com.ibm.di.jaxrs.storage.atom.AtomStorage;
import com.ibm.di.jaxrs.storage.atom.StorageException;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.ServerActivator;
import com.ibm.di.tp.server.TPServerApplication;
import com.ibm.di.tp.server.handler.base.PersistableFeed;
import com.ibm.di.tp.server.model.TouchpointDestination;
import com.ibm.di.tp.server.model.TouchpointInstance;
import com.ibm.di.tp.server.model.config.DestinationData;
import com.ibm.di.tp.server.model.config.ObjectFactory;
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
 * @since 7.1
 */
public class TPDestFeed extends PersistableFeed<TPDestEntry> {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	static final String URL = "tp-dest";

	private TouchpointInstance ti;

	@Context
	private ServletContext sctx;

	/**
	 * @param ti
	 * @param storage
	 * @param tpInstEntry
	 */
	protected TPDestFeed(TouchpointInstance ti, AtomStorage storage, TPInstEntry tpInstEntry) {
		super(storage);
		this.ti = ti;

		setRelativeLocation(tpInstEntry.getRelativeLocation() + "/" + URL);
		getFeedTemplate().setId(tpInstEntry.getEntryTemplate() + "/" + URL);
		getFeedTemplate().setTitle(new SyndText("Touchpoint Destination Feed"));
		getFeedTemplate().setUpdated(new Date(System.currentTimeMillis()));
	}

	/**
	 * @param ti
	 * @param storage
	 * @param href
	 * @throws InvalidNameException
	 * @throws StorageException
	 */
	protected TPDestFeed(TouchpointInstance ti, AtomStorage storage, String relativeLocation) throws StorageException,
			InvalidNameException {
		super(storage);
		setRelativeLocation(relativeLocation);
		this.ti = ti;

		if (!retrieveFeed()) {
			throw new IllegalStateException(ServerActivator.L10N.getString("TP.PERSISTENCE.MISSING.FEED.STATE", relativeLocation));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.handler.base.PersistableFeed#createEntry(java.lang
	 * .String)
	 */
	@Override
	protected TPDestEntry createEntry(String relativeLocation) {
		try {
			return new TPDestEntry(ti, getStorage(), relativeLocation, this);
		} catch (StorageException e) {
			TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"), e);
		} catch (InvalidNameException e) {
			TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"), e);
		} catch (IllegalStateException e) {
			TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"), e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.handler.base.Feed#expandFeedTemplate(javax.ws.rs
	 * .core.Request, javax.ws.rs.core.UriInfo)
	 */
	@Override
	public AtomFeed expandFeedTemplate(UriInfo uriInfo) throws SCMPException {
		AtomFeed feed = constructAtomFeed();

		URI absolutePath = uriInfo.getAbsolutePath();

		AtomLink feedSelfLink = new AtomLink();
		feedSelfLink.setRel(Constants.REL_SELF);
		feedSelfLink.setType(Constants.TYPE_APPLICATION_ATOM_XML_FEED);
		feedSelfLink.setHref(absolutePath.toString());
		feed.getLinks().add(feedSelfLink);

		synchronized (this) {
			for (TPDestEntry dest : getTpEntries().values()) {
				feed.getEntries().add(
						dest.expandEntryTemplate(sctx, AtomUtils.getSyntethicUriInfo(uriInfo.getBaseUri().toString(), uriInfo
								.getPath()
								+ "/" + dest.getEscapedId())));
			}
		}

		return feed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.handler.base.PersistableFeed#deleteEntry(java.lang
	 * .String)
	 */
	@Override
	public synchronized TPDestEntry deleteEntry(String shortId) throws Exception {
		TPDestEntry entry = super.deleteEntry(shortId);
		// store the feed only... the entry has already been removed
		storeFeed(true);

		return entry;
	}

	@Path("{dest}")
	public Object getDestEntry(@PathParam("dest") String dest, @Context UriInfo uriInfo) throws Exception {
		TPDestEntry entry = lookupEntry(dest);
		return entry != null ? entry : notFoundEntry;
	}

	@POST
	@Produces( { MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON,
			"text/javascript" })
	@Consumes(MediaType.APPLICATION_ATOM_XML)
	public synchronized Response postDestEntry(@Context UriInfo uriInfo, AtomEntry payload) throws Exception {
		TPDestEntry destEntry = null;

		Element elem = SCMPUtils.getDataElement(payload.getAny());
		DestinationData destData = ObjectFactory.createDestinationData(elem);
		TouchpointDestination dest = ti.createDestination(destData);

		synchronized (this) {
			long now = System.currentTimeMillis();
			String shortId = Long.toString(now, 32);
			payload.setId(shortId);
			payload.setUpdated(now);

			destEntry = TPDestEntry.create(shortId, dest, payload, this, uriInfo.getAbsolutePath());
			putEntry(destEntry.getEscapedId(), destEntry);

			// persist the feed state only
			storeFeed(true);
		}

		UriInfo destEntryUriInfo = AtomUtils.getSyntethicUriInfo(uriInfo.getBaseUri().toString(), uriInfo.getPath() + "/"
				+ destEntry.getEscapedId());
		destEntry.setUriAsId(destEntryUriInfo.getAbsolutePath());
		AtomEntry atomEntry = destEntry.expandEntryTemplate(sctx, destEntryUriInfo);

		String location = AtomUtils.findLinksByLitteralRelValue(atomEntry.getLinks(), Constants.REL_SELF).get(0).getHref();

		return Response.created(new URI(location)).entity(atomEntry).tag(destEntry.getETag()).build();
	}
}
