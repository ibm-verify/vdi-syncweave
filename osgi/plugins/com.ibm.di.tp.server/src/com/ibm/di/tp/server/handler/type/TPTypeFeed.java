/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.handler.type;

import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.naming.InvalidNameException;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.common.model.atom.AtomLink;
import org.apache.wink.common.model.synd.SyndText;

import com.ibm.di.jaxrs.storage.atom.AtomStorage;
import com.ibm.di.jaxrs.storage.atom.StorageException;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.ServerActivator;
import com.ibm.di.tp.server.TPServerApplication;
import com.ibm.di.tp.server.context.TPServerContext;
import com.ibm.di.tp.server.handler.base.PersistableFeed;
import com.ibm.di.tp.server.handler.node.TPNodeEntry;
import com.ibm.di.tp.server.handler.node.TPNodeFeed;
import com.ibm.di.tp.server.model.ConnectivityProvider;
import com.ibm.di.tp.server.model.TouchpointType;
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
public class TPTypeFeed extends PersistableFeed<TPTypeEntry> {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public static final String URL = "tp-type";

	private final ConnectivityProvider cp;

	private final Set<TouchpointType> ttCache = new HashSet<TouchpointType>();

	@Context
	private ServletContext sctx;

	public TPTypeFeed(ConnectivityProvider cp, TPServerContext ctx, TPNodeEntry tpNodeEntry) {
		super((AtomStorage) ctx.getAttribute(AtomStorage.class.getCanonicalName()));
		setRelativeLocation("/" + TPNodeFeed.URL + "/" + tpNodeEntry.getEscapedId() + "/" + URL);
		this.cp = cp;
	}

	private void initializeRepresentation(TPNodeEntry tpNodeEntry) {
		getFeedTemplate().setTitle(new SyndText("Touchpoint Types"));
		getFeedTemplate().getCategories().add(Constants.CAT_TOUCHPOINT_SYND);
		getFeedTemplate().setUpdated(new Date(System.currentTimeMillis()));
		getFeedTemplate().getAuthors().addAll(tpNodeEntry.getEntryTemplate().getAuthors());
	}

	@Override
	public Response getRepresentation(@Context Request request, @Context UriInfo uriInfo) throws Exception {
		// we must set the ID prior to checFeedConsistency call as it will store
		// the feed without the ID.
		setUriAsId(uriInfo.getAbsolutePath());

		checkFeedConsistency();
		return super.getRepresentation(request, uriInfo);
	}

	@Path("{type}")
	public Object getTypeEntry(@PathParam("type") String type, @Context UriInfo uriInfo) throws Exception {
		// we must set the ID prior to checFeedConsistency call as it will store
		// the feed without the ID.
		if (getFeedTemplate().getId() == null) {
			// at this point the uriInfo contains something like
			// .../tp-type/<type_entry_id>/... so we need to remove anything
			// after tp-type and set it as id.
			String absPath = uriInfo.getAbsolutePath().toString();
			absPath = absPath.substring(0, absPath.indexOf(URL) + URL.length());
			setUriAsId(URI.create(absPath));
		}

		checkFeedConsistency();
		TPTypeEntry entry = lookupEntry(type);
		return entry != null ? entry : notFoundEntry;
	}

	private void checkFeedConsistency() throws Exception {
		Collection<TouchpointType> connTypes = cp.getTypes();

		boolean feedUpdated = false;
		TPTypeEntry typeEntry = null;

		synchronized (this) {
			Set<TouchpointType> outersection = new HashSet<TouchpointType>(ttCache);

			String escapedId = null;
			for (TouchpointType connType : connTypes) {
				if (!ttCache.contains(connType)) {
					// missing type... add it
					feedUpdated = true;

					// escape the raw id and postfix it with an
					// index to make it unique if needed.
					escapedId = SCMPUtils.getUniqueKey(getTpEntries().keySet(), SCMPUtils.escapeId(connType.getId()));
					typeEntry = new TPTypeEntry(connType, escapedId, getStorage(), this);

					// this will be filled in by the TPTypeEntry constructor
					putEntry(typeEntry.getEscapedId(), typeEntry);
					ttCache.add(connType);
				} else {
					outersection.remove(connType);
				}
			}

			if (outersection.size() > 0 || connTypes.size() == 0) {
				// some types are removed on the server... delete them
				for (TouchpointType connType : outersection) {
					deleteEntry(connType.getId());
					ttCache.remove(connType);
				}
				feedUpdated = true;
			}

			if (feedUpdated) {
				// increment the eTag
				setETag(AtomUtils.increaseIntegerValue(getETag()));
				getFeedTemplate().setUpdated(new Date(System.currentTimeMillis()));
				// store the feed only... each new entry was already stored.
				storeFeed(true);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.handler.ImmutableFeed#expandFeedTemplate(javax.ws
	 * .rs.core.Request, javax.ws.rs.core.UriInfo)
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
			for (TPTypeEntry type : getTpEntries().values()) {
				feed.getEntries().add(
						type.expandEntryTemplate(sctx, AtomUtils.getSyntethicUriInfo(uriInfo.getBaseUri().toString(), uriInfo
								.getPath()
								+ "/" + type.getEscapedId())));
			}
		}

		return feed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.handler.ImmutableFeed#createEntry(com.ibm.di.tp.
	 * server.storage.AtomStorage, java.lang.String)
	 */
	@Override
	protected TPTypeEntry createEntry(String relativeLocation) {
		try {
			TPTypeEntry tpTypeEntry = new TPTypeEntry(cp, getStorage(), relativeLocation);
			synchronized (this) {
				// keep the cache consistent.
				TouchpointType tt = tpTypeEntry.getTouchpointType();
				if (tt != null) {
					ttCache.add(tt);
				}
			}
			return tpTypeEntry;
		} catch (StorageException e) {
			TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"), e);
		} catch (InvalidNameException e) {
			TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"), e);
		} catch (IllegalStateException e) {
			// error restoring state... log the problem and ignore
			TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"), e);
		}

		return null;
	}

	/**
	 * @param cp
	 * @param ctx
	 * @param tpNodeEntry
	 * @return
	 * @throws InvalidNameException
	 * @throws StorageException
	 */
	public static TPTypeFeed create(ConnectivityProvider cp, TPServerContext ctx, TPNodeEntry tpNodeEntry) throws StorageException,
			InvalidNameException {

		TPTypeFeed feed = new TPTypeFeed(cp, ctx, tpNodeEntry);
		boolean restored = false;
		try {
			restored = feed.retrieveFeed();
		} catch (StorageException e) {
			// unable to restore the state of the typeFeed
			TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"), e);
		} catch (InvalidNameException e) {
			// the provided type name was invalid
			TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"), e);
		} finally {
			if (!restored) {
				// couldn't retrieve feed... initialize the feed from scratch
				feed.initializeRepresentation(tpNodeEntry);
				// persist the new feed...
				feed.storeFeed(true);
			}
		}

		return feed;
	}
}
