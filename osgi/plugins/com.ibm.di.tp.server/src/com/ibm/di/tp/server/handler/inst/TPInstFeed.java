/*
 * Copyright contributors to the SyncWeave project
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
import javax.ws.rs.core.Request;
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
import com.ibm.di.tp.server.handler.type.TPTypeEntry;
import com.ibm.di.tp.server.model.TouchpointRole;
import com.ibm.di.tp.server.model.TouchpointType;
import com.ibm.di.tp.server.model.config.InstanceData;
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
public class TPInstFeed extends PersistableFeed<TPInstEntry> {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public static final String URL = "tp-inst";

	private final TouchpointType tt;

	@Context
	private ServletContext sctx;

	public TPInstFeed(TouchpointType tt, AtomStorage storage, TPTypeEntry tpTypeEntry) {
		super(storage);
		this.tt = tt;
		setRelativeLocation(tpTypeEntry.getRelativeLocation() + "/" + URL);
		getFeedTemplate().setId(tpTypeEntry.getEntryTemplate().getId() + "/" + URL);
		getFeedTemplate().setTitle(new SyndText("Touchpoint Instances"));
		getFeedTemplate().getCategories().add(Constants.CAT_TOUCHPOINT_SYND);
		getFeedTemplate().setUpdated(new Date(System.currentTimeMillis()));
	}

	public TPInstFeed(TouchpointType tt, AtomStorage storage, String relativeLocation) throws StorageException,
			InvalidNameException {
		super(storage);
		setRelativeLocation(relativeLocation);
		this.tt = tt;

		if (!retrieveFeed()) {
			throw new IllegalStateException(ServerActivator.L10N.getString("TP.PERSISTENCE.MISSING.FEED.STATE", relativeLocation));
		}
	}

	@Path("{inst}")
	public Object getInstEntry(@PathParam("inst") String inst) throws Exception {
		TPInstEntry entry = lookupEntry(inst);
		return entry != null ? entry : notFoundEntry;
	}

	@POST
	@Produces( { MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON,
			"text/javascript" })
	@Consumes(MediaType.APPLICATION_ATOM_XML)
	public Response postInstEntry(@Context Request request, @Context UriInfo uriInfo, AtomEntry payload) throws Exception {
		TPInstEntry instEntry = null;

		TouchpointRole tr = SCMPUtils.getTPRole(payload, tt);
		Element elem = SCMPUtils.getDataElement(payload.getAny());
		InstanceData instData = ObjectFactory.createInstanceData(elem);

		synchronized (this) {
			long now = System.currentTimeMillis();
			String shortId = Long.toString(now, 32);
			payload.setId(shortId);
			payload.setUpdated(now);

			instEntry = TPInstEntry.create(tt.createInstance(shortId, tr, instData), payload, this, uriInfo.getAbsolutePath());
			putEntry(instEntry.getEscapedId(), instEntry);

			// persist the feed state only
			storeFeed(true);
		}

		UriInfo destEntryUriInfo = AtomUtils.getSyntethicUriInfo(uriInfo.getBaseUri().toString(), uriInfo.getPath() + "/"
				+ instEntry.getEscapedId());
		instEntry.setUriAsId(destEntryUriInfo.getAbsolutePath());
		AtomEntry atomEntry = instEntry.expandEntryTemplate(sctx, destEntryUriInfo);

		String location = AtomUtils.findLinksByLitteralRelValue(atomEntry.getLinks(), Constants.REL_SELF).get(0).getHref();

		return Response.created(new URI(location)).entity(atomEntry).tag(instEntry.getETag()).build();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.handler.base.Feed#expandFeedTemplate(javax.ws.rs
	 * .core.UriInfo)
	 */
	@Override
	public AtomFeed expandFeedTemplate(UriInfo uriInfo) throws SCMPException {
		AtomFeed feed = super.expandFeedTemplate(uriInfo, true);

		AtomLink resType = new AtomLink();
		resType.setRel(Constants.REL_RESOURCE_TYPE);
		resType.setHref(AtomUtils.getParentURI(uriInfo.getAbsolutePath()).toString());
		resType.setType(Constants.TYPE_APPLICATION_ATOM_XML_ENTRY);
		feed.getLinks().add(resType);

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
	public TPInstEntry deleteEntry(String shortId) throws Exception {
		TPInstEntry entry = super.deleteEntry(shortId);
		if (entry != null) {
			tt.disposeInstance(entry.getTouchpointInstance().getId());
		}
		storeFeed(true);

		return entry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.handler.ImmutableFeed#createEntry(com.ibm.di.tp.
	 * server.storage.AtomStorage, java.lang.String)
	 */
	@Override
	protected TPInstEntry createEntry(String relativeLocation) {
		try {
			return new TPInstEntry(tt, getStorage(), relativeLocation, this);
		} catch (StorageException e) {
			TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"), e);
		} catch (InvalidNameException e) {
			TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"), e);
		} catch (IllegalStateException e) {
			TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"), e);
		}
		return null;
	}
}
