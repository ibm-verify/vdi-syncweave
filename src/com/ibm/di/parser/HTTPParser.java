/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;
import com.ibm.icu.util.StringTokenizer;

/**
 * 
 * The HTTP Parser interprets a byte stream according to the HTTP specification.
 * This Parser is used by the HTTP Client Connector and by the HTTP Server
 * Connector.
 * 
 */
public class HTTPParser extends ParserImpl {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "httpparser";

	/**
	 * String containing the status code and reason phrase for successfully
	 * received, understood and accepted action.
	 */
	final public static String HTTP_OK = "200 OK";

	/**
	 * String containing the status code and reason phrase for unsuccessful
	 * action when the server has not found anything matching the Request-URI.
	 */
	final public static String HTTP_FILE_NOT_FOUND = "404 File Not Found";

	/**
	 * String containing the status code and reason phrase for unsuccessful
	 * action because the request requires user authentication.
	 */
	final public static String HTTP_FORBIDDEN = "401 Forbidden";
	final public static String HTTP_BADMESSAGE = "400 Bad Message";

	/**
	 * String containing the status code and reason phrase for redirection when
	 * the requested resource resides temporarily under a different URI.
	 */
	final public static String HTTP_REDIR = "302 Found";

	/**
	 * (ISO Latin 1) Character Encoding.
	 */
	public static final String ENCODING_LATIN_1 = "iso-8859-1";

	/**
	 * String separator in HTTP header.
	 */
	private static final String HTTP_HEADER_SEPARATOR = ":";

	/**
	 * If true stores HTTP headers as properties, else stores them as
	 * attributes.
	 */
	private boolean headersAsProperties = false;

	/**
	 * If set, the parser operates in client HTTP response mode. If not set, the
	 * parser operates in server mode. This is of interest only if the Parser is
	 * writing an output stream.
	 */
	private boolean clientMode = false;

	/**
	 * character encoding
	 */
	private String charset = ENCODING_LATIN_1;

	/**
	 * Stores row data in byte array.
	 */
	private byte[] buffer = null;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	private boolean headRequest;
	
	/**
	 * {@inheritDoc}
	 */
	public void initParser() throws Exception {
		Trace.entrymin(this, "initParser");
		super.initParser();
		resetProperties();
		if (getParam("headersAsProperties") != null && getParam("headersAsProperties").equals("true"))
			headersAsProperties = true;
		if (getParam("clientMode") != null && getParam("clientMode").equals("true"))
			clientMode = true;
		Trace.exitmin(this, "initParser");

	}

	/**
	 * Sets the user properties.
	 * 
	 * @param useProps
	 *            the new properties.
	 */
	public void setUseProperties(boolean useProps) {
		this.headersAsProperties = useProps;
	}

	/**
	 * Sets the client mode.
	 * 
	 * @param clientMode
	 *            the new client mode.
	 */
	public void setClientMode(boolean clientMode) {
		this.clientMode = clientMode;
	}

	/**
	 * Reads one byte of the current input reader.
	 * 
	 * @return the read byte
	 * @throws Exception
	 *             If an I/O error occurs
	 */
	public int readByte() throws Exception {
		InputStream is = getInputStream();
		if (is == null) {
			if (getReader() != null)
				return getReader().read();
			else
				return -1;
		} else {
			return is.read();
		}
	}

	/**
	 * Reads line from the current input reader.
	 * 
	 * @return the read line
	 * @throws Exception
	 *             If an I/O error occurs
	 */
	public String readLine() throws Exception {

		InputStream is = getInputStream();
		if (is == null) {
			if (debugMode()) {
				debug(sResHash.getString("PARSER.HTTP.INPUTREADER.INFO"));
			}
			if (getReader() != null)
				return getReader().readLine();
			else
				return null;
		} else {
			if (debugMode()) {
				debug(sResHash.getString("PARSER.HTTP.USEINPUTSTREAM.INFO", charset));
			}
			StringBuffer buf = new StringBuffer();

			int ch;
			if (debugMode()) {
				debug(sResHash.getString("PARSER.HTTP.READLINEPARSE.START.INFO"));
			}
			while ((ch = is.read()) != -1) {
				// if ( getDebug() ){
				// debug(sResHash.getString("PARSER.HTTP.READLINE.CHARACTER.INFO",
				// new Object [] {"" + ch, "" + (char)ch}));
				// }
				if (ch == '\n') {
					return buf.toString();
				} else if (ch == '\r') {
					// no nothing...
					continue;
				} else {
					buf.append((char) ch);
				}
			}
		}

		return null;
	}

	/**
	 * Reads entry from the current input reader. Depending on whether the
	 * parameter <code>headersAsProperties</code> is checked, read headers are
	 * represented as attributes or properties.
	 * 
	 * @return the read entry
	 * @throws Exception
	 *             If an I/O error occurs
	 */
	public Entry readEntry() throws Exception {
		Trace.entrymax(this, "readEntry");
		if (getReader() == null && getInputStream() == null)
			return null;

		// Allow user to override the default charset
		charset = getParam("characterSet");
		if (charset == null || charset.trim().length() == 0)
			charset = ENCODING_LATIN_1;
		if (debugMode()) {
			debug(sResHash.getString("PARSER.HTTP.READCHARSET.INFO", charset));
		}

		String line = readLine();
		if (line == null || line.length()==0)
			return null;
		
		if (debugMode()) {
			debug(sResHash.getString("PARSER.HTTP.READLINE.READENTRY.INFO", line));
		}

		Entry entry = new Entry();
//		StringTokenizer st = new StringTokenizer(line, " ");
		String[] st = line.split(" ", 3);

		if (clientMode) {
			if (st.length >= 2)
				setProp(entry, "http.responseCode", st[1].trim());
			if (st.length >= 3)
				setProp(entry, "http.responseMsg", st[2].trim());
			parseHTTPHeaders(null, entry);
		} else {
			if (st.length >= 1)
				setProp(entry, "http.method", st[0]);
			if (st.length >= 2) {
				try {
					setProp(entry, "http.url", URLDecoder.decode(st[1], "UTF-8"));
				} catch (Exception e) {
					setProp(entry, "http.url", st[1]);
				}
			}
			parseHTTPHeaders(line, entry);
		}

		// Read posted data/request reply data if any
		parseBody(entry);

		// Decode body if it is x-www-form-urlencoded
		String contentType = (String) getProp(entry, "http.content-type");
		if ((contentType != null) && (contentType.trim().equalsIgnoreCase("application/x-www-form-urlencoded"))
				&& (getProp(entry, "http.body") != null)) {
			// Set parameters based on body
			String httpBody = getProp(entry, "http.body").toString();
			addParameters(new StringTokenizer(httpBody, "?&"), entry);
			try {
				setProp(entry, "http.body", URLDecoder.decode(httpBody, "UTF-8"));
			} catch (Exception e) {
				setProp(entry, "http.body", httpBody);
			}
		}
		Trace.exitmax(this, "readEntry", entry);
		return entry;
	}

	/**
	 * Writes an entry to the current output writer.
	 * 
	 * @param entry
	 *            the entry to be written.
	 * @throws Exception
	 *             <li>if <code>http.url</code> is missing</li> <li>if an I/O
	 *             error occurs</li> <li>if base64 encoding fails</li>
	 */
	public void writeEntry(Entry entry) throws Exception {
		Trace.entrymax(this, "writeEntry", entry);
		BufferedWriter out = getWriter();

		// Allow user to override the default charset
		charset = getParam("characterSet");
		if (charset == null || charset.trim().length() == 0)
			charset = ENCODING_LATIN_1;
		if (debugMode()) {
			debug(sResHash.getString("PARSER.HTTP.WRITEENTRY.MODE.INFO", "" + clientMode));
		}

		if (clientMode) {
			String reqMethod = (String) getProp(entry, "http.method");
			if (reqMethod == null)
				reqMethod = "GET";
			headRequest = "HEAD".equalsIgnoreCase(reqMethod);

			String reqURL = (String) getProp(entry, "http.url");
			if (reqURL == null) {
				throw new Exception(sResHash.getString("PARSER.HTTP.URLCLIENT.MISSING"));
			}

			URL url = new URL(reqURL);
			String file = url.getFile();
			if (!file.startsWith("/"))
				file = "/" + file;
			if (entry.getProperty("http.proxy") != null)
				file = url.toString();

			StringBuffer fileEnc = new StringBuffer();
			for (int i = 0; i < file.length(); i++) {
				if (file.charAt(i) == ' ')
					fileEnc.append("%20");
				else
					fileEnc.append(file.charAt(i));
			}

			out.write(reqMethod + " " + fileEnc.toString() + " HTTP/1.1\r\n");

			String httpHost = (String) getProp(entry, "http.host");
			if (httpHost == null) {
				httpHost = url.getHost();
				if (url.getPort() != -1) {
					httpHost += ":" + url.getPort();
				}
			}
			out.write("Host: " + httpHost + "\r\n");

			if (getProp(entry, "http.Authorization") == null)
				sendAuthorization(entry, out);
			sendProxyAuthorization((String) getProp(entry, "http.proxy_user"), 
					(String) getProp(entry, "http.proxy_pass"), out);

		} else {
			String status = (String) getProp(entry, "http.redirect");
			if (status != null) {
				// send redir and return
				if (debugMode()) {
					debug(sResHash.getString("PARSER.HTTP.REDIRECT.INFO", status));
				}
				out.write("HTTP/1.1 " + HTTP_REDIR + "\r\n");
				if (!status.endsWith("\n"))
					status += "\r\n";
				out.write("Location: " + status);
				out.write("Content-Length: 0\r\n\r\n");
				out.flush();
				return;
			}

			status = (String) getProp(entry, "http.status");
			if (status == null)
				status = HTTP_OK;

			if (status.equalsIgnoreCase("OK"))
				status = HTTP_OK;

			if (status.equalsIgnoreCase("FORBIDDEN"))
				status = HTTP_FORBIDDEN;

			if (status.equalsIgnoreCase(HTTP_BADMESSAGE)) {
				status = HTTP_BADMESSAGE;
			}
			if (status.equalsIgnoreCase("NOT FOUND"))
				status = HTTP_FILE_NOT_FOUND;

			if (status == HTTP_FORBIDDEN) {
				httpAuthenticationRequest((String) getProp(entry, "http.auth-realm"));
				return;
			}
			if (status == HTTP_BADMESSAGE) {
				String str=null;
				str = "HTTP/1.1 400 Bad Message\r\n";
				str += "Message reason: Invalid Content-Length Value" + "\"\r\n";						
				out.write(str);
				out.flush();				
			}
			if (debugMode()) {
				debug(sResHash.getString("PARSER.HTTP.STATUS.INFO", status));
			}
			out.write("HTTP/1.1 " + status + "\r\n");
		}

		// Write all other http headers
		String[] names = getNames(entry);
		// String[] names = entry.getAttributeNames();
		for (int i = 0; i < names.length; i++) {
			if (names[i].startsWith("http.body"))
				continue;

			if (names[i].startsWith("http.qs."))
				continue;

			if (names[i].equalsIgnoreCase("http.url") && clientMode)
				continue;

			if (names[i].equalsIgnoreCase("http.remote_user"))
				continue;

			if (names[i].equalsIgnoreCase("http.remote_pass"))
				continue;

			if (names[i].equalsIgnoreCase("http.proxy_user"))
				continue;

			if (names[i].equalsIgnoreCase("http.proxy_pass"))
				continue;

			if (names[i].equalsIgnoreCase("http.status") && !clientMode)
				continue;

			if (names[i].equalsIgnoreCase("http.content-length"))
				continue;

			if (names[i].equalsIgnoreCase("http.method") && clientMode)
				continue;

			if (names[i].equalsIgnoreCase("http.Authorization") && !clientMode)
				continue;

			if (names[i].equalsIgnoreCase("http.host") && clientMode)
				continue;

			if (names[i].startsWith("http.")) {
				writeProperty(out, entry, names[i]);
			}
		}

		// Make sure content-type is present
		Object data = getProp(entry, "http.body");
		String ct = (String) getProp(entry, "http.content-type");
		String chset = getCharset(ct);
		if (ct == null && data != null) {
			ct = "text/plain; charset=" + chset;
			if (debugMode()) {
				debug(sResHash.getString("PARSER.HTTP.NO.CONTENT.TYPE.DEFAULTS", ct));
			}
			out.write("Content-Type: " + ct + "\r\n");
		}

		if (data == null) {
			out.write("Content-Length: 0\r\n\r\n");
		} else {
			// debug msg but only if its not too large...
			if ((data.toString().length() < 100000) && (debugMode())) {
				debug(sResHash.getString("PARSER.HTTP.DATA.IS", data));
			}
			if (data instanceof java.io.File) {
				out.write("Content-Length: " + ((File) data).length() + "\r\n\r\n");
				out.flush();
				byte[] buf = new byte[1024];
				FileInputStream fis = new FileInputStream((File) data);
				try {
					int rc;
					while ((rc = fis.read(buf)) != -1) {
						getOutputStream().write(buf, 0, rc);
					}
				} finally {
					fis.close();
				}
			} else {
				CharArrayWriter caw = new CharArrayWriter();
				writeDataObject(caw, data);
				// 12323 starts

				byte[] arr;
				if (data instanceof byte[]) {
					arr = (byte[]) data;
				} else {

					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					OutputStreamWriter osw;
					if (chset != null)
						osw = new OutputStreamWriter(bos, chset);
					else
						osw = new OutputStreamWriter(bos);

					caw.writeTo(osw);
					osw.flush();

					arr = bos.toByteArray();
				}

				// 12323 ends

				out.write("Content-Length: " + arr.length + "\r\n\r\n");
				out.flush();
				OutputStream os = getOutputStream();
				if (os != null) {
					os.write(arr);
				} else {
					if (debugMode()) {
						debug(sResHash.getString("PARSER.HTTP.UNSAFE.OUTPUT.CONVERSION"));
					}
					caw.writeTo(out);
				}
			}
		}

		out.flush();
		Trace.exitmax(this, "writeEntry");
	}

	/**
	 * Write HTTP headers with the specified name
	 * @param out
	 * @param entry - Entry containing information to be written
	 * @param name - name of the property or Attribute to write
	 * @throws IOException
	 */
	private void writeProperty(BufferedWriter out, Entry entry, String name) throws IOException {
		if (debugMode()) {
			debug(sResHash.getString("PARSER.HTTP.HEADER.INFO", name.substring(5)));
		}
		if (headersAsProperties) {
			out.write(name.substring(5) + ": " + entry.getProperty(name) + "\r\n");
		} else {
			Attribute a = entry.getAttribute(name);
			if (a == null)
				return; // Cannot happen?
			for (int i = 0; i < a.size(); i++) {
				out.write(name.substring(5) + ": " + a.getValue(i) + "\r\n");
			}
		}
	}

	/**
	 * Reads character data from the provided object and writes it to the
	 * buffer.
	 * 
	 * @param caw
	 *            CharArray Writer object, that writes to the buffer
	 * @param data
	 *            data to be read InputStream , Reader or Entry
	 * @throws Exception
	 *             if an I/O error occurs.
	 */
	private void writeDataObject(CharArrayWriter caw, Object data) throws Exception {
		Trace.entrymax(this, "writeDataObject", caw, data);
		if (data instanceof String) {
			caw.write((String) data, 0, ((String) data).length());
			Trace.exitmax(this, "writeDataObject");
			return;
		}

		if (data instanceof InputStream) {
			BufferedReader b = new BufferedReader(new InputStreamReader((InputStream) data));
			try {
				int ch;
				while ((ch = b.read()) != -1)
					caw.write(ch);
			} finally {
				b.close();
			}
			Trace.exitmax(this, "writeDataObject");
			return;
		}

		if (data instanceof Reader) {
			BufferedReader b = new BufferedReader((Reader) data);
			try {
				int ch;
				while ((ch = b.read()) != -1)
					caw.write(ch);
			} finally {
				b.close();
			}
			Trace.exitmax(this, "writeDataObject");
			return;
		}

		if (data instanceof Entry) {
			Entry e = (Entry) data;
			com.ibm.di.util.XMLUtils xm = new com.ibm.di.util.XMLUtils();
			String str = xm.entry2XML(e);
			caw.write(str, 0, str.length());
			xm = null;
			Trace.exitmax(this, "writeDataObject");
			return;
		}

		// Catch all
		String str = data.toString();
		caw.write(str, 0, str.length());
		Trace.exitmax(this, "writeDataObject");
	}

	/**
	 * Retrieves encoding style.
	 * 
	 * @param ct
	 *            String - http.content-type
	 * @return encoding
	 */
	private String getCharset(String ct) {
		String chset = charset;
		if (ct == null)
			return charset;

		if (ct.indexOf("charset=") != -1) {
			chset = ct.substring(ct.indexOf("charset=") + 8);

			// ensure we get only the charset parameter
			int semiColonIdx = chset.indexOf(';');
			if (semiColonIdx != -1) {
				chset = chset.substring(0, semiColonIdx);
			}

			if (chset.startsWith("\""))
				chset = chset.substring(1, chset.length() - 1);
			if (chset.endsWith(";"))
				chset = chset.substring(0, chset.length() - 1);
		}
		return chset;
	}

	/**
	 * Reads the http body and stores it into the buffer
	 * 
	 * @param length
	 *            amount to read
	 * @param inputEncoding
	 *            used character encoding
	 * @throws Exception
	 *             if an error occurs
	 */
	private void readBody(int length, String inputEncoding) throws Exception {
		Trace.entrymax(this, "readBody");
		InputStream is = getInputStream();
		Reader ir = getReader();
		if (debugMode()) {
			debug(sResHash.getString("PARSER.HTTP.HTTP.PARSER.READBODY.LENGTH", new Object[] { String.valueOf(length),
					String.valueOf(is != null), String.valueOf(ir != null) }));
		}
		// Read raw data into a byte array
		buffer = null;
		if (length != -1) {
			buffer = new byte[length];
			if (is != null) {
				for (int i = 0; i < length; i++)
					buffer[i] = (byte) is.read();
			} else if (ir != null) {
				if (debugMode()) {
					debug(sResHash.getString("PARSER.HTTP.ONLY.A.READER.AVAILABLE.THIS.MIGHT"));
				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(baos, inputEncoding));
				while (baos.size() < length) {
					bw.append((char) ir.read());
					bw.flush();
				}
				bw.close();
				buffer = baos.toByteArray();
			} else {
				throw new Exception(sResHash.getString("PARSER.HTTP.NO.INPUT.STREAM.READER.AVAILABLE"));
			}
		}
		if (debugMode()) {
			debug(sResHash.getString("PARSER.HTTP.HTTP.PARSER.READBODY.OK"));
		}
		Trace.exitmax(this, "readBody");
	}

	/**
	 * Parses http body from the {@link Entry}
	 * 
	 * @param entry
	 *            {@link Entry}
	 * @throws Exception
	 *             if parsing error occurs.
	 */
	private void parseBody(Entry entry) throws Exception {
		Trace.entrymax(this, "parseBody", entry);

		String ct = (String) getProp(entry, "http.content-type");
		String chset = getCharset(ct == null ? "text/plain" : ct.trim());
		if (debugMode()) {
			debug(sResHash.getString("PARSER.HTTP.HTTPPARSER.CONTENT.TYPE.CHARSET", chset));
		}

		int length = getMessageLength(entry);

		if (length == -1) {
			buffer = readChunks(chset);
			parseHTTPHeaders(null, entry);
		} else if (length == -2) {
			buffer = readUntilClose(chset);
		} else {
			readBody(length, chset);
		}
		setProp(entry, "http.bodyAsBytes", buffer);

		String data = null;
		Exception conversionError = null;
		try {
			if (chset != null)
				data = new String(buffer, chset);
			else
				data = new String(buffer);
			setProp(entry, "http.bodyAsString", data);
		} catch (Exception e) {
			conversionError = e;
		}

		if (ct == null || ct.startsWith("text/") || ct.startsWith("application/x-www-form-urlencoded") || ct.startsWith("application/soap+xml")) {
			if (conversionError != null)
				throw conversionError;
			setProp(entry, "http.body", new StringBuffer(data));
		} else {
			setProp(entry, "http.body", buffer);
		}

		Trace.exitmax(this, "parseBody");
	}

	/** Returns the message length as specified by headers.
	 * -1 meaans chunked.
	 * -2 means read until connection closed.
	 * @param entry
	 * @return The message length (as above)
	 * @throws Exception if the content-length cannot be parsed as an integer
	 */
	private int getMessageLength(Entry entry) throws Exception {

		if (headRequest)
			return 0;
	
		String transferEncoding = (String) getProp(entry, "http.Transfer-Encoding");
		if (transferEncoding != null) {
			if (debugMode())
				debug(sResHash.getString("PARSER.HTTP.TRANSFER.ENCODING.IS", transferEncoding));
			if (transferEncoding.toLowerCase(Locale.ENGLISH).indexOf("chunked") != -1)
				return -1;
		}

		String contentLength = (String) getProp(entry, "http.content-length");
		boolean flag=false; 
		int cLength=0;
		if (contentLength != null){
			try{
				cLength=Integer.parseInt(contentLength.trim());
			}catch (NumberFormatException e){
				
				System.out.println("Number format exception in the content-length has occurred.");
				setProp(entry, "http.status", HTTP_BADMESSAGE);
				flag=true;
				}
			//return Integer.parseInt(contentLength.trim());
			}
			if (flag==false) {
				//System.out.println("no exception. cLength="+cLength);
				return cLength;			
			}
		if (!clientMode)
			return 0;
		
		String responseCode = (String) getProp(entry, "http.responseCode");
		if (responseCode == null)
			return 0; // impossible
		if (responseCode.startsWith("1") || responseCode.equals("204") || responseCode.equals("304"))
			return 0; // No body for these responses

		return -2; // Read until end of input
	}

	/**
	 * Read until the server closes connection.
	 * @return the bytes read.
	 */
	private byte[] readUntilClose(String chset) throws Exception {
		// read until server closes connection
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		// Use the input stream if we have it
		InputStream is = getInputStream();
		if (is != null) {
			for (int ch = is.read(); ch != -1; ch = is.read()) {
				bos.write(ch);
			}
			bos.flush();				
			return bos.toByteArray();
		}
		
		// Fall back to the input reader
		Reader ir = getReader();
		if (ir != null) {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(bos, chset));
			for (int ch = ir.read(); ch != -1; ch = ir.read()) {
				bw.append((char)ch);
			}
			bw.close();
		}
		return bos.toByteArray();
	}

	/**
	 * Reads a piece of information and returns it as byte array.
	 * 
	 * @param inputEncoding
	 *            used character encoding
	 * 
	 * @return byte array.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private byte[] readChunks(String inputEncoding) throws Exception {

		Trace.entrymax(this, "readChunks");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		while (true) {

			String chunk = readLine();
			if (chunk == null) {
				Trace.exitmax(this, "readChunks");
				return bos.toByteArray();
			}
			if (debugMode()) {
				debug(sResHash.getString("PARSER.HTTP.CHUNK.SIZE.LINE", chunk));
			}
			chunk = chunk.trim();

			int size;

			if (chunk.indexOf(";") != -1)
				size = Integer.parseInt((chunk.substring(0, chunk.indexOf(";"))).trim(), 16);
			else
				size = Integer.parseInt(chunk, 16);
			if (debugMode()) {
				debug(sResHash.getString("PARSER.HTTP.CHUNK.SIZE", String.valueOf(size)));
			}

			if (size == 0)
				return bos.toByteArray();

			readBody(size, inputEncoding);
			bos.write(buffer);

			if (debugMode()) {
				debug(sResHash.getString("PARSER.HTTP.CHUNK.PART.IS", new Object[] { String.valueOf(buffer.length),
						String.valueOf(size) }));
			}

			// Chunks are followed by CRLF
			readLine();
		}
	}

	/**
	 * Parses the http header into an entry.
	 * 
	 * @param str
	 *            line
	 * @param e
	 *            entry
	 * @return entry with the parsed headers
	 * @throws Exception
	 *             if an error occurs.
	 */
	private Entry parseHTTPHeaders(String str, Entry e) throws Exception {
		Trace.entrymax(this, "parseHTTPHeaders", str, e);
		String s=null;
		boolean crlf=false;
		String temp[]= {null};

		/*
		 * Parse request header (in case GET/POST)
		 */
		if (str != null) {
			StringTokenizer st = new StringTokenizer(str, " ?&");
			if (str.contains("%0D%0A")){
				crlf=true;
			        temp=str.split(" ", 3);
			}
			else{
				s = st.nextToken();
                        	s = st.nextToken();
			}
			try {
				if(!crlf)
					setProp(e, "http.base", URLDecoder.decode(s, "UTF-8"));
				else
					setProp(e, "http.base", URLDecoder.decode(temp[1], "UTF-8"));
			} catch (Exception ex) {
				if(!crlf)
					setProp(e, "http.base", URLDecoder.decode(s, "UTF-8"));
				else
					setProp(e, "http.base", URLDecoder.decode(temp[1], "UTF-8"));
			}
			addParameters(st, e);
		}

		/*
		 * Parse headers
		 */
		String headerName = null;
		StringBuilder headerValue = new StringBuilder();

		while ((str = readLine()) != null && str.trim().length() > 0) {
			if (debugMode()) {
				debug(sResHash.getString("PARSER.HTTP.PARSE.HEADERS", str));
			}

			if (!isHeaderFolded(str)) {
				if (headerName != null) {
					addHeader(e, headerName, headerValue);
					headerName = null;
					headerValue.setLength(0);
				}
				int colonIndex = str.indexOf(HTTP_HEADER_SEPARATOR);
				if (colonIndex <= 0)
					continue; // TODO: Maybe log a problem?
				headerName = "http." + str.substring(0, colonIndex);
				headerValue.append(str.substring(colonIndex + 1).trim());
			} else {
				headerValue.append(" ");
				headerValue.append(str.trim());
			}
		}

		if (headerName != null)
			addHeader(e, headerName, headerValue);

		/*
		 * Check for authentication header and decode
		 */
		if (!clientMode)
			parseAuthentication(e);
		Trace.exitmax(this, "parseHTTPHeaders", e);
		return e;
	}

	/**
	 * Add a header to the Entry
	 */
	private void addHeader(Entry e, String headerName, StringBuilder headerValue) {
		if (headersAsProperties) {
			Object oldHeaderValue = e.getProperty(headerName);
			if (oldHeaderValue != null) {
				headerValue.insert(0, ", ");
				headerValue.insert(0, oldHeaderValue);
			}
			e.setProperty(headerName, headerValue.toString());
		} else {
			e.addAttributeValue(headerName, headerValue.toString());
		}
	}

	/**
	 * Check if the provided header line is folded(as defined in the HTTP 1.1
	 * specification).
	 * 
	 * @param headerLine
	 *            to be checked
	 * @return True if folded, else false.
	 */
	private boolean isHeaderFolded(String headerLine) {
		return headerLine.charAt(0) == ' ' || headerLine.charAt(0) == '\t';
	}

	/**
	 * Decode the username and password specified in the Authorization header of
	 * the request HTTP message. The character encoding used is 'iso-8859-1'.
	 * 
	 * @param entry
	 *            entry containing the authorization header.
	 * @throws Exception
	 *             if the charset conversion failed
	 */
	public void parseAuthentication(Entry entry) throws Exception {
		Trace.entrymax(this, "parseAuthentication", entry);
		String auth = (String) getProp(entry, "http.authorization");

		if (auth == null) {
			return;
		}

		auth = auth.trim();
		if (!UserFunctions.startsWithIC(auth, "Basic "))
			return;
		auth = auth.substring(auth.indexOf(" ") + 1);
		auth = UserFunctions.base64Decode(auth, ENCODING_LATIN_1);

		int ix = auth.indexOf(":");
		if (ix < 0)
			return;
		setProp(entry, "http.remote_user", auth.substring(0, ix));
		setProp(entry, "http.remote_pass", auth.substring(ix + 1));
		Trace.exitmax(this, "parseAuthentication");
	}

	/**
	 * Extracts key/value pair from the provided StringTokenizer object and adds
	 * it to Entry's attributes.
	 * 
	 * @param st
	 *            StringTokenizer
	 * @param e
	 *            entry
	 * @throws UnsupportedEncodingException
	 *             if an error occurs.
	 */
	private void addParameters(StringTokenizer st, Entry e) throws UnsupportedEncodingException {
		Trace.entrymax(this, "addParameters", st, e);
		String s;
		int ix;

		while (st.hasMoreTokens()) {
			s = st.nextToken();
			if (s.indexOf("=") != -1) {
				ix = s.indexOf("=");
				String key = s.substring(0, ix);
				String val = s.substring(ix + 1);
				val=val.replaceAll("%0D%0A","%5Cn%5Cr");
				try {
					key = URLDecoder.decode(key, "UTF-8");
					val = URLDecoder.decode(val, "UTF-8");
				} catch (Exception ex) {
					SystemFunctions.doNothing();
				}

				key = "http.qs." + key;

				if (headersAsProperties) {
					if (getProp(e, key) != null)
						setProp(e, key, getProp(e, key) + ";" + val);
					else
						setProp(e, key, val);
				} else {
					e.addAttributeValue(key, val);
				}
			}
		}
		Trace.exitmax(this, "addParameters");
	}

	/**
	 * Sets a property/attribute's value of specified entry. The result depends
	 * on whether <code>headersAsProperties</code> parameter is checked.
	 * 
	 * @param e
	 *            the entry
	 * @param prop
	 *            The name of the property/attribute.
	 * @param value
	 *            The value of the property/attribute.
	 */
	public void setProp(Entry e, String prop, Object value) {
		if (headersAsProperties)
			e.setProperty(prop, value);
		else
			e.setAttribute(prop, value);
	}

	/**
	 * Return a property/attribute value of specified entry. The result depends
	 * on whether <code>headersAsProperties</code> parameter is checked.
	 * 
	 * @param e
	 *            the entry
	 * @param prop
	 *            The name of the property/attribute.
	 * @return The property/attribute's value or null if such does not exists.
	 */
	public Object getProp(Entry e, String prop) {
		if (headersAsProperties)
			return e.getProperty(prop);
		else
			return e.getObject(prop);
	}

	/**
	 * Returns property/attribute names in specified entry. The result depends
	 * on whether <code>headersAsProperties</code> parameter is checked.
	 * 
	 * @param e
	 *            the entry
	 * @return Array of strings with property/attribute names
	 */
	public String[] getNames(Entry e) {
		if (headersAsProperties)
			return e.getPropertyNames();
		else
			return e.getAttributeNames();
	}

	/**
	 * Send a HTTP response message with error code 401 (Forbidden) into the
	 * current output writer.
	 * 
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	public void httpForbidden() throws IOException {
		if (getWriter() == null)
			return;
		getWriter().write("HTTP/1.1 " + HTTP_FORBIDDEN + "\r\n");
		getWriter().write("Content-Length: 0\r\n\r\n");
		getWriter().flush();
	}

	/**
	 * Send a Forbidden response requesting authentication.
	 * <p>
	 * This method sends response message with error code 401 (Forbidden) when a
	 * request is made to protected resources. The response message includes a
	 * WWW-Authenticate header specifying a scheme and a realm.
	 * 
	 * @param realm
	 *            The <code>realm</code> is string that defines a protection
	 *            space (a set of protected resources) within the same host.
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	public void httpAuthenticationRequest(String realm) throws IOException {
		String str = null;
		String rlm = realm;
		rlm = "IBM-Directory-Integrator";
		str = "HTTP/1.1 401 Forbidden\r\n";
		str += "WWW-Authenticate: Basic realm=\"" + rlm + "\"\r\n";
		getWriter().write(str);
		getWriter().write("Content-Length: 0\r\n\r\n");
		getWriter().flush();
	}

	/**
	 * Send client authorization.
	 * 
	 * @param entry
	 *            entry containing HTTP message attributes.
	 * @param out
	 *            the output writer.
	 * @throws Exception
	 *             If an I/O error occurs or if base64 encoding fails.
	 */
	public void sendAuthorization(Entry entry, BufferedWriter out) throws Exception {
		Trace.entrymax(this, "sendAuthorization");
		String user = (String) getProp(entry, "http.remote_user");
		String pass = (String) getProp(entry, "http.remote_pass");
		if (debugMode()) {
			debug(sResHash.getString("PARSER.HTTP.SENDAUTH.HTTPREMOTE.USER.IS", new Object[] { user, "*****" }));
		}

		if (user == null && pass == null) {
			String httpUrl = (String) getProp(entry, "http.url");
			if (httpUrl == null)
				return;
			if (debugMode()) {
				debug(sResHash.getString("PARSER.HTTP.SENDAUTH.TRY.DECODE.URL", httpUrl));
			}
			URL url;
			try {
				url= new URL(httpUrl);
			} catch (Exception e) {
				return;
			}
			String info = url.getUserInfo();

			if (info == null)
				return;
			if (debugMode()) {
				debug(sResHash.getString("PARSER.HTTP.BREAK.UP.INFO", info));
			}

			int atIndex = info.indexOf(":");
			if (atIndex != -1) {
				user = info.substring(0, atIndex);
				pass = info.substring(atIndex + 1);
			} else {
				user = info;
			}
		}
		if (debugMode()) {
			debug(sResHash.getString("PARSER.HTTP.USER.PASS", new Object[] { user, "*****" }));
		}

		if (user == null && pass == null)
			return;

		if (user == null)
			user = "";
		if (pass == null)
			pass = "";

		String auth = "Basic " + b64Encode(user + ":" + pass);
		if (debugMode()) {
			debug(sResHash.getString("PARSER.HTTP.SENDAUTH.RESULT.AUTH", auth));
		}

		out.write("Authorization: " + auth + "\r\n");
		Trace.exitmax(this, "sendAuthorization");
	}

	public void sendProxyAuthorization(String user, String pass, BufferedWriter out) throws Exception {
		Trace.entrymax(this, "sendProxyAuthorization");
		if (user == null)
			return;

		if (pass == null)
			pass = "";

		if (user.isEmpty() && pass.isEmpty())
			return;

		String auth = "Basic " + b64Encode(user + ":" + pass);

		out.write("Proxy-Authorization: " + auth + "\r\n");
		Trace.exitmax(this, "sendProxyAuthorization");
	}

	/**
	 * Encodes a String using base64.
	 * 
	 * @param auth
	 *            the header value
	 * @return String
	 * @throws UnsupportedEncodingException
	 *             if an error occurs.
	 * 
	 */
	private String b64Encode(String auth) throws UnsupportedEncodingException {

		return UserFunctions.base64Encode(auth, ENCODING_LATIN_1);
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "2.1-di7.1.1 %I%, 20%E%";
	}

	/**
	 * Resets Parser's parameters.
	 */
	private void resetProperties() {
		headersAsProperties = false;
		clientMode = false;
		charset = ENCODING_LATIN_1;
		buffer = null;
	}
}
