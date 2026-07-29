/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.queue;

import java.util.Vector;

import com.ibm.di.connector.PESConnector;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.RS;
import com.ibm.di.server.SearchCriteria;

/**
 * A FIFO queue
 */
public class MemBufferQ {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/*
	 * a queue of pages containing queue of elements represented by vector of
	 * vectors
	 */
	private MemQ memQ;

	/* alternative buffer for paging */
	private MemQ chunk;

	// approx. number of entries that user expects
	private int watermark;

	// number of entries in one page
	private int pageSize;

	private IDGenerator IDgen;

	private boolean persist = true;

	private boolean block = false;

	/*
	 * isBeingPurged Flag is set when the queue is being purged and all read and
	 * write operations go into a wait mode till the flag is reset.
	 */
	private boolean isBeingPurged = false;

	private DBHandler dbHandler;

	private Log log;

	private MemQMutex memQMutex = new MemQMutex();

	private MemQMutex chunkMutex = new MemQMutex();

	private int percentMemoryUse = 50;

	private boolean doNotInitialiseDB = false;

	/**
	 * Move data from the temporary page to the memq
	 * 
	 * @throws Exception
	 */
	private void loadFromChunk() throws Exception {
		memQMutex.acquire();
		while (memQ.size() < watermark && chunk.size() > 0) {
			// Add actual object and not the pages
			// Read the individual entries from the page in the chunk.
			MemQ firstPage = (MemQ) chunk.firstElement();

			if (!firstPage.isEmpty()) {
				if (isMemoryAvailable())
					memQ.write(firstPage.read());
			} else
				chunk.read();// removes the empty page
		}
		releaseLock();

	}

	/**
	 * Constructor Initializes the memory buffer pipe when paging is enabled.
	 * 
	 */
	public MemBufferQ(int nEntries, int pagesize) throws Exception {
		initMemQ(nEntries, pagesize);

	}

	/**
	 * Constructor Initializes the memory buffer pipe when paging is
	 * enabled.Initialization of the DB properties is done only when the queue
	 * is being created from the UserFunctions. For queues being created from
	 * the MemQueue Connector or FC , the DB table name is required to be
	 * specified by the user.
	 * 
	 */
	public MemBufferQ(int nEntries, int pagesize, boolean doNotInitialiseDB)
			throws Exception {
		this.doNotInitialiseDB = doNotInitialiseDB;
		initMemQ(nEntries, pagesize);
	}

	/**
	 * Initialise the memq object being created.
	 * 
	 * @param nEntries
	 * @param pagesize
	 * @throws Exception
	 */
	private void initMemQ(int nEntries, int pagesize) throws Exception {
		watermark = nEntries;
		memQ = new MemQ(watermark);
		this.pageSize = pagesize;

		chunk = new MemQ(5);// temp queue to create page before adding to
		// the db
		IDgen = new IDGenerator();
		IDgen.reset();

		dbHandler = new DBHandler(pagesize, IDgen);
		// Since the JDBC properties are not specified by User.Use the default
		// values for SystemStore
		// and table name - memq+currentSystemTime
		if (!doNotInitialiseDB)
			initDB(null, null, null, "memq" + System.currentTimeMillis());

		setPercentMemoryUse(50);
		enablePersistence(true);

		log = RS.getServer() != null ? RS.getServer().getLog() : new Log("miserver");

	}

	/**
	 * Constructor Initializes the memory buffer pipe when paging is disabled
	 */
	@Deprecated
	public MemBufferQ(int nEntries) {
		watermark = nEntries;
		memQ = new MemQ(watermark);
		enablePersistence(false);

        if (RS.getServer() != null)
    		log = RS.getServer().getLog();
    	else
    		log = new Log("miserver");
	}

	/**
	 * Method adds the entry to the temporary store in memory
	 * 
	 * @param x
	 * @throws Exception
	 */
	private void addToChunk(Object x) throws Exception {
		chunkMutex.acquire();
		if (chunk.isEmpty()) {
			chunk.write(new MemQ(pageSize));
		}
		MemQ currPage = (MemQ) chunk.lastElement();
		if (currPage.size() < pageSize)
			currPage.write(x);
		else {
			dbHandler.addToDB(chunk); // Adds the full page to the db
			// chunk.read();
			// chunk.write(new MemQ(pageSize));
			addToChunk(x);
		}
		chunkMutex.release();
	}

	/**
	 * Initializes the system store paramemters.
	 * 
	 * @param sDBName
	 *            system store database name
	 * @param jdbcLogin
	 *            username to connect to the db
	 * @param jdbcPassword
	 *            password to connect to the db
	 * @param sTblName
	 *            table name
	 * @throws Exception
	 *             if system store is not initialized properly
	 */
	public void initDB(String sDBName, String jdbcLogin, String jdbcPassword,
			String sTblName) throws Exception {
		try {
			dbHandler.initialize(sDBName, jdbcLogin, jdbcPassword, sTblName);
		} catch (Exception err) {
			log.logerror(err.getMessage());
			throw new Exception(log.getString("queue.error.paging"));
		}

	}

	/**
	 * @return True if memQ is empty
	 */
	public boolean isEmpty() {
		return memQ.isEmpty();
	}

	/**
	 * @return Number of items in the memq.
	 */
	public int size() {
		return memQ.size();
	}

	/**
	 * synchronized method: The first item inserted in the queue and not yet
	 * removed. Requires !isEmpty (). Reading removes the item from the queue.
	 * read with no timeout.
	 */
	synchronized public Object read() throws Exception {
		return read(0);
	}

	/**
	 * synchronized method: The first item inserted in the queue and not yet
	 * removed. Requires !isEmpty (). Reading removes the item from the queue.
	 * Waits for timeout
	 */
	public Object read(int timeout) throws Exception {
		/*
		 * If the timeout is not specified,the reader will upload pages from the
		 * system store or from the chunk. Else will just read from queue and
		 * wait for timeout.
		 */

		checkQueueAvailability();
		if (!persist)
			return readFromPipe(timeout);
		else
			return readFromPage(timeout);

	}

	public void acquireLock() throws InterruptedException {
		memQMutex.acquire();
	}

	public void releaseLock() {
		memQMutex.release();

	}

	/**
	 * The method will read from the queue if it is empty else will load from
	 * the system store and if still empty from the chunk.
	 * 
	 * @return object read from the memq
	 */
	private Object readFromPage(int timeout) throws Exception {

		if (peek() != null) {
			return readFromPipe(timeout);
		} else {
			// The memq is empty.Add to memq from system store
			loadEntries();
			if (peek() != null)
				return readFromPipe(timeout);
			else {
				// Load from chunk
				try {
					loadFromChunk();
					return readFromPipe(timeout);
				} catch (Exception ex) {
					log.logerror(ex.getMessage());
				}
			}
		}
		return null;

	}

	/**
	 * Read object from the pipe in case of no paging memq is simply a sequence
	 * of objects.
	 * 
	 * @param timeout
	 *            Wait for timeout if memq is empty
	 * 
	 * @return object read from memq
	 */
	private Object readFromPipe(int timeout) {

		if (peek() == null) {
			// if pipe empty wait for TIMEOUT

			// synchronized (myObj) {
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException ignore) {
				log.logerror(ignore.getMessage());
			}
		}
		// }
		if (!(peek() == null) && memQMutex.isLockAvalailable()) {
			Object temp = null;
			try {
				acquireLock();
				temp = memQ.read();
				releaseLock();
			} catch (InterruptedException ie) {
				log.logerror(ie.getMessage());
			}
			return temp;
		} else
			return null;

	}

	/**
	 * write : add an object to the end of memq
	 * 
	 * @param x
	 *            object to be added
	 * @throws Exception
	 *             if add fails
	 */
	synchronized public void write(Object x) throws Exception {

		// acquireLock(); // Only single thread can read or write at the
		// same time.
		checkQueueAvailability();
		try {
			if (persist)
				writeToPage(x);
			else
				writeToPipe(x);
		} catch (Exception err) {
			log.logerror(err.getMessage());
			throw new Exception(err.getMessage());
		}

		// releaseLock();
	}

	/**
	 * 
	 * @param x
	 */
	private void writeMemq(Object x) {
		try {
			acquireLock();
			memQ.write(x);
			releaseLock();
		} catch (InterruptedException ie) {
			log.logerror(ie.getMessage());
		}
	}

	/**
	 * Add an object to a page in memq memq is implemented as a queue of pages
	 * when paging is enabled.
	 * 
	 * @param x
	 *            object to be added
	 * @throws Exception
	 *             if add fails
	 */
	synchronized private void writeToPage(Object x) throws Exception {

		if ((memQ.size() < watermark) && (isMemoryAvailable())) {
			if (dbHandler.isStoreEmpty()) {
				if (chunk.isEmpty()) {
					writeMemq(x); // No data waiting to be written to pipe
				} else {
					loadFromChunk();
					if ((memQ.size() < watermark) && (isMemoryAvailable())
							&& chunk.isEmpty())
						writeMemq(x);
					else
						addToChunk(x);
				}
			} else {
				loadEntries();
				if (dbHandler.isStoreEmpty()) {
					if ((memQ.size() < watermark) && (isMemoryAvailable())
							&& chunk.isEmpty())
						writeMemq(x);
					else
						addToChunk(x);
				} else {
					addToChunk(x);
				}
			}

		} else {
			// Start adding to temp page.

			addToChunk(x);
		}

	}

	/**
	 * Add an object to memq. memq is implemented as a queue of objects and not
	 * pages when paging is disabled.
	 * 
	 * @param x
	 *            Object to be added
	 * 
	 * @throws Exception
	 *             If add fails
	 */

	synchronized private void writeToPipe(Object x) throws Exception {

		acquireLock();
		if (memQ.size() < watermark) {
			memQ.write(x);
		} else {
			// if still full
			if (memQ.size() == watermark) {
				if (block) { // backward compatibility
					// block or fail the operation
					// wait();
					writeToPipe(x);
				} else
					throw new Exception(log.getString("queue.error.pipefull"));
			} else
				writeToPipe(x);
		}
		releaseLock();
	}

	/**
	 * Same as read but does not remove data from the memq
	 * 
	 * @return object read
	 */
	public Object peek() {
		if (isEmpty())
			return null;
		else {
			return memQ.firstElement();
		}
	}

	/**
	 * enablePersistence : enable/disable paging support using system store
	 * 
	 * @param enable
	 *            paging support enabled if true and disabled if false
	 */
	synchronized public void enablePersistence(boolean enable) {
		persist = enable;
	}

	/**
	 * 
	 * 
	 * @return isBeingPurged Returns if purging is enabled/disabled.
	 */
	synchronized public boolean isPurging() {
		return isBeingPurged;
	}

	/**
	 * isBeingPurged : enable/disable purging
	 * 
	 * @param purge
	 *            purging support enabled if true and disabled if false
	 */
	synchronized private void setPurging(boolean purge) {
		isBeingPurged = purge;
	}

	/**
	 * Enable disable blocking add this is used only when paging support is
	 * disabled.
	 * 
	 * @param enable
	 *            if true, add to memq blocks infinitely until there is space
	 *            for an object to be added to memq if false, add throws an
	 *            exception if memq is full
	 */
	public synchronized void blockAdd(boolean enable) {
		block = enable;
	}

	/**
	 * terminates the system store threads and drops table if dropSystemStore
	 * set to true
	 * 
	 * @param dropSystemStore
	 */
	public void terminate(boolean dropSystemStore) {

		if (dropSystemStore && dbHandler != null) {
			dbHandler.terminate();
		}

	}

	/**
	 * Method checks if the queue is available for a read or write operation.
	 * The isBeingPurgedflag is set when the queue is being purged and hence no
	 * read or write should be performed.
	 */
	private void checkQueueAvailability() {
		while (isPurging()) {
			try {
				wait(100);
			} catch (InterruptedException ex) {
				log.logerror(ex.getMessage());
			}
		}
	}

	/**
	 * Purges the queue. The isBeingPurged Flag is set which blocks all reader
	 * or writer threads until the purge operation is not completed. Deletes all
	 * data from the associated table in System Store if this queue has paging
	 * enabled.
	 * 
	 */

	public synchronized void purgeQueue() throws Exception {
		setPurging(true);
		try {
			acquireLock();
			if (persist) {
				dbHandler.emptySystemStore();
			}
			// Flush the in-memory queue.
			memQ.purgeQueue();
			chunk.purgeQueue();
			IDgen.reset();

		} catch (InterruptedException ie) {
			log.logerror(ie.getMessage());
		}
		releaseLock();
		setPurging(false);
	}

	/**
	 * Deletes this queue and drops the System Store table if paging is enabled.
	 */
	synchronized void deleteQueue() {
		try {
			acquireLock();
			if (persist){
				terminate(true);
				
				if(chunk!=null)
				chunk.clear();
				
				chunk = null;
			}			
			memQ.clear();
			memQ = null;
			
		} catch (InterruptedException ie) {
			log.logerror(ie.getMessage());
		}
		releaseLock();
	}

	public int getPercentMemoryUse() {
		return percentMemoryUse;
	}

	public void setPercentMemoryUse(int percentMemoryUse) {
		this.percentMemoryUse = percentMemoryUse;
	}

	/**
	 * Method loads pages from system store into the memq
	 */
	private void loadEntries() throws Exception {

		PESConnector connect = dbHandler.getConnect();
		boolean moreEntries = true;
		Vector idsToBeDeleted = new Vector<String>();
		if (null != connect) {

			acquireLock();
			try {
				// connect.reconnect();
				connect.selectEntries();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			while (moreEntries && (memQ.size() < watermark)
					&& isMemoryAvailable()) {
				try {
					Entry e = connect.getNextEntry();
					/*
					 * 1. reconnect to database 2. Read the next page from the
					 * systemstore 3. Insert the contents into the memq 4. If at
					 * any point the "memq" becomes full before all entries in
					 * page is added to it.write this entry back to db with same
					 * id so that the next time this method is called the same
					 * entry will be read first.
					 */

					if (e != null) {
						MemQ memqTemp = (MemQ) e.getAttribute("ENTRY")
								.getValuesVector().firstElement();
						if (memqTemp != null) {
							// acquireLock();
							while (!memqTemp.isEmpty()
									&& (memQ.size() < watermark)
									&& isMemoryAvailable()) {
								memQ.write(memqTemp.read());
							}
							// releaseLock();
							if (!memqTemp.isEmpty()) {
								// All data from page has not been added to DB
								// so
								// save it back to the
								// system store with same id ,so that this will
								// be
								// read first the next
								// time data is added to memq
								SearchCriteria sc = new SearchCriteria("ID",
										SearchCriteria.EXACT, e.getAttribute(
												"ID").getValue());
								Entry modifiedEntry = new Entry();
								modifiedEntry.addAttributeValue("ID", e
										.getAttribute("ID"));
								modifiedEntry.addAttributeValue("ENTRY",
										memqTemp);

								connect.modEntry(modifiedEntry, sc);
							} else {
								// Add this id to list so that it can be deleted
								// late
								idsToBeDeleted.add(e.getAttribute("ID")
										.getValue());
								// SearchCriteria sc = new SearchCriteria("ID",
								// SearchCriteria.EXACT, e.getAttribute(
								// "ID").getValue());
								// connect.deleteEntry(e, sc);
							}
						}

					} else {
						moreEntries = false;
					}
				} catch (Exception ex) {
					throw new Exception(log
							.getString("MEMQ.ERROR.ADDING.ENTRIES.SYSTEMSTORE"));
				}
			}

			if (!idsToBeDeleted.isEmpty()) {
				int count = idsToBeDeleted.size();
				for (int i = 0; i < count; i++) {
					SearchCriteria sc = new SearchCriteria("ID",
							SearchCriteria.EXACT, idsToBeDeleted.get(i));
					connect.deleteEntry(new Entry(), sc);
				}

			}
			releaseLock();
		}
	}

	/**
	 * Checks if there is enough memory available as specified by the user
	 */
	public boolean isMemoryAvailable() {
		boolean memAvailable = false;
		long totalMem = Runtime.getRuntime().totalMemory();
		long freeMem = Runtime.getRuntime().freeMemory();
		// Percentage memory available
		int percent = (int) ((totalMem - freeMem) / totalMem) * 100;
		if (percent < percentMemoryUse)
			memAvailable = true;
		return memAvailable;
	}

	public DBHandler getDbHandler() {
		return dbHandler;
	}

	public void setDbHandler(DBHandler dbHandler) {
		this.dbHandler = dbHandler;
	}

}
