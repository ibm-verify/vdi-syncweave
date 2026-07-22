/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class SQLUtilities provides static methods for manipulating SQL expressions and statements. 
 * 
 * @author yavor.gologanov
 *
 */
public class SQLUtilities {

	public static final String TYPE_VARGRAPHIC = "VARGRAPHIC";
	
	/**
	 * 
	 * @param value
	 * @return String
	 */
	public static String formatVargraphic(String value) {
		String formattedValue = "'" + value.replace("'", "''").trim() + "'";
		return "VARGRAPHIC(" + formattedValue + ")";
	}
	 
	/**
	 * 
	 * @param columnName
	 * @param columnType
	 * @param resultSet
	 * @param ignoreFieldErrors
	 * @return Object
	 * @throws SQLException
	 * @throws IOException
	 */
	public static Object getValue(String columnName, 
			int columnType, 
			ResultSet resultSet,
			boolean ignoreFieldErrors) 
		throws SQLException, IOException {

		Object val = null;

		switch (columnType) {

		// String & Character Data
		case Types.CHAR:
			
		case Types.VARCHAR:
			val = resultSet.getString(columnName);
			break;
			
		// Probably NCHAR or NVARCHAR ?
		case Types.OTHER:
			val = resultSet.getString(columnName);
			break;
			
		case Types.LONGVARBINARY:
			java.io.InputStream bis = resultSet.getBinaryStream(columnName);
			if (bis == null)
				break;
			try {
				int ch;
				ByteArrayOutputStream ba = new ByteArrayOutputStream();
				while ((ch = bis.read()) != -1) {
					ba.write(ch);
				}
				ba.close();
				val = ba.toByteArray();
			} catch (java.io.IOException ioe) {
				bis.close();
				if (ignoreFieldErrors) {
					val = ioe;
				} else {
					throw ioe;
				}
			}

			break;
			
		case Types.LONGVARCHAR:
			java.io.InputStream is = resultSet.getAsciiStream(columnName);
			if (is == null)
				break;
			try {
				int ch;
				StringBuffer buf = new StringBuffer();
				while ((ch = is.read()) != -1) {
					buf.append((char) ch);
				}
				is.close();
				val = buf.toString();
			} catch (java.io.IOException ioe) {
				is.close();
				if (ignoreFieldErrors) {
					val = ioe;
				} else {
					throw ioe;
				}
			}
			break;

		// Numbers
		case Types.INTEGER:
		case Types.TINYINT:
		case Types.SMALLINT:
			val = Integer.valueOf(resultSet.getInt(columnName));

			if (resultSet.wasNull()) {
				val = null;
			}
			break;

		// Real & Float
		case Types.BIGINT:
		case Types.DECIMAL:
		case Types.NUMERIC:
			val = resultSet.getObject(columnName);

			if (resultSet.wasNull()) {
				val = null;
			}
			break;
			
		case Types.REAL:
		case Types.FLOAT:
		case Types.DOUBLE:
			val = new java.lang.Double(resultSet.getDouble(columnName));
			if (resultSet.wasNull()) {
				val = null;
			}
			break;
			
		// Boolean
		case Types.BIT:
			// val = (rs.getBoolean(i) ? "true" : "false");
			val = Boolean.valueOf(resultSet.getBoolean(columnName));
			break;
			
		// Date/Time
		case Types.DATE:
			val = resultSet.getDate(columnName);
			break;			
		case Types.TIME:
			val = resultSet.getTime(columnName);
			break;			
		case Types.TIMESTAMP:
			
		case 11:
			val = resultSet.getTimestamp(columnName);
			break;
			
		case Types.VARBINARY:
			try {
				val = resultSet.getBytes(columnName);
			} catch (Exception ignore) {
				val = resultSet.getObject(columnName);
			}
			break;

		case Types.BLOB:
			Blob blob = resultSet.getBlob(columnName);
			if (null != blob) {
				java.io.InputStream bisb = blob.getBinaryStream();

				if (bisb == null)
					break;
				try {
					int ch;
					ByteArrayOutputStream ba = new ByteArrayOutputStream();
					while ((ch = bisb.read()) != -1) {
						ba.write(ch);
					}
					ba.close();
					val = ba.toByteArray();
				} catch (java.io.IOException ioe) {
					bisb.close();
					if (ignoreFieldErrors) {
						val = ioe;
					} else {
						throw ioe;
					}
				}
			}
			break;

		case Types.CLOB:
			Clob clob = resultSet.getClob(columnName);
			if (clob == null)
				break;
			java.io.InputStream isc = clob.getAsciiStream();
			if (isc == null)
				break;
			try {
				int ch;
				StringBuffer buf = new StringBuffer();
				while ((ch = isc.read()) != -1) {
					buf.append((char) ch);
				}
				isc.close();
				val = buf.toString();
			} catch (java.io.IOException ioe) {
				isc.close();
				if (ignoreFieldErrors) {
					val = ioe;
				} else {
					throw ioe;
				}
			}
			break;

		case Types.BINARY:
		case Types.JAVA_OBJECT:
		case Types.DISTINCT:
		case Types.STRUCT:
		case Types.ARRAY:

		case Types.REF:
		default:
			val = resultSet.getObject(columnName);
			break;
		}

		return val;
	}	
		
	/**
	 * 
	 * @param statement
	 * @param paramIndex
	 * @param value
	 * @throws SQLException
	 */
	public static void setValue(PreparedStatement statement,
			int paramIndex, 
			Object value) 
		throws SQLException {

		if (value == null) {
			statement.setNull(paramIndex, Types.VARCHAR);
			return;
		}
		
		if (value instanceof String) {
			statement.setString(paramIndex, (String) value); 
		} else if (value instanceof Integer) {
			statement.setInt(paramIndex, (Integer) value); 
		} else if (value instanceof Double) {
			statement.setDouble(paramIndex, (Double) value); 
		} else if (value instanceof Timestamp) {
			statement.setTimestamp(paramIndex, (Timestamp) value); 
		} else {
			statement.setObject(paramIndex, value);
		}
	}		
	
    /**
     * 
     * @param resultSet
     * @param ignoreFieldErrors
     * @return Map<String, Object>
     * @throws SQLException
     * @throws IOException
     */
	public static Map<String, Object> getRowData(ResultSet resultSet, 
			boolean ignoreFieldErrors) 
		throws SQLException, IOException {
	
		ResultSetMetaData metaData = resultSet.getMetaData();
		Map<String, Object> values = new TreeMap<String, Object>();
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
		
			String columnName = metaData.getColumnName(i);
			Object columnValue = getValue(columnName, 
					metaData.getColumnType(i), 
					resultSet,
					ignoreFieldErrors);
			values.put(columnName, columnValue);
		}
	
		return values;
	}	
	
}
