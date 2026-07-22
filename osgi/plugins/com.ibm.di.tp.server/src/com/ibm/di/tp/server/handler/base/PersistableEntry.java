/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.handler.base;

import javax.naming.InvalidNameException;
import javax.servlet.ServletContext;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.synd.SyndEntry;

import com.ibm.di.jaxrs.storage.atom.AtomStorage;
import com.ibm.di.jaxrs.storage.atom.StorageException;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.ServerActivator;
import com.ibm.di.tp.server.TPServerApplication;
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
public abstract class PersistableEntry extends Entry {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	// the location starting from tp-node to the resource itself (inclusive)
	private String relativeLocation;

	private AtomStorage storage;

	public PersistableEntry(AtomStorage storage, SyndEntry entryTemplate) {
		super(entryTemplate);
		this.storage = storage;
	}

	/**
	 * @param storage2
	 */
	public PersistableEntry(AtomStorage storage) {
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
	 * implementation in order for the atom entry template to be retrieved from
	 * the persistence store. This call depends on the {@link #relativeLocation}
	 * value as it is the key the template was persisted under.
	 * 
	 * @param shallow
	 *            specifies whether the {@link #storeState()} method will be
	 *            called.
	 * 
	 * @throws InvalidNameException
	 * @throws StorageException
	 */
	public void storeEntry(boolean shallow) throws StorageException, InvalidNameException {
		try {
			AtomEntry entry = constructAtomEntry();
			expandLinks(entry, AtomUtils.getSyntethicUriInfo("", getRelativeLocation()));

			storage.put(getRelativeLocation(), entry);
			if (!shallow) {
				storeState();
			}
		} catch (SCMPException e) {
			TPServerApplication.getLog().error(e.getMessage(), e);
		}
	}

	/**
	 * Called by {@link #storeEntry(AtomStorage)} to allow implementations to
	 * store additional resources like a child feed.
	 * 
	 * @throws StorageException
	 * @throws InvalidNameException
	 */
	protected abstract void storeState() throws StorageException, InvalidNameException;

	/**
	 * This method is called by the {@link #ImmutableEntry(AtomStorage, String)}
	 * constructor to restore the persisted entry. After this method returns,
	 * the entry object should be fully restored.
	 * 
	 * @param relativeLocation
	 *            the relative location of the entry to retrieve.
	 * 
	 * @return true if the entry was successfully restored from the persistence
	 *         store, false otherwise.
	 * @return the entry object or null if unable to restore it.
	 */
	protected boolean retrieveEntry() throws StorageException, InvalidNameException {
		AtomEntry entry = storage.getAtomEntry(getRelativeLocation());
		if (entry == null) {
			return false;
		}

		try {
			retrieveState(entry);
			entry.getLinks().removeAll(AtomUtils.findLinksByLitteralRelValue(entry.getLinks(), Constants.REL_SELF));
		} catch (RuntimeException re) {
			TPServerApplication.getLog().warn(ServerActivator.L10N.getString("TP.PERSISTENCE.ERROR.RESTORING.STATE"), re);
		}

		setEntryTemplate(entry.toSynd(new SyndEntry()));
		setETag(new EntityTag(Integer.toString(getEntryTemplate().hashCode())));
		return true;
	}

	/**
	 * Called by {@link #retrieveEntry()} to allow implementations to restore
	 * additional resources like a child feed. This method is also supposed to
	 * clean up the entry object of any data that is not supposed to get into
	 * the template (e.g. links with relations that we know of). Note the "self"
	 * link will be automatically removed right after this call.
	 * 
	 * @param entry
	 *            the entry that was retrieved out of the store.
	 * 
	 * @throws StorageException
	 * @throws InvalidNameException
	 */
	protected abstract void retrieveState(AtomEntry entry) throws StorageException, InvalidNameException;

	/**
	 * This method is called to purge the entry out of the persistence store.
	 * After this method returns, the entry object and all its children should
	 * be completely removed.
	 * 
	 * @return the entry object or null if unable to restore it.
	 */
	public void purgeEntry() throws StorageException, InvalidNameException {
		purgeState();
		storage.remove(getRelativeLocation());
	}

	/**
	 * Called by {@link #destroyEntry(AtomStorage)} to allow implementations to
	 * remove additional resources like a child feed.
	 * 
	 * @throws StorageException
	 * @throws InvalidNameException
	 */
	protected abstract void purgeState() throws StorageException, InvalidNameException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.handler.base.Entry#getRepresentation(javax.ws.rs
	 * .core.Request, javax.ws.rs.core.UriInfo)
	 */
	@Override
	public Response getRepresentation(ServletContext sc, Request request, UriInfo uriInfo) throws SCMPException {
		// Apparently the wink code needs this dummy overrider in order to
		// inherit the jax-rs annotations defined in the Entry class.
		return super.getRepresentation(sc, request, uriInfo);
	}
}
