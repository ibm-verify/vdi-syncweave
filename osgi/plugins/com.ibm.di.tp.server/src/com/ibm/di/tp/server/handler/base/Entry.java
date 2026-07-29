/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.handler.base;

import java.net.URI;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomLink;
import org.apache.wink.common.model.synd.SyndEntry;
import org.w3c.dom.Element;

import com.ibm.di.schema.internal.SchemaRewriter;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.model.exception.SCMPException;
import com.ibm.di.tp.server.util.SchemaRewriterAccessor;
import com.ibm.di.web.common.internal.atom.StringAtomText;

/**
 * This is the entry resource. This is the base entry resource that provides an
 * template Atom Entry.
 * <p>
 * The template atom entry: <br>
 * - contains only atom related information as: id, updated date, title, author,
 * etc. <br>
 * - does not contain context related information as links to other resources. <br>
 * - does not contain atom extending data as is result of extending the Atom
 * Synd standard.
 * <p>
 * When the template atom entry is expanded the resultant atom entry will
 * contain : <br>
 * - the atom related data <br>
 * - the runtime context information, e.g. links to other resources <br>
 * - the extension profile auxiliary data. <br>
 * <p>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public abstract class Entry {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private SyndEntry entryTemplate;

	private EntityTag eTag;

	public Entry() {
		this(new SyndEntry());
	}

	/**
	 * @param atomEntryTemplate
	 */
	public Entry(SyndEntry entryTemplate) {
		this.entryTemplate = entryTemplate;
		if (entryTemplate != null) {
			this.eTag = new EntityTag(Integer.toString(entryTemplate.hashCode()));
		}
	}

	/**
	 * Returns the representation of this resource after the expanding.
	 * 
	 * @param request
	 *            the JAX-RS request related data.
	 * @param uriInfo
	 *            the JAX-RS uri related data.
	 * @return the expanded AtomEntry
	 * @throws SCMPException
	 */
	@GET
	@Produces( { MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON,
			"text/javascript" })
	public Response getRepresentation(@Context ServletContext ctx, @Context Request request, @Context UriInfo uriInfo)
			throws SCMPException {
		ResponseBuilder builder = request.evaluatePreconditions(eTag);
		if (builder == null) {
			setUriAsId(uriInfo.getAbsolutePath());
			builder = Response.ok(expandEntryTemplate(ctx, uriInfo)).tag(eTag);
		}
		return builder.build();
	}

	/**
	 * This method is used to expand the template atom entry resource. See
	 * {@link Entry} class for definition of an expanded atom entry.
	 * <p>
	 * The default implementation first first constructs an AtomEntry using
	 * {@link #constructAtomEntry()} and then expands its context related
	 * information using the
	 * {@link #expandLinks(AtomEntry, ServletContext, UriInfo)} method.
	 * 
	 * @param uriInfo
	 *            the JAX-RS {@link UriInfo} injected when requesting this
	 *            resource representation.
	 * @return the expanded AtomEntry
	 * @throws SCMPException
	 */
	public AtomEntry expandEntryTemplate(ServletContext ctx, UriInfo uriInfo) throws SCMPException {
		AtomEntry entry = constructAtomEntry();
		expandLinks(entry, ctx, uriInfo);

		return entry;
	}

	/**
	 * Same as {@link #createReferenceEntry(URI)} but with "false" for the
	 * second argument.
	 * 
	 * @param thisURI
	 * @return
	 */
	public AtomEntry createReferenceEntry(URI thisURI) {
		return createReferenceEntry(thisURI, false);
	}

	/**
	 * Create a new Atom entry (a.k.a "reference entry") which links to this
	 * Atom entry. Normally the reference entry contains very little information
	 * of its own except the link to the original entry.
	 * 
	 * @param thisURI
	 *            the {@link URI} to this entry resource.
	 * @param editable
	 *            specifies whether the entry can be edited/deleted.
	 * 
	 * @return the AtomEntry representing a reference entry to this this
	 *         resource.
	 */
	public AtomEntry createReferenceEntry(URI thisURI, boolean editable) {
		AtomEntry entry = new AtomEntry();
		entry.setId(getEntryTemplate().getId());
		entry.setTitle(getEntryTemplate().getTitle() != null ? new StringAtomText(getEntryTemplate().getTitle().getValue()) : null);
		entry.setUpdated(getEntryTemplate().getUpdated());

		AtomLink ref = new AtomLink();
		ref.setType(Constants.TYPE_APPLICATION_ATOM_XML_ENTRY);
		ref.setRel(Constants.REL_SELF);
		ref.setHref(thisURI.toString());
		entry.getLinks().add(ref);

		if (editable) {
			ref = new AtomLink();
			ref.setType(Constants.TYPE_APPLICATION_ATOM_XML_ENTRY);
			ref.setRel(Constants.REL_EDIT);
			ref.setHref(thisURI.toString());
			entry.getLinks().add(ref);
		}

		return entry;
	}

	/**
	 * This method is called to set the ID of this template entry resource.
	 * Calling this method multiple times will not have effect on the entry ID
	 * once it has been set. This method should be called knowing the URI of
	 * this resource.
	 * 
	 * @param thisUri
	 */
	public void setUriAsId(URI thisUri) {
		if (entryTemplate.getId() == null) {
			synchronized (entryTemplate) {
				if (entryTemplate.getId() == null) {
					entryTemplate.setId(thisUri.toString());
				}
			}
		}
	}

	/**
	 * This method is responsible for constructing a {@link AtomEntry} object
	 * based on the internal template atom entry returned by
	 * {@link #getEntryTemplate()}. This method is expected to fill in both atom
	 * related information stored in the template and atom extension data if
	 * sub-classes have such data.
	 * 
	 * @return the created AtomEntry with the populated data read from both the
	 *         template and provided as atom extension data.
	 * @throws SCMPException
	 */
	protected AtomEntry constructAtomEntry() throws SCMPException {
		return new AtomEntry(getEntryTemplate());
	}

/**
	 * Called to expand any links that the {@link AtomEntry} might have. Each
	 * link depends on the URI that the client has requested, that is why the
	 * links are expanded for each request. The base implementation updates
	 * xsi:schemeLocation attributes.
	 * 
	 * @param payload
	 *            the {@link AtomEntry} where to put the expanded links.
	 * @param uriInfo
	 *            the {@link UriInfo} injected by JAX-RS when querying this
	 *            resource entry. <b>Note: </b>At some point a synthetic
	 *            {@link UriInfo} might be given to this method which implies
	 *            that not all of the provided by the {@link UriInfo} interface
	 *            methods will return usable data - see
	 *            {@link AtomUtils#getSyntethicUriInfo(String, String))
	 * @param sc the {@link ServletContext} to use when rewriting the xsi:schemeLocation attribute 
	 */
	protected void expandLinks(AtomEntry payload, ServletContext sc, UriInfo uriInfo) {
		if (sc == null || uriInfo == null) {
			return;
		}
		SchemaRewriter rewriter = ((SchemaRewriterAccessor) sc.getAttribute(SchemaRewriterAccessor.class.getName()))
				.getSchemaRewriter();

		if (rewriter != null) {
			for (Element e : payload.getAny()) {
				rewriter.rewriteSchema(e, uriInfo.getBaseUri().toString(), Constants.SCHEMA_CONTEXT_DIR);
			}
		}
	}

	/**
	 * Called to expand any links that the {@link AtomEntry} might have. Each
	 * link depends on the URI that the client has requested, that is why the
	 * links are expanded for each request.
	 * 
	 * @param payload
	 *            the {@link AtomEntry} where to put the expanded links.
	 * @param uriInfo
	 *            the {@link UriInfo} injected by JAX-RS when querying this
	 *            resource entry. <b>Note: </b>At some point a synthetic
	 *            {@link UriInfo} might be given to this method which implies
	 *            that not all of the provided by the {@link UriInfo} interface
	 *            methods will return usable data - see
	 *            {@link AtomUtils#getSyntethicUriInfo(String, String))
	 */
	protected void expandLinks(AtomEntry payload, UriInfo uriInfo) {
		expandLinks(payload, null, uriInfo);
	}

	/**
	 * @param entryTemplate
	 *            the entryTemplate to set
	 */
	protected void setEntryTemplate(SyndEntry entryTemplate) {
		this.entryTemplate = entryTemplate;
	}

	/**
	 * @return the entry
	 */
	public SyndEntry getEntryTemplate() {
		return entryTemplate;
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
	 * This is the short id under which a feed keeps this entry resource in its
	 * internal map.
	 * 
	 * @return the shortId
	 */
	public abstract String getEscapedId();
}
