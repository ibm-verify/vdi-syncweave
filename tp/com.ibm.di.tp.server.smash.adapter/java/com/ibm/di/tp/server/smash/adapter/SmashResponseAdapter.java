/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * (C) Copyright IBM Corporation. 2009, 2011
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 *
 * @version     %I%, %G%
 * @owner
 * @history
 */
package com.ibm.di.tp.server.smash.adapter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import zero.core.context.GlobalContext;

/**
 * This class adapts the response data in the sMash {@link GlobalContext} to the
 * Servlet API {@link ServletResponse}. <br>
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class SmashResponseAdapter implements HttpServletResponse {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private final HttpServlet servlet;
	private final OutputStreamDelegate sos = new OutputStreamDelegate((OutputStream) GlobalContext.zget("/request/outputStream"));

	private static final String DEFAULT_ENCODING = "ISO-8859-1";
	private static final String DEFAULT_MIME_TYPE = "text/plain";

	private String charEnc;
	private String mimeType;
	private Locale locale = Locale.getDefault();
	private PrintWriter writer;

	public SmashResponseAdapter(HttpServlet servlet) {
		this.servlet = servlet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie
	 * )
	 */
	public void addCookie(Cookie cookie) {
		zero.core.cookie.Cookie zcookie = new zero.core.cookie.Cookie(cookie.getName(), cookie.getValue(), cookie.getVersion(),
				cookie.getDomain(), cookie.getPath(), cookie.getComment(), cookie.getMaxAge(), cookie.getSecure(), false);
		GlobalContext.zpost("/request/cookies/out", zcookie);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String,
	 * long)
	 */
	public void addDateHeader(String name, long date) {
		addHeader(name, Long.toString(date));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String,
	 * java.lang.String)
	 */
	public void addHeader(String name, String value) {
		String zHeader = GlobalContext.zget("/request/headers/out/" + name);
		if (zHeader != null) {
			zHeader += "," + value;
		} else {
			zHeader = value;
		}
		GlobalContext.zput("/request/headers/out/" + name, zHeader);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String,
	 * int)
	 */
	public void addIntHeader(String name, int value) {
		addHeader(name, Integer.toString(value));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	public boolean containsHeader(String name) {
		return GlobalContext.zget("/request/headers/out/" + name, null) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String
	 * )
	 */
	public String encodeRedirectURL(String url) {
		return encodeURL(url);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String
	 * )
	 */
	public String encodeRedirectUrl(String url) {
		return encodeURL(url);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	public String encodeURL(String url) {
		try {
			return URLEncoder.encode(url, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			// Fallback to original URL if encoding fails
			return url;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	public String encodeUrl(String url) {
		return encodeURL(url);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	public void sendError(int sc) throws IOException {
		if (isCommitted()) {
			throw new IllegalStateException();
		}

		GlobalContext.zput("/request/status", sc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#sendError(int,
	 * java.lang.String)
	 */
	public void sendError(int sc, String msg) throws IOException {
		sendError(sc);
		GlobalContext.zput("/request/error/message", msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(String location) throws IOException {
		servlet.getServletContext().log("HttpServletResponse.setStatus(\"" + location + "\") is implemented!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String,
	 * long)
	 */
	public void setDateHeader(String name, long date) {
		setHeader(name, Long.toString(date));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String,
	 * java.lang.String)
	 */
	public void setHeader(String name, String value) {
		GlobalContext.zput("/request/headers/out/" + name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String,
	 * int)
	 */
	public void setIntHeader(String name, int value) {
		setHeader(name, Integer.toString(value));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	public void setStatus(int sc) {
		GlobalContext.zput("/request/status", sc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int,
	 * java.lang.String)
	 */
	public void setStatus(int sc, String sm) {
		servlet.getServletContext().log("HttpServletResponse.setStatus(" + sc + ", \"" + sm + "\") is not fully implemented!");
		GlobalContext.zput("/request/status", sc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	public void flushBuffer() throws IOException {
		if (writer != null) {
			writer.flush();
		} else {
			sos.flush();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	public int getBufferSize() {
		return sos.getSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		return charEnc == null ? DEFAULT_ENCODING : charEnc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponse#getContentType()
	 */
	public String getContentType() {
		return GlobalContext.zget("/request/headers/out/Content-Type", null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	public Locale getLocale() {
		return locale;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		return sos;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponse#getWriter()
	 */
	public PrintWriter getWriter() throws IOException {

		if (writer == null && getContentType() == null) {
			setContentType(DEFAULT_MIME_TYPE, DEFAULT_ENCODING);
		}

		return writer == null ? (writer = new PrintWriter(new OutputStreamWriter(sos, getCharacterEncoding()), false)) : writer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	public boolean isCommitted() {
		return sos.isFlushed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponse#reset()
	 */
	public void reset() {
		resetBuffer();
		setStatus(200);

		List<String> zHeaders = GlobalContext.zlist("/request/headers/out", true);

		for (String zHeader : zHeaders) {
			GlobalContext.zdelete(zHeader);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	public void resetBuffer() {
		checkCommitted();
		sos.reset();
	}

	private void checkCommitted() {
		if (isCommitted()) {
			throw new IllegalStateException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	public void setBufferSize(int size) {
		checkCommitted();
		sos.setSize(size);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String charset) {
		String mime = mimeType == null ? DEFAULT_MIME_TYPE : mimeType;
		setContentType(mime, charset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	public void setContentLength(int len) {
		setIntHeader("Content-Length", len);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	public void setContentType(String type) {
		// get the character encoding of the request
		if (type != null && writer == null) {
			String mime = null;
			String enc = null;

			int start = 0;
			int end = -1;

			// get the mime
			end = type.indexOf(";");
			end = end == -1 ? type.length() : end;
			mime = type.substring(start, end).trim();

			// get the enc
			start = type.indexOf("charset=", end);
			enc = start == -1 ? null : type.substring(start + 8).trim();

			setContentType(mime, enc);
		}
	}

	private void setContentType(String mime, String enc) {
		String type = mime;
		mimeType = mime;

		if (enc != null) {
			charEnc = enc;
			type += "; charset=" + enc;
		} else if (charEnc != null) {
			type += "; charset=" + charEnc;
		}

		setHeader("Content-Type", type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale loc) {
		setCharacterEncoding("UTF-8");
	}

	private static final class ResizableBufferedOutputStream extends BufferedOutputStream {

		private boolean flushed;

		/**
		 * @param out
		 * @param size
		 */
		public ResizableBufferedOutputStream(OutputStream out, int size) {
			super(out, size);
		}

		public void reset() {
			count = 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.BufferedOutputStream#write(byte[], int, int)
		 */
		@Override
		public synchronized void write(byte[] b, int off, int len) throws IOException {
			if (len >= buf.length || len > buf.length - count) {
				flushed = true;
			}
			super.write(b, off, len);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.BufferedOutputStream#write(int)
		 */
		@Override
		public synchronized void write(int b) throws IOException {
			if (count >= buf.length) {
				flushed = true;
			}
			super.write(b);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.BufferedOutputStream#flush()
		 */
		@Override
		public synchronized void flush() throws IOException {
			flushed = true;
			super.flush();
		}

		private boolean isFlushed() {
			return flushed;
		}
	}

	private static final class OutputStreamDelegate extends ServletOutputStream {

		private static final int SIZE = 8192;

		private final OutputStream zstream;

		private ResizableBufferedOutputStream buffer;

		private int bSize = SIZE;

		public OutputStreamDelegate(OutputStream delegate) {
			zstream = delegate;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.OutputStream#write(int)
		 */
		@Override
		public void write(int b) throws IOException {
			getBuffer().write(b);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.OutputStream#write(byte[], int, int)
		 */
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			getBuffer().write(b, off, len);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.OutputStream#flush()
		 */
		@Override
		public void flush() throws IOException {
			getBuffer().flush();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.OutputStream#close()
		 */
		@Override
		public void close() throws IOException {
			getBuffer().close();
		}

		private int getSize() {
			return bSize;
		}

		private void setSize(int size) {
			if (buffer != null) {
				throw new IllegalStateException();
			}

			bSize = size;
		}

		private ResizableBufferedOutputStream getBuffer() {
			if (buffer == null) {
				buffer = new ResizableBufferedOutputStream(zstream, bSize);
			}

			return buffer;
		}

		private void reset() {
			getBuffer().reset();
		}

		private boolean isFlushed() {
			return buffer != null && buffer.isFlushed();
		}
	}
}
