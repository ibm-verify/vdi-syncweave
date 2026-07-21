/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.handler.base;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InvalidNameException;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.common.model.atom.AtomLink;
import org.apache.wink.common.model.synd.SyndFeed;

import com.ibm.di.jaxrs.storage.atom.StorageException;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.model.exception.SCMPException;
import com.ibm.di.tp.server.util.AtomUtils;

/**
 * This is the feed resource. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public abstract class Feed<E extends Entry> {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private SyndFeed feedTemplate;

	private EntityTag eTag;

	private final Map<String, E> tpEntries = new HashMap<String, E>();

	public final static NotFoundEntry notFoundEntry = new NotFoundEntry();

	protected Feed() {
		this(new SyndFeed());
	}

	protected Feed(SyndFeed feed) {
		this.setFeedTemplate(feed);

		if (feed != null) {
			setETag(new EntityTag(Integer.toString(feed.hashCode())));
		}
	}

	@GET
	@Produces( { MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON,
			"text/javascript" })
	public Response getRepresentation(@Context Request request, @Context UriInfo uriInfo) throws Exception {
		// check whether the provided by the user tag matches the one we keep
		// locally. If for some reason we have edited the feed we would have
		// updated the eTag as well.
		ResponseBuilder builder = request.evaluatePreconditions(getETag());

		if (builder == null) {
			// the provided eTag is different from the one we have locally.
			// The feed must have changed as the eTag is different. Just
			// send the user the new content and the new eTag.
			setUriAsId(uriInfo.getAbsolutePath());
			builder = Response.ok(expandFeedTemplate(uriInfo)).tag(getETag());
		}

		return builder.build();
	}

	public SyndFeed getFeedTemplate() {
		return feedTemplate;
	}

	/**
	 * @param feedTemplate
	 *            the feedTemplate to set
	 */
	protected void setFeedTemplate(SyndFeed feedTemplate) {
		this.feedTemplate = feedTemplate;
	}

	/**
	 * This method is called to set the ID of this template feed resource.
	 * Calling this method multiple times will not have effect on the feed ID
	 * once it has been set. This method should be called knowing the URI of
	 * this resource.
	 * 
	 * @param thisUri
	 */
	public void setUriAsId(URI thisUri) {
		if (feedTemplate.getId() == null) {
			synchronized (feedTemplate) {
				if (feedTemplate.getId() == null) {
					feedTemplate.setId(thisUri.toString());
				}
			}
		}
	}

	/**
	 * Same as {@link #expandFeedTemplate(UriInfo, boolean)} but with "false"
	 * for the second argument.
	 * 
	 * @param uriInfo
	 * @return
	 * @throws SCMPException
	 */
	public AtomFeed expandFeedTemplate(UriInfo uriInfo) throws SCMPException {
		return expandFeedTemplate(uriInfo, false);
	}

	/**
	 * This method is used to expand the template atom feed resource. See
	 * {@link Feed} class for definition of an expanded atom feed.
	 * <p>
	 * The default implementation first constructs an AtomFeed using
	 * {@link #constructAtomFeed()} and then populates it with REFERENCES to the
	 * child entries.
	 * 
	 * @param uriInfo
	 *            the JAX-RS {@link UriInfo} injected when requesting this
	 *            resource representation.
	 * @param editable
	 *            specifies whether the entries in that feed can be
	 *            edited/deleted.
	 * @return the expanded AtomFeed
	 * @throws SCMPException
	 */
	public AtomFeed expandFeedTemplate(UriInfo uriInfo, boolean editable) throws SCMPException {
		AtomFeed feed = constructAtomFeed();

		// expand the links that depend on the request context:
		// this is done on each request and not statically because the
		// particular server might serve requests on different IPs/Hosts. It
		// will be a problem when a client cannot access one of those IPs/Hosts
		URI absolutePath = uriInfo.getAbsolutePath();

		AtomLink feedSelfLink = new AtomLink();
		feedSelfLink.setRel(Constants.REL_SELF);
		feedSelfLink.setType(Constants.TYPE_APPLICATION_ATOM_XML_FEED);
		feedSelfLink.setHref(absolutePath.toString());
		feed.getLinks().add(feedSelfLink);

		// no need to sync the tpEntries for this feed as they are initialized
		// only once and are never changed
		for (Entry entry : getTpEntries().values()) {
			// when first call the ID of the entry would not be set so we have
			// to make sure it is.
			URI entryAbsPath = URI.create(uriInfo.getAbsolutePath() + "/" + entry.getEscapedId());
			entry.setUriAsId(entryAbsPath);
			feed.getEntries().add(entry.createReferenceEntry(entryAbsPath, true));
		}

		return feed;
	}

	/**
	 * This method is responsible for constructing an {@link AtomFeed} object
	 * based on the internal template atom feed returned by
	 * {@link #getFeedTemplate()}. This method is expected to fill in both atom
	 * related information stored in the template and atom extension data if
	 * sub-classes have such data.
	 * 
	 * @return the created {@link AtomFeed} with the populated data read from
	 *         both the template and provided as atom extension data.
	 */
	protected AtomFeed constructAtomFeed() {
		return new AtomFeed(getFeedTemplate());
	}

	/**
	 * Searches for the specific entry.
	 * 
	 * @param id
	 *            the id of the entry to search for.
	 * @return the {@link Entry} object or null.
	 */
	public synchronized E lookupEntry(String id) throws Exception {
		return getTpEntries().get(id);
	}

	/**
	 * Replaces the entry object with the specified one by using the specified
	 * id. This method updates the EntityTag of the feed.
	 * 
	 * @param shortId
	 *            the short Id of the entry object.
	 * @param entry
	 *            the entry to save in the internal map.
	 * @return the old entry instance or null if such does not exist
	 * @throws StorageException
	 * @throws InvalidNameException
	 */
	public synchronized E putEntry(String shortId, E entry) throws Exception {
		setETag(AtomUtils.increaseIntegerValue(getETag()));

		return getTpEntries().put(shortId, entry);
	}

	/**
	 * Deletes the entry object mapped under the particular id. This method
	 * updates the EntityTag of the feed.
	 * 
	 * @param shortId
	 *            the short Id of the entry object.
	 * @return the deleted entry or null if one does not exist
	 * @throws StorageException
	 * @throws InvalidNameException
	 */
	public synchronized E deleteEntry(String shortId) throws Exception {
		setETag(AtomUtils.increaseIntegerValue(getETag()));

		return getTpEntries().remove(shortId);
	}

	/**
	 * @param eTag
	 *            the eTag to set
	 */
	protected void setETag(EntityTag eTag) {
		this.eTag = eTag;
	}

	/**
	 * @return the eTag
	 */
	public EntityTag getETag() {
		return eTag;
	}

	/**
	 * @return the tpEntries
	 */
	protected Map<String, E> getTpEntries() {
		return tpEntries;
	}

	/**
	 * This class represents a resource entry which does not exist in the feed
	 * list.
	 * 
	 * @since 7.1
	 */
	protected static class NotFoundEntry {

		@GET
		@Produces( { MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON,
				"text/javascript" })
		public Response getRepresentation() {
			return Response.status(Status.GONE).build();
		}
	}
}
