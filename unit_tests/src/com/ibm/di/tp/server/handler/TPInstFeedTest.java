package com.ibm.di.tp.server.handler;

import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createNodeEntryContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPIActiveProviderServerContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPIMissingProviderHandlerContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPITypesContext;
import static com.ibm.di.test.utils.atom.AtomUtils.atomCategoryComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.atomEntryComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.atomPersonComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.atomTextComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.containsRelations;
import static com.ibm.di.test.utils.atom.AtomUtils.createInstAtomEntry;
import static com.ibm.di.test.utils.atom.AtomUtils.createReferenceAtomEntry;
import static com.ibm.di.test.utils.atom.AtomUtils.deserializeEntry;
import static com.ibm.di.test.utils.atom.AtomUtils.deserializeFeed;
import static com.ibm.di.test.utils.atom.AtomUtils.elementComparator;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ibm.di.test.api.mock.ServerAPIMock;
import com.ibm.di.test.tp.UnitTestTPClientContext;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.config.node.TdiNodeConfig;
import com.ibm.di.tp.server.model.TouchpointRole;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class TPInstFeedTest extends UnitTestTPClientContext {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Override
	protected boolean isAutoInit() {
		return false;
	}

	@Test
	public void test_GET_Inst_Feed_Returns_Expected_Result() throws Exception {
		TdiNodeConfig nodeCfg = createNodeEntryContext(getTPServerConfig(), 1)[0];
		ServerAPIMock conn = createServerAPITypesContext(null, new String[] { "system:/Connectors/ibmdi.LDAP" });
		conn = createServerAPIActiveProviderServerContext(conn, nodeCfg.getProviderPort());
		setServerAPIConnection(conn);
		conn.activateMocks();
		initContext();

		MockHttpServletResponse response = app.getInstFeed("Id0", "system:/Connectors/ibmdi.LDAP");
		AtomFeed feed = deserializeFeed(response.getContentAsString());

		conn.verifyMockCalls();

		containsInAnyOrder(atomCategoryComparator, false, false, feed.getCategories(), Constants.CAT_TOUCHPOINT);
		containsRelations(feed.getLinks(), Constants.REL_SELF, Constants.REL_RESOURCE_TYPE);
		assertThat(feed.getEntries().size(), is(0));
	}

	@Test
	public void test_POST_Inst_Feed_Creates_Provider_Inst_Entry() throws Exception {
		TdiNodeConfig nodeCfg = createNodeEntryContext(getTPServerConfig(), 1)[0];
		ServerAPIMock conn = createServerAPITypesContext(null, new String[] { "system:/Connectors/ibmdi.LDAP" });
		conn = createServerAPIActiveProviderServerContext(conn, nodeCfg.getProviderPort());
		conn = createServerAPIMissingProviderHandlerContext(conn, "system:/Connectors/ibmdi.LDAP");
		setServerAPIConnection(conn);
		conn.activateMocks();
		initContext();

		// Create the entry.
		AtomEntry instEntry = createInstAtomEntry("0", TouchpointRole.PROVIDER, false, null);
		MockHttpServletResponse response = app.createInstEntry("Id0", "system:/Connectors/ibmdi.LDAP", instEntry);

		AtomEntry actEntry = deserializeEntry(response.getContentAsString());

		conn.verifyMockCalls();

		containsInAnyOrder(atomPersonComparator, false, instEntry.getAuthors(), actEntry.getAuthors());
		containsInAnyOrder(atomTextComparator, false, instEntry.getSummary(), actEntry.getSummary());
		containsInAnyOrder(atomTextComparator, false, instEntry.getTitle(), actEntry.getTitle());
		containsInAnyOrder(elementComparator, false, instEntry.getAny(), actEntry.getAny());
		containsRelations(actEntry.getLinks(), Constants.REL_SELF, Constants.REL_RESOURCE_TYPE, Constants.REL_STATUS,
				Constants.REL_EDIT);

		// verify the feed contains this entry.

		response = app.getInstFeed("Id0", "system:/Connectors/ibmdi.LDAP");

		AtomFeed feed = deserializeFeed(response.getContentAsString());
		containsInAnyOrder(atomEntryComparator, true, false, feed.getEntries(), createReferenceAtomEntry(actEntry, false));
	}

	@Test
	public void test_POST_Inst_Feed_Creates_Initiator_Inst_Entry() throws Exception {
		TdiNodeConfig nodeCfg = createNodeEntryContext(getTPServerConfig(), 1)[0];
		ServerAPIMock conn = createServerAPITypesContext(null, new String[] { "system:/Connectors/ibmdi.LDAP" });
		conn = createServerAPIActiveProviderServerContext(conn, nodeCfg.getProviderPort());
		conn = createServerAPIMissingProviderHandlerContext(conn, "system:/Connectors/ibmdi.LDAP");
		setServerAPIConnection(conn);
		conn.activateMocks();
		initContext();

		// Create the entry.
		AtomEntry instEntry = createInstAtomEntry("0", TouchpointRole.INITIATOR, false, null);
		MockHttpServletResponse response = app.createInstEntry("Id0", "system:/Connectors/ibmdi.LDAP", instEntry);

		AtomEntry actEntry = deserializeEntry(response.getContentAsString());

		conn.verifyMockCalls();

		containsInAnyOrder(atomPersonComparator, false, instEntry.getAuthors(), actEntry.getAuthors());
		containsInAnyOrder(atomTextComparator, false, instEntry.getSummary(), actEntry.getSummary());
		containsInAnyOrder(atomTextComparator, false, instEntry.getTitle(), actEntry.getTitle());
		containsInAnyOrder(elementComparator, false, instEntry.getAny(), actEntry.getAny());
		containsRelations(actEntry.getLinks(), Constants.REL_SELF, Constants.REL_RESOURCE_TYPE, Constants.REL_DESTINATION_FEED,
				Constants.REL_STATUS, Constants.REL_EDIT);

		// verify the feed contains this entry.

		response = app.getInstFeed("Id0", "system:/Connectors/ibmdi.LDAP");

		AtomFeed feed = deserializeFeed(response.getContentAsString());
		containsInAnyOrder(atomEntryComparator, true, false, feed.getEntries(), createReferenceAtomEntry(actEntry, false));
	}
}
