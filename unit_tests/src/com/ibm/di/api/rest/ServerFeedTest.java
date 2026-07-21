package com.ibm.di.api.rest;

import static com.ibm.di.api.rest.internal.AppConstants.CAT_COMP_CONN;
import static com.ibm.di.api.rest.internal.AppConstants.CAT_COMP_FC;
import static com.ibm.di.api.rest.internal.AppConstants.CAT_COMP_PARSER;
import static com.ibm.di.api.rest.internal.AppConstants.CAT_RES_SERVER;
import static com.ibm.di.api.rest.internal.AppConstants.CAT_SRV_CONTROL;
import static com.ibm.di.api.rest.internal.AppConstants.CAT_SRV_INFO;
import static com.ibm.di.api.rest.internal.AppConstants.CAT_SRV_NOTIFICATION;
import static com.ibm.di.api.rest.internal.AppConstants.MT_COMPONENT_XML;
import static com.ibm.di.api.rest.internal.AppConstants.MT_SERVER_CONTROL_XML;
import static com.ibm.di.api.rest.internal.AppConstants.MT_SERVER_INFO_XML;
import static com.ibm.di.api.rest.internal.AppConstants.MT_SERVER_NOTIFY_XML;
import static com.ibm.di.api.rest.internal.AppConstants.REL_COMPONENT;
import static com.ibm.di.api.rest.internal.AppConstants.REL_NOTIFY;
import static com.ibm.di.api.rest.internal.AppConstants.REL_SELF;
import static com.ibm.di.api.rest.internal.AppConstants.REL_SHUTDOWN;

import javax.ws.rs.core.MediaType;

import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomFeed;
import org.junit.Test;

import com.ibm.di.api.bind.CustomNotification;
import com.ibm.di.api.bind.Data;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.test.api.mock.ServerAPIMock;
import com.ibm.di.test.api.mock.ServerAPIMockBuilder;
import com.ibm.di.test.rest.UnitTestRestClientContext;
import com.ibm.di.test.utils.TestUtils;
import com.ibm.di.test.utils.atom.AtomEntryBuilder;
import com.ibm.di.test.utils.atom.AtomUtils;

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
public class ServerFeedTest extends UnitTestRestClientContext {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Test
	public void test_ServerFeed_Contains_Correct_Entries() throws Exception {
		AtomFeed serverFeed = app.getServerFeed();

		AtomUtils.containsAllInAnyOrder(AtomUtils.atomCategoryComparator, true, false, serverFeed.getCategories(), CAT_RES_SERVER);

		AtomEntry info = AtomEntryBuilder.newBuilder().id("info").cat(CAT_SRV_INFO).link(REL_SELF, MediaType.APPLICATION_ATOM_XML)
				.link(REL_COMPONENT, MediaType.APPLICATION_ATOM_XML).content(MT_SERVER_INFO_XML).build();
		AtomEntry control = AtomEntryBuilder.newBuilder().id("control").cat(CAT_SRV_CONTROL).link(REL_SELF,
				MediaType.APPLICATION_ATOM_XML).link(REL_SHUTDOWN, MT_SERVER_CONTROL_XML).build();
		AtomEntry notify = AtomEntryBuilder.newBuilder().id("notify").cat(CAT_SRV_NOTIFICATION).link(REL_SELF,
				MediaType.APPLICATION_ATOM_XML).link(REL_NOTIFY, MT_SERVER_NOTIFY_XML).build();

		AtomUtils.containsAllInAnyOrder(AtomUtils.atomEntryComparator, true, serverFeed.getEntries(), info, control, notify);
	}

	@Test
	public void test_ComponentsFeed_Contains_Correct_Entries() throws Exception {
		setIServerAPIConnection(new ServerAPIMockBuilder().conn("ibmdi.Http").fc("ibmdi.cmdln").parser("ibmdi.xml").build());
		AtomFeed cFeed = app.getComponentsFeed();

		AtomEntry conn = AtomEntryBuilder.newBuilder().id("conn").title("ibmdi.Http").cat(CAT_COMP_CONN).link(REL_SELF,
				MediaType.APPLICATION_ATOM_XML).content(MT_COMPONENT_XML).build();
		AtomEntry fc = AtomEntryBuilder.newBuilder().id("fc").title("ibmdi.cmdln").cat(CAT_COMP_FC).link(REL_SELF,
				MediaType.APPLICATION_ATOM_XML).content(MT_COMPONENT_XML).build();
		AtomEntry parser = AtomEntryBuilder.newBuilder().id("parser").title("ibmdi.xml").cat(CAT_COMP_PARSER).link(REL_SELF,
				MediaType.APPLICATION_ATOM_XML).content(MT_COMPONENT_XML).build();

		AtomUtils.containsAllInAnyOrder(AtomUtils.atomEntryComparator, true, cFeed.getEntries(), conn, fc, parser);
	}

	@Test
	public void test_String_Object_Is_Send_As_Notification_Object() throws Exception {
		ServerAPIMock validator = new ServerAPIMockBuilder().notification("stringType", "stringId", "stringValue").build();
		setIServerAPIConnection(validator);

		CustomNotification cn = new CustomNotification();
		cn.setType("stringType");
		cn.setId("stringId");
		Data d = new Data();
		d.setType("text/plain");
		d.setValue("stringValue");
		cn.setData(d);

		app.sendCustomNotification(cn);

		validator.verifyMockCalls();
	}

	@Test
	public void test_Bytes_Array_Is_Send_As_Notification_Object() throws Exception {
		byte[] bytes = "bytesValue".getBytes();

		ServerAPIMock validator = new ServerAPIMockBuilder().notification("byteType", "byteId", bytes).build();
		setIServerAPIConnection(validator);

		CustomNotification cn = new CustomNotification();
		cn.setType("byteType");
		cn.setId("byteId");
		Data d = new Data();
		d.setType("application/octet-stream");
		d.setValue(UserFunctions.base64Encode(bytes));
		cn.setData(d);

		app.sendCustomNotification(cn);
		validator.verifyMockCalls();
	}

	@Test
	public void test_Serialized_Object_Is_Send_As_Notification_Object() throws Exception {
		Entry e = new ComparableEntry();
		e.setAttribute("attr", "val");

		ServerAPIMock validator = new ServerAPIMockBuilder().notification("objType", "objId", e).build();
		setIServerAPIConnection(validator);

		CustomNotification cn = new CustomNotification();
		cn.setType("objType");
		cn.setId("objId");
		Data d = new Data();
		d.setType("application/octet-stream+object");

		d.setValue(UserFunctions.base64Encode(TestUtils.serializeObject(e)));
		cn.setData(d);

		app.sendCustomNotification(cn);
		validator.verifyMockCalls();
	}

	private static class ComparableEntry extends Entry {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean equals(Object o) {
			boolean res = false;
			if (o instanceof Entry) {
				res = ((Entry) o).size() == size();

				for (String name : ((Entry) o).getAttributeNames()) {
					res &= getObject(name).equals(((Entry) o).getObject(name));
				}
			}
			return res;
		}
	}
}
