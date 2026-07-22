/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jms.JMSException;

import com.ibm.icu.text.DateFormat;

/**
 * This is the common log class used by all of the Proxy Components.
 */
public class PWSyncLog {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	protected boolean debug = false;

	protected PrintWriter log = null;

	protected boolean logOpen = false;

	protected DateFormat dateFormat = DateFormat.getDateTimeInstance(
			DateFormat.SHORT, DateFormat.SHORT);

	private static final int MAX_PREFIX_LEN = 12;
	private static final String[] PADDING = new String[MAX_PREFIX_LEN];

	private boolean rotateLogs;
	private String lastLogTime;
	private SimpleDateFormat rotateFormat;
	private String logFilename;
	private String charSet;
	
	static {
		for (int i = 0; i < MAX_PREFIX_LEN; i++) {
			PADDING[i] = "";
			for (int j = 0; j < i; j++) {
				PADDING[i] += " ";
			}
		}
	}

	/**
	 * Creates a instance of the {@link PWSyncLog} class.
	 * 
	 * @param os
	 *            the output stream to which to log. If this is null no attempt
	 *            to open a stream will be made.
	 * @param charSet
	 *            the character set name used for encoding the stream.
	 * @param debug
	 *            "To Debug, Or Not? ...That is the question.
	 */
	public PWSyncLog(OutputStream os, String charSet, boolean debug) {

		if (os == null) {
			this.logOpen = false;
		} else {
			try {
				if(charSet == null || charSet.trim().length() == 0) {
					log = new PrintWriter(new OutputStreamWriter(os), true);
					this.charSet = null;
				} else {
					log = new PrintWriter(new OutputStreamWriter(os, charSet), true);	
					this.charSet = charSet;
				}				
			} catch (UnsupportedEncodingException uee) {
				log = new PrintWriter(new OutputStreamWriter(os), true);
				this.charSet = null;
			}

			this.debug = debug;
			this.logOpen = true;
		}
	}

	protected String getDateTime() {
		return dateFormat.format(new Date());
	}

	protected synchronized void log(String prefix, String level, String msg) {
		if (logOpen) {
			if (rotateLogs)
				checkRotation();
			
			if (prefix == null) {
				log.print("[" + getDateTime() + "]    "
						+ PADDING[PADDING.length - 1]);
			} else {

				int index = MAX_PREFIX_LEN - prefix.length();

				if (index < 0) {
					index = 0;
				}
				log.print("[" + getDateTime() + "] {" + prefix + "} "
						+ PADDING[index]);
			}

			if (level != null) {
				log.print(level + ":   ");
			}

			if (msg != null) {
				log.print(msg);
			}

			log.println();

			log.flush();
		}
	}

	private void checkRotation() {
		String now = rotateFormat.format(new Date());
		if (now.equals(lastLogTime))
			return; // No need to rotate
		
		log.close();
		if (! new File(logFilename).renameTo(new File(logFilename + "." + lastLogTime))) { 
			return; // Unable to rotate log, try again next time.
		}

		OutputStream os;
		try {
			os = new FileOutputStream(logFilename, true);
		} catch (FileNotFoundException fnfe) {
			logOpen = false; // This should never happen
			return;
		}

		try {
			if(charSet == null){
				log = new PrintWriter(new OutputStreamWriter(os), true);
			} else {
				log = new PrintWriter(new OutputStreamWriter(os, charSet), true);	
			}				
		} catch (UnsupportedEncodingException uee) {
			// This should never happen
			log = new PrintWriter(new OutputStreamWriter(os), true);
		}
		lastLogTime = now;
	}

	public void plain(String msg) {
		log(null, null, msg);
	}

	public void error(String prefix, String msg) {
		if (msg != null)
			log(prefix, "ERROR", msg);
	}

	public void error(String msg) {
		error(null, msg);
	}

	public synchronized void error(String prefix, String msg, Throwable error) {
		if (msg != null)
			log(prefix, "ERROR", msg);
		if (error != null && logOpen)
			printStackTrace(error);
	}

	public void error(String msg, Throwable error) {
		error(null, msg, error);
	}

	public synchronized void debug(String prefix, String msg) {
		if (debug && msg != null) {
			log(prefix, "DEBUG", msg);
		}
	}

	public void debug(String msg) {
		debug(null, msg);
	}

	public void info(String prefix, String msg) {
		if (msg != null)
			log(prefix, "INFO", msg);
	}

	public void info(String msg) {
		info(null, msg);
	}

	public void warn(String prefix, String msg) {
		if (msg != null)
			log(prefix, "WARN", msg);
	}

	public void warn(String msg) {
		warn(null, msg);
	}

	public synchronized void close() {
		if (logOpen && log != null) {
			log.flush();
			log.close();
			log = null;
			logOpen = false;
		}
	}

	public synchronized boolean isOpen() {
		return logOpen;
	}

	public synchronized void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Open the specified file for logging.
	 * 
	 * @param file
	 *            the file path to use.
	 * @param charSet
	 *            the encoding to output to. If this is null the default
	 *            encoding would be used ({@value #LOG_ENCODING})
	 * @param debug
	 *            if true more verbose messages will be printed
	 * @return an instance of the {@link PWSyncLog} class.
	 * 
	 * @throws FileNotFoundException
	 *             if the file path could not be resolved.
	 */
	public static PWSyncLog getLogForFile(String file, String charSet,
			boolean debug) throws FileNotFoundException {

		return new PWSyncLog(new FileOutputStream(file), charSet, debug);
	}

	public PrintWriter getPrintWriter() {
		return log;
	}

	protected void printStackTrace(Throwable error) {
		while (error != null) {
			error.printStackTrace(log);

			if (error instanceof JMSException) {
				error = ((JMSException) error).getLinkedException();
			} else {
				error = null;
			}
		}
	}
	
	
	/**
	 * Open the specified file for logging.
	 * 
	 * @param file
	 *            the file path to use.
	 * @param charSet
	 *            the encoding to output to. If this is null the default
	 *            encoding would be used ({@value #LOG_ENCODING})
	 * @param debug
	 *            if true more verbose messages will be printed
	 * @param rotatePattern
	 * 			  The pattern used for rotating logs.           
	 *  
	 * @return an instance of the {@link PWSyncLog} class.
	 * 
	 * @throws FileNotFoundException
	 *             if the file path could not be resolved.
	 */
	public static PWSyncLog getLogForFile(String file, String charSet,
			boolean debug, String rotatePattern) throws FileNotFoundException {
		if (rotatePattern == null || rotatePattern.trim().length() == 0)
			return new PWSyncLog(new FileOutputStream(file), charSet, debug);
		
		PWSyncLog logger = new PWSyncLog(new FileOutputStream(file, true), charSet, debug);
		
		logger.logFilename = file;
		logger.rotateLogs = true;
		logger.rotateFormat = new SimpleDateFormat(rotatePattern);
		logger.lastLogTime = logger.rotateFormat.format(new Date());

		return logger;
	}

}
