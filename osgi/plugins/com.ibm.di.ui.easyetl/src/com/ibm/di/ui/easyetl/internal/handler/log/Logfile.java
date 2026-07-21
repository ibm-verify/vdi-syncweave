/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.easyetl.internal.handler.log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.regex.Pattern;

public class Logfile {

	private File file;
	private FileInputStream inp;
	private FileChannel channel;
	
	private String pushBack;
	private Pattern pattern;
	
	/**
	 * The starting point in the logfile
	 */
	private long currentPos;
	
	/**
	 * The mapped area of the file
	 */
	private MappedByteBuffer map;
	
	/**
	 * The offset to the last returned message
	 */
	private long messageOffset;
	
	/**
	 * The offset to the next message 
	 */
	private long nextMessageOffset;
	
	/**
	 * Number of bytes in the last string
	 */
	private int strLength;

	/**
	 * Number of lines to parse - this is translated
	 * to a byte count with an average line length of 100. 
	 */
	private long lines;

	public Logfile(String path, Pattern pattern, long startPos, long lines) throws Exception {
		file = new File(path);
		inp = new FileInputStream(file);
		channel = inp.getChannel();
		currentPos = startPos;
		if(currentPos > file.length())
			currentPos = file.length();
		if (currentPos < 0)
			currentPos = 0;
		this.lines = lines;
		this.pattern = pattern;
		
		//
		// map a segment of the file to a byte array
		// this is the segment we are searching.
		// multiply lines by 100 which is an average line length
		//
		long length = computeLength();
		map = channel.map(MapMode.READ_ONLY, currentPos, length);
	}
	
	private long computeLength() {
		long length = lines * 100;
		if((currentPos + length) > file.length()) {
			length = file.length() - currentPos;
			
		}
		return length;
	}
	
	/**
	 * Returns the current offset in the file
	 * @return
	 */
	public long getStartOffset() {
		return currentPos;
	}

	/**
	 * Returns the offset in the file for the last message returned
	 * 
	 * @return
	 */
	public long getMessageOffset() {
		return messageOffset;
	}
	
	/**
	 * Returns the position where the next message should start
	 * 
	 * @return
	 */
	public long getNextMessageOffset() {
		return this.nextMessageOffset;
	}
	
	/**
	 * Push the last message
	 * 
	 * @param str
	 */
	private void pushback(String str) {
		this.pushBack = str;
	}
	
	/**
	 * Returns the next line from the input stream
	 * 
	 * @param inp
	 * @return
	 * @throws Exception
	 */
	private String nextLine() throws Exception {
		String str = null;
		if(this.pushBack != null) {
			str = this.pushBack;
			this.pushBack = null;
		} else {
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			while(true) {
				// if we dont get a NL the line is not "complete"
				// and we return null
				if(!map.hasRemaining()) {
					this.currentPos += map.position();
					if(this.currentPos >= file.length())
						return null;
					map = channel.map(MapMode.READ_ONLY, this.currentPos, computeLength());
				}
				byte b = map.get();
				if(b == '\r') {
					continue; //ignore
				} else if(b == '\n') {
					if(buf.size() > 0)
						break;
				} else {
					buf.write(b);
				}
			}
			strLength = buf.size();			
			if(strLength > 0) {
				str = buf.toString();
			}
		}
		return str;
	}
	
	/**
	 * Returns the next log message from the log. This can be a single line or multiple lines in
	 * case of stack traces etc.
	 * 
	 * @param inp
	 * @return
	 * @throws Exception
	 */
	public String nextLogMessage() throws Exception {
		StringBuffer buf = new StringBuffer();
		String str = nextLine();
		
		// skip until we get a proper log message - move mapped area if necessary
		while(str != null && !pattern.matcher(str).matches()) {
			str=nextLine();
		}
		
		// at this point we are at EOF
		if(str == null)
			return null;
		
		boolean collectingDumpEntry = false;
		
		// message offset is position in file + position in currently mapped buffer
		this.messageOffset = this.currentPos + (map.position() -  (strLength + 1));
		this.nextMessageOffset = this.currentPos + map.position();
		
		// collect lines until the next proper log message is found
		while(str != null) {
			if(!collectingDumpEntry && buf.length() > 0 && pattern.matcher(str).matches()) {
				pushback(str);
				break;
			} else {
				if(buf.length() > 0)
					buf.append("\n");
				if(collectingDumpEntry) {
					buf.append(str.substring(str.indexOf(" - ")+3));
				} else {
					buf.append(str);
				}
			}
			if(str.indexOf("CTGDIS003I ") != -1)
				collectingDumpEntry = true;
			if(str.indexOf("CTGDIS004I ") != -1)
				collectingDumpEntry = false;
			if(str.indexOf("CTGDIS100I") != -1)
				collectingDumpEntry = true;
			if(str.indexOf("CTGDIS101I") != -1)
				collectingDumpEntry = false;
			str = nextLine();
		}
		
		return buf.length() == 0 ? null : buf.toString();		
	}
}
