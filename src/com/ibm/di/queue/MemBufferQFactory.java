/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.queue;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.di.server.Log;

/**
 * This class provides a global lookup table functionality. Every config
 * instance has a hashtable associated with it where:<br>
 * <br>
 * key : Thread.currentThread().getThreadGroup().getName()<br>
 * object : hashtable of memory buffer pipes<br>
 * Hash table of memory buffer pipes has :<br>
 * key : any name specified by the user<br>
 * object : MemBufferQ object.<br>
 * <br>
 * This enables sharing of memory buffer pipes between two threads based on
 * names giving it a named pipe like functionality.
 */
public class MemBufferQFactory {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static Hashtable instance = new Hashtable();

	private Hashtable memBuffer = new Hashtable();;

	private static Log log = new Log("miserver");

	private boolean doNotInitDB = false;

	public static void setLog(Log l) {
		log = l;
	}

	/**
	 * Get the MemBufferQ Factory for a particular instance.
	 * 
	 * @param inst
	 *            The instance name
	 * 
	 * @return MemBufferQFactory object for that instance
	 */
	public static MemBufferQFactory getInstance(String inst) {
		if (inst == null || inst.equals(""))
			return getInstance();
		if (instance.containsKey(inst))
			return (MemBufferQFactory) instance.get(inst);
		else {
			MemBufferQFactory temp = new MemBufferQFactory();
			instance.put(inst, temp);
			return temp;
		}
	}

	/**
	 * create a MemBufferQ factory for the current instance.
	 * 
	 */
	public static MemBufferQFactory getInstance() {
		if (instance.containsKey(Thread.currentThread().getThreadGroup()
				.getName()))
			return (MemBufferQFactory) instance.get(Thread.currentThread()
					.getThreadGroup().getName());
		else {
			MemBufferQFactory temp = new MemBufferQFactory();
			instance.put(Thread.currentThread().getThreadGroup().getName(),
					temp);
			return temp;
		}
	}

	/**
	 * Returns a reference to a memory buffer pipe.
	 * 
	 * @param pipeName
	 *            The name of memory buffer pipe of that instance
	 * 
	 * @return Returns a refernce to MemBufferQ object if one exists else null
	 * @throws Exception
	 *             if either instName or pipeName is null
	 */
	public MemBufferQ getPipe(String pipeName) throws Exception {
		if (memBuffer.containsKey(pipeName))
			return (MemBufferQ) memBuffer.get(pipeName);
		else {
			// no running instance
			log.error("queue.error.does.not.exist");
			throw new Exception(log.getString("queue.error.does.not.exist"));
		}
	}

	/**
	 * Checks if queue exists in memory.
	 * 
	 * @param pipeName
	 *            The name of the queue.
	 * 
	 * @return Returns true if the queue exists. Otherwise, false is returned.
	 */
	public boolean queueExists(String pipeName) {
		boolean pipeExists = false;
		if (memBuffer.containsKey(pipeName))
			pipeExists = true;
		return pipeExists;
	}

	/**
	 * Returns reference to a memory buffer pipe.
	 * 
	 * @param pipeName
	 *            The name of memory buffer pipe of that instance if not found
	 *            create a memory buffer pipe with that name with paging
	 *            disabled, only watermark specified and return refernce to the
	 *            newly created pipe.
	 * 
	 * @return A refernce to MemBufferQ object.
	 * @throws Exception
	 *             if either instName or pipeName is null
	 */
	@Deprecated
	public MemBufferQ newPipe(String pipeName, int watermark) throws Exception {
		if (pipeName == null || pipeName.equals("")) {
			log.error("queue.error.noname");
			throw new Exception(log.getString("queue.error.noname"));
		} else {
			if (memBuffer.containsKey(pipeName)) {
				log.info("queue.info.alreadyExists", pipeName);
				return (MemBufferQ) memBuffer.get(pipeName);
			} else {
				synchronized (memBuffer) {
					memBuffer.put(pipeName, new MemBufferQ(watermark));
				}
				return (MemBufferQ) memBuffer.get(pipeName);
			}
		}
	}

	/**
	 * Returns reference to a memory buffer pipe.
	 * 
	 * @param pipeName
	 *            A name of memory buffer pipe of that instance if not found
	 *            create a memory buffer pipe with that name with paging enabled
	 *            and return refernce to the newly created pipe
	 * 
	 * @return A refernce to MemBufferQ object
	 * @throws Exception
	 *             if either instName or pipeName is null.
	 */
	public MemBufferQ newPipe(String pipeName, int watermark, int pagesize)
			throws Exception {
		if (pipeName == null || pipeName.equals("")) {
			log.error("queue.error.noname");
			throw new Exception(log.getString("queue.error.noname"));
		} else {
			if (memBuffer.containsKey(pipeName)) {
				log.info("queue.info.alreadyExists", pipeName);
				return (MemBufferQ) memBuffer.get(pipeName);
			} else {
				synchronized (memBuffer) {
					if (!doNotInitDB)
						memBuffer.put(pipeName, new MemBufferQ(watermark,
								pagesize));
					else
						memBuffer.put(pipeName, new MemBufferQ(watermark,
								pagesize, doNotInitDB));
				}
				return (MemBufferQ) memBuffer.get(pipeName);
			}
		}
	}

	/**
	 * Returns all active pipes for the specified instance.
	 * 
	 * @param instName
	 *            An instance name.
	 * 
	 * @return All active pipes in the specified instance
	 */
	public static Vector getPipesForInstance(String instName) {
		if (instName == null || instName.equals("")) {
			instName = Thread.currentThread().getThreadGroup().getName();
			log.info("current.instance.name", instName);
		}
		Vector pipes = new Vector();
		MemBufferQFactory memqFactory = MemBufferQFactory.getInstance(instName);
		if (memqFactory != null && memqFactory.memBuffer != null) {
			pipes.addAll(memqFactory.memBuffer.keySet());
		}
		return pipes;
	}

	/**
	 * Gets all currently active instances.
	 * 
	 * @return All active instances.
	 */
	public static Vector getActiveInstances() {
		Vector inst = new Vector();
		inst.addAll(instance.keySet());
		return inst;
	}

	/**
	 * Deletes queue from memory
	 * 
	 * @param pipeName
	 * @throws Exception
	 */
	public void deleteQueue(String pipeName) throws Exception {
		if (pipeName == null || pipeName.equals("")) {
			log.error("queue.error.noname");
			throw new Exception(log.getString("queue.error.noname"));
		} else {
			if (memBuffer.containsKey(pipeName)) {
				synchronized (memBuffer) {
					MemBufferQ memBufferQ = (MemBufferQ) memBuffer
							.get(pipeName);
					log.info("MEMBUFFERQ.BEING.DELETED", pipeName);
					memBufferQ.deleteQueue();
					memBuffer.remove(pipeName);
				}
			} else {
				log.error("queue.error.does.not.exist");
				throw new Exception(log.getString("queue.error.does.not.exist"));
			}
		}
	}

	public boolean isDoNotInitDB() {
		return doNotInitDB;
	}

	public void setDoNotInitDB(boolean doNotInitDB) {
		this.doNotInitDB = doNotInitDB;
	}
}
