/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.handler.type;

import java.net.URI;
import java.util.Date;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.wink.common.model.atom.AtomCategory;
import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomLink;
import org.apache.wink.common.model.synd.SyndCategory;
import org.apache.wink.common.model.synd.SyndText;

import com.ibm.di.jaxrs.storage.atom.AtomStorage;
import com.ibm.di.jaxrs.storage.atom.StorageException;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.ServerActivator;
import com.ibm.di.tp.server.TPServerApplication;
import com.ibm.di.tp.server.handler.base.PersistableEntry;
import com.ibm.di.tp.server.handler.inst.TPInstFeed;
import com.ibm.di.tp.server.model.ConnectivityProvider;
import com.ibm.di.tp.server.model.TouchpointType;
import com.ibm.di.tp.server.model.config.PropertySheetDefinition;
import com.ibm.di.tp.server.model.exception.SCMPException;
import com.ibm.di.tp.server.util.AtomUtils;

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
public class TPTypeEntry extends PersistableEntry {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final String URL_PROPSHEET_DEF = "tp-prop-def";

	private TPInstFeed instFeed;
	private TouchpointType tt;
	private String localId;
	private ConnectivityProvider cp;

	@Context
	private ServletContext sctx;

	/**
	 * @param connType
	 * @param storage
	 * @param relativeLocation
	 * @throws InvalidNameException
	 * @throws StorageException
	 */
	public TPTypeEntry(TouchpointType tt, String localId, AtomStorage storage, TPTypeFeed tpTypeFeed) {
		super(storage);
		this.tt = tt;
		this.localId = localId;

		setRelativeLocation(tpTypeFeed.getRelativeLocation() + "/" + localId);
		getEntryTemplate().setId(tpTypeFeed.getFeedTemplate().getId() + "/" + localId);
		getEntryTemplate().setTitle(new SyndText(tt.getId() + " Touchpoint Type"));
		getEntryTemplate().getAuthors().addAll(tpTypeFeed.getFeedTemplate().getAuthors());
		getEntryTemplate().setUpdated(new Date(System.currentTimeMillis()));

		SyndCategory customCat = new SyndCategory();
		customCat.setScheme(Constants.SCHEME_TP_TYPE);
		customCat.setTerm(tt.getId());
		getEntryTemplate().getCategories().add(customCat);

		// the resource-type category
		getEntryTemplate().getCategories().add(Constants.CAT_TOUCHPOINT_SYND);
		getEntryTemplate().getCategories().add(Constants.CAT_RES_TYPE_ENTRY_SYND);

		instFeed = new TPInstFeed(tt, getStorage(), this);
	}

	public TPTypeEntry(ConnectivityProvider cp, AtomStorage storage, String relativeLocation) throws StorageException,
			InvalidNameException, IllegalStateException {
		super(storage);
		this.cp = cp;
		setRelativeLocation(relativeLocation);

		if (!retrieveEntry()) {
			throw new IllegalStateException(ServerActivator.L10N.getString("TP.PERSISTENCE.MISSING.ENTRY.STATE", relativeLocation));
		}
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

		AtomLink selfLink = new AtomLink();
		selfLink.setRel(Constants.REL_SELF);
		selfLink.setType(Constants.TYPE_APPLICATION_ATOM_XML_ENTRY);
		selfLink.setHref(resourceURI.toString());
		payload.getLinks().add(selfLink);

		// instFeedLink
		AtomLink instFeedLink = new AtomLink();
		instFeedLink.setRel(Constants.REL_INSTANCE_FEED);
		instFeedLink.setType(Constants.TYPE_APPLICATION_ATOM_XML_FEED);
		instFeedLink.setHref(resourceURI.toString() + "/" + TPInstFeed.URL);
		payload.getLinks().add(instFeedLink);

		if (tt.hasPropertySheetDefinition()) {
			// propSheetDef
			AtomLink propSheetDef = new AtomLink();
			propSheetDef.setRel(Constants.REL_PROPSHEET_DEF);
			propSheetDef.setType(MediaType.TEXT_XML);
			propSheetDef.setHref(resourceURI.toString() + "/" + URL_PROPSHEET_DEF);
			payload.getLinks().add(propSheetDef);
		}

		super.expandLinks(payload, sctx, uriInfo);
	}

	@Path(TPInstFeed.URL)
	public TPInstFeed getInstFeed() {
		return instFeed;
	}

	@GET
	@Path(URL_PROPSHEET_DEF)
	@Produces( { MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON, "text/javascript" })
	public Response getPropertySheetDefinition(@Context Request req, @Context UriInfo uri) throws SCMPException {
		PropertySheetDefinition propSheetDef = tt.getPropertySheetDefinition();
		if (propSheetDef == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		EntityTag resETag = new EntityTag(Integer.toString(propSheetDef.hashCode()));
		ResponseBuilder builder = req.evaluatePreconditions(resETag);

		URI remoteSchemaUri = uri.getBaseUri().resolve("schema/propertysheet.xsd");
		propSheetDef.setSchemaLocation(remoteSchemaUri.toString());

		return builder != null ? builder.build() : Response.ok(propSheetDef).tag(resETag).build();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.handler.base.Entry#getShortId()
	 */
	@Override
	public String getEscapedId() {
		return localId;
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
	protected void retrieveState(AtomEntry entry) {
		String tpSpecificTypeId = null;
		for (AtomCategory cat : entry.getCategories()) {
			if (Constants.SCHEME_TP_TYPE.equals(cat.getScheme())) {
				tpSpecificTypeId = cat.getTerm();
				break;
			}
		}

		if (tpSpecificTypeId == null || tpSpecificTypeId.trim().length() == 0) {
			throw new IllegalStateException(ServerActivator.L10N.getString("TP.SERVER.RESOURCE.MISSING.ENTRY.CATEGORY",
					getRelativeLocation()));
		}

		TouchpointType tempTT = null;
		try {
			for (TouchpointType type : cp.getTypes()) {
				if (tpSpecificTypeId.equals(type.getId())) {
					tempTT = type;
					break;
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		if (tempTT == null) {
			throw new IllegalStateException(ServerActivator.L10N.getString("TP.SERVER.RESOURCE.TYPE.IS.NOT.ON.SERVER",
					new Object[] { tpSpecificTypeId, cp.getId() }));
		}

		tt = tempTT;
		int pos = entry.getId().lastIndexOf('/');
		localId = pos > -1 ? entry.getId().substring(pos + 1) : entry.getId();

		// cp is not needed any more... nulify it just in case
		cp = null;

		// find the uri of the inst feed.
		List<AtomLink> instLinks = AtomUtils.findLinksByLitteralRelValue(entry.getLinks(), Constants.REL_INSTANCE_FEED);

		if (!instLinks.isEmpty()) {
			try {
				AtomLink link = instLinks.get(0);
				instFeed = new TPInstFeed(tt, getStorage(), link.getHref());
				entry.getLinks().remove(link);
			} catch (StorageException e) {
				TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"), e);
			} catch (InvalidNameException e) {
				TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"), e);
			} catch (IllegalStateException e) {
				TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"), e);
			}
		}

		if (instFeed == null) {
			// the feed could not be restored for some reason... create a new
			// one.
			instFeed = new TPInstFeed(tt, getStorage(), this);
		}

		entry.getLinks().removeAll(AtomUtils.findLinksByLitteralRelValue(entry.getLinks(), Constants.REL_PROPSHEET_DEF));
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
		instFeed.storeFeed(false);
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
		instFeed.purgeFeed();
	}

	TouchpointType getTouchpointType() {
		return tt;
	}
}
