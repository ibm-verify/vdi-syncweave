/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.easyetl.internal.handler.ds;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.di.store.StoreFactory;
import com.ibm.di.ui.easyetl.bind.RowData;
import com.ibm.di.ui.easyetl.bind.Rows;
import com.ibm.di.ui.easyetl.bind.Store;
import com.ibm.di.ui.easyetl.bind.Stores;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
@Path(DeltaStoreHandler.URL)
public class DeltaStoreHandler {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public final static String URL = "ds";
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response defaultPage() throws Exception {
		Connection connection = StoreFactory.getConnection();
		ResultSet rs = connection.getMetaData().getTables(null, null, "IDI_%", null);
		Stores stores = new Stores();
		while (rs.next()) {
			Store store = new Store();
			store.setName(rs.getString(3));
			store.setType(">>>" + rs.getString(4));
			store.setColumnData(new RowData());
			store.getColumnData().getValue().add("Funky");
			store.setRowData(new Rows());
			RowData rd = new RowData();
			rd.getValue().add("Data value 1");
			rd.getValue().add("Data value 2");
			store.getRowData().getData().add(rd);
			stores.getStores().add(store);
		}
		rs.close();
		connection.close();
		return Response.ok(stores).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{store}")
	public Response storeContents(@Context HttpServletRequest req, @PathParam("store") String store) throws Exception {
		Store ds = new Store();
		ds.setName(URLEncoder.encode(store));
		ds.setType("Table");

		Connection connection = StoreFactory.getConnection();
		Statement statement = connection.createStatement();
		try {
			validateStoreName(connection, store);

			ResultSet resultSet = statement.executeQuery("SELECT * FROM " + store);
			try {

				RowData columns = new RowData();
				ds.setColumnData(columns);

				int count = resultSet.getMetaData().getColumnCount();
				for(int i = 1; i <= count; i++) {
					columns.getValue().add(resultSet.getMetaData().getColumnName(i));
				}

				Rows rows = new Rows();
				ds.setRowData(rows);

				while(resultSet.next()) {
					RowData rd = new RowData();
					for(int i = 1; i <= count; i++) {
						String value = null;
						if(resultSet.getMetaData().getColumnType(i) == Types.BLOB) {
							try {
								Object obj = StoreFactory.deserializeObject(resultSet.getObject(i));
								if(obj != null)
									value = obj.toString();
							} catch (Exception e) {
								value = e.toString();
							}
						} else {
							value = resultSet.getString(i);
						}

						if(value == null)
							value = "[null]";

						if(value.length() > 1000)
							value = value.substring(0,1000);

						rd.getValue().add(value);
					}
					rows.getData().add(rd);
				}
			} finally {
				resultSet.close();
			}
		} finally {
			statement.close();
			connection.close();
		}

		return Response.ok(ds).build();
	}

	private void validateStoreName(Connection connection, String store) throws Exception {
		ResultSet rs = connection.getMetaData().getTables(null, null, "IDI_%", null);

		try {
			boolean valid = false;

			while (rs.next()) {
				String name = rs.getString(3);

				if (store.equals(name)) {
					valid = true;
					break;
				}
			}

			if (!valid) {
				throw new Exception(store + " is not a known store name.");
			}
		} finally {
			rs.close();
		}
	}
}
