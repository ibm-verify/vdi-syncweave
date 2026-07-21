/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.ibm.di.server.BindAddressPolicy;
import com.ibm.di.server.BindAddressPolicyImpl;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.ServerSocketFactoryEX;

/**
 * This class represents a custom FTP client, that has the functionality to
 * connect to a FTP server, to login, list contents, transfer data, etc.
 * 
 */
public class FTPClient {

	/**
	 * Copyright information.
	 */
	protected static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * EndOfLine separator.
	 */
	static final String EOL = "\r\n";
	
	/**
	 * Default charset
	 */
	private static final String CHARSET = "ISO-8859-1";

	/**
	 * This parameter specifies if the connector that uses this FTP client is in
	 * detailed logging mode.
	 */
	private boolean debug = false;

	/**
	 * The control channel used for command transfer.
	 */
	private Socket control = null;

	/**
	 * Used for reading commands from the FTP server.
	 */
	private BufferedReader reader = null;

	/**
	 * Used for writing commands to the FTP server.
	 */
	private Writer writer = null;

	/**
	 * The data channel used for transferring data.
	 */
	private ServerSocket dataServer = null;

	/**
	 * Socket used by the client when in passive mode.
	 */
	private Socket passive = null;

	/**
	 * The socket used for transferring files to and from the FTP server.
	 */
	private Socket data = null;

	/**
	 * This parameter determines whether the client will run in passive mode.
	 */
	private boolean usePassive = false;

	/**
	 * This parameter determines whether the ftp data channel uses SSL.
	 */
	private boolean useSSLonDataChannel;

	/**
	 * Logger used to log messages when in detailed mode.
	 */
	private Log logger;

	/**
	 * The name of the properties file.
	 */
	private static final String PROPERTIES_FILE = "miserver";

	/**
	 * Resource hash object for accessing TMS messages
	 */
	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Default constructor.
	 */
	public FTPClient() {
		logger = null;
	}

	/**
	 * Constructor.
	 * 
	 * @param logger
	 *            a Log object used for logging debug messages.
	 */
	public FTPClient(Log logger) {
		this.logger = logger;
	}

	/**
	 * This method does nothing.
	 * 
	 * @param millis
	 *            the timeout to be set in milliseconds.
	 */
	public void setTimeout(int millis) {
	}

	/**
	 * Sets the client in detailed logging mode.
	 * 
	 * @param debug
	 *            whether to log in details or not.
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Gets the debug level used.
	 * 
	 * @return whether the client is in detail logging mode.
	 */
	public boolean getDebug() {
		return debug;
	}

	/**
	 * Creates a connection to the given host and port. The security of the two
	 * channels established is determined by the other two input parameters.
	 * 
	 * @param host
	 *            the host to connect to.
	 * @param port
	 *            the port to connect to.
	 * @param useSSLonCommandChannel
	 *            whether to use SSL on the command channel.
	 * @param useSSLonDataChannel
	 *            whether to use SSL on the data channel.
	 * @throws Exception
	 *             if there is a problem during the connection establishment.
	 */
	public void connect(String host, int port, boolean useSSLonCommandChannel, boolean useSSLonDataChannel) throws Exception {
		connect(host, port, useSSLonCommandChannel, useSSLonDataChannel, false);
	}
	
	/**
	 * Creates a connection to the given host and port. The two parameters useSSLonCommandChannel and
	 * useSSLonDataChannel determine the SSL settings on the two channels respectively. If useSSLonCommandChannel
	 * is true, then the useExplicitModeSSL parameter will determine if the command channel will use explicit
	 * or implicit mode SSL.
	 *  
	 * 
	 * @param host
	 *            the host to connect to.
	 * @param port
	 *            the port to connect to.
	 * @param useSSLonCommandChannel
	 *            whether to use SSL on the command channel.
	 * @param useSSLonDataChannel
	 *            whether to use SSL on the data channel.
	 * @param useExplicitModeSSL
	 *            whether to negotiate SSL on the control channel (ftpes).
	 * @throws Exception
	 *             if there is a problem during the connection establishment.
	 */
	public void connect(String host, int port, boolean useSSLonCommandChannel, boolean useSSLonDataChannel, boolean useExplicitModeSSL) throws Exception {

		this.useSSLonDataChannel = useSSLonDataChannel;
		// throw an Exception if a SSL data connection is attempted
		// without using SSL on the command channel
		if (!useSSLonCommandChannel && useSSLonDataChannel) {
			throw new Exception(sResHash.getString("MISERVER.FTPCLIENT.CONNECT.SSL"));
		}
		if (debug && (logger != null)) {
			logger.logdebug(sResHash.getString("MISERVER.FTPCLIENT.CONNECT.TO.HOST", new Object[] { host, Integer.valueOf(port) }));
		}

		// create a SSL secured socket or a plain one depending on the
		// configuration
		if (useSSLonCommandChannel && !useExplicitModeSSL) {
			SocketFactory sslSocketFactory = SSLSocketFactory.getDefault();
			control = sslSocketFactory.createSocket(host, port);
		} else {
			control = new Socket(host, port);
		}

		reader = new BufferedReader(new InputStreamReader(control.getInputStream(), CHARSET));
		writer = new OutputStreamWriter(control.getOutputStream(), CHARSET);

		String str = getResponse();
		if (!str.startsWith("2")) {
			throw new Exception(str);
		}
		
		// explicit mode?
		if(useExplicitModeSSL) {
			// request SSL on the control channel
			str = sendCommand("AUTH SSL");
			if(!str.startsWith("2"))
				throw new Exception(str);
			
			// Create SSLSocket on top of existing socket and recreate the reader/writer objects
			SSLSocketFactory sslFact = (SSLSocketFactory) SSLSocketFactory.getDefault();
			control = (SSLSocket) sslFact.createSocket(control, control.getInetAddress().getHostAddress(), control.getPort(), true);
			((SSLSocket)control).startHandshake();
			
			reader = new BufferedReader(new InputStreamReader(control.getInputStream(), CHARSET));
			writer = new OutputStreamWriter(control.getOutputStream(), CHARSET);
		}

		// determine the security on the data channel
		if (useSSLonDataChannel) {
			// PBSZ sets the buffer size used for the sent encoded data.
			// It is required before the PROT command, according to RFC2228.
			// However, TLS/SSL handles blocking of data, so '0' is used.
			str = sendCommand("PBSZ 0");
			if (!str.startsWith("2")) {
				throw new Exception(str);
			}
			// Set SSL support for the data channel.
			str = sendCommand("PROT P");
			if (!str.startsWith("2")) {
				throw new Exception(str);
			}
		}
	}

	/**
	 * Disconnects the client from the FTP server.
	 */
	public void disconnect() {
		try {
			reader.close();
			writer.close();
			control.close();
		} catch (Exception ignore) {
		}
		try {
			data.close();
		} catch (Exception ignore) {
		}
		try {
			if (dataServer != null)
				dataServer.close();
		} catch (Exception ignore) {
		}
		try {
			if (passive != null)
				passive.close();
		} catch (Exception ignore) {
		}
		control = null;
		reader = null;
		writer = null;
		data = null;
		dataServer = null;
		passive = null;
	}

	/**
	 * Logs to the FTP server using the given credentials.
	 * 
	 * @param user
	 *            the username to be used.
	 * @param password
	 *            the password to be used.
	 * @throws Exception
	 *             if the logging process is unsuccessful.
	 */
	public void login(String user, String password) throws Exception {
		String str = sendCommand("USER " + user);
		if (!str.startsWith("3")) {
			throw new Exception(str);
		}

		str = sendCommand("PASS " + password);
		if (!str.startsWith("2")) {
			throw new Exception(str);
		}
	}

	/**
	 * Changes the working directory with the one specified.
	 * 
	 * @param cwd
	 *            the new working directory.
	 * @throws Exception
	 *             if the process fails.
	 */
	public void cwd(String cwd) throws Exception {
		String rc = sendCommand("CWD " + cwd);
		if (!rc.startsWith("250"))
			throw new Exception(rc);
	}

	/**
	 * Converts the given byte value to a short one.
	 * 
	 * @param value
	 *            the given byte value.
	 * @return the resulting short value.
	 */
	private short toShort(byte value) {
		return (value < 0) ? (short) (value + 256) : (short) value;
	}

	/**
	 * Converts the given short value to a byte array.
	 * 
	 * @param value
	 *            the given byte value.
	 * @return the resulting byte array.
	 */
	private byte[] toByteArray(short value) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (value >> 8); // bits 1- 8
		bytes[1] = (byte) (value & 0x00FF); // bits 9-16
		return bytes;
	}

	/**
	 * Creates the data socket used for data transferring between the FTP client
	 * and server. If the client is in passive mode it creates a Socket and
	 * attempts to connect to the server, otherwise it creates a ServerSocket
	 * and waits for the server to make the connection. The security settings of
	 * the client(whether SSL is used or not) are taken into account when
	 * sreating the socket.
	 * 
	 * @throws Exception
	 *             if the operation fails.
	 */
	public void setupDataSocket() throws Exception {

		String str; // Used to receive the response
		passive = null;

		InetAddress remote = control.getInetAddress();
		if (remote.getAddress().length > 4) {
			// IPv6, try extended Passive
			str = sendCommand("EPSV");

			if (str.startsWith("2")) {
				str = str.substring(str.indexOf('(') + 1);
				char delimiter = str.charAt(0);
				for (int i = 0; i < 3; i++)
					str = str.substring(str.indexOf(delimiter) + 1);
				int portno = Integer.parseInt(str.substring(0, str.indexOf(delimiter)));
				if (useSSLonDataChannel) {
					SocketFactory socketFactory = SSLSocketFactory.getDefault();
					passive = socketFactory.createSocket(remote, portno);
				} else {
					passive = new Socket(remote, portno);
				}
				return;
			} else {
				throw new Exception(str);
			}
		}

		if (usePassive) {
			// Try Passive mode
			str = sendCommand("PASV");
			if (str.startsWith("2")) {
				try {
					str = str.substring(str.indexOf('(') + 1);
					char delimiter = ',';
					for (int i = 0; i < 4; i++)
						str = str.substring(str.indexOf(delimiter) + 1);
					int port1 = Integer.parseInt(str.substring(0, str.indexOf(delimiter)));
					str = str.substring(str.indexOf(delimiter) + 1);
					int port2 = Integer.parseInt(str.substring(0, str.indexOf(')')));

					if (useSSLonDataChannel) {
						SocketFactory socketFactory = SSLSocketFactory.getDefault();
						passive = socketFactory.createSocket(control.getInetAddress(), 256 * port1 + port2);
					} else {
						passive = new Socket(control.getInetAddress(), 256 * port1 + port2);
					}
					return;
				} catch (Exception err) {
					if (debug) {
						logger.logerror(sResHash.getString("MISERVER.FTPCLIENT.PASV.REPLY", err), err);
					}
				}
			}
		}

		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(System.getProperties());
		ServerSocketFactory ssocketFactory = new ServerSocketFactoryEX(bindAddr, useSSLonDataChannel);
		if (useSSLonDataChannel) {
			dataServer = ssocketFactory.createServerSocket(0);

			// this reverses the roles of client and server for the SSL
			// handshake
			((SSLServerSocket) dataServer).setUseClientMode(true);
		} else {
			dataServer = ssocketFactory.createServerSocket(0);
		}

		// Get local ip/port where we listen
		InetAddress localhost = control.getLocalAddress();
		int port = dataServer.getLocalPort();

		byte[] hostBytes = localhost.getAddress();
		byte[] portBytes = toByteArray((short) port);

		// assemble the PORT command
		String cmd = new StringBuffer("PORT ").append(toShort(hostBytes[0])).append(",").append(toShort(hostBytes[1])).append(",")
				.append(toShort(hostBytes[2])).append(",").append(toShort(hostBytes[3])).append(",").append(toShort(portBytes[0]))
				.append(",").append(toShort(portBytes[1])).toString();

		// Send PORT command to server
		str = sendCommand(cmd);
		if (!str.startsWith("2"))
			throw new Exception(str);
	}

	/**
	 * Retrieves the specified file from the FTP server.
	 * 
	 * @param remoteFile
	 *            the file to be copied.
	 * @param binary
	 *            the type of the file (ASCII text or binary data). This
	 *            determines the transfer mode used by the client.
	 * @return a socket from which to read the contents of the file.
	 * @throws Exception
	 *             if the transfer fails.
	 */
	public Socket getFile(String remoteFile, boolean binary) throws Exception {

		setupDataSocket();

		setTransferMode(binary);

		String response = sendCommand("RETR " + remoteFile);
		if (!response.startsWith("1"))
			throw new Exception(response);

		if (passive != null) {
			data = passive;
		} else {
			data = dataServer.accept();
		}
		return data;

	}

	/**
	 * Retrieves the specified file from the FTP server.
	 * 
	 * @param remoteFile
	 *            the file to be copied.
	 * @param localFile
	 *            the local file in which the contents of the remote to be
	 *            transfered.
	 * @param binary
	 *            the type of the file (ASCII text or binary data). This
	 *            determines the transfer mode used by the client.
	 * @throws Exception
	 *             if the transfer fails.
	 */
	public void getFile(String remoteFile, String localFile, boolean binary) throws Exception {

		File f = new File(localFile);
		if (f.isDirectory()) {
			throw new Exception(sResHash.getString("MISERVER.FTPCLIENT.LOCALFILE.IS.A.DIRECTORY", localFile));
		}

		InputStream is = getFile(remoteFile, binary).getInputStream();

		if (binary) {
			try {
				OutputStream os = new FileOutputStream(localFile);
				try {
					byte[] buffer = new byte[2048];
					int rc;
					while ((rc = is.read(buffer)) != -1) {
						os.write(buffer, 0, rc);
					}
					os.flush();
				} finally {
					os.close();
				}
			} finally {
				is.close();
			}
		} else {
			BufferedReader inp = new BufferedReader(new InputStreamReader(is));
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(localFile));
				try {
					String str;
					while ((str = inp.readLine()) != null) {
						out.write(str);
						out.newLine();
					}
					out.flush();
				} finally {
					out.close();
				}
			} finally {
				inp.close();
			}
		}

		checkComplete();
	}

	/**
	 * Sends a local file to the server.
	 * 
	 * @param remoteFile
	 *            the file to be put on the server.
	 * @param binary
	 *            the type of the file (ASCII text or binary data). This
	 *            determines the transfer mode used by the client.
	 * @return the socket used for sending the file contents.
	 * @throws Exception
	 *             if the operation fails.
	 */
	public Socket putFile(String remoteFile, boolean binary) throws Exception {

		setupDataSocket();

		setTransferMode(binary);

		String response = sendCommand("STOR " + remoteFile);
		if (!response.startsWith("1"))
			throw new Exception(response);

		if (passive != null) {
			data = passive;
		} else {
			data = dataServer.accept();
		}

		data.setSoLinger(true, 10); // Random number
		return data;
	}

	/**
	 * Sends a local file to the server.
	 * 
	 * @param localPath
	 *            the location of the transfered file on the FTP client machine.
	 * @param remoteFile
	 *            the file to be put on the server.
	 * @param binary
	 *            the type of the file (ASCII text or binary data). This
	 *            determines the transfer mode used by the client.
	 * 
	 * @throws Exception
	 *             if the operation fails.
	 */
	public void putFile(String localPath, String remoteFile, boolean binary) throws Exception {

		File f = new File(localPath);
		if (!f.exists() || f.isDirectory()) {
			throw new Exception(sResHash.getString("MISERVER.FTPCLIENT.LOCALFILE.DOES.NOT.EXIST", localPath));
		}

		OutputStream os = putFile(remoteFile, binary).getOutputStream();

		if (binary) {
			InputStream is = new FileInputStream(localPath);
			try {
				byte[] buffer = new byte[2048];
				int rc;
				while ((rc = is.read(buffer)) != -1) {
					os.write(buffer, 0, rc);
				}
				os.flush();
			} finally {
				is.close();
				os.close();
			}
		} else {
			BufferedReader inp = new BufferedReader(new FileReader(localPath));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));
			try {
				String str;
				while ((str = inp.readLine()) != null) {
					out.write(str);
					out.write("\r\n");
				}
				out.flush();
			} finally {
				inp.close();
				out.close();
			}
		}

		checkComplete();
	}

	/**
	 * Sets the transfer mode according to the type of files to be transfered.
	 * 
	 * @param binary
	 *            if true the files will be sent as binary data, otherwise they
	 *            will be sent as ASCII text.
	 * @throws Exception
	 *             if the operation failed and the server did not return a OK
	 *             code.
	 */
	public void setTransferMode(boolean binary) throws Exception {

		String reply;
		if (binary)
			reply = sendCommand("TYPE I");
		else
			reply = sendCommand("TYPE A");

		if (!reply.startsWith("2"))
			throw new Exception(reply);
	}

	/**
	 * Deletes a given file from the FTP server.
	 * 
	 * @param remoteFile
	 *            the file to be deleted.
	 * @throws Exception
	 *             if the operation fails.
	 */
	public void deleteFile(String remoteFile) throws Exception {
		String reply = sendCommand("DELE " + remoteFile);
		if (!reply.startsWith("2"))
			throw new Exception(reply);
	}

	/**
	 * Renames a file or directory on the FTP server.
	 * 
	 * @param from
	 *            name of file or directory to rename.
	 * @param to
	 *            intended name.
	 * @throws Exception
	 *             if the operation fails.
	 */
	public void rename(String from, String to) throws Exception {

		String reply = sendCommand("RNFR " + from);
		if (!reply.startsWith("3"))
			throw new Exception(reply);

		reply = sendCommand("RNTO " + to);
		if (!reply.startsWith("2"))
			throw new Exception(reply);
	}

	/**
	 * Get the current remote working directory.
	 * 
	 * @return the current working directory on the FTP server.
	 * @throws Exception
	 *             if the request fails.
	 */
	public String pwd() throws Exception {

		String reply = sendCommand("PWD");
		if (!reply.startsWith("2"))
			throw new Exception(reply);
		return reply.substring(4);
	}
	
	/**
	 * Creates the directory
	 * 
	 * @param dir
	 * 			Path of the directory to be created
	 * @throws Exception
	 *             if the request fails.
	 */
	public String mkdir(String dir) throws Exception {

		String reply = sendCommand("MKD " + dir);
		if (!reply.startsWith("2"))
			throw new Exception(reply);
		return reply.substring(4);
	}

	/**
	 * Sends the given command through the command channel.
	 * 
	 * @param cmd
	 *            the command sent to the FTP server.
	 * @return the FTP server response.
	 * @throws Exception
	 *             if the operation fails.
	 */
	public String sendCommand(String cmd) throws Exception {
		if (debug && (logger != null)) {
			if (cmd.startsWith("PASS ")) {
				logger.logdebug(sResHash.getString("ftp.client.send.password"));
			} else {
				logger.logdebug(sResHash.getString("ftp.client.send.command", cmd));
			}
		}
		writer.write(cmd);
		writer.write(EOL);
		writer.flush();

		return getResponse();
	}

	/**
	 * Returns a long list of the contents of the current working directory on
	 * the FTP server.
	 * 
	 * @return an array of the contents.
	 * @throws Exception
	 */
	public String[] dir() throws Exception {
		return dir(null, true);
	}

	/**
	 * Returns a short list of the contents of the current working directory on
	 * the FTP server.
	 * 
	 * @return the
	 * @throws Exception
	 */
	public String[] list() throws Exception {
		return dir(null, false);
	}

	/**
	 * Returns a string array with the contents of the given path.
	 * 
	 * @param path
	 *            the path which contents will be listed.
	 * @param longlisting
	 *            whether the list will be long or short.
	 * @return the contents of the path.
	 * @throws Exception
	 */
	public String[] dir(String path, boolean longlisting) throws Exception {

		String str;
		InputStream is = list(path, longlisting).getInputStream();

		if (debug && (logger != null)) {
			logger.logdebug(sResHash.getString("MISERVER.FTPCLIENT.BEGIN.READING.DIRECTORY.LISTING"));
		}
		Vector<String> v = new Vector<String>();
		BufferedReader inp = new BufferedReader(new InputStreamReader(is));
		while ((str = inp.readLine()) != null) {
			if (debug && (logger != null)) {
				logger.logdebug(sResHash.getString("MISERVER.FTPCLIENT.DIRLIST", str));
			}
			v.add(str);
		}

		checkComplete();
		return (String[]) v.toArray(new String[0]);
	}

	/**
	 * Checks whether the file transfer (when getting/putting a file on the FTP
	 * server) is over.
	 * 
	 * @throws Exception
	 *             if the operation fails.
	 */
	public void checkComplete() throws Exception {
		try {
			data.close();
		} catch (Exception ignore) {
		}

		String response = getResponse();
		if (!response.startsWith("2"))
			throw new Exception(response);
	}

	/**
	 * Lists the contents of a given path on the FTP server. If the path refers
	 * to a file sends information for that file.
	 * 
	 * @param path
	 *            the path which contents must be shown.
	 * @param longlisting
	 *            determines whether the list should be short or long.
	 * @return the socket used for transferring the contents.
	 * @throws Exception
	 *             if the operation fails.
	 */
	public Socket list(String path, boolean longlisting) throws Exception {
		setupDataSocket();
		String str;
		if (longlisting)
			str = sendCommand(path != null ? "LIST " + path : "LIST");
		else
			str = sendCommand(path != null ? "NLST " + path : "NLST");

		if (!str.startsWith("1"))
			throw new Exception(str);

		if (passive != null) {
			data = passive;
		} else {
			data = dataServer.accept();
		}

		return data;
	}

	/**
	 * Retrieves the server response to a previously sent by the client command.
	 * 
	 * @return the server response.
	 * @throws Exception
	 *             if the operation fails.
	 */
	public String getResponse() throws Exception {

		StringBuffer reply = new StringBuffer();
		String line = reader.readLine();

		if (debug && (line != null) && (logger != null)) {
			logger.logdebug(sResHash.getString("ftp.client.line.after.read", line));
		}

		if (line != null) {
			reply.append(line);

			if (line.charAt(3) == '-') {
				String replyCode = line.substring(0, 3);
				line = reader.readLine();
				while (line != null) {
					reply.append("\n");
					reply.append(line);
					if (debug && (logger != null)) {
						logger.logdebug(sResHash.getString("ftp.client.line.after.append", line));
					}

					if (line.length() > 3 && line.charAt(3) == ' ' && line.startsWith(replyCode)) {
						reply.append("\n");
						return reply.toString();
					}

					line = reader.readLine();
				}
			}
		}
		return reply.toString();
	}

	/**
	 * Sets this FTPClient to use passive mode (or not).
	 * 
	 * @param value
	 *            If true, try to use passive mode before falling back to the
	 *            old PORT mode. If false, never use passive mode.
	 */
	public void setUsePassive(boolean value) {
		usePassive = value;
	}

	/**
	 * Returns true if we should try to use passive mode.
	 * 
	 * @return true if passive mode is to be used, otherwise false.
	 */
	public boolean getUsePassive() {
		return usePassive;
	}

}
