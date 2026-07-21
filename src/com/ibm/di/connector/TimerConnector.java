/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.util.Date;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

/**
 * This connector provides a simplistic version of a crontab style timer.
 */
public class TimerConnector extends Connector implements ConnectorInterface {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * name of the component
	 */
	private static final String PROPERTIES_FILE = "timerconnector";

	/**
	 * date format
	 */
	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
			DateFormat.MEDIUM);

	/**
	 * fields for the date format
	 */
	int[] fields = { Calendar.MONTH, Calendar.DAY_OF_MONTH,
			Calendar.DAY_OF_WEEK, Calendar.HOUR_OF_DAY, Calendar.MINUTE };

	/**
	 * schedule
	 */
	String schstr = null;
	// For a runtime connector returned in Server mode these two are used
	/**
	 * next entry for a connector returned in server mode
	 */
	private Entry nextEntry;

	/**
	 * single entry returned
	 */
	private boolean isSingleEntry = false;

	/**
	 * Resoruce hash object used for accessing TMS messages
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Class constructor
	 */
	public TimerConnector() {
		super();
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE, });
	}

	/**
	 * Default implementation
	 * 
	 * @throws Exception
	 *             never
	 */
	public void selectEntries() throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize(Object o) throws Exception {
		// get the params from the config tab and convert them into the masked
		// format.
		// covert only if schedule is specified in new format.
		String str = getParam("schedule");

		// StringTokenizer st = new StringTokenizer(str, " \t");
		if (str != null && !str.equals("0") && str.length() > 0) {
			// old style schedule
			schstr = str;
		} else {
			// new style schedule
			schstr = getMaskSchedule();
		}

		if (schstr != null) {
			logmsg(sResHash.getString("CONNECTOR.TIMER.INIT.INFO", schstr));
		}
	}

	/**
	 * Sets next entry for server mode
	 * 
	 * @param e
	 *            next clien
	 */
	public void setNextEntry(Entry e) {
		nextEntry = e;
		isSingleEntry = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getNextEntry() throws Exception {
		// Single entry mode connector instance (used in server mode)
		if (isSingleEntry) {
			Entry e = nextEntry;
			nextEntry = null;
			return e;
		}

		calcNextRun();
		Entry e = new Entry();
		e.setAttribute("timestamp", new java.util.Date());
		return e;
	}

	/**
	 * {@inheritDoc}
	 */
	public ConnectorInterface getNextClient() throws Exception {
		Entry e = getNextEntry();
		TimerConnector ci = new TimerConnector();
		ci.setConfiguration(getConfiguration());
		ci.setName(getName());
		ci.setNextEntry(e);
		ci.setLog(getLog());
		return ci;
	}

	/**
	 * Checks if the specified day options are correct
	 * 
	 * @param month
	 *            month name
	 * @param dateNum
	 *            date number
	 * @param checkTime
	 *            time
	 * @throws Exception
	 *             if the day is not valid
	 */
	public void checkDay(String month, int dateNum, String[] checkTime)
			throws Exception {
		if (!(checkTime[1].equals("*")) && (dateNum < 1 || dateNum > 29)) {
			if (dateNum < 1) {
				throw new Exception(sResHash.getString(
						"CONNECTOR.TIMER.EXCEPTION.DAYSMALL", "" + dateNum));
			} else {
				if (month.equals("1") && dateNum > 29) {
					throw new Exception(sResHash.getString(
							"CONNECTOR.TIMER.EXCEPTION.DAYBIGFORFEB", ""
									+ dateNum));
				} else {
					if ((month.equals("3") || month.equals("5")
							|| month.equals("8") || month.equals("10"))
							&& dateNum > 30) {
						throw new Exception(sResHash.getString(
								"CONNECTOR.TIMER.EXCEPTION.DAYBIGFORMONTH",
								new Object[] { "" + dateNum, month }));
					} else {
						if (dateNum > 31) {
							throw new Exception(sResHash.getString(
									"CONNECTOR.TIMER.EXCEPTION.DAYLARGE", ""
											+ dateNum));
						} // if
					} // else
				} // else
			} // if
		} // first else
	}

	/**
	 * Checks if the date specified is correct
	 * 
	 * @param checkTime
	 *            time
	 * @param dateNum
	 *            month number
	 * @param whichDate
	 *            date number
	 * @throws Exception
	 *             if an error occurs
	 */
	public void checkDate(String[] checkTime, int dateNum, int whichDate)
			throws Exception {
		if (whichDate == 0 && (!(checkTime[0].equals("*")))
				&& (dateNum < 0 || dateNum > 11)) {
			throw new Exception(sResHash.getString(
					"CONNECTOR.TIMER.EXCEPTION.MONTHBAD", "" + dateNum));
		}

		if (whichDate == 1) {
			if (checkTime[0].indexOf(",") != -1) {
				// if more than one month go here
				StringTokenizer month = new StringTokenizer(checkTime[0], ",");
				while (month.hasMoreTokens()) {
					checkDay(month.nextToken(), dateNum, checkTime);
				}
			} else {
				checkDay(checkTime[0], dateNum, checkTime);
			}
		}

		if (whichDate == 2 && !(checkTime[2].equals("*"))
				&& (dateNum < 1 || dateNum > 7)) {
			throw new Exception(sResHash.getString(
					"CONNECTOR.TIMER.EXCEPTION.DAYOFWEEKBAD", "" + dateNum));
		}

		if (whichDate == 3 && !(checkTime[3].equals("*"))
				&& (dateNum < 0 || dateNum > 23)) {
			throw new Exception(sResHash.getString(
					"CONNECTOR.TIMER.EXCEPTION.HOURBAD", "" + dateNum));
		}

		if (whichDate == 4 && !(checkTime[4].equals("*"))
				&& (dateNum < 0 || dateNum > 59)) {
			throw new Exception(sResHash.getString(
					"CONNECTOR.TIMER.EXCEPTION.MINUTEBAD", "" + dateNum));
		}
	}

	/**
	 * Verifies the the schedule is correct
	 * 
	 * @param checkTime
	 *            time to be checked
	 * @throws Exception
	 *             if a problem occurs
	 */
	public void validSchedule(String[] checkTime) throws Exception {
		int i, checkDatenum = 0;

		for (i = 0; i < checkTime.length; i++) {
			if (!(checkTime[i].equals("*"))) {
				if (checkTime[i].indexOf(",") != -1) {
					StringTokenizer st2 = new StringTokenizer(checkTime[i], ",");
					while (st2.hasMoreTokens()) {
						checkDatenum = Integer.parseInt(st2.nextToken());
						checkDate(checkTime, checkDatenum, i);
					} // while
				}// if 2
				else {
					checkDatenum = Integer.parseInt(checkTime[i]);
					checkDate(checkTime, checkDatenum, i);
				}
			} // if 1
			else {
				checkDate(checkTime, checkDatenum, i);
			}
		}// for loop
	}

	/**
	 * Calculates the next run
	 * 
	 * @throws Exception
	 *             if schedule is invalid or missing
	 */
	public void calcNextRun() throws Exception {

		if (schstr == null) {
			throw new Exception(sResHash
					.getString("CONNECTOR.TIMER.EXCEPTION.SCHEDULEMISSING"));
		}

		StringTokenizer st = new StringTokenizer(schstr, " \t");
		if (st.countTokens() != fields.length) {
			throw new Exception(sResHash
					.getString("CONNECTOR.TIMER.EXCEPTION.SCHEDULEBAD"));
		}

		String[] tok = new String[fields.length];
		int i = 0;
		while (st.hasMoreTokens()) {
			tok[i++] = st.nextToken();
		}
		validSchedule(tok);

		// month day weekday hour minute function
		GregorianCalendar gc = (GregorianCalendar) GregorianCalendar
				.getInstance();

		// Advance gc to next minute
		gc.add(Calendar.MINUTE, 1);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);

		boolean change;

		do {
			change = false;

			for (i = 0; i < fields.length; i++) {
				if (tok[i].compareTo("*") != 0) {
					int good;
					if (tok[i].indexOf(",") != -1) {
						StringTokenizer st2 = new StringTokenizer(tok[i], ",");
						good = 9999;
						while (st2.hasMoreTokens()) {
							int v = Integer.parseInt(st2.nextToken());
							// Assume tokens are sorted
							if (gc.get(fields[i]) <= v) {
								good = v;
								break;
							}
							if (v < good) {
								good = v;
							}
						}
						if (good == 9999) {
							good = 0; // Impossible ?
						}
					} else {
						good = Integer.parseInt(tok[i]);
					}

					if (gc.get(fields[i]) != good) {
						change = true;
						while (gc.get(fields[i]) != good) {
							gc.add(Calendar.MINUTE, 1);
						}
					}
				}
			}
		} while (change);

		logmsg(sResHash.getString("CONNECTOR.TIMER.NEXTRUN.INFO", gc.getTime()
				.toString()));

		doWait(gc.getTime().getTime(), System.currentTimeMillis());
	}

	/**
	 * Connector waits for the specified difference in the parameters
	 * 
	 * @param p1
	 *            wait until
	 * @param p2
	 *            current time in milliseconds
	 * @throws InterruptedException
	 *             if the connector is interrupted
	 */
	public void doWait(long p1, long p2) throws InterruptedException {
		long wait;
		String until = df.format(new Date(p1));

		if (p1 > p2) {
			wait = p1 - p2;
		} else {
			wait = 0;
		}

		while (wait > 0) {
			wait += 20;
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.TIMER.SLEEPSUMMARY.INFO",
						new Object[] { "" + (wait + 500) / 1000, until }));
			}

			Thread.sleep(wait);
			wait = p1 - System.currentTimeMillis();
		}
	}

	/**
	 * This function is not used directly, but maybe some old configurations use
	 * this.Connector sleeps for the specified time
	 * 
	 * @param p1
	 *            time to wait
	 * @return is connector sleeping
	 */
	public boolean sleepSeconds(long p1) {
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.TIMER.SLEEP.INFO", "" + p1));
		}

		try {
			Thread.sleep((p1 * 1000));
			return true;
		} catch (InterruptedException ie) {
			return false;
		}
	}

	/**
	 * Retrieves the schedule to next run
	 * 
	 * @return the schedule
	 */
	public String getMaskSchedule() {
		String str;
		str = getMonthValue(getParam("month")) + " \t";
		// No need to any checking.
		str = str + getParam("day") + " \t";
		str = str + getWeekValue(getParam("weekday")) + " \t";
		str = str + getParam("hours") + " \t";
		str = str + getParam("minutes");
		return str;
	}

	/**
	 * Translates month of the year to number
	 * 
	 * @param strMonth
	 *            string representation
	 * @return month number
	 */
	public String getMonthValue(String strMonth) {
		// chnage to a lookup table later
		if (strMonth.equalsIgnoreCase("January")) {
			return "0";
		} else if (strMonth.equalsIgnoreCase("February")) {
			return "1";
		} else if (strMonth.equalsIgnoreCase("March")) {
			return "2";
		} else if (strMonth.equalsIgnoreCase("April")) {
			return "3";
		} else if (strMonth.equalsIgnoreCase("may")) {
			return "4";
		} else if (strMonth.equalsIgnoreCase("june")) {
			return "5";
		} else if (strMonth.equalsIgnoreCase("july")) {
			return "6";
		} else if (strMonth.equalsIgnoreCase("august")) {
			return "7";
		} else if (strMonth.equalsIgnoreCase("september")) {
			return "8";
		} else if (strMonth.equalsIgnoreCase("october")) {
			return "9";
		} else if (strMonth.equalsIgnoreCase("november")) {
			return "10";
		} else if (strMonth.equalsIgnoreCase("december")) {
			return "11";
		} else {
			return "*";
		}
	}

	/**
	 * Translates day of the week to number
	 * 
	 * @param strWeekDay
	 *            string representation
	 * @return day number
	 */
	public String getWeekValue(String strWeekDay) {
		if (strWeekDay.equalsIgnoreCase("Sunday")) {
			return "1";
		} else if (strWeekDay.equalsIgnoreCase("Monday")) {
			return "2";
		} else if (strWeekDay.equalsIgnoreCase("tuesday")) {
			return "3";
		} else if (strWeekDay.equalsIgnoreCase("wednesday")) {
			return "4";
		} else if (strWeekDay.equalsIgnoreCase("thursday")) {
			return "5";
		} else if (strWeekDay.equalsIgnoreCase("friday")) {
			return "6";
		} else if (strWeekDay.equalsIgnoreCase("saturday")) {
			return "7";
		} else {
			return "*";
		}
	}

	/**
	 * Return version information
	 * 
	 * @return version info
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I% 20%E%";
	}

}
