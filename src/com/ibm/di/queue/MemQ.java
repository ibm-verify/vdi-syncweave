/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.queue;

import java.util.ArrayList;
import java.util.Vector;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;

/**
 * This class is a memory mapped FIFO queue.
 * 
 */
public class MemQ extends ArrayList {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private int front, rear;

	private int size;

	private Log log;

	private boolean full = false;

	private int count = 0;

	MemQ(Vector v) {
		super(v);
	}

	MemQ(int n) {
		size = n;
		initialize();
	}

	/**
	 * This method is called by the constructor and the emptyQueue() method.
	 * 
	 */
	private void initialize() {
		front = 0;
		rear = 0;
		count = 0;
		log = new Log("miserver");
		full = false;

		// add dummy
		Entry e = new Entry();
		e.addAttributeValue("DUMMY", "DUMMY");
		this.add(e);

	}

	public boolean write(Object o) {
		rear = (rear + 1) % size;
		if (size() == size)
			return false; // queue full
		count++;
		try {
			this.get(rear);
		} catch (IndexOutOfBoundsException e) {
			this.add(rear, o);
			return true;
		}
		this.set(rear, o);

		return true;
	}

	public Object read() {
		if (count == 0)// queue empty
			return null;
		front = (front + 1) % size;
		count--;
		return this.get(front);
	}

	public Object firstElement() {
		if (isEmpty())
			return null;
		return this.get((front + 1) % size);
	}

	public Object lastElement() {
		if (isEmpty())
			return null;
		return this.get(rear);
	}

	public boolean isEmpty() {
		if (count == 0)/* (front==rear) */
			return true;
		else
			return false;
	}

	public int size() {
		return count;
	}

	public boolean isFull() {
		return full;
	}

	public void setFull() {
		full = true;
	}

	/**
	 * Methods resets all flags of queue
	 */
	synchronized void purgeQueue() {
		// Reset all variables
		initialize();
	}
}
