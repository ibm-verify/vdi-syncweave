/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.handler.base;

import java.net.URI;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.common.model.atom.AtomLink;
import org.apache.wink.common.model.synd.SyndFeed;

import com.ibm.di.jaxrs.storage.atom.AtomStorage;
import com.ibm.di.jaxrs.storage.atom.StorageException;
import com.ibm.di.tp.server.Constants;
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
public abstract class PersistableFeed<E extends PersistableEntry> extends Feed<E> {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	// the location starting from tp-node to the resource itself (inclusive)
	private String relativeLocation;

	private AtomStorage storage;

	protected PersistableFeed(AtomStorage storage) {
		super();
		this.storage = storage;
	}

	/**
	 * @param relativeLocation
	 *            the relativeLocation to set
	 */
	public void setRelativeLocation(String relativeLocation) {
		this.relativeLocation = relativeLocation;
	}

	/**
	 * @return the relativeLocation
	 */
	public String getRelativeLocation() {
		return relativeLocation;
	}

	/**
	 * @return the storage
	 */
	public AtomStorage getStorage() {
		return storage;
	}

	/**
	 * This method is called at the appropriate time by the underlying
	 * implementation in order to persist the feed template. This call depends
	 * on the {@link #relativeLocation} value as it is used as a key. This call
	 * will cycle through all the contained entries and will persist them if the
	 * shallow parameter is true.
	 * 
	 * @param shallow
	 *            specifies whether the child entries will be persisted.
	 * 
	 * @throws InvalidNameException
	 * @throws StorageException
	 */
	public void storeFeed(boolean shallow) throws StorageException, InvalidNameException {
		AtomFeed feed = constructAtomFeed();

		List<AtomEntry> entries = feed.getEntries();

		// sync the access to the entries.
		synchronized (this) {
			if (getTpEntries() != null) {
				for (E entry : getTpEntries().values()) {
					entries.add(entry.createReferenceEntry(URI.create(entry.getRelativeLocation())));
				}
			}

			storage.put(getRelativeLocation(), feed);

			if (!shallow && getTpEntries() != null) {
				for (E entry : getTpEntries().values()) {
					entry.storeEntry(false);
				}
			}
		}
	}

	/**
	 * This method is called at the appropriate time by the underlying
	 * implementation in order for the atom feed template to be retrieved from
	 * the persistence store. This call depends on the {@link #relativeLocation}
	 * value as it is the key the template was persisted under. This call will
	 * cycle through all the contained entries and will restore them as well.
	 * 
	 * @return true if the feed was successfully restored from the persistence
	 *         store, false otherwise.
	 * @throws InvalidNameException
	 * @throws StorageException
	 */
	protected boolean retrieveFeed() throws StorageException, InvalidNameException {
		AtomFeed feed = storage.getAtomFeed(getRelativeLocation());
		if (feed == null) {
			return false;
		}

		// sync the access to the entries.
		synchronized (this) {
			if (getTpEntries() != null) {
				E entry = null;
				List<AtomLink> selfs = null;
				for (AtomEntry e : feed.getEntries()) {
					selfs = AtomUtils.findLinksByLitteralRelValue(e.getLinks(), Constants.REL_SELF);
					if (!selfs.isEmpty()) {
						entry = createEntry(selfs.get(0).getHref());
						if (entry != null) {
							getTpEntries().put(entry.getEscapedId(), entry);
						}
					}
				}
			}
		}

		feed.getEntries().clear();
		setFeedTemplate(feed.toSynd(new SyndFeed()));
		setETag(new EntityTag(Integer.toString(getFeedTemplate().hashCode())));
		return true;
	}

	/**
	 * This method is called by the {@link #retrieveFeed()} method to allow the
	 * implementation to return the specific child entry. After this method
	 * returns the entry object should be full restored.
	 * 
	 * @param relativeLocation
	 *            the relative location of the entry to retrieve.
	 * 
	 * @return the entry object or null if unable to restore it.
	 */
	protected abstract E createEntry(String relativeLocation);

	/**
	 * This method is called at the appropriate time by the underlying
	 * implementation in order for the atom feed template to be purged from the
	 * persistence store. This call depends on the {@link #relativeLocation}
	 * value as it is the key the template was persisted under. This call will
	 * cycle through all the contained entries and will remove them as well.
	 * 
	 * @return true if the feed was successfully restored from the persistence
	 *         store, false otherwise.
	 * @throws InvalidNameException
	 * @throws StorageException
	 */
	public void purgeFeed() throws StorageException, InvalidNameException {
		// delete the child entries
		synchronized (this) {
			if (getTpEntries() != null) {
				for (E entry : getTpEntries().values()) {
					entry.purgeEntry();
				}
			}
			storage.remove(getRelativeLocation());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.handler.base.Feed#getRepresentation(javax.ws.rs.
	 * core.Request, javax.ws.rs.core.UriInfo)
	 */
	@Override
	public Response getRepresentation(Request request, UriInfo uriInfo) throws Exception {
		// Apparently the wink code needs this dummy overrider in order to
		// inherit the jax-rs annotations defined in the Feed class.
		return super.getRepresentation(request, uriInfo);
	}

	/**
	 * First persists the entry and then puts it into the internal map. This way
	 * if the persistence fails the incorrect entry won't be added to the map.
	 */
	@Override
	public synchronized E putEntry(String shortId, E entry) throws Exception {
		entry.storeEntry(false);
		return super.putEntry(shortId, entry);
	}

	/**
	 * First removes the entry from the internal map and then purges the
	 * persisted data. This way if an error occurs while purging the state the
	 * entry will be deleted.
	 */
	@Override
	public synchronized E deleteEntry(String shortId) throws Exception {
		E temp = super.deleteEntry(shortId);

		if (temp != null) {
			temp.purgeEntry();
		}

		return temp;
	}
}
