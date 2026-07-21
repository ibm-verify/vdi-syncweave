/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.UpdateInstaller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Used to siphon off the standard and error output of commands executed from a
 * Java program. Threads are used because without them, commands will often hang
 * the underlying Java program.
 * 
 * @author Alan Watkins
 * 
 */
public class GetStreamOutput extends Thread {
	/**
	 * The copyright notice for binary java code required by legal.
	 */
	private static final String COPYRIGHT = com.ibm.di.UpdateInstaller.FixUtils.OBJECT_CODE;
	/**
	 * Input stream tied to an executing command
	 */
	private InputStream is;

	/**
	 * Message read from the associated input stream
	 */
	private String message = "";

	/**
	 * Class constructor that takes an InputStream to read from.
	 * 
	 * @param is
	 *            InputStream to read from
	 */
	GetStreamOutput(InputStream is) {
		this.is = is;
	}

	/**
	 * Gets messages from the associated stream. This method should be called
	 * after the executing command completes.
	 * 
	 * @return Message from the input stream
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Required run method. Kicked off when start() method from thread is
	 * called.
	 */
	public void run() {
		String line = "";
		StringBuffer sb = new StringBuffer();

		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			// Without a new thread, the next line hangs...no idea why
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException ioe) {
			System.out.println(UpdateInstallerMsgs
					.getString("GENERIC.STREAM.READ.ERROR"));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs
					.getString("GENERIC.STREAM.READ.ERROR")
					+ ":" + ioe.getLocalizedMessage(),
					UpdateInstallerMsgs.DEBUG);
		}
		message = sb.toString();
	}
}
