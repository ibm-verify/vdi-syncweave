package com.ibm.di.test.utils.func;

import java.io.File;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.derby.drda.NetworkServerControl;

import com.ibm.di.test.utils.FileRecorder;

/**
 * Run Derby Server in network mode for testing purposes. Database is created in
 * the temp folder and is deleted when the {@link #close()} method is called.
 */
public class DerbyServer {

	private static int PING_COUNT = 20;

	private static int PING_DELAY_MILLIS = 1000;

	private static String DB_NAME_PREFIX = "test_derby_db_";

	private Driver jdbcDriver;

	private int serverPort;

	private String username;

	private String password;

	private NetworkServerControl server;

	private File dbDir;

	private FileRecorder rec;

	public DerbyServer(int port, String username, String password) throws Exception {

		this.serverPort = port;
		this.username = username;
		this.password = password;

		this.jdbcDriver = (Driver) Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();

		InetAddress host = InetAddress.getByName("localhost");
		this.server = new NetworkServerControl(host, port, username, password);
		server.start(null);

		waitForServerToStart(server);

		File tempDir = new File("temp");
		tempDir.mkdir();
		String dbDirName = generageDBDirName();
		this.dbDir = new File(tempDir, dbDirName);
		rec = new FileRecorder(new File(tempDir, "backup"));
		rec.recordModifyDir(dbDir, true);
		
		createDB();
	}

	private String generageDBDirName() {
		return DB_NAME_PREFIX + System.currentTimeMillis();
	}

	private void waitForServerToStart(NetworkServerControl server) throws Exception {
		boolean unavailable = true;
		for (int i = 0; i < PING_COUNT && unavailable; ++i) {
			try {
				server.ping();
				unavailable = false;
			} catch (Exception ex) {
				Thread.sleep(PING_DELAY_MILLIS);
			}
		}
		if (unavailable) {
			throw new Exception("Could not start Derby server.");
		}
	}

	private void waitForServerToStop(NetworkServerControl server) throws Exception {
		boolean available = true;
		for (int i = 0; i < PING_COUNT && available; ++i) {
			try {
				server.ping();
				Thread.sleep(PING_DELAY_MILLIS);
			} catch (Exception ex) {
				available = false;
			}
		}
		if (available) {
			throw new Exception("Could not stop Derby server.");
		}
	}

	private void createDB() throws Exception {
		String jdbcUrl = getJdbcUrl() + ";create=true";
		Connection conn = getConnection(jdbcUrl);
		conn.close();
	}

	private void closeDB() throws Exception {
		String jdbcUrl = getJdbcUrl() + ";shutdown=true";
		Connection conn = getConnection(jdbcUrl);
		conn.close();
	}

	private Connection getConnection(String jdbcUrl) throws Exception {
		Properties info = new Properties();
		info.setProperty("user", getUsername());
		info.setProperty("password", getPassword());
		return jdbcDriver.connect(jdbcUrl, info);
	}

	public Connection getConnection() throws Exception {
		return getConnection(getJdbcUrl());
	}

	public String getJdbcUrl() {
		return "jdbc:derby://localhost:" + serverPort + "/" + dbDir.getAbsolutePath();
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getJdbcDriverClassName() {
		return jdbcDriver.getClass().getCanonicalName();
	}

	public void close() throws Exception {
		try {
			/*
			 * must shutdown the db, otherwise the server does not release the
			 * lock
			 */
			closeDB();
		} catch (SQLException sqlex) {
			if (!"08006".equals(sqlex.getSQLState())) {
				// "08006" is "normal":
				// http://db.apache.org/derby/papers/DerbyTut/embedded_intro.html#shutdown
				// http://forums.sun.com/thread.jspa?threadID=5194143
				throw sqlex;
			}
		}
		server.shutdown();
		waitForServerToStop(server);
		rec.destroy();
	}
}
