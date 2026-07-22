/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.rmi.Naming;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.remote.SessionFactory;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.SchedulerConfig;
import com.ibm.di.fc.AssemblyLineFC;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.util.Schedule;

/**
 * This class schedules an AssemblyLine to be run,
 * either at specified times, or as soon as the previous run finishes.
 *
 */
public class Scheduler extends Thread {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	final private SchedulerConfig config;

	final private Log log;

	final private RS rs;

	final private String configID;
	final private String server;
	final private String alName;
	private boolean isSequence; // True if alName is the name of a sequence.

	private boolean terminated;
	private volatile boolean pause;

	private Date next; // Next run for the AL
	
	private int hashCode;
	
	private final static String PROPERTIES = "miserver";
	private final static ResourceHash res = ResourceHash.getHash(PROPERTIES);

	/**
	 * The constructor for the class.
	 * @param rs The RS this belongs to.
	 * @param config The SchedulerConfig to use.
	 */
	public Scheduler(RS rs, SchedulerConfig config) {
		this.rs = rs;
		this.config = config;
		setName(config.getShortName());
		configID = getParam("config");
		server = getServerName();
		alName = config.getScheduledName();
		log = new Log(PROPERTIES, "Scheduler." + getName());
		log.info("Scheduler.Started.by", currentThread().getName());
		hashCode = AssemblyLine.createHashCode();
	}

	@Override
	public void run() {
		if (rs != null)
			rs.registerScheduler(this);
		Monitor.threadStarted(this, null);
		try {
			if (alName == null || alName.length() == 0)
				throw new Exception(res.getString("Scheduler.no.AssemblyLine"));
			if (alName.contains("/" + MetamergeConfig.DEFAULT_SEQUENCE_FOLDER + "/"))
				isSequence = true;
			if (config.getType() == SchedulerConfig.KEEP_ALIVE)
				runKeepAlive();
			else if (config.getType() == SchedulerConfig.TIMER)
				runTimer();
		} catch (Exception e) {
			log.logerror(e.getMessage());
		}
		log.info("Scheduler.Finished");
		log.close();
		if (rs != null)
			rs.deregisterScheduler(this);
		Monitor.threadStopped(this, null, null);
	}

	private void runTimer() throws Exception {
		if (server != null && configID == null)
			throw new Exception(res.getString("Scheduler.no.ConfigInstance"));

		Schedule schedule = new Schedule(config.getStartTimes());
		boolean stopOnFailure = config.getBooleanParameter("stopOnFailure", false);
		while ( !isTerminated() ) {
			try {
				synchronized (this) {
					while (pause) {
						wait();
					}
				}
				next = schedule.getNext(null);
				Long time;
				while ((time = next.getTime() - new Date().getTime()) > 0)
					sleep(time);
				if (pause)
					continue;
				TaskStatistics stats = startAL(true, true);
				if (stats != null) {
					Exception e = stats.getError();
					if (e == null) {
						// If we continued to the end, no error has happened
						if (stats.getBailoutStep() >= ALState.MS_TERMINATE )
							continue; 
						log.error("Scheduler.problem.AssemblyLine", ""); // Mysterious error?
					} else {
						log.error("Scheduler.problem.AssemblyLine", e.toString(), e);
					}
					runFailureAL();
					if (stopOnFailure)
						terminated = true;
				}
			} catch (InterruptedException e) {
				return;
			} catch (Exception e) {
				log.error("Scheduler.problem.AssemblyLine", e.toString(), e);
				runFailureAL();
				if (stopOnFailure)
					terminated = true;
			}
		}
	}

	private void runKeepAlive() throws Exception{
		long limit = config.getIntegerParameter("WithinSeconds", 0) * 1000;

		while (!isTerminated()) {
			try {
				synchronized (this) {
					while (pause) {
						wait();
					}
				}
				long startTime = new Date().getTime();
				TaskStatistics stats = startAL(true, false);
				boolean failure = false;
				if (stats != null) {
					Exception e = stats.getError();
					if (e == null) {
						// If we did not continue to the end, a mysterious error has happened
						if (stats.getBailoutStep() < ALState.MS_TERMINATE ) {
							log.error("Scheduler.problem.AssemblyLine", "");
							failure = true;
						}
					} else {
						log.error("Scheduler.problem.AssemblyLine", e.toString(), e);
						failure = true;
					}
				}
				if (!failure && limit > 0L && new Date().getTime() < startTime + limit) {
					log.error("Scheduler.problem2.AssemblyLine");
					failure = true;
				}
				if (failure)
					runFailureAL();
				sleep(1000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	private TaskStatistics startAL(boolean join, boolean checkIfRunning) throws Exception {
		TaskCallBlock tcb = isSequence ? new TaskCallBlock() : new TaskCallBlock(alName);

		// Set the operation
		String operation = config.getStringParameter("operation");
		if (operation != null && operation.length() > 0)
			tcb.setALOperation(operation);

		// Get the init params off the raw configuration
		for (String key: config.getKeys(BaseConfiguration.ONE_LEVEL)) {
			if (key.startsWith(AssemblyLineFC.OPERATION_INIT_PREFIX)) {
				String param = key.substring(AssemblyLineFC.OPERATION_INIT_PREFIX.length());
				tcb.setOperationInitParam(param, config.getParameter(key));
			}
		}

		// Simulation mode?
		if (rs != null && rs.commandLineParam(RS.CL_SIMULATION_MODE) != null ||
			RS.gRS != null && RS.gRS.commandLineParam(RS.CL_SIMULATION_MODE) != null)
			tcb.setProperty(AssemblyLine.TCB_SIMULATE_MODE, "true");

		if (server != null) {
			ConfigInstance configInstance = getConfigInstance();
			if (checkIfRunning && alreadyRunning(configInstance))
				return null;
			return configInstance.startAssemblyLine(alName, tcb, join).getStatistics();
		} else {
			RS tmpRS = configID != null ? RS.getServer(configID) : rs;
			if (tmpRS == null)
				throw new Exception(res.getString("Scheduler.cannot.find.configinstance", configID));
			if (checkIfRunning && alreadyRunning(tmpRS))
				return null;
			if (isSequence) {
				Sequence s = tmpRS.startSequence(alName, tcb);
				if (join) {
					s.join();
				}
				return s.getStats();
			} else {
				AssemblyLine al = tmpRS.startAL(tcb);
				if (join) {
					al.join();
				}
				return al.getStats();
			}
		}
	}

	private boolean alreadyRunning(RS tmpRS) {
		if (isSequence) {
			for (Sequence a:UserFunctions.getRunningSequences(alName)) {
				if (a.getParent() == tmpRS)
					return true;
			}
		} else {
			for (AssemblyLine a:UserFunctions.getRunningALs(alName)) {
				if (a.getParent() == tmpRS)
					return true;
			}
		}
		return false;
	}

	private boolean alreadyRunning(ConfigInstance configInstance) throws Exception {
		for (com.ibm.di.api.remote.AssemblyLine a:configInstance.getAssemblyLines()) {
			String name = a.getName();
			if (name == null)
				continue;
			int i = name.lastIndexOf('/');
			if (i > 0)
				name = name.substring(i+1);
			if (name.equals(alName))
				return true;
		}
		return false;
	}

	private void runFailureAL() {
		String name = getParam("FailureAL");
		if (name == null || name.length() == 0 || rs == null)
			return;
		try {
			AssemblyLine fail = rs.startAL(name);
			if (fail != null)
				fail.join();
		} catch (Exception e) {
			if (e instanceof InterruptedException)
				terminated = true;
			else
				log.error("Scheduler.problem.Failure.AL" , e.toString(), e);
			
		}
	}

	private ConfigInstance getConfigInstance() throws Exception {
		SessionFactory sf = (SessionFactory) Naming.lookup("rmi://"
				+ server + "/SessionFactory");
		Session session = sf.createSession();

		ConfigInstance configInstance = session.getConfigInstance(configID);
		if (configInstance == null)
			configInstance = session.startConfigInstance(configID);
		return configInstance;
	}

	private String getServerName() {
		if (config.getType() == SchedulerConfig.KEEP_ALIVE)
			return null;
		String s = getParam("server");
		if (s == null || s.equalsIgnoreCase("local"))
			return null;
		if (s.indexOf(":") == -1)
			s += ":1099";

		return s;
	}

	private String getParam(String name) {
		String s = config.getStringParameter(name);
		if ( s == null )
			return null;
		s = s.trim();
		if (s.equals(""))
			return null;
		return s;
	}

	/**
	 * Shuts down the Scheduler.
	 * The running AssemblyLine will continue
	 */
	public void shutdown() {
		terminated = true;
		interrupt();
	}

	/**
	 * Pauses the Scheduler.
	 * The running AssemblyLine will continue
	 */
	public void pauseScheduler() {
		pause = true;
	}

	/**
	 * Resumes the Scheduler.
	 */
	public synchronized void resumeScheduler() {
		pause = false;
		notifyAll();
	}
	
	/**
	 * Returns true if this Scheduler is paused.
	 * @return true if this Scheduler is paused.
	 */
	public boolean isPaused() {
		return pause;
	}
	
	/**
	 * Returns information about this Scheduler.
	 * The following keys may be present in the map: <br>
	 * schedulerName - The name of this Scheduler <br>
	 * assemblyLineName - The name of the AssemblyLine/Sequence <br>
	 * serverName - The Server where the AssemblyLine is run <br>
	 * configID - The name of the Config Instance <br>
	 * isPaused - Has the value "true" if this Scheduler is paused <br>
	 * isKeepAlive - Has the value "true" if this Scheduler keeps the AL alive <br>
	 * nextRun - The java.util.Date for the next run of the AssemblyLine <br>
	 * @return
	 */
	public Map<String, Object> getInfo() {
		Map<String, Object> result = new HashMap<String, Object> ();
		result.put("schedulerName", getName());
		result.put("assemblyLineName", alName);
		if (server != null)
			result.put("serverName", server.substring(0, server.lastIndexOf(':')));
		if (configID != null)
			result.put("configID", configID);
		if (pause)
			result.put("isPaused", "true");
		if (config.getType() == SchedulerConfig.KEEP_ALIVE)
			result.put("isKeepAlive", "true");
		if (next != null)
			result.put("nextRun", next);
		return result;
	}
	
	/**
	 * Returns the RS that started this Scheduler
	 * @return
	 */
	public RS getRS() {
		return rs;
	}
	
	private boolean isTerminated() {
		return terminated || (rs != null && ! rs.isAlive());
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
}
