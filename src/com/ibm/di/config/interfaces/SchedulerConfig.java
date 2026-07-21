/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * Configuration for a scheduler that will run an AssemblyLine/Sequence.
 *
 */
public interface SchedulerConfig extends BaseConfiguration {

	/**
	 * The type could be one of the following values.<br/>
	 * SCHEDULER - Runs at scheduled times<br/>
	 * KEEP_ALIVE - Keeps the AL/Sequence alive<br/>
	 */
	public final static int TIMER = 0;

	public final static int KEEP_ALIVE = 1;
	
	
	/**
	 * @return the type of this scheduler, one of TIMER, KEEP_ALIVE.
	 */
	public int getType();
	
	/**
	 * Sets the type of this scheduler.
	 * @param type The type to set, one of TIMER, KEEP_ALIVE.
	 */
	public void setType(int type);
	
	
	/**
	 * @return  The name of the AssemblyLine/Sequence that will be scheduled.
	 */
	public String getScheduledName();
	
	/**
	 * Sets name of the AssemblyLine/Sequence that will be scheduled.
	 * @param name The name.
	 */
	public void setScheduledName(String name);
	
	/**
	 * Returns a crontab style string specifying start times.
	 */
	public String getStartTimes();
	
	/**
	 * Sets the start times.
	 * @param times The start times.
	 */
	public void setStartTimes(String times);
}
