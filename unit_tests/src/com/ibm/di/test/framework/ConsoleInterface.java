package com.ibm.di.test.framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class ConsoleInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private BufferedReader in;
	private OutputStreamWriter out;
	private OutputStreamWriter err;

	/**
	 * 
	 */
	public ConsoleInterface(InputStream in, OutputStream out, OutputStream err) {
		this.in = new BufferedReader(new InputStreamReader(in));
		this.out = new OutputStreamWriter(out);
		this.err = new OutputStreamWriter(err);
	}

	/**
	 * @param string
	 * @throws IOException
	 */
	public boolean getYesNo(String question, boolean defVal) throws IOException {
		out.write(question + " (y/n)" + (defVal ? "[y]" : "[n]"));
		out.flush();

		boolean result = defVal;
		String line = in.readLine();
		if (line != null) {
			line = line.trim();
			if (line.length() != 0) {
				result = line.regionMatches(true, 0, "y", 0, 1);
			}
		}
		return result;
	}

	/**
	 * @param string
	 * @throws IOException
	 */
	public void println(String string) throws IOException {
		out.write(string);
		out.write('\n');
		out.flush();
	}
}
