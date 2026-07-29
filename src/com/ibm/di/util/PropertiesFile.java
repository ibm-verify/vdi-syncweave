/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.di.api.security.CryptoUtils;
import com.ibm.di.security.Crypto;

/**
 * <p>
 * In-memory representation of a TDI properties file. It may contain comments
 * and empty lines as well as property definitions. It may also contain
 * references to other properties files. The properties file could also be
 * encrypted.
 * </p>
 * 
 * <p>
 * A property in a TDI properties file can be protected or non-protected.
 * Protected properties normally have their keys prefixed with
 * PropertiesFile.PROTECT_PREFIX. Protected properties may have their values
 * encrypted. If a property value is encrypted, it will be prefixed with
 * PropertiesFile.PROTECT_VAL_PREFIX. If a property's value is encrypted, the
 * property is considered protected no matter if it is actually marked as
 * protected.
 * </p>
 * 
 * @see BasePropertiesFile
 * @since 7.0
 */
public class PropertiesFile extends BasePropertiesFile {

	/**
	 * <p>
	 * Include directive "&#33;include &lt;other-file/url&gt;"
	 * </p>
	 * <p>
	 * Include one properties file into another properties file. Properties
	 * defined in the main properties file before the include directive can be
	 * potentially overridden by properties from the included file. Properties
	 * defined in the main properties file after the include directive can
	 * potentially override properties from the included file.
	 * </p>
	 */
	public static final String INCLUDE_DIRECTIVE = "!include ";

	/**
	 * <p>
	 * Merge directive - "&#33;merge &lt;other-file/url&gt;".
	 * </p>
	 * <p>
	 * Merge the contents of one properties file into another properties file.
	 * Properties defined in the main properties file before the merge directive
	 * will not be overwritten by the properties from the merged file.
	 * Properties defined in the main properties file after the merge directive
	 * can potentially override properties from the merged file.
	 * </p>
	 */
	public final static String MERGE_DIRECTIVE = "!merge ";

	/**
	 * Create an empty object.
	 * 
	 * @param propertyCrypto
	 *            object used to encrypt/decrypt values of protected properties
	 */
	public PropertiesFile(Crypto propertyCrypto) {
		super(propertyCrypto);
	}

	/**
	 * Load a properties file in memory. Uses a default Crypto.
	 * 
	 * @param path
	 *            properties file to load
	 * @param resolveReferences
	 *            whether to load the properties files that the specified file
	 *            references
	 * @throws Exception
	 *             error while reading the properties file
	 */
	public PropertiesFile(String path, boolean resolveReferences) throws Exception {
		this(CryptoUtils.getDefaultCrypto(), path, resolveReferences, null, null);
	}

	/**
	 * Load a properties file in memory.
	 * 
	 * @param propertyCrypto
	 *            object used to encrypt/decrypt values of protected properties
	 * @param path
	 *            properties file to load
	 * @param resolveReferences
	 *            whether to load the properties files that the specified file
	 *            references
	 * @throws Exception
	 *             error while reading the properties file
	 */
	public PropertiesFile(Crypto propertyCrypto, String path, boolean resolveReferences) throws Exception {
		this(propertyCrypto, path, resolveReferences, null, null);
	}

	/**
	 * Load a properties file in memory. If the specified file is encrypted as a
	 * whole, a cryptographic object to decrypt the file must be provided. If a
	 * file is encrypted as a whole, all the files it references are expected to
	 * be encrypted too. The specified crypto object will be used to decrypt
	 * them also.
	 * 
	 * @param propertyCrypto
	 *            object used to encrypt/decrypt values of protected properties
	 * @param path
	 *            properties file to load
	 * @param resolveReferences
	 *            whether to load the properties files that the specified file
	 *            references
	 * @param fileCrypto
	 *            object used to decrypt the file; pass null if the properties
	 *            file is not encrypted as a whole
	 * @param prefixToSkip
	 *            prefix that matches lines from the properties file, which will
	 *            be skipped during processing; pass null to read all lines
	 * @throws Exception
	 *             error while reading the properties file, or error while
	 *             decrypting it
	 */
	public PropertiesFile(Crypto propertyCrypto, String path, boolean resolveReferences, Crypto fileCrypto, String prefixToSkip)
			throws Exception {

		this(propertyCrypto);

		/*
		 * Collect lines only for the main properties file but not for its
		 * referenced files.
		 */
		load(path, resolveReferences, fileCrypto, prefixToSkip, properties, lines);
	}

	/**
	 * Load a properties file. Encrypted values are not decrypted at this time.
	 * 
	 * @param path
	 *            the path of the properties file
	 * @param resolveReferences
	 *            whether to resolve references to other properties file - that
	 *            is parse them and gather their properties
	 * @param fileCrypto
	 *            object used to decrypt the file; pass null if the properties
	 *            file is not encrypted as a whole
	 * @param prefixToSkip
	 *            prefix that matches lines from the properties file, which will
	 *            be skipped during processing; pass null to read all lines
	 * @param props
	 *            a container, where to put the read properties
	 * @param content
	 *            a container, where to add the lines of the properties file;
	 *            each line is a StringBuilder object; can be null
	 * @throws Exception
	 *             error while reading the properties file, or error while
	 *             decrypting it
	 */
	private void load(String path, boolean resolveReferences, Crypto fileCrypto, String prefixToSkip, Map<String, Property> props,
			List<StringBuilder> content) throws Exception {

		InputStream input;
		try {
			input = new URL(path).openStream();
		} catch (MalformedURLException malformed) {
			input = new FileInputStream(path);
		}

		// the file is encrypted as a whole
		if (fileCrypto != null) {

			byte[] encrypted = FileUtils.readInputStream(input);
			input.close();
			byte[] decrypted = fileCrypto.decrypt(encrypted);
			input = new ByteArrayInputStream(decrypted);
		}

		Reader inpReader = null;

		try {

			inpReader = new InputStreamReader(input);
			readStream(inpReader, resolveReferences, fileCrypto, prefixToSkip, props, content);
		} finally {

			if (inpReader != null) {
				inpReader.close();
			}
		}
	}

	/**
	 * Load a properties file. Encrypted values are not decrypted at this time.
	 * 
	 * @param inpReader
	 *            the properties file
	 * @param resolveReferences
	 *            whether to resolve references to other properties file - that
	 *            is parse them and gather their properties
	 * @param fileCrypto
	 *            object used to decrypt the file; pass null if the properties
	 *            file is not encrypted as a whole
	 * @param prefixToSkip
	 *            prefix that matches lines from the properties file, which will
	 *            be skipped during processing; pass null to read all lines
	 * @param props
	 *            a container, where to put the read properties
	 * @param content
	 *            a container, where to add the lines of the properties file;
	 *            each line is a StringBuilder object; can be null
	 * @throws Exception
	 *             error while reading the properties file, or error while
	 *             decrypting it
	 */
	private void readStream(Reader inpReader, boolean resolveReferences, Crypto fileCrypto, String prefixToSkip,
			Map<String, Property> props, List<StringBuilder> content) throws Exception {

		BufferedReader inp = new BufferedReader(inpReader);

		String str;
		String[] av;
		int lineNumber = 0;

		while ((str = inp.readLine()) != null) {
			lineNumber++;
			str = str.trim();

			// skip the header line
			if (prefixToSkip != null && str.startsWith(prefixToSkip)) {
				continue;
			}

			// line will remain null if the property comes from an referenced
			// properties file
			StringBuilder line = null;
			if (content != null) {
				line = new StringBuilder(str);
				content.add(line);
			}

			if (str.startsWith("#") || str.startsWith("/") || str.startsWith("'") || str.length() == 0) {
				continue;
			}

			if (str.startsWith(INCLUDE_DIRECTIVE)) {

				/*
				 * Read properties from specified file/url (add + replace). Do
				 * not collect the lines of referenced files but resolve their
				 * references.
				 */
				if (resolveReferences) {
					load(str.substring(INCLUDE_DIRECTIVE.length()), resolveReferences, fileCrypto, prefixToSkip, props, null);
				}

			} else if (str.startsWith(MERGE_DIRECTIVE)) {

				/*
				 * Merge properties from specified file/url (add but don't
				 * replace). Do not collect the lines of referenced files but
				 * resolve their references.
				 */
				if (resolveReferences) {

					Map<String, Property> propsToMerge = new TreeMap<String, Property>();
					load(str.substring(MERGE_DIRECTIVE.length()), resolveReferences, fileCrypto, prefixToSkip, propsToMerge, null);

					mergeInto(propsToMerge, props);
				}

			} else if ((av = splitString(str)) != null) {

				String prop = av[0];
				String value = av[1];

				Property p = new Property(prop, value, line, propertyCrypto);
				// get the 'clean' key without prefixes
				String key = p.getKey();

				props.put(key, p);
			} else if (str.length() > 1) {
				throw new Exception(resHash.getString("PROPERTIESFILE.CANNOT.PARSE", new Object[] { str, "" + lineNumber }));
			}

		}
	}

	/**
	 * Write the contents of this properties file to disk.
	 * 
	 * @param path
	 *            a file, whether the contents will be saved
	 * @param header
	 *            an optional header, that will be put as the first line in the
	 *            properties file; must be a single comment line
	 * @param fileCrypto
	 *            object used to encrypt the file as a whole; if null the file
	 *            will not be encrypted as a whole
	 * @throws Exception
	 *             error while writing the file or error while encrypting the
	 *             file
	 */
	public void store(String path, String header, Crypto fileCrypto) throws Exception {

		OutputStream output = null;

		try {

			output = new FileOutputStream(path);

			if (fileCrypto != null) {

				// the file will be encrypted as a whole
				ByteArrayOutputStream plaintext = new ByteArrayOutputStream();
				writeStream(new OutputStreamWriter(plaintext), header);
				byte[] encrypted = fileCrypto.encrypt(plaintext.toByteArray());
				output.write(encrypted);
			} else {

				// the file will be written plain text
				writeStream(new OutputStreamWriter(output), header);
			}

			output.flush();

		} finally {

			if (output != null) {
				output.close();
			}
		}
	}

	/**
	 * Write the contents of this properties file to disk. The file is not
	 * encrypted and no header is written.
	 * 
	 * @param path
	 *            a file, whether the contents will be saved
	 * 
	 * @throws Exception
	 *             error while writing the file
	 */
	public void store(String path) throws Exception {
		store(path, null, null);
	}

	/**
	 * Write the properties file to a stream.
	 * 
	 * @param outWriter
	 *            where to write the properties file
	 * @param header
	 *            an optional header, that will be put as the first line in the
	 *            properties file; must be a single comment line
	 * @throws IOException
	 *             error while writing
	 */
	private void writeStream(Writer outWriter, String header) throws IOException {

		PrintWriter out = new PrintWriter(outWriter);

		if (header != null) {

			// Ensure the header is a comment
			if (!header.startsWith("#")) {
				header = "#" + header;
			}

			out.println(header);
		}

		Iterator<StringBuilder> it = lines.iterator();

		while (it.hasNext()) {
			out.println(it.next());
		}

		out.flush();
	}

	/**
	 * Split a property line into property key and property value. If a property
	 * cannot be parsed out of this line, null is returned.
	 * 
	 * @param str
	 *            a property line
	 * @return an array, whose first element is the key and second is the value
	 */
	private String[] splitString(String str) {
		if (str == null)
			return null;

		int colon = str.indexOf(":");
		int equal = str.indexOf("=");
		int index;

		if (colon == -1 && equal == -1)
			return null;
		else if (colon == -1)
			index = equal;
		else if (equal == -1)
			index = colon;
		else if (colon < equal)
			index = colon;
		else
			index = equal;

		return new String[] { str.substring(0, index).trim(), str.substring(index + 1).trim() };
	}

	/**
	 * Merge the contents of one container into another.
	 * 
	 * @param src
	 *            the container, whose contents will be merged into the other
	 *            container
	 * @param dest
	 *            the container, where the contents will be merged
	 */
	private static void mergeInto(Map<String, Property> src, Map<String, Property> dest) {

		Iterator<Map.Entry<String, Property>> it = src.entrySet().iterator();
		while (it.hasNext()) {

			Map.Entry<String, Property> e = it.next();
			if (!dest.containsKey(e.getKey())) {

				dest.put(e.getKey(), e.getValue());
			}
		}
	}
}
