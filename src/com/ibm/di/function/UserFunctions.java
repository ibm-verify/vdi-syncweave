/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.function;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.ibm.di.automation.COMProxy;
import com.ibm.di.automation.IDispatch;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.ExternalPropertiesConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.interfaces.TDIProperties;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.exceptions.AbortALException;
import com.ibm.di.exceptions.ContinueLoopException;
import com.ibm.di.exceptions.ExitBranchException;
import com.ibm.di.exceptions.IgnoreEntryException;
import com.ibm.di.exceptions.RestartEntryException;
import com.ibm.di.exceptions.RetryEntryException;
import com.ibm.di.exceptions.SkipEntryException;
import com.ibm.di.exceptions.SkipToException;
import com.ibm.di.fc.FunctionInterface;
import com.ibm.di.loader.IDILoader;
import com.ibm.di.parser.LDIFParser;
import com.ibm.di.parser.ParserInterface;
import com.ibm.di.plugin.security.pki.IDIPasswordCrypto;
import com.ibm.di.queue.MemBufferQ;
import com.ibm.di.queue.MemBufferQFactory;
import com.ibm.di.script.ScriptEngineOptions;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.AssemblyLinePool;
import com.ibm.di.server.Log;
import com.ibm.di.server.Monitor;
import com.ibm.di.server.RS;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Sequence;
import com.ibm.di.server.TaskCallBlock;
import com.ibm.di.store.StoreFactory;
import com.ibm.di.util.FileUtils;
import com.ibm.di.util.ParameterSubstitution;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.StringTokenizer;

/**
 * This class contains a number of convenience methods widely used by the
 * scripting environment. An instance of this class is available in scripts,
 * with the scripting name of <i>system</i>.
 */

public class UserFunctions {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Name of the properties file.
	 */
	private static final String PROPERTIES_FILE = "miserver";

	/**
	 * The Exception object set by the last call in this library.
	 */
	public Exception lastError;

	/**
	 * {@link RSInterface} object.
	 */
	public RSInterface server = null;

	public final static char[] INVALID_XML_CHARS = { '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006',
			'\u0007', '\u0008', '\u000b', '\u000c', '\u000e', '\u000f', '\u0010', '\u0011', '\u0012', '\u0013', '\u0014', '\u0015',
			'\u0016', '\u0017', '\u0018', '\u0019', '\u001f' };

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Default constructor.
	 */
	public UserFunctions() {
		server = SystemFunctions.getServer();
	}

	/**
	 * Constructor with one parameter.
	 * 
	 * @param server
	 *            server instance
	 */
	public UserFunctions(RSInterface server) {
		this.server = server;
	}

	/**
	 * Returns the RS instance associated with the current ThreadGroup, or the
	 * dummy RSInterface object defined by the Config Editor. Although this
	 * method is public, it is meant for internal use,. The usual way to get the
	 * current RS instance would be to use the <code>main</code> object in
	 * JavaScript.
	 * 
	 * @return the {@link RS} instance or <code>null</code> if it couldn't be
	 *         found, e.g. because the current Thread was not created by the TDI
	 *         framework.
	 */
	public RSInterface getServer() {
		return (server == null ? SystemFunctions.getServer() : server);
	}

	/**
	 * Remove characters from a string. For example, if you want to remove all
	 * blanks from the string "J O P" then you would use <code>
	 * remove (" ", "J O P")</code>
	 * . The returned value would then be "JOP".
	 * 
	 * <pre>
	 * var a = &quot;A string with blanks and vowels&quot;;
	 * var b = system.remove(&quot;AEIOUaeiou &quot;, a);
	 * task.logmsg(&quot;Result: &quot; + b); // &quot;strngwthblnksndvwls&quot;
	 * </pre>
	 * 
	 * @param s
	 *            The characters to be removed
	 * @param source
	 *            The string from which characters are removed
	 * @return string with removed characters specified by <code>s</code>
	 * @throws Exception
	 */
	public String remove(String s, String source) throws Exception {
		if (source == null)
			return null;
		if (s == null)
			return source;

		StringBuffer ns = new StringBuffer();
		char ch;

		for (int i = 0; i < source.length(); i++) {
			ch = source.charAt(i);
			if (s.indexOf(ch) == -1)
				ns.append(ch);
		}

		return ns.toString();
	}

	/**
	 * Trims leading/trailing white-space from a string. Returns an empty string
	 * if the argument is null.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var a = &quot;   A string with leading/trailing white-spaces   &quot;;
	 * var b = system.trim(a);
	 * task.logmsg(&quot;Result: &quot; + b); // &quot;A string with leading/trailing white-spaces&quot;
	 * </pre>
	 * 
	 * @param str
	 *            The string to trim
	 * @return The trimmed string
	 */
	public String trim(String str) {
		return str == null ? "" : str.trim();
	}

	/**
	 * Convert a string to a java.lang.Integer object.
	 * 
	 * @param str
	 *            The string with a number
	 * @return The Integer object
	 * @throws Exception
	 */
	public Integer toInt(String str) throws Exception {
		return new Integer(str);
	}

	/**
	 * Returns true if a string holds a valid Integer.
	 * 
	 * @param str
	 *            The string to test
	 * @return True if the string can be converted to an Integer
	 */
	public boolean isValidInt(String str) {
		try {
			toInt(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Opens a file in append mode and returns the associated BufferedWriter
	 * object. The default character encoding is used.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var out = system.openFileForAppend(&quot;out.txt&quot;);
	 * out.write(&quot;Hello world!&quot;);
	 * out.newLine();
	 * out.close();
	 * </pre>
	 * 
	 * @param path
	 *            The file path to open. If the file does not exist it is
	 *            created.
	 * @return The BufferedWriter object
	 * @throws Exception
	 */
	public BufferedWriter openFileForAppend(String path) throws Exception {
		FileWriter w = new FileWriter(path, true);
		return new BufferedWriter(w);
	}

	/**
	 * Opens a file in output mode and returns the associated BufferedWriter
	 * object.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var out = system.openFileForOutput(&quot;out.txt&quot;);
	 * out.write(&quot;Hello world!&quot;);
	 * out.newLine();
	 * out.close();
	 * </pre>
	 * 
	 * @param path
	 *            The file path to open (overwrites existing file)
	 * @return The BufferedWriter object
	 * @throws Exception
	 */
	public BufferedWriter openFileForOutput(String path) throws Exception {
		FileWriter w = new FileWriter(new File(path));
		return new BufferedWriter(w);
	}

	/**
	 * Opens a file for input and returns the associated BufferedReader object.
	 * The default character encoding is used.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var inp = system.openFileForInput(&quot;inp.txt&quot;);
	 * var str = inp.readLine();
	 * if (str == null)
	 * 	task.logmsg(&quot;End of file&quot;);
	 * inp.close();
	 * </pre>
	 * 
	 * @param path
	 *            The file path to open
	 * @return The BufferedReader object
	 * @throws Exception
	 *             FileNotFoundException
	 */
	public BufferedReader openFileForInput(String path) throws Exception {
		BufferedReader r = new BufferedReader(new FileReader(new File(path)));
		return r;
	}

	/**
	 * Writes a string plus a CRLF using a Writer object.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var a = &quot;Some line with text.&quot;;
	 * var b = &quot;Another line with text.&quot;;
	 * var fileWriter = new java.io.FileWriter(&quot;c:\\docs\\myfile.txt&quot;);
	 * system.writeln(fileWriter, a);
	 * system.writeln(fileWriter, b);
	 * </pre>
	 * 
	 * The result in the file would look like this:
	 * <p>
	 * <i> Some line with text. <br>
	 * Another line with text. </i>
	 * 
	 * @param w
	 *            The writer object
	 * @param str
	 *            The string to write
	 * @throws Exception
	 */
	public void writeln(Writer w, String str) throws Exception {
		w.write(str + "\r\n");
		w.flush();
	}

	/**
	 * Sends an email message. Make sure the <i>mail.smtp.host</i> Java property
	 * is configured with the hostname of a valid SMTP server.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 *      var res = system.sendMail(&quot;Sender&quot;,&quot;address1@mail.or,
	 *      		address2@mail.or&quot;,&quot;Subject&quot;,&quot;Message text&quot;,
	 *      		&quot;C:\\docs\\myfile.txt&quot;);
	 *      if(res != null)
	 *      	task.logmsg(&quot;Error occurred: &quot;+res);
	 * </pre>
	 * 
	 * @param from
	 *            The From field
	 * @param recipient
	 *            A comma separated list of recipient addresses
	 * @param subject
	 *            The Subject field
	 * @param body
	 *            The message text
	 * @param attachments
	 *            If specified a comma separated list of file-paths that will be attached to the message
	 * @return If null, the message was sent. Otherwise, this is the error
	 *         message.
	 * @throws Exception
	 */
	public String sendMail(String from, String recipient, String subject, String body, String attachments) throws Exception {
		try {
			Message message = constructMessage(from, recipient, subject);
			Multipart mp = constructAttachment(body, attachments);
			if (mp != null) {
				message.setContent(mp);
			} else {
				message.setText(body);
			}
			Transport.send(message);
		} catch (MessagingException me) {
			System.err.println(sResHash.getString("USER.FUNCTIONS.SENDMAIL.WARNING", me));
			return me.getMessage();
		}

		return null;
	}

	/**
	 * Sends an email message with ReplyTo field. Make sure the
	 * <i>mail.smtp.host</i> Java property is configured with the hostname of a
	 * valid SMTP server.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 *      var res = system.sendMail(&quot;Sender&quot;,&quot;address1@mail.or,
	 *      		address2@mail.or&quot;,&quot;Subject&quot;,&quot;Message text&quot;,
	 *      		&quot;c\\docs\\myfile.txt&quot;,&quot;my_address@mail.or&quot;);
	 *      if(res != null)
	 *      	task.logmsg(&quot;Error occurred: &quot;+res);
	 * </pre>
	 * 
	 * @param from
	 *            The From field
	 * @param recipient
	 *            A comma separated list of recipient addresses
	 * @param subject
	 *            The Subject field
	 * @param body
	 *            The message text
	 * @param attachments
	 *            If specified a comma separated list of file-paths that will be
	 *            attached to the message
	 * @param replyTo
	 *            A comma separated list of ReplyTo addresses
	 * @return If null, the message was sent. Otherwise, this is the error
	 *         message.
	 * @throws Exception
	 */
	public String sendMail(String from, String recipient, String subject, String body, String attachments, String replyTo)
			throws Exception {
		try {
			Message message = constructMessage(from, recipient, subject);
			// Set Reply To field
			if (replyTo != null && replyTo.length() > 0) {
				StringTokenizer st = new StringTokenizer(replyTo, ",");
				Address[] replyAddress = new Address[st.countTokens()];
				int addressCounter = 0;
				while (st.hasMoreTokens()) {
					replyAddress[addressCounter++] = new InternetAddress(st.nextToken());
				}
				message.setReplyTo(replyAddress);
			}
			Multipart mp = constructAttachment(body, attachments);
			if (mp != null) {
				message.setContent(mp);
			} else {
				message.setText(body);
			}
			Transport.send(message);
		} catch (MessagingException me) {
			System.err.println(sResHash.getString("USER.FUNCTIONS.SENDMAIL.WARNING", me));
			return me.getMessage();
		}

		return null;
	}

	/**
	 * Constructs a Multipart object containing body and attachments
	 * 
	 * @param body
	 *            The message text
	 * @param attachments
	 *            A comma separated list of file-paths that will be attached to
	 *            the message
	 * @return null if no attachments are specified. Otherwise - a Multipart
	 *         object containing the body and the attachments
	 * @throws Exception
	 *             MessagingException - if an error occurs, when constructing
	 *             the Multipart Object
	 * 
	 */
	private Multipart constructAttachment(String body, String attachments) throws Exception {
		if (attachments != null && attachments.length() > 0) {
			Multipart mp = new MimeMultipart();
			MimeBodyPart m1 = new MimeBodyPart();
			m1.setText(body);

			StringTokenizer at = new StringTokenizer(attachments, ",");
			while (at.hasMoreTokens()) {
				String attachment = at.nextToken();
				MimeBodyPart m2 = new MimeBodyPart();
				FileDataSource fds = new FileDataSource(attachment);
				m2.setDataHandler(new DataHandler(fds));
				m2.setFileName(fds.getName());
				mp.addBodyPart(m2);
			}
			mp.addBodyPart(m1);

			return mp;
		}
		return null;
	}

	/**
	 * Constructs an email message, used later by the sendMail methods. Make
	 * sure the <i>mail.smtp.host</i> Java property is configured with the
	 * hostname of a valid SMTP server.
	 * 
	 * @param from
	 *            The From field
	 * @param recipient
	 *            A comma separated list of recipient addresses
	 * @param subject
	 *            The Subject field
	 * @return If null, the message was sent. Otherwise, this is the error
	 *         message.
	 * @throws MessagingException
	 *             If an error occurs, when constructing the Message Object
	 */
	private Message constructMessage(String from, String recipient, String subject) throws MessagingException {
		Properties props = System.getProperties();
		Session session = Session.getDefaultInstance(props, null);
		Message message = new MimeMessage(session);

		message.setFrom(new InternetAddress(from));

		StringTokenizer st = new StringTokenizer(recipient, ",");
		while (st.hasMoreTokens()) {
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(st.nextToken()));
		}

		message.setSubject(subject);
		return message;
	}

	/**
	 * Copy file. This method copies fromPath to toPath. The overwrite flag
	 * specifies whether the destination file should be overwritten.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var fromPath = &quot;c:\\docs\\myfile.txt&quot;;
	 * var toPath = &quot;c:\\backup\\myfile.txt&quot;;
	 * if (!system.copyFile(fromPath, toPath, false))
	 * 	task.logmsg(&quot;Error &quot; + toPath + &quot; file exist!&quot;);
	 * </pre>
	 * 
	 * @param fromPath
	 *            The source file
	 * @param toPath
	 *            The destination file
	 * @param overwrite
	 *            Specify true if destination should be overwritten.
	 * @return true if file was copied, false if toPath exists and
	 *         overwrite=false.
	 * @throws Exception
	 */
	public static boolean copyFile(String fromPath, String toPath, boolean overwrite) throws Exception {
		return FileUtils.copyFile(fromPath, toPath, overwrite);
	}

	/**
	 * Copy file. This method copies fromPath to toPath. The overwrite flag
	 * specifies whether the destination file should be overwritten.
	 * <p>
	 * 
	 * @param fromFile
	 *            The source file
	 * @param toFile
	 *            The destination file
	 * @param overwrite
	 *            Specify true if destination should be overwritten.
	 * @return true if file was copied, false if toPath exists and
	 *         overwrite=false.
	 * @throws Exception
	 */
	public static boolean copyFile(File fromFile, File toFile, boolean overwrite) throws Exception {
		return FileUtils.copyFile(fromFile, toFile, overwrite);
	}

	/**
	 * Copy file. This method copies fromPath to toPath without using buffer.
	 * The overwrite flag specifies whether the destination file should be
	 * overwritten.
	 * 
	 * @param fromPath
	 *            The name of the file to copy
	 * @param toPath
	 *            The name of the new file
	 * @param overwrite
	 *            Specify true if destination should be overwritten.
	 * @return <code>true</code> if copyBinaryFile successed, otherwise
	 *         <code>false</code>.
	 * @deprecated Use {@link #copyFile(String, String)} instead
	 * @throws Exception
	 */
	@Deprecated
	public boolean copyBinaryFile(String fromPath, String toPath, boolean overwrite) throws Exception {
		File fp = new File(fromPath);
		File tp = new File(toPath);

		if (!overwrite && tp.exists())
			return false;

		FileInputStream fis = new FileInputStream(fp);
		try {
			FileOutputStream fos = new FileOutputStream(tp);
			try {
				int ch;
		
				while ((ch = fis.read()) != -1) {
					fos.write(ch);
				}
			} finally {
				fos.close();
			}
		} finally {
			fis.close();
		}

		return true;
	}

	/**
	 * Copy a directory. The recursive flag specifies whether recursion should
	 * be used to copy child directories of <code>target</code>.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var dir1 = &quot;c:\\docs&quot;;
	 * var dir2 = &quot;c:\\backup&quot;;
	 * system.copyDirectory(dir1, dir2, true, true, null);
	 * </pre>
	 * 
	 * @param source
	 *            Source directory
	 * @param target
	 *            Target directory
	 * @param recursive
	 *            Specify true if recursion should be used
	 * @param overwrite
	 *            Specify true if existing files should be overwritten.
	 * @param log
	 *            If not null, log activity to this Log
	 * @throws Exception
	 */
	public void copyDirectory(String source, String target, boolean recursive, boolean overwrite, Log log) throws Exception {
		File src = new File(source);
		File dst = new File(target);

		if ((dst.mkdir()) && (log != null)) {
			log.info(sResHash.getString("USER.FUNCTIONS.CREATEDDIRECTORY.INFO", dst.getAbsolutePath()));
		}

		if (!dst.exists()) {
			throw new Exception(sResHash.getString("USER.FUNCTIONS.CANNOTCREATEDIR.ERROR", dst.getAbsolutePath()));
		}

		String[] list = src.list();
		for (int i = 0; i < list.length; i++) {
			File f1 = new File(src, list[i]);
			File f2 = new File(dst, list[i]);
			if (f1.isDirectory()) {
				if (recursive)
					copyDirectory(f1.getAbsolutePath(), f2.getAbsolutePath(), recursive, overwrite, log);
				else if (log != null) {
					log.info(sResHash.getString("USER.FUNCTIONS.DONTCOPYDIR.INFO", f1.getAbsolutePath()));
				}
			} else if (!overwrite && f2.exists()) {
				if (log != null) {
					log.info(sResHash.getString("USER.FUNCTIONS.DONTOVERWRITEFILE.INFO", f2.getAbsolutePath()));
				}
			} else {
				if (copyFile(f1.getAbsolutePath(), f2.getAbsolutePath(), overwrite) && log != null) {
					log.info(sResHash.getString("USER.FUNCTIONS.CREATEDFILE.INFO", f2.getAbsolutePath()));
				}
			}
		}

	}

	/**
	 * Creates a new Attribute object.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * ocAttr = system.newAttribute(&quot;objectClass&quot;);
	 * ocAttr.addValue(&quot;top&quot;);
	 * ocAttr.addValue(&quot;person&quot;);
	 * ocAttr.addValue(&quot;organizationalPerson&quot;);
	 * ocAttr.addValue(&quot;inetOrgPerson&quot;);
	 * work.setAttribute(ocAttr);
	 * </pre>
	 * 
	 * @param name
	 *            The attribute name
	 * @return The Attribute object
	 */
	public Attribute newAttribute(String name) {
		Attribute a = new Attribute(name);
		return a;
	}

	/**
	 * Creates a new rscSearchCriteira object.
	 * 
	 * @return The SearchCriteria object
	 * 
	 * @see com.ibm.di.server.SearchCriteria
	 */
	public SearchCriteria newSearchCriteria() {
		SearchCriteria rs = new SearchCriteria();
		return rs;
	}

	/**
	 * Creates a new Entry object.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var entry = system.newEntry();
	 * entry.setAttribute(&quot;linenumber&quot;, &quot;1&quot;);
	 * entry.setAttribute(&quot;line&quot;, &quot;Simple line of text!&quot;);
	 * 
	 * write.getConnector().putEntry(entry);
	 * </pre>
	 * 
	 * @return The Entry object
	 * @see #newAttribute(String)
	 */
	public Entry newEntry() {
		return new Entry();
	}

	/**
	 * Creates a new object. This method only works for Java objects that have
	 * empty constructors. It is a convenience method for scripting languages
	 * that cannot create Java objects directly.
	 * 
	 * @param className
	 *            The java class name
	 * @return The newly created object
	 */
	public Object newObject(String className) {
		try {
			Class<?> c = Class.forName(className);
			return c.newInstance();
		} catch (Exception e) {
			lastError = e;
			return null;
		}
	}

	/**
	 * Throws a SkipEntryException which causes the AssemblyLine to stop the
	 * current cycle and pass control to the currently active Iterator in order
	 * to get the next entry.
	 * <p>
	 * This call bypasses End-of-cycle behaviors, like accumulating (see
	 * TaskCallBlock), committing JDBC operations or persisting Iterator State
	 * for Change Detection Connectors. If you instead wish to stop the current
	 * cycle and still invoke End-of-cycle behaviors, use the
	 * {@link #exitFlow()} call instead.
	 * 
	 * @throws SkipEntryException
	 *             to tell the AssemblyLine to skip the current Entry.
	 * @see #skipTo(String)
	 */
	public void skipEntry() throws com.ibm.di.exceptions.SkipEntryException {
		throw new com.ibm.di.exceptions.SkipEntryException(sResHash.getString("USER.FUNCTIONS.SKIP.ENTRY.EXCEPTION"));
	}

	/**
	 * * Throws a SkipEntryException which causes the AssemblyLine to stop the
	 * current cycle and pass control to the currently active Iterator in order
	 * to get the next entry.
	 * <p>
	 * This call bypasses End-of-cycle behaviors, like accumulating (see
	 * TaskCallBlock), committing JDBC operations or persisting Iterator State
	 * for Change Detection Connectors. If you instead wish to stop the current
	 * cycle and still invoke End-of-cycle behaviors, use the
	 * {@link #exitFlow()} call instead.
	 * 
	 * @param msg
	 *            A message supplied by the user
	 * @throws SkipEntryException
	 *             to tell the AssemblyLine to skip the current Entry.
	 * @see #skipEntry()
	 */
	public void skipEntry(String msg) throws com.ibm.di.exceptions.SkipEntryException {
		throw new com.ibm.di.exceptions.SkipEntryException(msg);
	}

	/**
	 * 
	 * Throws an IgnoreEntryException to tell the AssemblyLine to skip the
	 * current component and continue with the next component in flow.
	 * 
	 * @throws IgnoreEntryException
	 */
	public void ignoreEntry() throws com.ibm.di.exceptions.IgnoreEntryException {
		throw new com.ibm.di.exceptions.IgnoreEntryException(sResHash.getString("USER.FUNCTIONS.SKIP.CONNECTOR.EXCEPTION"));
	}

	/**
	 * Throws an IgnoreEntryException to tell the AssemblyLine to skip the
	 * current component and continue with the next component in flow.
	 * 
	 * @param msg
	 *            A message supplied by the user
	 * @throws IgnoreEntryException
	 */
	public void ignoreEntry(String msg) throws com.ibm.di.exceptions.IgnoreEntryException {
		throw new com.ibm.di.exceptions.IgnoreEntryException(msg);
	}

	/**
	 * Throws a RestartEntryException to tell the AssemblyLine to restart. The
	 * AssemblyLine will continue at the first non-Iterator component in the
	 * AssemblyLine, using the current work object.
	 * 
	 * @throws RestartEntryException
	 */
	public void restartEntry() throws com.ibm.di.exceptions.RestartEntryException {
		throw new com.ibm.di.exceptions.RestartEntryException(sResHash.getString("USER.FUNCTIONS.RESTART.AL.EXCEPTION"));
	}

	/**
	 * Throws a RestartEntryException to tell the AssemblyLine to restart, using
	 * the current work object.
	 * 
	 * @param msg
	 *            A message supplied by the user
	 * @see #restartEntry()
	 * @throws RestartEntryException
	 */
	public void restartEntry(String msg) throws com.ibm.di.exceptions.RestartEntryException {
		throw new com.ibm.di.exceptions.RestartEntryException(msg);
	}

	/**
	 * Throws a RetryEntryException to tell the AssemblyLine to retry this
	 * component. The AssemblyLine will perform the operation of the current
	 * component again, using the current work object.
	 * 
	 * @throws RetryEntryException
	 */
	public void retryEntry() throws com.ibm.di.exceptions.RetryEntryException {
		throw new com.ibm.di.exceptions.RetryEntryException(sResHash.getString("USER.FUNCTIONS.RETRY.CONNECTOR.EXCEPTION"));
	}

	/**
	 * Throws a SkipToException to tell the AssemblyLine to skip to the named
	 * Connector/ScriptComponent.
	 * 
	 * @param name
	 *            The name of the Connector to skip to.
	 * @throws SkipToException
	 */
	public void skipTo(String name) throws com.ibm.di.exceptions.SkipToException {
		throw new com.ibm.di.exceptions.SkipToException(name);
	}

	/**
	 * Throws an AbortALException to instruct the AssemblyLine to terminate. The
	 * AssemblyLine will continue with the Epilog. If the Epilog is already
	 * executed, continue on to the next step (closing Connectors or "Epilog -
	 * After Close").
	 * <p>
	 * If you want your AssemblyLine to terminate gracefully (i.e. not abort),
	 * use one of the following functions system.exitBranch("AssemblyLine") or
	 * task.shutdown() instead.
	 * 
	 * @param reason
	 *            Descriptive text why the AssemblyLine is terminated
	 * @throws AbortALException
	 * @see #exitBranch()
	 */
	public void abortAssemblyLine(String reason) throws com.ibm.di.exceptions.AbortALException {
		throw new com.ibm.di.exceptions.AbortALException(reason);
	}

	/**
	 * Throws a generic java.lang.Exception.
	 * <p>
	 * Whereas the JavaScript throw command allows you to throw a JavaScript
	 * exception, this method creates and throws a {@link Exception} object.
	 * 
	 * @param message
	 *            The message text of the Exception
	 * @throws Exception
	 */
	public void throwException(String message) throws Exception {
		throw new Exception(message);
	}

	/**
	 * Throws an ExitBranchException that tells the AssemblyLine to exit the
	 * current branch/loop.
	 * 
	 * @throws ExitBranchException
	 */
	public void exitBranch() throws com.ibm.di.exceptions.ExitBranchException {
		exitBranch(null);
	}

	/**
	 * Throws an ExitBranchException that tells the AssemblyLine to exit the
	 * named branch/loop. Some special values for name can also be used:<br>
	 * null - exit current (innermost) branch or loop<br>
	 * "Loop" - exit current Loop<br>
	 * "Branch" - exit current branch<br>
	 * "Cycle" - exit this cycle (jump to end of cycle), and begin the next
	 * cycle<br>
	 * "Flow" - jump to end of cycle, and send response if there is a Connector
	 * in Server mode. Then begin the next cycle<br>
	 * "AssemblyLine" - exit dataflow, jump to Epilog
	 * 
	 * @param name
	 *            The name of the branch/loop to exit
	 * @throws ExitBranchException
	 *             to tell the AssemblyLine to exit the named branch/loop
	 */
	public void exitBranch(String name) throws com.ibm.di.exceptions.ExitBranchException {
		throw new com.ibm.di.exceptions.ExitBranchException(name);
	}

	/**
	 * Throws an ExitBranchException that tells the AssemblyLine to not execute
	 * any more of the Flow Section components. In other words, the current
	 * cycle of the AL ends, and in the case of a Server mode Connector, the
	 * Response is carried out.
	 * <p>
	 * This behavior is identical to that caused by the following call:
	 * <p>
	 * <tt>system.exitBranch("Flow");</tt>
	 * 
	 * @throws ExitBranchException
	 *             to tell the AssemblyLine to exit the Flow Section
	 */
	public void exitFlow() throws com.ibm.di.exceptions.ExitBranchException {
		throw new com.ibm.di.exceptions.ExitBranchException("Flow");
	}

	/**
	 * Throws an ExitBranchException that tells the AssemblyLine to not execute
	 * any more of the Flow Section components. In other words, the current
	 * cycle of the AL ends.
	 * <p>
	 * If the skipResponse parameter pass is <tt>false</tt>, then in the case of
	 * a Server mode Connector, the Response is carried out. If skipResponse is
	 * <tt>true</tt>, no Response is sent.
	 * 
	 * @param skipResponse
	 *            Whether or not a Response should be sent if a Server mode
	 *            Connector is feeding this AL.
	 * @throws ExitBranchException
	 *             to tell the AssemblyLine to exit the Flow Section
	 */
	public void exitFlow(boolean skipResponse) throws com.ibm.di.exceptions.ExitBranchException {
		throw new com.ibm.di.exceptions.ExitBranchException(skipResponse ? "Cycle" : "Flow");
	}

	/**
	 * Throws a ContinueloopException to tell the AssemblyLine to continue with
	 * the next value in the loop.
	 * 
	 * @throws ContinueLoopException
	 */
	public void continueLoop() throws com.ibm.di.exceptions.ContinueLoopException {
		throw new com.ibm.di.exceptions.ContinueLoopException();
	}

	/**
	 * Throws a ContinueLoopException to tell the AssemblyLine to continue with
	 * the next value in the named loop.
	 * 
	 * @param name
	 *            The name of the loop
	 * @throws ContinueLoopException
	 */
	public void continueLoop(String name) throws com.ibm.di.exceptions.ContinueLoopException {
		throw new com.ibm.di.exceptions.ContinueLoopException(name);
	}

	/**
	 * Load a Connector Interface from the current Config.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var con = system.loadConnector(&quot;ADChangelogConnectorv2&quot;);
	 * con.initialize(null);
	 * </pre>
	 * 
	 * @param connectorName
	 *            The connector name as it appears in the configuration file
	 * @return The connector object
	 */
	public com.ibm.di.connector.ConnectorInterface loadConnector(String connectorName) {
		try {
			return SystemFunctions.loadConnector(connectorName, getServer());
		} catch (Exception e) {
			lastError = e;
			return null;
		}
	}

	/**
	 * Returns the number of milliseconds since Jan 1 1970 as a string.
	 * 
	 * @return Number of milliseconds
	 */
	public String dtSeconds() {
		Date d = new Date();
		String str = Long.toString(d.getTime());
		d = null;
		return str;
	}

	/**
	 * Causes the current thread (e.g. AssemblyLine, etc..) to sleep for a
	 * number of seconds. If the sleep is interrupted the InterruptedException
	 * value is returned. If not, null is returned.
	 * 
	 * @param seconds
	 *            Number of seconds to sleep
	 * @return null if successful, exception object otherwise
	 */
	public InterruptedException sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000L);
			return null;
		} catch (InterruptedException e) {
			return e;
		}
	}

	/**
	 * Removes occurrences of characters from a string. The method is case
	 * sensitive.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var str = &quot;Some short string&quot;;
	 * var str1 = system.removeStringChars(str, 's');
	 * task.logmsg(&quot;Result: &quot; + str1); //Some hort tring
	 * </pre>
	 * 
	 * @param source
	 *            The source string
	 * @param fromSet
	 *            A string specifying characters to be removed from source
	 * @return The resulting string
	 */
	public String removeStringChars(String source, String fromSet) {
		StringBuffer res = new StringBuffer();

		for (int i = 0; i < source.length(); i++) {
			char ch = source.charAt(i);
			if (fromSet.indexOf(ch) == -1)
				res.append(ch);
		}
		return res.toString();
	}

	/**
	 * Convert A String Into Title Case (Like This), using the current Locale.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var str = &quot;some short string&quot;;
	 * var str1 = system.makeTitleCase(str);
	 * task.logmsg(&quot;Result: &quot; + str1); //Some Short String
	 * </pre>
	 * 
	 * @param in
	 *            The string to convert
	 * @return The converted string
	 */
	public String makeTitleCase(String in) {
		if (in != null) {
			StringBuffer out = new StringBuffer(in.length());
			StringTokenizer tokens = new StringTokenizer(in, "' \t\n\r-", true);
			while (tokens.hasMoreElements()) {
				String token = tokens.nextToken();
				if (token.length() >= 1) {
					out.append(Character.toTitleCase(token.charAt(0)));
				}
				if (token.length() >= 2) {
					out.append(token.substring(1).toLowerCase());
				}
			}
			return out.toString();
		}
		return null;
	}

	/**
	 * Translates characters in a string. The fromSet and toSet contains the
	 * characters used to perform substitution. The first character in fromSet
	 * is replace with the first character in toSet etc.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var str = system.mapString(&quot;Some example text&quot;, &quot;Somexamplt&quot;, &quot;Noneracklg&quot;);
	 * task.logmsg(&quot;Result: &quot; + str); //None erankle gerg
	 * </pre>
	 * 
	 * @param source
	 *            The source string
	 * @param fromSet
	 *            The characters to be replaced
	 * @param toSet
	 *            The characters to replace characters in fromSet
	 * @return The substituted string
	 */
	public String mapString(String source, String fromSet, String toSet) {
		String res = source;

		if (fromSet.length() != toSet.length())
			return source;

		for (int i = 0; i < fromSet.length(); i++) {
			res = res.replace(fromSet.charAt(i), toSet.charAt(i));
		}

		return res;
	}

	/**
	 * Translate a string from one character set to another.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var str = system.translateString(&quot;Some example text&quot;, &quot;UTF-8&quot;, &quot;UTF-16&quot;);
	 * task.logmsg(&quot;Result: &quot; + str);
	 * </pre>
	 * 
	 * @param str
	 *            The source string
	 * @param fromCharset
	 *            The source character set
	 * @param toCharset
	 *            The target character set
	 * @return The translated string
	 */
	public String translateString(String str, String fromCharset, String toCharset) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(str.getBytes());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			InputStreamReader is;
			OutputStreamWriter os;

			if (fromCharset != null && fromCharset.length() > 0)
				is = new InputStreamReader(bis, fromCharset);
			else
				is = new InputStreamReader(bis);

			if (toCharset != null && toCharset.length() > 0)
				os = new OutputStreamWriter(bos, toCharset);
			else
				os = new OutputStreamWriter(bos);

			int ch;

			while ((ch = is.read()) != -1)
				os.write(ch);

			os.flush();
			return bos.toString();

		} catch (Exception e) {
			lastError = e;
			return null;
		}
	}

	/**
	 * Converts a string to a hexadecimal string where each character is
	 * converted to a two-byte hex value.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var str = system.toHex(&quot;text&quot;);
	 * task.logmsg(&quot;Result: &quot; + str); //74 65 78 74
	 * </pre>
	 * 
	 * @param str
	 *            The source string
	 * @return The hexadecimal string
	 */
	public String toHex(String str) {

		return com.ibm.di.util.StringUtils.toHex(str);
	}

	/**
	 * Returns an attribute value from an X.400 address.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var str = &quot;C=no;ADMD= ;PRMD=uninett;O=sintef;OU=delab;S=Smith;G=John&quot;;
	 * task.logmsg(&quot;Result: &quot; + system.getX400Attribute(str, ';', &quot;PRMD&quot;));
	 * </pre>
	 * 
	 * @param x400
	 *            The X.400 address
	 * @param sep
	 *            The separator used in the address ( typically "/" or ";" )
	 * @param attribute
	 *            The X.400 attribute
	 * @return The value or null if no attribute was found
	 */
	public String getX400Attribute(String x400, String sep, String attribute) {
		StringTokenizer st = new StringTokenizer(x400, sep);

		while (st.hasMoreTokens()) {
			String str = st.nextToken();
			int index = str.indexOf("=");
			if ((index > -1) && (str.substring(0, index).equalsIgnoreCase(attribute))) {
				return str.substring(index + 1);
			}
		}

		return null;
	}

	/**
	 * Converts an X.400 address to a string using short form attribute names.
	 * Attributes are sorted in order of significance.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var str = &quot;C=no;ADMD= ;PRMD=uninett;O=sintef;OU=delab;S=Smith;G=John&quot;;
	 * task.logmsg(&quot;Result: &quot; + system.normalizeX400(str, ';', '/'));
	 * </pre>
	 * 
	 * @param value
	 *            The X.400 address
	 * @param cursep
	 *            The separator used in value
	 * @param newsep
	 *            The separator to be used in the result
	 * @return The reformatted X.400 address
	 */
	public String normalizeX400(String value, String cursep, String newsep) {
		StringTokenizer st = new StringTokenizer(value, cursep);
		Hashtable<String, String> h = new Hashtable<String, String>();

		// System.out.println ("Get X.400 attribute '" + key + "' from '" +
		// value + "'");
		while (st.hasMoreTokens()) {
			String x = st.nextToken();
			// System.out.println ("Check : " + x);

			int index = x.indexOf('=');
			if (index < 0)
				continue;

			h.put(x.substring(0, index).toLowerCase(Locale.ENGLISH), x.substring(index + 1));
		}

		StringBuffer result = new StringBuffer();
		String[] order = { "c", "admd", "prmd", "o", "ou1", "ou2", "ou3", "ou4", "s", "g", "i" };
		String[] order2 = { "c", "a", "p", "o", "ou1", "ou2", "ou3", "ou4", "s", "g", "i" };
		for (int i = 0; i < order.length; i++) {
			String key = h.get(order[i]);
			if (key != null) {
				result.append(order2[i]);
				result.append("=");
				result.append(key);
				result.append(newsep);
			}
		}

		return result.toString();
	}

	/**
	 * Converts a String to a java.util.Date object.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var dateobj = system.parseDate(&quot;23/01/07&quot;, &quot;DD/MM/yy&quot;);
	 * task.logmsg(&quot;Result: &quot; + dateobj); //Tue Jan 23 00:00:00 EET 2007
	 * </pre>
	 * 
	 * @param value
	 *            A string representing date
	 * @param format
	 *            The format of <i>value</i> (e.g. "yyyy.MM.DD", "MM/DD/yy" etc
	 *            ...) A complete list of format characters can be found at
	 *            http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html
	 * @return The Date object or null if an error occurred
	 * @see #lastError
	 */
	public Date parseDate(String value, String format) {
		try {
			return new java.text.SimpleDateFormat(format).parse(value);
		} catch (Exception e) {
			lastError = e;
			try {
				return new SimpleDateFormat(format).parse(value);
			} catch (Exception e2) {
				return null;
			}
		}
	}

	/**
	 * This method formats a java.util.Date object using the provided template.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var d = com.ibm.icu.util.Calendar.getInstance().getTime();
	 * task.logmsg(&quot;Result: &quot; + system.formatDate(d, &quot;dd/MM/yy&quot;));
	 * task.logmsg(&quot;Result: &quot; + system.formatDate(d, &quot;yyyy.MM.dd&quot;));
	 * </pre>
	 * 
	 * @param date
	 *            The date object
	 * @param format
	 *            The format of <i>value</i> (e.g. "yyyy.MM.dd", "MM/dd/yy" etc
	 *            ...) A complete list of format characters can be found at
	 *            http://icu.sourceforge.net/apiref/icu4j/com/ibm/icu/text/
	 *            SimpleDateFormat.html.
	 * @return The string representation or null if an error occurred
	 * @see #lastError
	 */
	public String formatDate(Date date, String format) {
		try {
			SimpleDateFormat df = new SimpleDateFormat(format);
			return df.format(date);
		} catch (Exception e) {
			lastError = e;
			return null;
		}
	}

	/**
	 * Splits a string into an array of strings.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var str = &quot;Some short string&quot;;
	 * task.logmsg(&quot;Result: &quot; + system.splitString(str, ' ')); //Some,short,string
	 * </pre>
	 * 
	 * @param source
	 *            The source string
	 * @param separators
	 *            The word-separating characters
	 * @return Array of strings
	 */
	public String[] splitString(String source, String separators) {
		StringTokenizer st = new StringTokenizer(source, separators);
		String res[] = new String[st.countTokens()];
		int i = 0;
		while (st.hasMoreTokens())
			res[i++] = st.nextToken();

		return res;
	}

	/**
	 * Load a connector. This method loads a connector from the current config
	 * file. The call to this method is the same as loadConnector(String).
	 * 
	 * @param name
	 *            The connector name as it appears in the configuration file
	 * @return The connector object
	 * @see #loadConnector(String)
	 */
	public com.ibm.di.connector.ConnectorInterface getConnector(String name) {
		try {
			return SystemFunctions.loadConnector(name, getServer());
		} catch (Exception e) {
			lastError = e;
			return null;
		}
	}

	/**
	 * Load a parser Interface from the current Config.
	 * 
	 * @param name
	 *            The parser name as it appears in the configuration file
	 * @return The parser object
	 */
	public com.ibm.di.parser.ParserInterface getParser(String name) {
		try {
			return SystemFunctions.loadParser(name, getServer());
		} catch (Exception e) {
			lastError = e;
			return null;
		}
	}

	/**
	 * Use a parser to interpret data. This method will either use the data
	 * object as-is if it is a reader or inputstream class, or it will create a
	 * StringReader from the string representation of the data object and pass
	 * it to the parser. The parser will be called to interpret the byte stream
	 * and return an Entry. If the parse fails a null is returned.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var data = new java.io.FileInputStream(&quot;c:\\docs\\LDIFfile.txt&quot;);
	 * var entry = system.newEntry();
	 * entry = system.parseObject(&quot;LDIFParser&quot;, data);
	 * task.dumpEntry(entry);
	 * </pre>
	 * 
	 * @param parser
	 *            The parser name
	 * @param data
	 *            Any object of type Reader, InputStream or object that has a
	 *            toString method
	 * @return The parsed entry or null if the parser fails
	 * @see #lastError
	 */
	public com.ibm.di.entry.Entry parseObject(String parser, Object data) {
		ParserInterface p = getParser(parser);
		if (p == null)
			return null;

		try {
			if (data instanceof Reader) {
				p.setInputStream((Reader) data);
			} else if (data instanceof InputStream) {
				p.setInputStream((InputStream) data);
			} else {
				p.setInputStream(new StringReader(data.toString()));
			}

			p.initParser();
			return p.readEntry();

		} catch (Exception e) {
			lastError = e;
			return null;
		}
	}

	/**
	 * Executes a shell command.
	 * 
	 * @param command
	 *            A String containing the shell command to execute. This String
	 *            will be parsed with a simple StringTokenizer, to split the
	 *            command and arguments.
	 * @return An ExecuteCommand object
	 * @see ExecuteCommand
	 */
	public ExecuteCommand shellCommand(String command) {
		ExecuteCommand cmd = new ExecuteCommand();
		cmd.exec(command);
		return cmd;
	}

	/**
	 * Executes a shell command with arguments.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 *    myArr = [&quot;-c&quot;, &quot;/bin/ls&quot;, &quot;/mnt/cd rom&quot;];
	 *    cmd = system.shellCommand (&quot;su&quot;, myArr);
	 *    main.logmsg(&quot;The result was:\n&quot; + cmd.getOutputBuffer() );
	 * </pre>
	 * 
	 * @param command
	 *            The shell command to execute
	 * @param args
	 *            The arguments to the command. E.g. a String array containing
	 *            the arguments.
	 * @return An ExecuteCommand object
	 * @see ExecuteCommand
	 */
	public ExecuteCommand shellCommand(String command, Object args) {
		String[] cmdarray;

		if (args instanceof Object[]) {
			Object[] arr = (Object[]) args;
			cmdarray = new String[arr.length + 1];

			for (int i = 0; i < arr.length; i++) {
				cmdarray[i + 1] = (arr[i] == null) ? "" : arr[i].toString();
			}
		} else if (args instanceof Collection<?>) {
			Collection<?> c = (Collection<?>) args;
			cmdarray = new String[c.size() + 1];
			int i = 1;

			for (Object arg : c) {
				cmdarray[i++] = arg.toString();
			}
		} else if (args != null) {
			cmdarray = new String[2];
			cmdarray[1] = args.toString();
		} else {
			cmdarray = new String[1];
		}

		cmdarray[0] = command;

		ExecuteCommand cmd = new ExecuteCommand();
		cmd.exec(cmdarray);
		return cmd;
	}

	/**
	 * Executes a shell command with encoding (codepage).
	 * On some operating systems, e.g. Windows, an issue could
	 * arise because the output from the command is encoded
	 * with an old codepage. This method allows you to specify
	 * the encoding to use when reading the output from the command.
	 * 
	 * @param command
	 *            A String containing the shell command to execute. This String
	 *            will be parsed with a simple StringTokenizer, to split the
	 *            command and arguments.
	 * @param cp The CodePage (encoding) to use
	 * @return An ExecuteCommand object
	 * @see ExecuteCommand
	 */
	public ExecuteCommand shellCommandCP(String command, String cp) {
		ExecuteCommand cmd = new ExecuteCommand();
		cmd.setEncoding(cp);
		cmd.exec(command);
		return cmd;
	}

	/**
	 * Executes a shell command with encoding (codepage) and arguments.
	 * On some operating systems, e.g. Windows, an issue could
	 * arise because the output from the command is encoded
	 * with an old codepage. This method allows you to specify
	 * the encoding to use when reading the output from the command.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 *    myArr = [&quot;-c&quot;, &quot;/bin/ls&quot;, &quot;/mnt/cd rom&quot;];
	 *    cmd = system.shellCommand (&quot;su&quot;, &quot;cp850&quot;, myArr);
	 *    main.logmsg(&quot;The result was:\n&quot; + cmd.getOutputBuffer() );
	 * </pre>
	 * 
	 * @param command
	 *            The shell command to execute
	 * @param cp The CodePage to use
	 * @param args
	 *            The arguments to the command. E.g. a String array containing
	 *            the arguments. 
	 * @return An ExecuteCommand object
	 * @see ExecuteCommand
	 */
	public ExecuteCommand shellCommand(String command, String cp, Object args) {
		String[] cmdarray;

		if (args instanceof Object[]) {
			Object[] arr = (Object[]) args;
			cmdarray = new String[arr.length + 1];

			for (int i = 0; i < arr.length; i++) {
				cmdarray[i + 1] = (arr[i] == null) ? "" : arr[i].toString();
			}
		} else if (args instanceof Collection<?>) {
			Collection<?> c = (Collection<?>) args;
			cmdarray = new String[c.size() + 1];
			int i = 1;

			for (Object arg : c) {
				cmdarray[i++] = arg.toString();
			}
		} else if (args != null) {
			cmdarray = new String[2];
			cmdarray[1] = args.toString();
		} else {
			cmdarray = new String[1];
		}

		cmdarray[0] = command;

		ExecuteCommand cmd = new ExecuteCommand();
		cmd.setEncoding(cp);
		cmd.exec(cmdarray);
		return cmd;
	}

	/**
	 * Returns the name of the operating system.
	 * 
	 * @return The OS name
	 */
	public String getOSName() {
		return System.getProperty("os.name");
	}

	/**
	 * Returns the value for a system property.
	 * 
	 * @param prop
	 *            The property name
	 * @return The property value or null if no such property exists
	 */
	public String getJavaProperty(String prop) {
		return System.getProperty(prop);
	}

	/**
	 * Sets the value of a property name.
	 * 
	 * @param prop
	 *            The property name
	 * @param value
	 *            The property value
	 */
	public void setJavaProperty(String prop, String value) {
		System.setProperty(prop, value);
	}

	/**
	 * Converts an Entry object to an LDIF string. If the passed entry is tagged
	 * with delta codes then the resulting LDIF will be <i>incremental</i>,
	 * reflecting this tagging.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var entry = system.newEntry();
	 * entry.addAttributeValue(&quot;$dn&quot;, &quot;cn=Login Server&quot;);
	 * entry.addAttributeValue(&quot;cn&quot;, &quot;Login Server&quot;);
	 * entry.addAttributeValue(&quot;description&quot;, &quot;Central Authentication Authority&quot;);
	 * entry.addAttributeValue(&quot;objectClass&quot;, &quot;top&quot;);
	 * entry.addAttributeValue(&quot;objectClass&quot;, &quot;applicationProcess&quot;);
	 * task.logmsg(&quot;Result: &quot; + system.entry2LDIF(entry));
	 * </pre>
	 * 
	 * @param e
	 *            The entry
	 * @return The LDIF string
	 * @see #lastError
	 */
	public String entry2LDIF(Entry e) {
		try {
			LDIFParser ldif = new LDIFParser();
			StringWriter sw = new StringWriter();
			ldif.setOutputStream(sw);
			ldif.writeEntry(e);
			return sw.toString();
		} catch (Exception err) {
			lastError = err;
			return null;
		}
	}

	/**
	 * Returns an instance of the FTP object.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var ftpbean = system.getFTP();
	 * ftpbean.connect(&quot;ftp://ftp.myhost.com&quot;, &quot;user&quot;, &quot;pass&quot;);
	 * ftpbean.get(&quot;ftp://ftp.myhost.com/myfile.txt&quot;, &quot;c:\\docs\\myfile.txt&quot;);
	 * </pre>
	 * 
	 * @return The FTP object
	 * @see com.ibm.di.protocols.FTPBean
	 */
	public com.ibm.di.protocols.FTPBean getFTP() {
		return new com.ibm.di.protocols.FTPBean();
	}

	/**
	 * Dumps an entry to the console log. In order to get more verbose
	 * information use the toDeltaString() method of an {@link Entry}.
	 * 
	 * @param e
	 *            The entry object
	 * @see Entry
	 */
	public void dumpEntry(Entry e) {
		((RS) getServer()).getLog().dump(e);
	}

	/**
	 * Returns the Apache XPathAPI
	 * 
	 * @return XPathAPI object
	 */
	public XPathAPI getXPathAPI() {
		return new XPathAPI();
	}

	/**
	 * Selects a single node using an XPath expression from an XML node. For
	 * example if we have the following xml file:
	 * <p>
	 * <code>
	 * &lt;?xml version="1.0" ?&gt;<br>
	 * 	&lt;note&gt;<br>
	 * 		&lt;from&gt;Tony&lt;/from&gt;<br>
	 * 		&lt;to&gt;Michael&lt;/to&gt;<br>
	 * 		&lt;to&gt;John&lt;/to&gt;<br>
	 * 		&lt;heading&gt;Question&lt;/heading&gt;<br>
	 * 		&lt;body&gt;Are you ready?&lt;/body&gt;<br>
	 * 	&lt;/note&gt;
	 * </code>
	 * <p>
	 * Since com.ibm.di.entry.Entry implements the org.w3c.dom.Document to get
	 * the first <code>from</code> node we could use XMLParser to read an Entry
	 * which could be passed to this method as a <code>contextNode</code>
	 * parameter.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var entry = input.getConnector().getNextEntry();
	 * var res = system.selectSingleNode(entry, &quot;note/to&quot;);
	 * task.logmsg(res); // to:Michael
	 * </pre>
	 * 
	 * @param contextNode
	 *            The XML document node
	 * @param str
	 *            The XPath search string
	 * @return XML Document node
	 * @see #lastError
	 */
	public Node selectSingleNode(Node contextNode, String str) {
		try {
			return XPathAPI.selectSingleNode(contextNode, str);
		} catch (Exception e) {
			lastError = e;
			return null;
		}
	}

	/**
	 * Selects nodes using an XPath expression from an XML node. For example if
	 * we have the following xml file:
	 * <p>
	 * <code>
	 * &lt;?xml version="1.0" ?&gt;<br>
	 * 	&lt;note&gt;<br>
	 * 		&lt;from&gt;Tony&lt;/from&gt;<br>
	 * 		&lt;to&gt;Michael&lt;/to&gt;<br>
	 * 		&lt;to&gt;John&lt;/to&gt;<br>
	 * 		&lt;heading&gt;Question&lt;/heading&gt;<br>
	 * 		&lt;body&gt;Are you ready?&lt;/body&gt;<br>
	 * 	&lt;/note&gt;
	 * </code>
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var entry = input.getConnector().getNextEntry();
	 * var res = system.selectNodeList(entry, &quot;note/to&quot;);
	 * for (var i = 0; i &lt; res.getLength(); i++) {
	 * 	task.logmsg(res.item(i)); //to:Michael, to:John
	 * }
	 * </pre>
	 * 
	 * @param contextNode
	 *            The XML document node
	 * @param str
	 *            The XPath search string
	 * @return A NodeList object
	 * @see #lastError
	 * @see #selectSingleNode(Node, String)
	 */
	public NodeList selectNodeList(Node contextNode, String str) {
		try {
			return XPathAPI.selectNodeList(contextNode, str);
		} catch (Exception e) {
			lastError = e;
			return null;
		}
	}

	/**
	 * Selects nodes using an XPath expression from an XML node. For example if
	 * we have the following xml file:
	 * <p>
	 * <code>
	 * &lt;?xml version="1.0" ?&gt;<br>
	 * 	&lt;note&gt;<br>
	 * 		&lt;from&gt;Tony&lt;/from&gt;<br>
	 * 		&lt;to&gt;Michael&lt;/to&gt;<br>
	 * 		&lt;to&gt;John&lt;/to&gt;<br>
	 * 		&lt;heading&gt;Question&lt;/heading&gt;<br>
	 * 		&lt;body&gt;Are you ready?&lt;/body&gt;<br>
	 * 	&lt;/note&gt;
	 * </code>
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var entry = input.getConnector().getNextEntry();
	 * var iter = system.selectNodeIterator(entry, &quot;note&quot;);
	 * var node;
	 * while (node = iter.nextNode()) {
	 * 	task.logmsg(node);
	 * }
	 * </pre>
	 * 
	 * @param contextNode
	 *            The XML document node
	 * @param str
	 *            The XPath search string
	 * @return A NodeIterator object
	 * @see #lastError
	 * @see #selectSingleNode(Node, String)
	 */
	public NodeIterator selectNodeIterator(Node contextNode, String str) {
		try {
			return XPathAPI.selectNodeIterator(contextNode, str);
		} catch (Exception e) {
			lastError = e;
			return null;
		}
	}

	/**
	 * Calls the XSLTransformer to transform an XML document using a given style
	 * sheet."\n" needs to be present in the XSL and XML string for xslTransfrom
	 * to work correctly.
	 * 
	 * @param xsl
	 *            The XSL Style sheet (String, java.io.File, java.io.Reader )
	 * @param xml
	 *            The XML document (String, java.io.File, java.io.Reader )
	 * @return The translated document
	 * @see #lastError
	 */
	public String xslTransform(Object xsl, Object xml) {
		try {
			// Transform
			TransformerFactory transfactory = TransformerFactory.newInstance();
			Transformer transformer;

			ErrorListenerImpl el = new ErrorListenerImpl();
			transfactory.setErrorListener(el);
			transformer = transfactory.newTransformer(getStreamSource(xsl));
			if (el.excep != null)
				throw el.excep;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			el.excep = null;
			transformer.setErrorListener(el);
			transformer.transform(getStreamSource(xml), new StreamResult(bos));
			String outputencoding = transformer.getOutputProperty("encoding");// Defect
			// 11699

			if (el.excep != null)
				throw el.excep;

			// Defect # 11699
			if (outputencoding != null) {
				return bos.toString(outputencoding);
			} else {
				return bos.toString();
			}
		} catch (Exception error) {
			lastError = error;
			return null;
		}
	}

	/**
	 * Return a StreamSource, if the argument is a filename, String (with
	 * newlines), Reader, InputStream or File
	 * 
	 * @param o
	 *            The Object that is to be used as the source
	 * @return A StreamSource object
	 * @throws IllegalArgumentException
	 *             if the argument is not recognized
	 */
	private StreamSource getStreamSource(Object o) throws IllegalArgumentException {
		if (o instanceof String) {
			if (((String) o).indexOf("\n") != -1)
				return new StreamSource(new StringReader((String) o));
			else
				return new StreamSource(new File((String) o));
		}

		if (o instanceof Reader)
			return new StreamSource((Reader) o);

		if (o instanceof InputStream)
			return new StreamSource((InputStream) o);

		if (o instanceof File)
			return new StreamSource((File) o);

		if (o instanceof StreamSource)
			return (StreamSource) o;

		throw new IllegalArgumentException(sResHash.getString("USER.FUNCTIONS.ARGUMENT.NOT.DOCUMENT", o));
	}

	/**
	 * Dumps the public methods for a Java class.
	 * 
	 * @param className
	 *            The java class name
	 * @return True if dump succeeded
	 * @see #lastError
	 */
	public boolean dumpJavaClass(String className) {
		try {
			Class<?> cls = Class.forName(className);
			dumpJavaClass(cls, System.out, "");
			return true;
		} catch (Exception e) {
			lastError = e;
			return false;
		}
	}

	private boolean dumpJavaClass(Class<?> cls, PrintStream out, String indent) {
		try {
			Method[] m = cls.getDeclaredMethods();
			int i;
			out.println(indent + sResHash.getString("USER.FUNCTIONS.DUMP.JAVA.CLASS.CLASSNAME", cls.getName()));
			for (i = 0; i < m.length; i++) {
				out.print(indent + "\t" + m[i].getName() + " (");
				Class<?>[] p = m[i].getParameterTypes();
				for (int j = 0; j < p.length; j++) {
					if (j > 0)
						out.print(", ");
					out.print(p[j].getName());
				}
				out.println(indent + ");");
			}
			Class<?>[] other = cls.getClasses();
			for (i = 0; i < other.length; i++) {
				out.println(indent + sResHash.getString("USER.FUNCTIONS.DUMP.JAVA.CLASS.SUPERCLASS"));
				dumpJavaClass(other[i], out, indent + "   ");
			}

			return true;
		} catch (Exception e) {
			lastError = e;
			return false;
		}
	}

	/**
	 * Change Java runtime working directory. Sets the "user.dir" property.
	 * 
	 * @param directory
	 *            File system directory
	 * @return True if directory exists, false if directory is not valid
	 */
	public boolean chdir(String directory) {

		File f = new File(directory);
		if (!f.exists())
			return false;

		System.setProperty("user.dir", directory);
		return true;
	}

	/**
	 * Returns the current working directory.
	 * 
	 * @return working directory
	 */

	public String getcwd() {
		return (new File("")).getAbsolutePath();
	}

	/**
	 * Returns the text from the Script Library.
	 * 
	 * @param name
	 *            The script name as it appears in the configuration.
	 * @return The script text or null if not found.
	 */
	public String getScriptText(String name) {
		ScriptConfig tm = SystemFunctions.loadScript(name, getServer());
		if (tm == null)
			return null;
		else
			return tm.getScript();
	}

	/**
	 * Sends an SNMP trap. This method only accepts a String as the value. If
	 * you need to send more complex data use the other snmpTrap() method in
	 * this library.
	 * 
	 * @param host
	 *            The IP host
	 * @param port
	 *            The TCP port
	 * @param oid
	 *            The OID
	 * @param value
	 *            The value
	 * @return True if Trap was sent
	 * @see #lastError
	 */
	public boolean snmpTrap(String host, int port, String oid, String value) {
		try {
			com.ibm.di.protocols.SNMP.sendTrap(host, port, oid, value);
			return true;
		} catch (Exception e) {
			lastError = e;
			return false;
		}
	}

	/**
	 * Sends an SNMP trap. This method allows you to set most of the attributes
	 * of the SNMP trap PDU. If <code>oid</code> is null, <code>value</code>
	 * must be an Entry. All Attribute names will be taken as oids, and the
	 * values of that Attribute will be the corresponding values.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var entry = system.newEntry();
	 * entry.setAttribute(&quot;1.2.3.4.1&quot;, &quot;MyString&quot;);
	 * entry.setAttribute(&quot;1.2.3.4.2&quot;, com.ibm.di.protocols.SNMP.createIPAddress(&quot;10.0.0.1&quot;));
	 * entry.setAttribute(&quot;1.2.3.4.3&quot;, com.ibm.di.protocols.SNMP.createGauge(200));
	 * 
	 * if (!system.snmpTrap(&quot;192.1.1.1&quot;, targetIP, 162, &quot;public&quot;, enterpriseOID, 0, 0, null, entry)) {
	 * 	task.logmsg(&quot;Error sending trap: &quot; + system.lastError);
	 * }
	 * </pre>
	 * 
	 * If oid is non-null, value should be a java.util.Vector, a javascript
	 * array or any other object. The conversion of the values to SNMP PDU
	 * values are as follows: If you provide an object whose class starts with
	 * "com.tivoli.snmp.data" the value is used asis (see
	 * com.ibm.di.protocols.SNMP on how to create these objects). If you provide
	 * an Integer then a com.tivoli.snmp.data.Counter object is created. In all
	 * other cases an OctetString object is created from the object value's
	 * toString() method.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 *     var varBind = [ &quot;MyString&quot;, com.ibm.di.protocols.SNMP.createIPAddress(&quot;10.0.0.1&quot;), com.ibm.di.protocols.SNMP.createGauge(200) ];
	 *     if ( !system.snmpTrap( &quot;192.1.1.1&quot;, targetIP, 162, &quot;public&quot;, enterpriseOID, 0, 0, &quot;1.2.3.4&quot;, varBind) ) {
	 *     	task.logmsg(&quot;Error sending trap: &quot; + system.lastError);
	 *     }
	 * </pre>
	 * 
	 * @param agentIP
	 *            The agent IP address or null to use the local host ip address
	 *            (e.g. InetAddress.getLocalHost().getHostAddress())
	 * @param host
	 *            The target IP host
	 * @param port
	 *            The target TCP port
	 * @param community
	 *            The SNMP community string
	 * @param enterprise
	 *            The Enterprise OID
	 * @param genericTrap
	 *            Trap type: coldStart(0), warmStart(1), linkDown(2), linkUp(3),
	 *            authenticationFailure(4), egpNeighborLoss(5),
	 *            enterpriseSpecific(6)
	 * @param specificTrap
	 *            Used for enterpriseSpecific traps
	 * @param oid
	 *            The OID for the values. If oid is null, value must be an Entry
	 *            where the Attribute names will be used as OIDs
	 * @param value
	 *            The value(s)
	 * @return True if Trap was sent, otherwise check the system.lastError
	 *         object for the exception
	 * @see #lastError
	 */
	public boolean snmpTrap(String agentIP, String host, int port, String community, String enterprise, int genericTrap,
			int specificTrap, String oid, Object value) {
		try {
			com.ibm.di.protocols.SNMP.sendTrap(agentIP, host, port, community, enterprise, genericTrap, specificTrap, oid, value);
			return true;
		} catch (Exception e) {
			lastError = e;
			return false;
		}
	}

	/**
	 * Gets file from a web server. Calling this method is equivalent to calling
	 * httpRequest(&quot;GET&quot;, null, url, null) and both will return
	 * identical results.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var response = system.httpGet(&quot;http://www.mysite.com/files&quot;);
	 * if (response == null) {
	 * 	task.logmsg(&quot;Error getting file: &quot; + system.lastError);
	 * }
	 * </pre>
	 * 
	 * @param url
	 *            Identifies the resource to get from the web server
	 * @return The response from the server is encapsulated into an Entry object
	 *         or NULL if an error occurred.
	 * @see #httpRequest(String, String, String, Object)
	 * @see #lastError
	 */
	public Entry httpGet(String url) {
		return httpRequest("GET", null, url, null);
	}

	/**
	 * Posts file to a web server. This method sends to the server request
	 * message with content type "application/octet-stream". So
	 * <code>file</code> typically will be an application or a document that
	 * must be opened in an application.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 *     var file = &quot;c:\\docs\\myfile.doc&quot;);
	 *     var response =system.httpPost(&quot;http://www.mysite.com/files&quot;,file);
	 *     if (response == null){
	 *      	task.logmsg(&quot;Error posting file: &quot; + system.lastError);
	 *      } else {
	 *      	task.logmsg(&quot;HTTP server response: &quot; + response);
	 *      }
	 * </pre>
	 * 
	 * @param url
	 *            The URL to the web server
	 * @param file
	 *            The file name to be sent. You can provide this parameter as a
	 *            String or as a java.io.File object. If this parameter is NULL
	 *            the method will do as GET with no additional data, otherwise a
	 *            POST is performed.
	 * @return The response from the server is encapsulated into an Entry object
	 *         or NULL if an error occurred.
	 * @see #httpRequest(String, String, String, Object)
	 * @see #lastError
	 */
	public Entry httpPost(String url, Object file) {
		return httpRequest("POST", "application/octet-stream", url, file);
	}

	/**
	 * Sends HTTP Request message to web server. This methods uses HTTPClient
	 * Connector to send request message of type specified by
	 * <code>method</code> to web server at given address <code>url</code>.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var file = new java.lang.FileInputStream(&quot;c:\\docs\\myfile.html&quot;);
	 * var response;
	 * if (file.exist())
	 * 	response = system.httpRequest(&quot;POST&quot;, &quot;text/html&quot;, &quot;http://www.mysite.com/files&quot;, file);
	 * if (response == null) {
	 * 	task.logmsg(&quot;Error sending file: &quot; + system.lastError);
	 * } else {
	 * 	task.logmsg(&quot;HTTP server response: &quot; + response);
	 * }
	 * </pre>
	 * 
	 * @param method
	 *            Type of request method. Possible values: POST, GET, PUT etc.
	 * @param contentType
	 *            Type of the contents.
	 * @param url
	 *            The URL to the web server
	 * @param file
	 *            The body of the request message
	 * @return The response from the server is encapsulated into an Entry object
	 *         or NULL if an error occurred.
	 * @see #httpGet(String)
	 * @see #httpPost(String, Object)
	 */
	public Entry httpRequest(String method, String contentType, String url, Object file) {
		try {
			ConnectorInterface http = getConnector("ibmdi.HTTPClient");
			if (http == null)
				return null;
			http.initialize(null);
			Entry entry = new Entry();
			entry.setAttribute("http.url", url);
			entry.setAttribute("http.method", method);
			if (file != null)
				entry.setAttribute("http.body", file);

			if (contentType != null)
				entry.setAttribute("http.content-type", contentType);

			http.putEntry(entry);
			http.terminate();
			return entry;
		} catch (Exception error) {
			lastError = error;
			return null;
		}
	}

	/**
	 * Converts a ByteArray to a string using platform's default charset. For
	 * example, if you want to set a password(which is sometime a binary value)
	 * you could use this in the attribute mapping.
	 * <p>
	 * <b>Example: </b>
	 * 
	 * <pre>
	 * ret.value = system.arrayToString(work.getObject(&quot;userpassword&quot;));
	 * </pre>
	 * 
	 * @param array
	 *            The byte array to be converted
	 * @return The String object created from byte array
	 */
	public String arrayToString(byte[] array) {
		return new String(array);
	}

	/**
	 * Deletes a file.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var filePath = new java.lang.String(&quot;c:\\docs\\myfile.txt&quot;);
	 * if (!system.deleteFile(filePath))
	 * 	main.logmsg(&quot;Error file &quot; + file + &quot; not deleted!&quot;);
	 * </pre>
	 * 
	 * @param filePath
	 *            The name of the file to be deleted
	 * @return True if file was deleted, false if not deleted or if an error
	 *         occurred
	 * @throws Exception
	 *             if <code>filePath</code> is not a file
	 * @see #lastError
	 */
	public boolean deleteFile(String filePath) throws Exception {
		try {
			File fp = new File(filePath);
			if (fp.isFile())
				return fp.delete();
			else {
				throw new Exception(sResHash.getString("USER.FUNCTIONS.NOTFILE.ERROR", filePath));
			}
		} catch (Exception error) {
			lastError = error;
			return false;
		}
	}

	/**
	 * Rename a file.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var oldName = &quot;c:\\docs\\myfile.txt&quot;;
	 * var newName = &quot;c:\\docs\\newname.txt&quot;;
	 * if (! system.renameFile(oldName, newName) ) {
	 *     //The rename failed. Handle the problem.
	 * }
	 * </pre>
	 * 
	 * @param oldName
	 *            The old name of the file
	 * @param newName
	 *            The new name of the file
	 * @return True if the rename succeeded, false otherwise
	 */
	public boolean renameFile(String oldName, String newName) {
		try {
			File file1 = new File(oldName);
			File file2 = new File(newName);
			return file1.renameTo(file2);
		} catch (Exception error) {
			lastError = error;
			return false;
		}
	}

	/**
	 * Copy a file. <b>Example:</b>
	 * 
	 * <pre>
	 * var oldName = &quot;c:\\docs\\myfile.txt&quot;;
	 * var newName = &quot;c:\\docs\\newname.txt&quot;;
	 * system.copyFile(oldName, newName);
	 * </pre>
	 * 
	 * @param oldFile
	 *            The name of the file to copy
	 * @param newFile
	 *            The name of the new file
	 * @return true if the copying succeeded, false if an exception occurred
	 * @see #lastError
	 */
	public boolean copyFile(String oldFile, String newFile) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(oldFile));
			BufferedWriter out = new BufferedWriter(new FileWriter(newFile));
			int c;

			while ((c = in.read()) != -1)
				out.write(c);

			in.close();
			out.close();
			return true;
		} catch (Exception error) {
			lastError = error;
			return false;
		}
	}

	/**
	 * Create an empty TaskCallBlock.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var tcb = system.newTCB();
	 * 
	 * tcb.setAssemblyLineName(&quot;ALName&quot;);
	 * tcb.setRunMode(com.ibm.di.server.AssemblyLine.RUNMODE_NORMAL); // &quot;normal&quot;
	 * 
	 * var entry = system.newEntry();
	 * entry.setAttribute(&quot;linenumber&quot;, &quot;1&quot;);
	 * entry.setAttribute(&quot;line&quot;, &quot;Simple line of text!&quot;);
	 * tcb.setInitialWorkEntry(entry);
	 * 
	 * var al = main.startAL(tcb);
	 * al.join(); // Wait for called AL to complete
	 * </pre>
	 * 
	 * @return TaskCallBlock object
	 * @see #newEntry()
	 */
	public TaskCallBlock newTCB() {
		return new com.ibm.di.server.TaskCallBlock();
	}

	/**
	 * Create a TaskCallBlock with i/o specifications from an existing
	 * assemblyline. The TCB will contain all input/output parameters as well as
	 * all connectors and their initial parameters and values.
	 * 
	 * @param assemblyLine
	 *            name of the assembly line
	 * @return TaskCallBlock object with i/o specifications from an existing
	 *         assemblyline
	 */
	public TaskCallBlock newTCB(String assemblyLine) {
		try {
			return new com.ibm.di.server.TaskCallBlock(assemblyLine, getServer().getTask(assemblyLine), null);
		} catch (Exception error) {
			lastError = error;
			return null;
		}
	}

	/**
	 * This method retrieves a named object from the default system property
	 * store.
	 * 
	 * @param key
	 *            The unique key
	 * @return Object
	 * @throws Exception
	 */
	public Object getPersistentObject(String key) throws Exception {
		return StoreFactory.getDefaultPropertyStore().getProperty(key);
	}

	/**
	 * This method stores a named object in the default system property store.
	 * 
	 * @param key
	 *            The unique key
	 * @param value
	 *            The object to store (must be java serializable)
	 * @return The old object if any
	 * @throws Exception
	 */
	public Object setPersistentObject(String key, Object value) throws Exception {
		return StoreFactory.getDefaultPropertyStore().setProperty(key, value);
	}

	/**
	 * This method deletes a named object in the default system property store.
	 * 
	 * @param key
	 *            The unique key
	 * @return The old object if any
	 * @throws Exception
	 */
	public Object deletePersistentObject(String key) throws Exception {
		return StoreFactory.getDefaultPropertyStore().removeProperty(key);
	}

	/**
	 * This method returns a Vector containing all AssemblyLines that were
	 * running when the function was called. The example code shows how to print
	 * the names of all running AssemblyLines.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var ral = system.getRunningALs();
	 * var al = new com.ibm.di.server.AssemblyLine();
	 * task.logmsg(&quot;Running ALs:&quot;);
	 * for (var i = 0; i &lt; ral.size(); i++) {
	 * 	al = ral.get(i);
	 * 	task.logmsg(al.getShortName());
	 * }
	 * </pre>
	 * 
	 * @return a java.util.Vector containing the AssemblyLines
	 */
	public static Vector<AssemblyLine> getRunningALs() {
		return Monitor.runningALs();
	}

	/**
	 * This method returns a Vector containing all AssemblyLines with the given
	 * name that were running when the function was called
	 * 
	 * @param name
	 *            Find all AssemblyLines with this name. Only the last part of
	 *            the name (after optional /) is used.
	 * @return a java.util.Vector containing the AssemblyLines
	 */
	public static Vector<AssemblyLine> getRunningALs(String name) {
		if (name == null)
			return Monitor.runningALs();

		int i = name.lastIndexOf('/');
		if (i >= 0)
			name = name.substring(i + 1);

		Vector<AssemblyLine> list = new Vector<AssemblyLine>();

		for (AssemblyLine al : Monitor.runningALs()) {
			String s = al.getName();
			i = s.lastIndexOf('/');
			if (i >= 0)
				s = s.substring(i + 1);
			if (name.equals(s))
				list.add(al);
		}

		return list;
	}

	/**
	 * This method returns a Vector containing all Sequences with the given
	 * name that were running when the function was called
	 * 
	 * @param name
	 *            Find all Sequences with this name. Only the last part of
	 *            the name (after optional /) is used.
	 * @return a java.util.Vector containing the Sequences
	 */
	public static Vector<Sequence> getRunningSequences(String name) {
		if (name == null)
			return Monitor.runningSequences();

		int i = name.lastIndexOf('/');
		if (i >= 0)
			name = name.substring(i + 1);

		Vector<Sequence> list = new Vector<Sequence>();

		for (Sequence seq : Monitor.runningSequences()) {
			String s = seq.getName();
			i = s.lastIndexOf('/');
			if (i >= 0)
				s = s.substring(i + 1);
			if (name.equals(s))
				list.add(seq);
		}

		return list;
	}

	/**
	 * getRsaEncrypted: Obtain encrypted (and ascii-encoded) value for plain
	 * text specified, null strings are not processed and will be returned as
	 * null.
	 * 
	 * @param plainText
	 *            String representing value to be encrypted using public key
	 * @param ksPath
	 *            String representing file path to jks file
	 * @param ksPassword
	 *            String representing password for jks file as specified by path
	 * @param certificateAlias
	 *            String naming the alias of certificate in keystore file
	 * @return String representing encrypted format, null is returned if a null
	 *         is passed in.
	 * @throws java.lang.Exception
	 *             when underlying function fails
	 * @throws Exception
	 */
	public String getRsaEncrypted(String plainText, String ksPath, String ksPassword, String certificateAlias)
			throws java.lang.Exception {
		return IDIPasswordCrypto.encrypt(plainText, ksPath, ksPassword, certificateAlias);

	}

	/**
	 * getRsaDecrypted: Obtain plain ascii text for encrypted ciphertext
	 * specified. Null strings are not processed and will be returned as
	 * received. Empty strings will be encoded/encrypted.
	 * 
	 * @param cipherText
	 *            String representing value to be decrypted using private key
	 * @param ksPath
	 *            String representing file path to jks file
	 * @param ksPassword
	 *            String representing password for jks file as specified by path
	 * @param certificateAlias
	 *            String naming the alias of certificate in keystore file
	 * @param certificatePassword
	 *            String representing password certificate
	 * @return String representing the decrypted format of the received string.
	 *         Null is returned when a null is received.
	 * @throws java.lang.Exception
	 *             when underlying function fails
	 * @throws Exception
	 */
	public String getRsaDecrypted(String cipherText, String ksPath, String ksPassword, String certificateAlias,
			String certificatePassword) throws java.lang.Exception {

		return IDIPasswordCrypto.decrypt(cipherText, ksPath, ksPassword, certificateAlias, certificatePassword);

	}

	/**
	 * Creates an AssemblyLine Pool object from the specified AssemblyLine name.
	 * 
	 * @param assemblyLine
	 *            The name of the assemblyline
	 * @param log
	 *            The Log object to use or null to use the system logger
	 * @return created AssemblyLinePool object
	 * @throws Throwable
	 */
	public AssemblyLinePool createALPool(String assemblyLine, Log log) throws Throwable {
		RS server = (RS) getServer();
		AssemblyLineConfig config = server.getTask(assemblyLine);
		Log logger = (log == null ? server.getLog() : log);
		return new AssemblyLinePool(assemblyLine, logger, server, config);
	}

	/**
	 * Load a Function component Interface from the current Config.
	 * 
	 * @param name
	 *            The name of the function.
	 * @return The Function object
	 * @throws Exception
	 */
	public FunctionInterface getFunction(String name) throws Exception {
		return SystemFunctions.loadFunction(name, getServer());
	}

	/*
	 * Arrays used for base64 encoding/decoding
	 */
	private static int[] decode = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58,
		59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
		21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43,
		44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1 };

	// Initialize encode array
	private static char[] encode = new char[64];
	static {
		for (int i = 0; i < 128; i++) {
			if (decode[i] >= 0)
				encode[decode[i]] = (char) i;
		}
	}

	/**
	 * base64Encode: Obtain Base 64 encoded String from a binary Byte Array
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var e = Array(6);
	 * e[0] = 7;
	 * e[1] = -66;
	 * e[2] = -35;
	 * e[3] = -21;
	 * e[4] = -66;
	 * e[5] = -35;
	 * task.logmsg(&quot;Result: &quot; + system.base64Encode(e)); //B77d677d
	 * </pre>
	 * 
	 * @param b
	 *            byte array containing binary data
	 * @return String containing the base64 encoded representation of the data.
	 */

	public static String base64Encode(byte[] b) {

		StringWriter w = new StringWriter();
		int res = 0;
		int i = 0;
		while (i < b.length) {
			int ch = b[i] & 0xff;
			switch (i % 3) {
			case 0:
				w.write(encode[ch >> 2]);
				res = (ch & 3) << 4;
				break;
			case 1:
				w.write(encode[res | (ch >> 4)]);
				res = (ch & 0xf) << 2;
				break;
			case 2:
				w.write(encode[res | (ch >> 6)]);
				w.write(encode[ch & 0x3f]);
			}
			i++;
		}
		i %= 3;
		if (i != 0) {
			w.write(encode[res]);
			if (i == 1)
				w.write("==");
			else
				w.write("=");
		}
		return w.toString();
	}

	/**
	 * Return the base64 encoding of a String.
	 * @param string The String to encode.
	 * @param encoding Encoding used to convert the String to bytes.
	 * If null, use platform specific encoding.
	 * @return The base64 encoding of the string.
	 * @throws UnsupportedEncodingException If the string cannot be converted
	 * to bytes with the provided encoding.
	 * @since 7.2
	 */
	public static String base64Encode(String string, String encoding) throws UnsupportedEncodingException {
		if (encoding == null)
			return base64Encode(string.getBytes());
		else
			return base64Encode(string.getBytes(encoding));
	}

	/**
	 * base64Decode: Obtain Byte Array from a Base 64 encoded String.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var str = &quot;B77d677d&quot;;
	 * task.logmsg(&quot;Result: &quot; + system.base64Decode(str)); //7,-66,-35,-21,-66,-35
	 * </pre>
	 * 
	 * @param str
	 *            String containing base64 Data.
	 * @return Byte array containing the decoded binary data.
	 */

	public static byte[] base64Decode(String str) {

		ByteArrayOutputStream w = new ByteArrayOutputStream();
		int mode = 0;
		int res = 0;
		for (int i = 0; i < str.length(); i++) {
			int ch = (int) str.charAt(i);
			if (ch > 0 && ch < 128)
				ch = decode[ch];
			else
				continue;

			if (ch < 0)
				continue;

			switch (mode) {
			case 0:
				res = ch << 2;
				break;
			case 1:
				w.write(res | (ch >> 4));
				res = (ch << 4) & 0xff;
				break;
			case 2:
				w.write(res | (ch >> 2));
				res = (ch << 6) & 0xff;
				break;
			case 3:
				w.write(res | ch);
				break;
			}
			mode = (mode + 1) % 4;
		}
		return w.toByteArray();
	}

	/**
	 * Converts a base64 encoded String back to a regular String.
	 * @param str The base64 encoded String
	 * @param encoding Character encoding used to convert bytes to characters.
	 * If null, platform specific encoding is used.
	 * @return The decoded String
	 * @throws UnsupportedEncodingException If the bytes cannot be converted to String=
	 * with the given encoding.
	 * @since 7.2
	 */
	public static String base64Decode(String str, String encoding) throws UnsupportedEncodingException {
		if (encoding == null)
			return new String(base64Decode(str));
		else
			return new String(base64Decode(str), encoding);
	}

	/**
	 * encodeToHexstring: Obtain HexString from a byte array.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var e = new Array(4);
	 * e[0] = 7;
	 * e[1] = -66;
	 * e[2] = -35;
	 * e[3] = -21;
	 * task.logmsg(&quot;Result: &quot; + system.encodeToHexstring(e)); // \07\be\dd\eb
	 * </pre>
	 * 
	 * @param data
	 *            byte array containing binary data
	 * @return String containing the Hexadecimal representation of the data.
	 */

	public static String encodeToHexstring(byte[] data) {
		StringBuffer encodestr = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			encodestr.append("\\");
			String hexstr = Integer.toHexString((int) data[i]);
			int hexstrlen = 2;
			if (hexstr.length() < 2)
				hexstr = "0" + hexstr;
			else
				hexstrlen = hexstr.length();
			encodestr.append(hexstr.substring(hexstrlen - 2));
		}
		return encodestr.toString();
	}

	/**
	 * Creates an IDispatch automation object. This method creates a new
	 * COMProxy object and then calls new IDispatch(progID) on it.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var cominst = system.createCOMInstance(&quot;Word.Basic&quot;);
	 * </pre>
	 * 
	 * @param progID
	 *            the progID (Programmatic IDentifier)is a string that uniquely
	 *            identifies the COM object, stored in the registry and is of
	 *            the form: Project.ClassName
	 * @return IDispatch object, null if not running under Windows
	 */
	public static IDispatch createCOMInstance(String progID) {
		try {
			return COMProxy.create().createInstance(progID);
		} catch (Exception err) {
			return null;
		}
	}

	/**
	 * This method create a new Memory Buffer Queue if it does not already
	 * exist. If the pipe already exists with the specified instaName and
	 * pipeName then a handle to the same pipe is returned. Paging is disabled
	 * in this case.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var con = input.getConnector();
	 * var pipe = system.newPipe(null, &quot;new_pipe&quot;, 2);
	 * 
	 * var entry1 = con.getNextEntry();
	 * pipe.write(entry1);
	 * 
	 * var entry2 = con.getNextEntry();
	 * pipe.write(entry2);
	 * </pre>
	 * 
	 * @param instName
	 *            name of the instance. Default instance will be used if this
	 *            param is null.
	 * @param pipeName
	 *            name of the pipe to be created
	 * @param watermark
	 *            With Paging On, it is the threshold at which objects are
	 *            persisted to the System Store With Paging Off, it is the
	 *            maximum queue size
	 * @return MemBufferQ
	 * @throws Exception
	 */
	public static MemBufferQ newPipe(String instName, String pipeName, int watermark) throws Exception {
		return MemBufferQFactory.getInstance(instName).newPipe(pipeName, watermark);
	}

	/**
	 * This method create a new Memory Buffer Queue if it does not already
	 * exist. If the pipe already exists with the specified instaName and
	 * pipeName then a handle to the same pipe is returned. Paging is enabled in
	 * this case.
	 * 
	 * @param instName
	 *            name of the instance. Default instance will be used if this
	 *            param is null.
	 * @param pipeName
	 *            name of the pipe to be created
	 * @param watermark
	 *            With Paging On, it is the threshold at which objects are
	 *            persisted to the System Store With Paging Off, it is the
	 *            maximum queue size
	 * @param pagesize
	 * @return MemBufferQ
	 * @throws Exception
	 */
	public static MemBufferQ newPipe(String instName, String pipeName, int watermark, int pagesize) throws Exception {
		return MemBufferQFactory.getInstance(instName).newPipe(pipeName, watermark, pagesize);
	}

	/**
	 * This method returns a handle to a pipe with the specified instName and
	 * pipeName (if it already exists). If the pipe does not exist, then this
	 * method throws an Exception.
	 * 
	 * @param instName
	 *            name of the instance. Default instance will be used if this
	 *            param is null.
	 * @param pipeName
	 *            name of the pipe to be returned
	 * @return MemBufferQ
	 * @throws Exception
	 */
	public static MemBufferQ getPipe(String instName, String pipeName) throws Exception {
		return MemBufferQFactory.getInstance(instName).getPipe(pipeName);
	}

	/**
	 * Deletes the specified pipe from the specified instance. Drops the
	 * associated table in System Store with the specified memory queue (if it's
	 * a persistent queue). This method throws an exception if the pipe name is
	 * invalid or does not exist.
	 * 
	 * @param instName
	 *            name of the instance. Default instance will be used if this
	 *            param is null.
	 * @param pipeName
	 *            name of the pipe to be deleted
	 * @throws Exception
	 */
	public static void deletePipe(String instName, String pipeName) throws Exception {
		MemBufferQFactory.getInstance(instName).deleteQueue(pipeName);
	}

	/**
	 * Deletes specified pipe from default instance Drops the associated table
	 * in System Store with the specified memory queue (if it's a persistent
	 * queue). This method throws an exception if the pipe name is invalid or
	 * does not exist.
	 * 
	 * @param pipeName
	 *            name of the pipe to be deleted
	 * @throws Exception
	 */

	public static void deletePipe(String pipeName) throws Exception {
		deletePipe(null, pipeName);
	}

	/**
	 * Get external property using delegator object.
	 * <p>
	 * Note that the {@link #getTDIProperty(String)} method is recommended over
	 * this older version.
	 * 
	 * @param propName
	 * @return external property
	 * @throws Exception
	 * @deprecated use {@link #getTDIProperty(String)} instead
	 */
	@Deprecated
	public Object getExternalProperty(String propName) throws Exception {
		return getExternalProperty(null, propName);
	}

	/**
	 * Set external property using delegator object.
	 * 
	 * @param propName
	 * @param value
	 * @throws Exception
	 * @deprecated use {@link #setTDIProperty(String, Object)} instead
	 */
	@Deprecated
	public void setExternalProperty(String propName, Object value) throws Exception {
		setExternalProperty(null, propName, value);
	}

	/**
	 * Get external property from specific extprop object.
	 * <p>
	 * Note that the {@link #getTDIProperty(String, String)} method is
	 * recommended over this older version.
	 * 
	 * @param extObj
	 * @param propName
	 * @return external property
	 * @throws Exception
	 * @deprecated use {@link #getTDIProperty(String, String)} instead
	 *             Implementation of the method is changed due to defect 12968
	 */
	@Deprecated
	public Object getExternalProperty(String extObj, String propName) throws Exception {
		if (getServer() == null)
			return null;

		if (extObj == null) {
			return getTDIProperties().getProperty(propName);
		} else {
			return getTDIProperties().getProperty(extObj, propName);
		}
		// return getExtProp(extObj).getParameter(propName);
	}

	/**
	 * Set external property in a specific extprop object
	 * 
	 * @param extObj
	 * @param propName
	 * @param value
	 * @throws Exception
	 * @deprecated use {@link #setTDIProperty(String, String, Object)} instead
	 */
	@Deprecated
	public void setExternalProperty(String extObj, String propName, Object value) throws Exception {
		getExtProp(extObj).setParameter(propName, value);
	}

	/**
	 * Returns a named extprop object.
	 * <p>
	 * Note that the {@link #getTDIProperty(String)} and
	 * {@link #getTDIProperty(String, String)} methods are recommended over this
	 * older version.
	 * 
	 * @param name
	 *            name of the extprop
	 * @return ExternalPropertiesConfig object
	 * @throws Exception
	 * @deprecated use {@link #getTDIProperties()} instead
	 */
	@Deprecated
	public ExternalPropertiesConfig getExtProp(String name) throws Exception {
		if (getServer() == null)
			return null;

		if (name == null)
			return getServer().getMetamergeConfig().getExternalProperties();
		else
			return (ExternalPropertiesConfig) getServer().getMetamergeConfig().lookup(
					MetamergeConfig.DEFAULT_EXTPROP_FOLDER + "/" + name);
	}

	/**
	 * Generates the hexadecimal String representation of an Active Directory
	 * GUID based on its 128-bit binary representation. The String
	 * representation of a GUID has the form
	 * "{xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}". The digits used are the
	 * hexadecimal digits 0,1,2,3,4,5,6,7,8,9,A,B,C,D,E and F.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var e = new Array(4);
	 * 
	 * e[0] = 0xd0;
	 * e[8] = 0x8a;
	 * e[1] = 0xef;
	 * e[9] = 0x94;
	 * e[2] = 0x68;
	 * e[10] = 0xb1;
	 * e[3] = 0x8e;
	 * e[11] = 0xc1;
	 * e[4] = 0xbe;
	 * e[12] = 0x46;
	 * e[5] = 0x1a;
	 * e[13] = 0x85;
	 * e[6] = 0x5c;
	 * e[14] = 0xbe;
	 * e[7] = 0x40;
	 * e[15] = 0xd7;
	 * 
	 * task.logmsg(&quot;Result: &quot; + system.binaryGUIDtoString(e)); //{8E68EFD0-1ABE-405C-8A94-B1C14685BED7}
	 * </pre>
	 * 
	 * @param binaryData
	 *            a 16-byte byte array, holding the 128-bit binary
	 *            representation of the GUID.
	 * @return The hexadecimal String representation of the binary GUID.
	 */
	public String binaryGUIDtoString(byte[] binaryData) {
		if (binaryData == null || binaryData.length != 16) {
			return null;
		}

		StringBuffer stringGUID = new StringBuffer("{");
		String str;
		int n;

		// generate the first 8 hexadecimal digits
		n = 0;
		n |= ((int) (binaryData[0])) & 0x000000FF;
		n |= (((int) (binaryData[1])) << 8) & 0x0000FF00;
		n |= (((int) (binaryData[2])) << 16) & 0x00FF0000;
		n |= (((int) (binaryData[3])) << 24) & 0xFF000000;
		str = java.lang.Integer.toHexString(n);
		stringGUID.append(insertLeadingZeros(str, 8));
		stringGUID.append("-");

		// generate the first and the second groups of 4 hexadecimal digits
		for (int i = 2; i < 4; i++) {
			n = 0;
			n |= ((int) (binaryData[i * 2])) & 0x000000FF;
			n |= (((int) (binaryData[i * 2 + 1])) << 8) & 0x0000FF00;
			str = java.lang.Integer.toHexString(n);
			stringGUID.append(insertLeadingZeros(str, 4));
			stringGUID.append("-");
		}

		// generate the third group of 4 hexadecimal digits
		n = 0;
		n |= (((int) (binaryData[8])) << 8) & 0x0000FF00;
		n |= (((int) (binaryData[9]))) & 0x000000FF;
		str = java.lang.Integer.toHexString(n);
		stringGUID.append(insertLeadingZeros(str, 4));
		stringGUID.append("-");

		// generate the last 12 hexadecimal digits
		for (int i = 5; i < 8; i++) {
			n = 0;
			n |= (((int) (binaryData[i * 2 + 1]))) & 0x000000FF;
			n |= (((int) (binaryData[i * 2])) << 8) & 0x0000FF00;
			str = java.lang.Integer.toHexString(n);
			stringGUID.append(insertLeadingZeros(str, 4));
		}

		stringGUID.append("}");

		return stringGUID.toString().toUpperCase();
	}

	/**
	 * Inserts leading zeros. For example, if the number 21 is required to be
	 * represented by exactly 4 digits, then the call "insertLeadingZeros("21",
	 * 4)" will return "0021". If the number is already represented by the
	 * required number of digits or more, no leading zeros will be inserted.
	 * 
	 * @param strNumber
	 *            the String representation of a number.
	 * @param requiredDigits
	 *            the number of digits required to represent the number.
	 * @return The String representation of the number with the necessary number
	 *         of leading zeros.
	 */
	private String insertLeadingZeros(String strNumber, int requiredDigits) {
		String result = strNumber;
		while (result.length() < requiredDigits) {
			result = "0" + result;
		}
		return result;
	}

	/**
	 * Removes invalid XML chars.
	 * 
	 * @param aString
	 *            string to clean
	 * @return cleaned string
	 */
	public static String removeInvalidXMLChars(String aString) {

		if (aString == null) {
			return null;
		}

		StringBuffer cleanXML = new StringBuffer(aString);

		for (int i = cleanXML.length() - 1; i > -1; i--) {
			for (int j = 0; j < INVALID_XML_CHARS.length; j++) {
				if (cleanXML.charAt(i) == INVALID_XML_CHARS[j]) {
					cleanXML.deleteCharAt(i);
					break;
				}
			}
		}

		return cleanXML.toString();
	}

	/**
	 * Dynamically add jar file containing class definitions. TDI has a loader
	 * that finds all classes in jar files in the jars directory of the
	 * installation folder. If you want to dynamically add additional jar files,
	 * you can use this method. An alternative to dynamically loading additional
	 * jar files, is to set the "com.ibm.di.loader.userjars" property in
	 * global.properties.
	 * 
	 * @param path
	 *            The full path name of a jar file or a directory containing jar
	 *            files
	 * @see com.ibm.di.loader.IDILoader#addFiles(String)
	 * 
	 */
	public static void loadJarFile(String path) {
		ClassLoader loader = UserFunctions.class.getClassLoader();
		if (loader instanceof IDILoader) {
			((IDILoader) loader).addFiles(path);
			ScriptEngineOptions.clearNoClassSet();			
		}
	}

	/**
	 * Returns the TDIProperties object for the current configuration
	 * 
	 * @return TDIProperties object
	 * @throws Exception
	 */
	public TDIProperties getTDIProperties() throws Exception {
		if (getServer() == null)
			return null;
		else
			return getServer().getMetamergeConfig().getTDIProperties();
	}

	/**
	 * Returns the value for a TDI property
	 * 
	 * @param name
	 *            The name of the property
	 * @return TDI property value
	 * @throws Exception
	 */
	public Object getTDIProperty(String name) throws Exception {
		return getTDIProperties().getProperty(name);
	}

	/**
	 * Returns the property value from a specific TDI property store
	 * 
	 * @param propstore
	 *            The property store name
	 * 
	 * @param name
	 *            The name of the property
	 * @return TDI property value
	 * @throws Exception
	 */
	public Object getTDIProperty(String propstore, String name) throws Exception {
		return getTDIProperties().getProperty(propstore, name);
	}

	/**
	 * Sets the property value for a property (store selection based on naming
	 * rules and order).
	 * 
	 * @param name
	 *            The name of the property
	 * @param value
	 *            The property value
	 * @throws Exception
	 */
	public void setTDIProperty(String name, Object value) throws Exception {
		getTDIProperties().setProperty(name, value);
	}

	/**
	 * Sets the property value in a specific TDI property store
	 * 
	 * @param propstore
	 *            The property store name
	 * @param name
	 *            The name of the property
	 * @param value
	 *            The property value
	 * @throws Exception
	 */
	public void setTDIProperty(String propstore, String name, Object value) throws Exception {
		getTDIProperties().setProperty(propstore, name, value);
	}

	/**
	 * Returns true if the first String starts with the second String, ignoring
	 * case. If at least one if the Strings are null, returns false. This method
	 * is case insensitive.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var str1 = &quot;IBM Corporation&quot;;
	 * var str2 = &quot;ibm&quot;;
	 * if (system.startsWithIC(str1, str2)) {
	 * 	task.logmsg(&quot;str1 starts with str2&quot;);
	 * } else {
	 * 	task.logmsg(&quot;str1 does not start with str2&quot;);
	 * }
	 * </pre>
	 * 
	 * @param first
	 *            The first String
	 * @param second
	 *            The second String
	 * @return true if and only if the first String starts with the second
	 *         String, ignoring case
	 * @since 6.1.1
	 */
	public static boolean startsWithIC(String first, String second) {
		if (first == null || second == null)
			return false;
		return first.regionMatches(true, 0, second, 0, second.length());
	}

	/**
	 * Returns true if the first String ends with the second String, ignoring
	 * case If at least one if the Strings are null, returns false.This method
	 * is case insensitive.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var str1 = &quot;Directory Integrator&quot;;
	 * var str2 = &quot;Rator&quot;;
	 * if (system.endsWithIC(str1, str2)) {
	 * 	task.logmsg(&quot;str1 ends with str2&quot;);
	 * } else {
	 * 	task.logmsg(&quot;str1 does not end with str2&quot;);
	 * }
	 * </pre>
	 * 
	 * @param first
	 *            The first String
	 * @param second
	 *            The second String
	 * @return true if and only if the first String ends with the second String,
	 *         ignoring case
	 * @since 6.1.1
	 */
	public static boolean endsWithIC(String first, String second) {
		if (first == null || second == null)
			return false;
		if (first.length() < second.length())
			return false;
		return first.substring(first.length() - second.length()).equalsIgnoreCase(second);
	}

	/**
	 * Returns true if the second String is a substring of the first, ignoring
	 * case. If at least one if the Strings are null, returns false. Examples:
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * system.containsIC(&quot;abcde&quot;, &quot;BCD&quot;); // Returns true
	 * system.containsIC(&quot;abcde&quot;, &quot;bd&quot;); // Returns false
	 * </pre>
	 * 
	 * @param first
	 *            The first String
	 * @param second
	 *            The second String
	 * @return true if and only if the first String contains the second String,
	 *         ignoring case
	 * @since 6.1.1
	 */
	public static boolean containsIC(String first, String second) {
		if (first == null || second == null)
			return false;
		int n = second.length();
		for (int i = 0, end = first.length() - n; i <= end; i++) {
			if (first.substring(i, i + n).equalsIgnoreCase(second))
				return true;
		}
		return false;
	}

	/**
	 * Returns a ParameterSubstitution object using the given pattern. For
	 * example if we have the following file:
	 * <p>
	 * <i>&quot; John 62-58-99<br>
	 * Lily 056/6563425<br>
	 * Michael +359 88 540 90&quot;<br>
	 * </i>
	 * <p>
	 * And read this file into two fields called 'name' and 'phone' you could
	 * print the information by this way:
	 * 
	 * <pre>
	 * expression = system.getTDIExpression(&quot;{work.name}'s number is {work.phone}.&quot;);
	 * map = new java.util.HashMap();
	 * map.put(&quot;mc&quot;, main.getMetamergeConfig());
	 * 
	 * while ((work = input.getConnector().getNextEntry()) != null) {
	 * 	map.put(&quot;work&quot;, work);
	 * 	task.logmsg(expression.substitute(map)); // John's number is 62-58-99. and so on...
	 * }
	 * </pre>
	 * 
	 * @param pattern
	 *            The pattern to use for substitution.
	 * @return A ParameterSubstitution with the given pattern
	 * @throws Exception
	 * @see #substitute(String, Map)
	 */
	public static ParameterSubstitution getTDIExpression(String pattern) throws Exception {
		return new ParameterSubstitution(pattern);
	}

	/**
	 * Performs a one-time parsing and substitution of pattern with the objects
	 * available in params. This method uses a Map object where you provide the
	 * available objects for pattern expansion.
	 * <p>
	 * You should at least provide "mc=MetamergeConfig" or
	 * "config=BaseConfiguration" object, otherwise expansion of TDI-properties
	 * will not work. If you want to expand AL component parameters, you need to
	 * provide a "config=BaseConfiguration" object.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * map = new java.util.HashMap();
	 * map.put(&quot;mc&quot;, main.getMetamergeConfig());
	 * map.put(&quot;work&quot;, work);
	 * result = system.substitute(&quot;{work.cn} {property.myprop}&quot;, map);
	 * </pre>
	 * 
	 * @param pattern
	 *            The pattern string to expand
	 * @param params
	 *            The available objects (e.g. conn, work, task etc)
	 * @return The expanded string
	 * @throws Exception
	 */
	public static String substitute(String pattern, Map<String, Object> params) throws Exception {
		return new ParameterSubstitution(pattern).substitute(params);
	}

	/**
	 * Performs a one-time parsing and substitution of pattern with named
	 * objects. You should at least provide "mc=MetamergeConfig" or
	 * "config=BaseConfiguration" objects, otherwise expansion of TDI-properties
	 * will not work. If you want to expand AL component parameters, you need to
	 * provide a "config=BaseConfiguration" object.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 *    result = system.substitute(&quot;{work.cn} {property.myprop}&quot;, [&quot;mc&quot;, &quot;work&quot;], [main.getMetamergeConfig(), work]);
	 * </pre>
	 * 
	 * @param pattern
	 *            The pattern string to expand
	 * @param names
	 *            The names of the available objects (e.g. "conn", "work",
	 *            "task" etc)
	 * @param objects
	 *            The available objects (e.g. conn, work, task etc)
	 * @return The expanded string
	 * @throws Exception
	 */
	public static String substitute(String pattern, String[] names, Object[] objects) throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		if (names != null && objects != null) {
			for (int i = 0; i < names.length; i++) {
				map.put(names[i], (i < objects.length ? objects[i] : null));
			}
		}
		return new ParameterSubstitution(pattern).substitute(map);
	}
	
	/**
	 * Returns the backtrace for a throwable.
	 * @param t - The Throwable
	 * @return A string representation of the backtrace.
	 * @since 7.2
	 */
	public String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		return sw.toString();		
	}
	
	/**
	 * Returns all bytes in the file as a byte array.
	 * @param fileName Name of the file to read
	 * @return The bytes contained in the file as a byte[]
	 * @throws IOException If the file is not found or not readable
	 * @since SDI 7.2 FP0004
	 */
	public byte[] readBytes(String fileName) throws IOException {
		FileInputStream fis = new FileInputStream(fileName);
		try {
			return FileUtils.readInputStream(fis);
		} finally {
			fis.close();
		}
	}
	
	public static int getNoOperationsPayload(String jsonStr){
		ObjectMapper objectMapper = new ObjectMapper();
		int count=0;
		try {
            // Parse JSON into JsonNode
            JsonNode rootNode = objectMapper.readTree(jsonStr);
			JsonNode opNode = rootNode.path("Operations");			
			if (opNode.isArray()) {
				count = opNode.size(); 
				System.out.println("misever:No.of operations = "+count);
			}			
        } catch (IOException e) {
            e.printStackTrace();
        }		
		return count;
	}
	
	public static String getmethodPayload(String jsonStr, int index)
	{
		ObjectMapper objectMapper = new ObjectMapper();
		int count=0;
		String method=null;
		try {
            // Parse JSON into JsonNode
            JsonNode rootNode = objectMapper.readTree(jsonStr);
			JsonNode opNode = rootNode.path("Operations");			
			if (opNode.isArray()) {
				count = opNode.size(); 
			}		
			JsonNode cNode;
			for (int i=0;i<count;i++){
				if (i == index){
					cNode = opNode.get(i).path("method");
					method=cNode.toString();
					System.out.println("misever:Method="+method);
				}
			}	
        } catch (IOException e) {
            e.printStackTrace();
        }		
		return method;
	}
	
	public static String getpathPayload(String jsonStr, int index)
	{
		ObjectMapper objectMapper = new ObjectMapper();
		int count=0;
		String path=null;
		try {
            // Parse JSON into JsonNode
            JsonNode rootNode = objectMapper.readTree(jsonStr);
			JsonNode opNode = rootNode.path("Operations");			
			if (opNode.isArray()) {
				count = opNode.size(); 
			}		
			JsonNode cNode;
			for (int i=0;i<count;i++){
				if (i == index){
					cNode = opNode.get(i).path("path");
					path=cNode.toString();
					System.out.println("misever:Path="+path);
				}
			}	
        } catch (IOException e) {
            e.printStackTrace();
        }		
		return path;
	}
	
	public static String getdataPayload(String jsonStr, String nodeName, int index)
	{
		ObjectMapper objectMapper = new ObjectMapper();
		int count=0;
		String data=null;
		try {
            // Parse JSON into JsonNode
            JsonNode rootNode = objectMapper.readTree(jsonStr);
			JsonNode opNode = rootNode.path("Operations");			
			if (opNode.isArray()) {
				count = opNode.size(); 
			}		
			JsonNode cNode;
			for (int i=0;i<count;i++){
				if (i == index){
					cNode = opNode.get(i).path(nodeName);
					data=cNode.toString();
					System.out.println("misever:Data="+data);
				}
			}	
        } catch (IOException e) {
            e.printStackTrace();
        }		
		return data;
	}
	
	public static String getpatchopPayload(String jsonStr, int index)
	{
		ObjectMapper objectMapper = new ObjectMapper();
		int count=0;
		String op=null;
		try {
            // Parse JSON into JsonNode
            JsonNode rootNode = objectMapper.readTree(jsonStr);
			JsonNode opNode = rootNode.path("Operations");			
			if (opNode.isArray()) {
				count = opNode.size(); 
			}		
			JsonNode cNode;
			for (int i=0;i<count;i++){
				if (i == index){
					cNode = opNode.get(i).path("op");
					op=cNode.toString();
					System.out.println("op="+op);
				}
			}	
        } catch (IOException e) {
            e.printStackTrace();
        }		
		return op;
	}
	
	public static String getpatchattrValuePayload(String jsonStr, String attrName, int index)
	{
		
		ObjectMapper objectMapper = new ObjectMapper();
		String var1,result,attrValue=null;
		JsonNode bNode,cNode,dNode,eNode;
			
		try{
			// Parse JSON into JsonNode
			JsonNode rootNode = objectMapper.readTree(jsonStr);
			JsonNode aNode = rootNode.path("Operations");
			System.out.println("array size="+aNode.size());
			for (int i=0;i<aNode.size();i++){
				bNode = aNode.get(i).path("op");
				cNode = aNode.get(i).path("path");
				var1=cNode.toString();
				result = var1. replaceAll("\"", "");
				System.out.println("read attribute name from json="+cNode.toString());
				System.out.println("received attribute from function= "+attrName);
				if (result.equals(attrName)){
				dNode = aNode.get(i).path("value");
				//System.out.println("array size="+dNode.size());
				//for (int j=0;j<dNode.size();j++){		
					eNode = dNode.get(0).path(result);
					attrValue = eNode.toString();
					System.out.println("misever:attribute value="+attrValue);
				//}
				break;
				}//end of if
				
			}
		}catch (Exception e) {
            System.out.println("catching exception");
			e.printStackTrace();	
		}
		return attrValue;
	}

}

class ErrorListenerImpl implements javax.xml.transform.ErrorListener {
	public java.lang.Exception excep = null;

	ErrorListenerImpl() {
		excep = null;
	}

	public void warning(javax.xml.transform.TransformerException e) {
		// Do Nothing.

	}

	public void error(javax.xml.transform.TransformerException e) {
		this.excep = e;
	}

	public void fatalError(javax.xml.transform.TransformerException e) {
		this.excep = e;
	}
}
