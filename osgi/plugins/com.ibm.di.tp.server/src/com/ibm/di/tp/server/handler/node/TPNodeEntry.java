/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.handler.node;

import java.net.URI;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomLink;
import org.apache.wink.common.model.synd.SyndPerson;
import org.apache.wink.common.model.synd.SyndText;
import org.w3c.dom.Element;

import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.config.node.TdiNodeConfig;
import com.ibm.di.tp.server.context.TPServerContext;
import com.ibm.di.tp.server.handler.base.Entry;
import com.ibm.di.tp.server.handler.type.TPTypeFeed;
import com.ibm.di.tp.server.model.ConnectivityProvider;
import com.ibm.di.tp.server.model.exception.SCMPException;
import com.ibm.di.tp.server.util.SCMPUtils;

/**
 * 
 * This class represents a connectivity provider entry resource. This entry is
 * not persisted because it is configured each time based on the read
 * configuration. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class TPNodeEntry extends Entry {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private final TPTypeFeed typeFeed;
	private final Element dataElement;
	private String localId;

	@Context
	private ServletContext sctx;

	public TPNodeEntry(ConnectivityProvider cp, String localId, TdiNodeConfig cfg, TPServerContext ctx) throws Exception {
		// the localId will be expanded to an absolute URL on the first request
		// and will be set as an ID on the entry template.
		this.localId = localId;

		// create the SCMP data element
		// TODO: Creation of the data element should not be so explicit. There
		// should be a way to request the implementation specific data element
		// from the ConnectivityProvider abstraction.
		dataElement = SCMPUtils.createConnectivityProviderElement(Constants.CONNECTIVITY_PROVIDER_TDI_TYPE, cfg.getLocation(), cfg
				.getOrganization(), cfg.getContact());

		if (cfg.getTitle() != null) {
			getEntryTemplate().setTitle(new SyndText(cfg.getTitle()));
		}

		SyndPerson entryPerson = null;
		if (cfg.getEmail() != null) {
			entryPerson = new SyndPerson();
			getEntryTemplate().getAuthors().add(entryPerson);

			entryPerson.setEmail(cfg.getEmail());
		}

		if (cfg.getAuthor() != null) {
			if (entryPerson == null) {
				entryPerson = new SyndPerson();
				getEntryTemplate().getAuthors().add(entryPerson);
			}
			entryPerson.setName(cfg.getAuthor());
		}

		getEntryTemplate().setUpdated(new Date(System.currentTimeMillis()));

		if (cfg.getSummary() != null) {
			getEntryTemplate().setSummary(new SyndText(cfg.getSummary()));
		}

		// restore the whole hierarchy for this node
		this.typeFeed = TPTypeFeed.create(cp, ctx, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.handler.base.Entry#getEntryFromTemplate()
	 */
	@Override
	protected AtomEntry constructAtomEntry() throws SCMPException {
		AtomEntry entry = super.constructAtomEntry();
		entry.getAny().add(dataElement);
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

		AtomLink selfLink = new AtomLink();
		selfLink.setRel(Constants.REL_SELF);
		selfLink.setType(Constants.TYPE_APPLICATION_ATOM_XML_ENTRY);
		selfLink.setHref(resourceURI.toString());
		payload.getLinks().add(selfLink);

		AtomLink typeFeedLink = new AtomLink();
		typeFeedLink.setHref(resourceURI.toString() + "/" + TPTypeFeed.URL);
		typeFeedLink.setRel(Constants.REL_TOUCHPOINT);
		typeFeedLink.setType(Constants.TYPE_APPLICATION_ATOM_XML_FEED);
		payload.getLinks().add(typeFeedLink);

		super.expandLinks(payload, sctx, uriInfo);
	}

	@Path(TPTypeFeed.URL)
	public TPTypeFeed getTypeFeed() {
		return typeFeed;
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
}
