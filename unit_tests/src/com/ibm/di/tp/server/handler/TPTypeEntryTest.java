package com.ibm.di.tp.server.handler;

import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createNodeEntryContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPIActiveProviderServerContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPITypesContext;
import static com.ibm.di.test.utils.atom.AtomUtils.atomCategoryComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.deserializeEntry;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import org.apache.wink.common.model.atom.AtomCategory;
import org.apache.wink.common.model.atom.AtomEntry;
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
public class TPTypeEntryTest extends UnitTestTPClientContext {
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
	public void test_GET_Type_Entry_Returns_Expected_Entry() throws Exception {
		TdiNodeConfig nodeCfg = createNodeEntryContext(getTPServerConfig(), 1)[0];

		ServerAPIMock conn = createServerAPITypesContext(null, new String[] { "system:/Connectors/ibmdi.LDAP",
				"system:/Connectors/ibmdi.HTTPServer" });
		conn = createServerAPIActiveProviderServerContext(conn, nodeCfg.getProviderPort());
		setServerAPIConnection(conn);
		conn.activateMocks();
		initContext();

		MockHttpServletResponse response = app.getTypeEntry("Id0", "system:/Connectors/ibmdi.HTTPServer");
		AtomEntry entry = deserializeEntry(response.getContentAsString());

		conn.verifyMockCalls();

		AtomCategory ac = new AtomCategory();
		ac.setScheme(Constants.SCHEME_TP_TYPE);
		ac.setTerm("system:/Connectors/ibmdi.HTTPServer");

		containsInAnyOrder(atomCategoryComparator, entry.getCategories(), ac, Constants.CAT_RES_TYPE_ENTRY);
	}
}
