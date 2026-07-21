/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.easyetl.internal.handler.log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.di.api.remote.Session;
import com.ibm.di.api.syslog.LogUtils;
import com.ibm.di.ui.easyetl.bind.Logsearch;
import com.ibm.di.ui.easyetl.bind.Logsearchresult;

public class LogWrapper {
	
	public static final String ROOT_LOG_DIR = LogUtils.ROOT_LOG_DIR;
	
	public static final String AL_LOG_DIR_PREFIX = LogUtils.AL_LOG_DIR_PREFIX;
	
	private Logsearch search;

	private Pattern pattern;

	/**
	 * Contains the next message number. This number is relative to
	 * the start of the search. It is used to detect gaps in the returned
	 * log search result set.
	 */
	private long msgNumber;
	
	private int fDate = 1;
	private int fTime = 2;
	private int fType = 3;
	private int fSource = 4;
	private int fMessage = 5;

	/*
	 * while finding a tombstone's file we require that the
	 * logfile be created no later than LOGFILE_DIFF_MAX seconds.
	 */
	private long LOGFILE_DIFF_MAX = 60;

	public LogWrapper(Logsearch search) {
		this.search = search;
	}

	public Logsearch search(Session session) throws Exception {
		Logfile inp = null;
		String str;
		
		msgNumber = 1;
		search.getResult().clear();
		
		long start = 0;
		long lines = 300;
		if(search.getLocation() != null) {
			start = search.getLocation().getStart();
			lines = search.getLocation().getCount();
		}
		
		if(search.getConfig() == null && search.getAssemblyline() == null) {
			this.pattern = Pattern.compile("(.*) (.*) (INFO|DEBUG|WARN|ERROR)\\s*(\\[.*\\]) - (.*)", Pattern.DOTALL);
			inp = new Logfile("logs/" + search.getLogfile(), pattern, start, lines);
		} else {
			this.pattern = Pattern.compile("(.*) (.*) (INFO|DEBUG|WARN|ERROR)\\s* - (.*)", Pattern.DOTALL);
			String resolvedFile = findLogForAssemblyLine(session, search.getConfig(), search.getAssemblyline(), search.getLogfile());
			String logFileName = ROOT_LOG_DIR + search.getConfig() + "/" + AL_LOG_DIR_PREFIX + search.getAssemblyline() + "/" + resolvedFile;
			inp = new Logfile(logFileName, pattern, start, lines);
			this.fSource = -1;
			this.fMessage = 4;
		}
		
		String subsearch = search.getRegex() != null ? search.getRegex().toLowerCase() : null;
		
		ArrayList<Pattern> excludedSources = new ArrayList<Pattern>();
		if(search.getExcludesource() != null) {
			for(String patt : search.getExcludesource().split("\n")) {
				excludedSources.add(Pattern.compile(patt.trim(), Pattern.CASE_INSENSITIVE));
			}
		}
		
		while((str = inp.nextLogMessage()) != null) {
			if(subsearch == null || str.toLowerCase().contains(subsearch)) {
				Logsearchresult ls = new Logsearchresult();
				ls.setLine(inp.getMessageOffset());
				ls.setEndline(inp.getNextMessageOffset());
				ls.setText(str);
				
				Matcher m = pattern.matcher(str);
				if(m.matches()) {
					// excluded by type? 
					ls.setType(m.group(fType).toUpperCase());
					if(search.getQualifiers() != null) {
						if(!search.getQualifiers().isDebug() && ls.getType().equals("DEBUG"))
							continue;
						if(!search.getQualifiers().isInfo() && ls.getType().equals("INFO"))
							continue;
						if(!search.getQualifiers().isWarn() && ls.getType().equals("WARN"))
							continue;
						if(!search.getQualifiers().isError() && ls.getType().equals("ERROR"))
							continue;
					}
					
					if(fSource != -1) {
						ls.setSource(m.group(fSource));
						if(search.getSource() != null &&
							!search.getSource().equals(ls.getSource())) {
								continue;
						}
						for(Pattern pt : excludedSources) {
							if(pt.matcher(ls.getSource()).matches()) {
								ls = null;
								break;
							}
						}
						if(ls == null)
							continue;
					}
					
					// move multi-line to content
					ls.setText(m.group(fMessage));
					int index = ls.getText().indexOf("\n");
					if(index != -1) {
						ls.setContent(ls.getText().substring(index+1));
						ls.setText(ls.getText().substring(0, index));
					}
					ls.setDate(m.group(fDate));
					ls.setTime(m.group(fTime));
				}
				ls.setMsgno(msgNumber++);
				search.getResult().add(ls);
				
				if(msgNumber >= lines)
					break;
			}
		}
		
		return search;
	}
	

	private String findLogForAssemblyLine(Session sess, String config, String assemblyline, String timestamp) throws Exception {
		String closestMatch = null;
		long lastDiff = Long.MAX_VALUE;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss_SSS");
		Pattern p = Pattern.compile(".*(\\d{4}_\\d{2}_\\d{2}__\\d{2}_\\d{2}_\\d{2}_\\d{3})\\.log");
		Date ts = null;
		
		try {
			ts = sdf.parse(timestamp);
		} catch(ParseException ignore) {
			Matcher m = p.matcher(timestamp);
			if(m.matches()) {
				ts = sdf.parse(m.group(1));
			}
		}
		
		for(String str: sess.getSystemLog().getALLogFileNames(config, assemblyline)) {
			// LongRunning_2011_10_11__11_39_49_621
			Matcher m = p.matcher(str);
			if(m.matches()) {
				Date d = sdf.parse(m.group(1));
				// -- Start time is never after the log filename timestamp
				if(!d.before(ts)) {
					long diff = d.getTime() - ts.getTime();
					if(diff < lastDiff) {
						closestMatch = str;
						lastDiff = diff;
					}
				}
			}
		}
		// if diff is too large we did not find the log file
		if(lastDiff > LOGFILE_DIFF_MAX )
			return timestamp;
		else
			return closestMatch != null ? closestMatch : timestamp;
	}
}
