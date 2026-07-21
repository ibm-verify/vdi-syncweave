package com.ibm.di.tp.server.handler;

import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createNodeEntryContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPIActiveProviderServerContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPITypesContext;
import static com.ibm.di.test.utils.atom.AtomUtils.atomCategoryComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.containsInAnyOrder;
import static com.ibm.di.test.utils.atom.AtomUtils.deserializeFeed;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ibm.di.test.api.mock.ServerAPIMock;
import com.ibm.di.test.tp.UnitTestTPClientContext;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.config.node.TdiNodeConfig;

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
public class TPTypeFeedTest extends UnitTestTPClientContext {
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
	public void test_GET_Type_Feed_Returns_Expected_Types() throws Exception {
		TdiNodeConfig nodeCfg = createNodeEntryContext(getTPServerConfig(), 1)[0];
		ServerAPIMock conn = createServerAPITypesContext(null, new String[] { "system:/Connectors/ibmdi.LDAP",
				"system:/Connectors/ibmdi.HTTPServer" });
		conn = createServerAPIActiveProviderServerContext(conn, nodeCfg.getProviderPort());
		setServerAPIConnection(conn);
		conn.activateMocks();
		initContext();

		MockHttpServletResponse response = app.getTypeFeed(nodeCfg.getId());
		AtomFeed feed = deserializeFeed(response.getContentAsString());

		// verify the mock object
		conn.verifyMockCalls();

		// make sure the entries are the one we expect + the virtual one
		assertThat(feed.getEntries().size(), is(3));

		List<String> ids = new ArrayList<String>();
		for (AtomEntry e : feed.getEntries()) {
			ids.add(e.getId());
		}
		assertThat("Comparing feed categories", containsInAnyOrder(atomCategoryComparator, false, false, feed.getCategories(),
				Constants.CAT_TOUCHPOINT), is(true));
	}
}
