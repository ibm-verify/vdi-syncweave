/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.util.Calendar;
import java.util.Date;

import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.GregorianCalendar;

/**
 * Class used to compute the next date a Scheduler will be run
 *
 */
public class Schedule {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private ResourceHash res = ResourceHash.getHash("miserver");

	private boolean[] month = new boolean[12];
	private boolean[] mday = new boolean[32];
	private boolean[] wday = new boolean[8];
	private boolean[] hour = new boolean[24];
	private boolean[] minute = new boolean[60];
	private boolean[] second = new boolean[62]; // Add space for leap seconds
	/**
	 * Constructor.
	 * The schedule parameter must be similar to crontab format,
	 * with 6 fields, separated by space.
	 * @param schedule 
	 */
	public Schedule(String schedule) throws Exception {
		String[] part = schedule.split(" ");
		if (part.length != 6)
			throw new Exception(res.getString("Schedule.number.of.fields", schedule));
		assign(part[0], month);
		assign(part[1], mday);
		assign(part[2], wday);
		assign(part[3], hour);
		assign(part[4], minute);
		assign(part[5], second);
	}

	/**
	 * Assigns values to the boolean array.
	 * @param part String to parse
	 * @param arr Array to get values
	 */
	private void assign(String part, boolean[] arr) throws Exception {
		if (part.equals("*")) {
			for (int i = 0; i < arr.length; i++)
				arr[i] = true;
			return;
		}

		for (String num:part.split(",")) {
			String[] range = num.split("-");
			if(range.length == 2) {
				for(int i = Integer.valueOf(range[0]); i <= Integer.valueOf(range[1]); i++) {
					if (i < 0 || i >= arr.length)
						throw new Exception(res.getString("Schedule.value.incorrect", num));
					arr[i] = true;
				}
			} else {
				int i = Integer.valueOf(num);
				if (i < 0 || i >= arr.length)
					throw new Exception(res.getString("Schedule.value.incorrect", num));
				arr[i] = true;
			}
		}
	}

	/**
	 * Computes the next date after the given date.
	 * @param current - If null, uses current date instead.
	 * @return
	 * @throws Exception If no date can be found.
	 */
	public Date getNext(Date current) throws Exception {
		GregorianCalendar calendar = new GregorianCalendar();
		if (current != null)
			calendar.setTime(current);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.SECOND, 1);

		boolean change;
		do {
			change = false;
			while (! month[calendar.get(Calendar.MONTH)]) {
				resetDay(calendar);
				calendar.add(Calendar.MONTH, 1);
			}
			while (! mday[calendar.get(Calendar.DAY_OF_MONTH)] ||
					! wday[calendar.get(Calendar.DAY_OF_WEEK)]) {
				resetHour(calendar);
				calendar.add(Calendar.DAY_OF_MONTH, 1);
				change = true;
				// Add a small check to make it less probable to spin forever
				if (calendar.get(Calendar.YEAR) >= 2400)
					throw new Exception(res.getString("Schedule.cannot.compute"));
			}
			while (! hour[calendar.get(Calendar.HOUR_OF_DAY)]) {
				resetMinute(calendar);
				calendar.add(Calendar.HOUR_OF_DAY, 1);
				change = true;
			}
			while (! minute[calendar.get(Calendar.MINUTE)]) {
				resetSecond(calendar);
				calendar.add(Calendar.MINUTE, 1);
				change = true;
			}
			while (! second[calendar.get(Calendar.SECOND)]) {
				calendar.add(Calendar.SECOND, 1);
				change = true;
			}
		} while (change);
		return calendar.getTime();
	}

	private void resetDay(GregorianCalendar calendar) {
		resetHour(calendar);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
	}

	private void resetHour(GregorianCalendar calendar) {
		resetMinute(calendar);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
	}

	private void resetMinute(GregorianCalendar calendar) {
		resetSecond(calendar);
		calendar.set(Calendar.MINUTE, 0);
	}

	private void resetSecond(GregorianCalendar calendar) {
		calendar.set(Calendar.SECOND, 0);
	}

}
