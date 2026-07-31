/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.Vector;
import java.text.MessageFormat;

import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.fc.FunctionInterface;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.store.StoreFactory;
import com.ibm.di.server.*;

/**
 * This class interposes connectors to record and playback all operations
 * performed by the connector. Every method call is intercepted and the result
 * is stored in a table for later playback.
 * 
 * The table is created by using the StoreFactory requesting a table name of
 * "SANDBOX_<conn name>".
 */
public class RecordAL implements InvocationHandler {

	private Object obj;

	private Log log;

	private Connection conn;

	private Statement st;

	private ResultSet rs;

	private PreparedStatement insert1;
	private PreparedStatement insert2;
	private PreparedStatement insert3;
	
	private int insertOrder;
	
	private String table;

	private String prefix;

	private String database;

	private String name;

	private String type;

	private boolean replay;

	private boolean record;

	private boolean trace = Boolean.getBoolean("com.ibm.di.sandbox.trace");

	private static final String PROPERTIES_FILE = "miserver";

	private static final String SEPARATOR = ";";

	private static final String UNIQUE = "{UNIQUE}";

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Utility method to return a new proxy instance for a given object.
	 * 
	 * @param obj
	 *            The java object to interpose.
	 * 
	 * @return A proxy object interposing obj
	 */
	public static Object newInstance(String prefix, Object obj, String database, boolean record) {
		return Proxy.newProxyInstance(obj.getClass().getClassLoader(),
				obj.getClass().getInterfaces(),
				new RecordAL(prefix, obj, database, record));
	}

	public static Object newInstance(String prefix, Object obj,	String database, boolean record, Class<?>[] interfaces) {
		return Proxy.newProxyInstance(obj.getClass().getClassLoader(), 
				interfaces, 
				new RecordAL(prefix, obj, database, record));
	}

	/**
	 * Private constructor.
	 */
	private RecordAL(String prefix, Object obj, String database, boolean record) {
		this.obj = obj;
		this.prefix = prefix;
		this.database = database;
		this.record = record;
		this.replay = !record;

		if (obj instanceof ConnectorInterface) {
			name = ((ConnectorInterface) obj).getName();
			type = "Connector";
		} else if (obj instanceof FunctionInterface) {
			name = ((FunctionInterface) obj).getConfiguration().getParent().getName().toString();
			type = "Function";
		} else {
			name = obj.getClass().getName();
			if (name.lastIndexOf(".") != -1)
				name = name.substring(name.lastIndexOf(".") + 1);
			type = name;
		}

		log = new Log(type + "Trace." + prefix + "." + name);
		table = fixTable(prefix + "_" + name);
	}

	private String fixTable(String table) {
		char[] t = table.toCharArray();
		for (int i = 0; i < t.length; i++) {
			if (!Character.isLetterOrDigit(t[i]))
				t[i] = '_';
		}
		return new String(t);
	}

	/**
	 * This method is called for every method call to the target object.
	 */
	public Object invoke(Object proxy, Method m, Object[] args)
			throws Throwable {

		Object result = null;
		Throwable error = null;
		String name = m.getName();

		try {

			if (trace) {
				StringBuffer buf = new StringBuffer();
				Class<?>[] types = m.getParameterTypes();
				buf.append(name + "(");
				for (int i = 0; i < types.length; i++) {
					if (i > 0)
						buf.append(", ");
					buf.append("" + args[i]);
				}
				buf.append(")");
				log.info(sResHash.getString("MISERVER.RECORDAL.INVOKE.INFO",
						buf.toString()));
			}

			if (conn == null)
				initializeTable();

			if (replay) {
				if (!rs.next()) {
					throw new Exception(sResHash.getString(
							"call.to.name.table", new Object[] { name, table }));
				}

				String xName = rs.getString(1);
				if (!name.equals(xName)) {
					throw new Exception(sResHash
							.getString("call.to.name.method", new Object[] {
									name, xName }));
				}

				result = rs.getObject(3);
				if (rs.wasNull())
					result = StoreFactory.deserializeObject(rs.getObject(2));
				else
					error = (Throwable) StoreFactory.deserializeObject(result);

			} else {
				result = m.invoke(obj, args);
				if (result == null) {
					if (insert1 == null)
						insert1 = conn.prepareStatement("INSERT INTO " + table + " (METHOD, INSERTORDER) VALUES (?,?) ");
					insert1.setString(1, name);
					insert1.setInt(2, insertOrder++);
					insert1.execute();
				} else {
					if (insert2 == null)
						insert2 = conn.prepareStatement("INSERT INTO " + table + " (METHOD, RESULT, INSERTORDER) VALUES (?,?,?) ");
					insert2.setString(1, name);
					insert2.setBytes(2, StoreFactory.serializeObject(result));
					insert2.setInt(3, insertOrder++);
					insert2.execute();
				}
				conn.commit();
			}
		} catch (InvocationTargetException e) {
			if (insert3 == null)
				insert3 = conn.prepareStatement("INSERT INTO " + table + " (METHOD, ERROR, INSERTORDER) VALUES (?,?,?) ");
			insert3.setString(1, name);
			insert3.setBytes(2, StoreFactory.serializeObject(e.getTargetException()));
			insert3.setInt(3, insertOrder++);
			insert3.execute();
			conn.commit();
			error = e.getTargetException();
		} catch (Exception e) {
			throw new RuntimeException(sResHash.getString("interlan.tdi.proxy.exception", e.getMessage()), e);
		}

		if (trace && (result != null || error != null)) {
			if (error == null) {
				log.info("trace.result.method.invocation", result);
			} else {
				log.info("trace.error.method.invocation", error);
			}
		}

		if ("terminate".equals(name)) {
			if (insert1 != null) {
				insert1.close();
				insert1 = null;
			}
			if (insert2 != null) {
				insert2.close();
				insert2 = null;
			}
			if (insert3 != null) {
				insert3.close();
				insert3 = null;
			}
			// TODO: Should we also close conn and st? Would fail if the connector is terminated/initialized multiple times
		}

		if (error == null)
			return result;
		else
			throw error;
	}

	/**
	 * Initialize the table that holds/receives connector operations.
	 */
	private void initializeTable() throws Exception {

		conn = StoreFactory.getConnection(database, false);

		if (record) {
			log.info(sResHash.getString("recording.connector.to.table",
					new Object[] { table, database }));
			try {
				Statement st = conn.createStatement();
				try {
					st.execute("DROP TABLE " + table);
				} finally {
					st.close();
				}
				conn.commit();
			} catch (Exception ignore) {
				SystemFunctions.doNothing();
			}

			Vector<String> v = new Vector<String>();
			String time = Long.toHexString(System.currentTimeMillis());
			if (time.length() > 6)
				time = time.substring(time.length() - 6, time.length());
			
			String sql1 = StoreFactory.getProperty("com.ibm.di.store.create.recal.conops");
			if (sql1 == null || ! sql1.contains("INSERTORDER"))
				sql1 = "CREATE TABLE {0} (METHOD varchar(VARCHAR_LENGTH), RESULT BLOB, ERROR BLOB, INSERTORDER int)";
			
			for (String sql: sql1.split(SEPARATOR)) {
				sql = sql.replaceFirst(StoreFactory.REGEX, StoreFactory.VARCHAR_LENGTH);
				if (sql.contains(UNIQUE))
					sql = sql.replace(UNIQUE, "{1}");
				v.add(MessageFormat.format(sql, new Object[] { table, time }));
			}

			StoreFactory.verifyTable(conn, table, v);
		}

		if (replay) {
			st = conn.createStatement();
			log.info(sResHash.getString("reading.connector.from.table",
					new Object[] { table, database }));
			rs = st.executeQuery("SELECT METHOD, RESULT, ERROR FROM " + table + " ORDER BY INSERTORDER");
		}
	}

}
