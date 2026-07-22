/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser.xml;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to work around problems like multi-rooted xml documents
 * with more than one declarations. If this class is used to omit XML
 * declarations then it will skip ANY xml declaration from the stream. This
 * class handles the BOM char if the reader this class is initialized with is
 * properly decoded.
 * 
 * @since 7.0
 */
public class TDIReaderProxy extends Reader {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The name of the dummy root to use.
	 */
	private static final String DUMMY_ROOT_NAME = XMLParser2.DUMMY_ROOT_NAME;

	/**
	 * The reader we are using to read data from the stream.
	 */
	private Reader in = null;

	/**
	 * Used to hold the dummy root tag and the first xml declaration (if any).
	 */
	private StringBuilder startRoot = new StringBuilder();

	/**
	 * Shows the next char position in the startRoot string.
	 */

	private int startRead = 0;

	/**
	 * Shows the length of the startRoot string.
	 */
	private int startLength = 0;

	/**
	 * Used to hold the dummy end root tag.
	 */

	private StringBuilder endRoot = new StringBuilder("</" + DUMMY_ROOT_NAME
			+ ">\n");

	/**
	 * Used to hold the dummy end root tag.
	 */
	private int endRead = 0;

	/**
	 * Shows the length of the endRoot string.
	 */
	private int endLength = endRoot.length();

	/**
	 * This is a wrapper of the StreamReader. This wrapper will handle
	 * multi-rooted xml data coming from the stream by putting a dummy root tag
	 * 
	 * @param reader
	 *            - the reader to read the data from
	 * @param omitXML
	 *            - whether to skip any xml declaration except the first one
	 * @throws IOException
	 *             - in case read error occurs
	 */
	public TDIReaderProxy(Reader reader, boolean omitXML) throws IOException {

		// here we will hold the data before putting it in the startRoot object
		// for reading
		StringBuilder sb = new StringBuilder(100);

		XMLInputStreamDecoder decoder = null;

		// shows weather the data from the sb should be put before or after the
		// dummy root. before in case a xml declaration is found and after if
		// the xml document does not have any declaration and the data read is
		// just some opening tag, that should become a child of the dummy root
		boolean aboveDummyRoot = true;

		if (reader instanceof XMLInputStreamDecoder) {
			decoder = (XMLInputStreamDecoder) reader;
			// optimization code to prevent reading of the xml declaration again
		}

		if (decoder != null && decoder.getNoXMLDeclFlag()
				&& decoder.getXMLDeclaration() != null) {

			// If we are here then the received reader is a known one and it was
			// created with a specific flag that tells the class not to provide
			// the xml declaration for re-reading if it was already read. If it
			// was read then the call getXmlDeclaration() method will return a
			// string different from null.
			aboveDummyRoot = true;
			sb.append(decoder.getXMLDeclaration());

		} else {
			int c = -1;

			// handle the first xml declaration
			while ((c = reader.read()) != -1) {
				if (c == '<') {

					// keeps the first 6 chars
					StringBuilder xmlDecl = new StringBuilder(6);
					xmlDecl.append((char) '<');

					int temp = -1;
					for (int i = 0; i < 5 && (temp = reader.read()) != -1; i++) {
						xmlDecl.append((char) temp);
					}

					sb.append(xmlDecl.toString());

					// check if the first 6 chars are a xml declaration
					Pattern patern = Pattern.compile("<\\?[xX][mM][lL]\\s");
					Matcher matcher = patern.matcher(xmlDecl);

					// yes they are
					if (matcher.find()) {
						while ((c = reader.read()) != '>' && c != -1) {
							sb.append((char) c);
						}
						if (c != -1) {
							sb.append((char) '>');
						}
						break;
					}
					// no they are not
					else {
						aboveDummyRoot = false;
						break;
					}
				}
				// skip the BOM
				else if (c != 0xfeff) {
					sb.append((char) c);
				}
			}
		}

		String startTag = "<" + DUMMY_ROOT_NAME + ">";

		if (aboveDummyRoot) {
			startRoot.append(sb.toString());
			startRoot.append("\n" + startTag);
		} else {
			startRoot.append(startTag);
			startRoot.append(sb.toString());
		}

		startLength = startRoot.length();

		if (omitXML) {
			// use the buffered input stream and skip any xml declaration in it
			in = new SkipXMLDeclReader(reader);
		} else {
			// use the default input stream
			in = reader;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int read() throws IOException {

		int c = -1;

		if (startRead < startLength) {
			c = startRoot.charAt(startRead++);
			return c;
		}

		c = in.read();

		if (c != -1) {
			return c;
		}

		if (endRead < endLength) {
			c = endRoot.charAt(endRead++);
			return c;
		}

		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	public int read(char[] cbuf, int off, int len) throws IOException {

		if ((off < 0) || (off > cbuf.length) || (len < 0)
				|| ((off + len) > cbuf.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}

		int readLen = -1;

		int c = read();

		if (c != -1) {
			cbuf[off++] = (char) c;
			readLen = 1;
		} else {
			return -1;
		}

		while (readLen < len - 1 && (c = read()) != -1) {
			cbuf[off++] = (char) c;
			readLen++;
		}
		if (readLen < len && (c = read()) != -1) {
			cbuf[off] = (char) c;
			readLen++;
		}

		return readLen;
	}

	/**
	 * Used to buffer data and remove any xml declarations in it.
	 * 
	 * 
	 * @since 7.0
	 */
	private static class SkipXMLDeclReader extends Reader {

		/**
		 * The buffer.
		 */
		private int[] buffer = null;

		/**
		 * The default size for the buffer
		 */
		private static int defaultSize = 32768;

		/**
		 * Pointer to the last char of the buffer.
		 */
		private int bufferEndPos = 0;

		/**
		 * Pointer to the next char in the buffer.
		 */
		private int bufferNextCharPos = 0;

		/**
		 * Shows whether the stream is exhausted.
		 */

		private boolean exhausted = false;

		/**
		 * Shows whether the buffer is filled for a first time.
		 */
		private boolean firstFill = true;

		/**
		 * The Reader we are reading from.
		 */
		private Reader in = null;

		/**
		 * Constructs the buffered input stream. The default buffer size is
		 * 32768 ~ 128 KB of memory.
		 * 
		 * @param reader
		 *            - the input stream we are buffering, must not be null
		 * @throws IOException
		 *             - in case a read error occurs
		 */
		public SkipXMLDeclReader(Reader reader) throws IOException {
			this(reader, defaultSize);
		}

		/**
		 * Constructs the buffered input stream.
		 * 
		 * @param reader
		 *            - the input stream we are buffering, must not be null
		 * @param bufferSize
		 *            - the size of the buffer, at least 16
		 * @throws IOException
		 *             - in case a read error occurs
		 */
		SkipXMLDeclReader(Reader reader, int bufferSize) throws IOException {
			in = reader;
			buffer = new int[bufferSize];
			firstFill = true;
			refillTheBuffer();
		}

		/**
		 * {@inheritDoc}
		 * 
		 */
		public int read() throws IOException {
			// once the stream have ended no data could be read
			if (bufferEndPos < bufferNextCharPos && !exhausted) {
				refillTheBuffer();
			}

			// read the data from the buffer or just return -1 if no data left
			if (bufferEndPos >= bufferNextCharPos) {
				return buffer[bufferNextCharPos++];
			} else {
				return -1;
			}
		}

		/**
		 * Fills the buffer in. Sets the pointers. Removes xml declaration while
		 * filling the buffer.
		 * 
		 * @throws IOException
		 *             - in case a read error occurs
		 */
		protected void refillTheBuffer() throws IOException {

			// just a precaution
			if (exhausted)
				return;

			int c = in.read();

			if (c == -1) {
				// provide the last six chars for reading
				bufferNextCharPos = buffer.length - 6;
				bufferEndPos = buffer.length - 1;
				exhausted = true;
				return;

			} else {
				// set to default pointers
				bufferNextCharPos = 0;
				bufferEndPos = 0;
			}

			// first element is the new character to set the second is the
			// position to move the last char pointer to (either 0 or 5)
			int[] xmlDeclRes = null;
			// used for handling last six chars
			int tempEndPos = 0;
			// shows weather the first six chars were transferred
			boolean firstSixDone = false;

			while (bufferEndPos + tempEndPos < buffer.length && c != -1) {

				if (!firstFill && !firstSixDone && bufferEndPos < 6) {
					// transfer the last 6 chars to front

					buffer[bufferEndPos] = buffer[buffer.length - 6
							+ bufferEndPos];
					bufferEndPos++;
					if (bufferEndPos == 6)
						firstSixDone = true;

				} else if (buffer.length - 6 == bufferEndPos) {
					// fill the last six characters and don't move the end
					// pointer. This is done in case the xml declaration is
					// split by the buffer

					xmlDeclRes = skipXMLDecl(c, bufferEndPos + tempEndPos);
					if (xmlDeclRes[1] > tempEndPos) {
						// we have found an xml declaration that starts before
						// the end pointer, so fix the pointer to rewrite those
						// characters
						int endPosOffset = xmlDeclRes[1] - tempEndPos;
						bufferEndPos -= endPosOffset;
						tempEndPos = 0;

						if ((c = xmlDeclRes[0]) == -1)
							break;

						buffer[bufferEndPos++] = c;

					} else {
						// no xml declaration was found or the start of the
						// declaration starts after the end pointer
						tempEndPos -= xmlDeclRes[1];

						if ((c = xmlDeclRes[0]) == -1)
							break;

						buffer[bufferEndPos + tempEndPos++] = c;

					}

					// do not read another char if we do not have an empty cell
					// to put it in
					if (bufferEndPos + tempEndPos != buffer.length)
						c = in.read();

				} else {
					// middle characters

					xmlDeclRes = skipXMLDecl(c, bufferEndPos);
					bufferEndPos -= xmlDeclRes[1];
					if ((c = xmlDeclRes[0]) == -1)
						break;

					buffer[bufferEndPos++] = c;
					c = in.read();
				}

			}

			// after exiting the loop the end pointer points to the next cell so
			// bring it back to the last written char's cell
			bufferEndPos--;

			if (c == -1) {

				if (tempEndPos > 0)
					// if the end of the stream was found when dealing with the
					// last 6 chars the provide those for reading too
					bufferEndPos += tempEndPos;

				// the loop exited because the end of the stream was reached
				// so clear the rest of the buffer just in case
				for (int i = bufferEndPos + 1; i < buffer.length; i++) {
					buffer[i] = -1;
				}

				// remember that the stream ended
				exhausted = true;
			}

			if (firstFill)
				// we do not deal with the first six chars on the first fill,
				// but we do after the first one
				firstFill = false;

		}

		/**
		 * Checks the last five chars and the current one for the pattern <code>"<\?[xX][mM][lL]\s"</code>
		 * if found the the last pointer should move five steps back to rewrite
		 * the those elements.
		 * 
		 * @param c
		 *            - the last read char
		 * @param endPos
		 *            - the position of the buffer the last read char should be
		 *            put
		 * @return - an array of two elements (1st - the new character read if
		 *         an xml declaration was found; 2nd - the offset from the
		 *         current end pointer, either 0 or 5)
		 * @throws IOException
		 *             - in case a read error occurs
		 */
		private int[] skipXMLDecl(int c, int endPos) throws IOException {
			int retEndPosOffset = 0;
			// check if we have enough elements to start checking also check
			// for the key features of the xml declaration
			if ((endPos > 5 && buffer[endPos - 5] == '<')
					&& (buffer[endPos - 4] == '?' && c == ' ')
					&& (buffer[endPos - 3] == 'x' || buffer[endPos - 3] == 'X')
					&& (buffer[endPos - 2] == 'm' || buffer[endPos - 2] == 'M')
					&& (buffer[endPos - 1] == 'l' || buffer[endPos - 1] == 'L')) {

				// fix the buffer end position
				retEndPosOffset = 5;

				// if we are here then the pattern "<\?[xX][mM][lL]\s"
				// was found so skip all the chars before the next '>'
				// char
				do {
					c = in.read();
					// do nothing, we are looking for '>'
				} while (c != -1 && c != '>');

				if (c != -1 && (c = in.read()) == -1) {
					// the character after the '>' is telling us that
					// we have reached the end of the stream so let the
					// rest of the calling method do its magic
					return new int[] { -1, retEndPosOffset };
				}
			}
			return new int[] { c, retEndPosOffset };
		}

		/**
		 * {@inheritDoc}
		 */
		public void close() throws IOException {
			in.close();
		}

		/**
		 * {@inheritDoc}
		 */
		public int read(char[] cbuf, int off, int len) throws IOException {
			// this method is not used directly
			return 0;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void close() throws IOException {
		in.close();
	}

}
