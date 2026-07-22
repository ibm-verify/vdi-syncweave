/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class checks the provided input stream for a BOM and if it is able to
 * find it the information this mark carries is interpreted and the encoding is
 * discovered. If no BOM is found a check for a xml declaration is done. If a
 * XML exists then it is checked for an encoding attribute. If that attribute is
 * found its values is taken and the stream is decoded using that encoding. If
 * none of above is found then the InputStream is decoded using the XMLParser2's
 * default encoding. If it is set to null then the system default encoding is
 * used.
 * 
 * @since 7.0
 */
public class XMLInputStreamDecoder extends Reader {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Text providing information that BOM is found.
	 */
	private static final String MSG_BOM_FOUND = "XML.PARSER.2.DECODER.BOM.FOUND";
	/**
	 * Text providing information that XML encoding is found.
	 */
	private static final String MSG_XML_ENC_FOUND = "XML.PARSER.2.DECODER.XML.ENC.FOUND";
	/**
	 * Text providing information no encoding is found.
	 */
	private static final String MSG_ENC_NOT_FOUND = "XML.PARSER.2.DECODER.ENC.NOT.FOUND";

	/**
	 * Encoding style - default value UTF-8
	 */
	private String encoding = XMLParser2.DEFAULT_ENCODING;

	/**
	 * The input stream to check for BOM or XML declaration and to decode.
	 */
	private InputStream is = null;

	/**
	 * Reads character stream with the provided encoding.
	 */
	private Reader in = null;

	/**
	 * This represents the longest xml declaration + the BOM char (at least 8)
	 */

	private int prefixLength = 8;

	/**
	 * Holds the lead byte sequence.
	 */
	private byte[] prefix = null;

	/**
	 * The total number of bytes read into the buffer, or -1 is there is no more
	 * data because the end of the stream has been reached.
	 */
	private int size = 0;

	/**
	 * BOM length.
	 */
	private int start = 0;

	/**
	 * Holds XML declaration.
	 */
	private String xmlDeclaration = null;

	/**
	 * If true the xml declaration will not be readable if this class have
	 * already read it to find out the encoding. If false then the declaration
	 * will be readable
	 */
	private boolean noXMLDecl = false;

	/**
	 * Message that holds information for the encoding style found.
	 */
	private String msgStatus = MSG_ENC_NOT_FOUND;

	/**
	 * Constructor.
	 * 
	 * @param is
	 *            the input stream to check for BOM or XML declaration and to
	 *            decode.
	 * @param noXMLDecl
	 *            if true the xml declaration will not be readable if this class
	 *            have already read it to find out the encoding. If false then
	 *            the declaration will be readable. If the encoding was not
	 *            searched in a xml Declaration but is found using a BOM then
	 *            this parameter will be ignored and the getXMLDeclaration will
	 *            return null.
	 * @throws IOException
	 *             if a reading error occurs or unsupported exception is used in
	 *             the xml declaration
	 */
	XMLInputStreamDecoder(InputStream is, boolean noXMLDecl) throws IOException {
		this.is = is;
		prefix = new byte[prefixLength];
		this.noXMLDecl = noXMLDecl;
		init();

	}

	/**
	 * {@inheritDoc}
	 */
	public int read() throws IOException {
		return in.read();
	}

	/**
	 * Retrieves encoding type.
	 * 
	 * @return The character set name that was discovered from the input stream.
	 */
	String getEncoding() {
		return encoding;
	}

	/**
	 * Retrieves XML declaration.
	 * 
	 * @return the XML Declaration string if it was read from the stream or null
	 *         if it wasn't
	 */
	String getXMLDeclaration() {
		return xmlDeclaration;
	}

	/**
	 * Checks if XML declaration can be re-read.
	 * 
	 * @return shows weather the Reader will be able to re-read the xml
	 *         Declaration if it was already read. This flag is set on the class
	 *         initialization.
	 */
	boolean getNoXMLDeclFlag() {
		return noXMLDecl;
	}

	/**
	 * Retrieves message key.
	 * 
	 * @return the message key that should be print out to the log.
	 */
	String getStatus() {
		return msgStatus;
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
		return in.read(cbuf, off, len);
	}

	/**
	 * Initializes encoding , buffer size and stream reader.
	 * 
	 * @throws IOException
	 *             if I/O error occurs
	 */
	private void init() throws IOException {

		size = is.read(prefix);

		if (readBOM() || readXMLDeclaration() || encoding != null) {
			if (xmlDeclaration != null && noXMLDecl) {
				// we have read the xml Declaration and the caller does not want
				// to read the XML declaration again
				in = new InputStreamReader(is, encoding);
			} else {
				// this means we have found a BOM or the caller wants to re-read
				// the xml Declaration again
				in = new InputStreamReader(new PrefixedInputStream(prefix,
						start, size, is), encoding);
			}
		} else {
			// we will get here if nothing could be found and the default
			// encoding for the parser is set to null
			in = new InputStreamReader(new PrefixedInputStream(prefix, start,
					size, is));
			encoding = ((InputStreamReader) in).getEncoding();
		}
	}

	/**
	 * Checks for a BOM and modifies the encoding variable if an encoding is
	 * found.
	 * 
	 * @return true if a BOM is found.
	 */
	private boolean readBOM() {

		int BOMLength = 0;

		// check for a BOM byte sequence
		if (size >= 3 && (prefix[0] == (byte) 0xEF)
				&& (prefix[1] == (byte) 0xBB) && (prefix[2] == (byte) 0xBF)) {
			encoding = SimpleReader.encodingNames[SimpleReader.UTF_8_ID];
			BOMLength = 3;

		} else if (size >= 4 && (prefix[0] == (byte) 0x00)
				&& (prefix[1] == (byte) 0x00) && (prefix[2] == (byte) 0xFE)
				&& (prefix[3] == (byte) 0xFF)) {
			encoding = SimpleReader.encodingNames[SimpleReader.UTF_32BE_ID];
			BOMLength = 4;

		} else if (size >= 4 && (prefix[0] == (byte) 0xFF)
				&& (prefix[1] == (byte) 0xFE) && (prefix[2] == (byte) 0x00)
				&& (prefix[3] == (byte) 0x00)) {
			encoding = SimpleReader.encodingNames[SimpleReader.UTF_32LE_ID];
			BOMLength = 4;

		} else if (size >= 2 && (prefix[0] == (byte) 0xFE)
				&& (prefix[1] == (byte) 0xFF)) {
			encoding = SimpleReader.encodingNames[SimpleReader.UTF_16BE_ID];
			BOMLength = 2;

		} else if (size >= 2 && (prefix[0] == (byte) 0xFF)
				&& (prefix[1] == (byte) 0xFE)) {
			encoding = SimpleReader.encodingNames[SimpleReader.UTF_16LE_ID];
			BOMLength = 2;

		}

		if (BOMLength > 0) {
			// we found something
			start = BOMLength;
			msgStatus = MSG_BOM_FOUND;
			return true;
		}

		return false;
	}

	/**
	 * Checks for a XML Declaration with an encoding attribute in it. Uses the
	 * found encoding to decode the stream.
	 * 
	 * @return true if XML declaration is found.
	 * @throws IOException
	 *             if I/O error occurs.
	 */

	private boolean readXMLDeclaration() throws IOException {

		String baseEnc = guessEncoding();
		if (baseEnc == null) {
			return false;
		}

		Reader reader = new SimpleReader(is, SimpleReader.getNameId(baseEnc));

		StringBuilder xmlDecl = new StringBuilder(6);
		String strXMLDecl = null;
		int c = -1;
		boolean found = false;

		// append what we have read so far
		xmlDecl.append(new String(prefix, baseEnc));

		// we need the first 6 chars to check if it is a xml declaration
		// if the stream is UTF-8 like encoded then we have read as many as
		// "prefixLength"'s value is
		for (int i = xmlDecl.length(); i < 6 && (c = reader.read()) != -1; i++) {
			xmlDecl.append((char) c);
		}

		// check if the first 6 chars are a xml declaration
		Pattern patern = Pattern.compile("<\\?[xX][mM][lL]\\s");
		Matcher matcher = patern.matcher(xmlDecl);

		if (matcher.find()) {
			// lets find the end of this xml declaration
			while ((c = reader.read()) != '>' && c != -1) {
				xmlDecl.append((char) c);
			}
			if (c != -1) {
				xmlDecl.append((char) '>');
			}

			strXMLDecl = xmlDecl.toString();
			xmlDeclaration = strXMLDecl;
			String[] encodings = strXMLDecl.split("encoding");

			if (encodings.length > 1) {

				String rawStr = encodings[1];

				int quotePos = rawStr.indexOf('"');
				int aposPos = rawStr.indexOf('\'');
				int startPos = -1;

				if (quotePos != -1 && aposPos != -1)
					startPos = Math.min(quotePos, aposPos);
				else
					startPos = Math.max(quotePos, aposPos);

				if (startPos != -1) {

					String enc = rawStr.substring(startPos + 1);

					int endPos = startPos == quotePos ? enc.indexOf('"') : enc
							.indexOf('\'');

					if (endPos != -1) {
						// this should be it
						encoding = enc.substring(0, endPos);

						found = true;
						msgStatus = MSG_XML_ENC_FOUND;
					}
				}
			}
		}

		// fix the prefix array
		prefix = strXMLDecl != null ? strXMLDecl.getBytes(baseEnc) : xmlDecl
				.toString().getBytes(baseEnc);
		start = 0;
		size = prefix.length;

		return found;
	}

	/**
	 * Checks the the byte sequence and tries to guess the encoding.
	 * 
	 * @return the encoding , or <code>null</code> if nothing found
	 */
	private String guessEncoding() {

		// check for "<?" in different encodings

		if (size >= 8 && prefix[0] == (byte) 0x00 && prefix[1] == (byte) 0x00
				&& prefix[2] == (byte) 0x00 && prefix[3] == (byte) 0x3c
				&& prefix[4] == (byte) 0x00 && prefix[5] == (byte) 0x00
				&& prefix[6] == (byte) 0x00 && prefix[7] == (byte) 0x3f) {
			return SimpleReader.encodingNames[SimpleReader.UTF_32BE_ID];
		} else if (size >= 8 && prefix[0] == (byte) 0x3c
				&& prefix[1] == (byte) 0x00 && prefix[2] == (byte) 0x00
				&& prefix[3] == (byte) 0x00 && prefix[4] == (byte) 0x3f
				&& prefix[5] == (byte) 0x00 && prefix[6] == (byte) 0x00
				&& prefix[7] == (byte) 0x00) {
			return SimpleReader.encodingNames[SimpleReader.UTF_32LE_ID];
		} else if (size >= 4 && prefix[0] == (byte) 0x00
				&& prefix[1] == (byte) 0x3c && prefix[2] == (byte) 0x00
				&& prefix[3] == (byte) 0x3f) {
			return SimpleReader.encodingNames[SimpleReader.UTF_16BE_ID];
		} else if (size >= 4 && prefix[0] == (byte) 0x3f
				&& prefix[1] == (byte) 0x00 && prefix[2] == (byte) 0x3c
				&& prefix[3] == (byte) 0x00) {
			return SimpleReader.encodingNames[SimpleReader.UTF_16LE_ID];
		} else if (size >= 2 && prefix[0] == (byte) 0x3c
				&& prefix[1] == (byte) 0x3f) {
			return SimpleReader.encodingNames[SimpleReader.UTF_8_ID];
		} else if (size >= 2 && prefix[0] == (byte) 0x4c
				&& prefix[1] == (byte) 0x6f) {
			return SimpleReader.encodingNames[SimpleReader.IBM_1047_ID]; // EBCDIC
		}

		return null;
	}

	/**
	 * InputStream wrapper that have byte array as a prefix to the wrapped
	 * stream.
	 */
	private static class PrefixedInputStream extends InputStream {

		/**
		 * The input stream to read from after the byte array is exhausted.
		 */
		private InputStream is = null;

		/**
		 * The bytes which will be read before any byte from the input stream to
		 * be read
		 */
		private byte[] prefix = null;

		/**
		 * The position from the byte array to start from.
		 */
		private int nextByte = 0;

		/**
		 * All the bytes before that position will be read.
		 */
		private int byteCount = 0;

		/**
		 * Constructs an InputStream wrapper that have byte array as a prefix to
		 * the wrapped stream.
		 * 
		 * @param prefix
		 *            the bytes which will be read before any byte from the
		 *            input stream to be read
		 * @param startPos
		 *            the position from the byte array to start from
		 * @param endPos
		 *            all the bytes before that position will be read
		 * @param is
		 *            the input stream to read from after the byte array is
		 *            exhausted
		 */
		public PrefixedInputStream(byte[] prefix, int startPos, int endPos,
				InputStream is) {
			this.prefix = prefix;
			this.is = is;
			this.byteCount = endPos;
			this.nextByte = startPos;
		}

		/**
		 * {@inheritDoc}
		 */
		public int read() throws IOException {
			if (nextByte < byteCount)
				return prefix[nextByte++];
			return is.read();
		}

		/**
		 * {@inheritDoc}
		 */
		public void close() throws IOException {
			is.close();
		}
	}

	/**
	 * This class is used to decode the xml Declaration only.
	 */
	private static class SimpleReader extends Reader {

		/**
		 * the names of the supported encodings
		 */
		public static final String[] encodingNames = { "UTF-32BE", "UTF-32LE",
				"UTF-16BE", "UTF-16LE", "UTF-8", "IBM-1047" };

		/** the position from the encodingNames array for the UTF-32BE string */
		public static final int UTF_32BE_ID = 0;

		/** the position from the encodingNames array for the UTF-32LE string */
		public static final int UTF_32LE_ID = 1;

		/** the position from the encodingNames array for the UTF-16BE string */
		public static final int UTF_16BE_ID = 2;

		/** the position from the encodingNames array for the UTF-16LE string */
		public static final int UTF_16LE_ID = 3;

		/** the position from the encodingNames array for the UTF-8 string */
		public static final int UTF_8_ID = 4;

		/** the position from the encodingNames array for the IBM-1047 string */
		public static final int IBM_1047_ID = 5;

		/** the number of bytes each character is represented by */
		public static final int[] byteCount = { 4, 4, 2, 2, 1, 1 };

		/**
		 * Input stream to read from.
		 */
		private InputStream is = null;

		/**
		 * Encoding id.
		 */
		private int encodingId = -1;

		/**
		 * Class constructor.
		 * 
		 * @param is
		 *            input stream.
		 * @param encId
		 *            encoding id
		 */
		SimpleReader(InputStream is, int encId) {
			this.is = is;
			encodingId = encId;
		}

		/**
		 * {@inheritDoc}
		 */
		public void close() throws IOException {
			// not used
		}

		/**
		 * {@inheritDoc}
		 */
		public int read(char[] cbuf, int off, int len) throws IOException {
			// not used
			return 0;
		}

		/**
		 * {@inheritDoc}
		 */
		public int read() throws IOException {
			byte[] character = new byte[byteCount[encodingId]];
			int read = -1;

			read = is.read(character);

			if (read == -1)
				return -1;

			switch (encodingId) {

			case 1: // UTF-32LE
			case 3: // UTF-16LE
			case 4: // UTF-8
				return character[0];
			case 0: // UTF-32BE
			case 2: // UTF-16BE
				return character[character.length - 1];
			case 5: // EBCDIC
				return new String(character, encodingNames[5]).charAt(0);
			}

			return -1;
		}

		/**
		 * Find the position of the specified name in the encodingNames array
		 * 
		 * @param encName
		 *            the name of the encoding style
		 * @return the position of the style in {@link #encodingNames} array or
		 *         -1 if nothing found
		 */
		static int getNameId(String encName) {
			for (int i = 0; i < encodingNames.length; i++) {
				if (encodingNames[i].equals(encName))
					return i;
			}
			return -1;
		}
	}
}
