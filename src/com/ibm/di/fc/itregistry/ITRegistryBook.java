/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.itregistry;

import java.util.Date;

/**
 * This book is statically shared between the IT registry Components.
 */
public class ITRegistryBook {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Whether a Refresh operation will be performed by the IT registry
	 * Components sharing this book.
	 */
	private boolean refresh;

	/**
	 * The IT registry Book Name.
	 */
	private String bookName;

	/**
	 * The time when MSS got registered.
	 */
	private Date initTime;

	/**
	 * Remove stale operation should be performed (cleaning the IT registry from
	 * all artifacts not modified since a date).
	 */
	private boolean removedStale;

	/**
	 * Determines if the IT registry Book is currently opened.
	 */
	private boolean opened = false;

	/**
	 * Constructor.
	 * 
	 * @param bookName
	 *            name of the book.
	 */
	public ITRegistryBook(String bookName) {
		opened = false;
		this.bookName = bookName;
	}

	/**
	 * Whether this is a refresh book. Refresh causes all stale managed elements
	 * to be removed form the IT registry.
	 * 
	 * @return true if it is a refresh book, otherwise false.
	 */
	public boolean isRefresh() {
		return refresh;
	}

	/**
	 * Returns the IT registry book name.
	 * 
	 * @return the book name.
	 */
	public String getBookName() {
		return bookName;
	}

	/**
	 * Returns the timestamp of the moment when the MSS associated with this
	 * book was registered.
	 * 
	 * @return a time stamp (in milliseconds since January 1, 1970, 00:00:00
	 *         GMT).
	 */
	public long getInitTime() {
		return initTime.getTime();
	}

	/**
	 * Checks if this book is already opened. A book must be opened in order to
	 * add information to it.
	 * 
	 * @return <b>true</b> if this book is already opened, otherwise false.
	 */
	public boolean isOpened() {
		return opened;
	}

	/**
	 * Closes this IT registry book.
	 * 
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public void close() throws Exception {
		opened = false;
	}

	/**
	 * Opens the IT registry book.
	 * 
	 * @param refresh
	 *            whether to perform a refresh operation to the artifacts added.
	 * @param inittime
	 *            the time when MSS has successfully got registered (in
	 *            milliseconds).
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public void open(boolean refresh, long inittime) throws Exception {
		this.refresh = refresh;
		this.initTime = new Date(inittime);
		this.opened = true;
	}

	/**
	 * Check if one of the Components working with this book has already cleared
	 * the IT registry from older artifacts.
	 * 
	 * @return true if a removeStale() operation has been performed, false
	 *         otherwise.
	 */
	public boolean isRemovedStale() {
		return removedStale;
	}

	/**
	 * Sets whether one of the Components working with this book has already
	 * cleared the IT registry from older artifacts.
	 * 
	 * @param removedStale
	 *            either true or false.
	 */
	public void setRemovedStale(boolean removedStale) {
		this.removedStale = removedStale;
	}

}
