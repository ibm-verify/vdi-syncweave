package com.ibm.di.tp.server.handler;

import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createNodeEntryContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPIActiveProviderServerContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPITypesContext;
import static com.ibm.di.test.utils.atom.AtomUtils.atomEntryComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.createNodeEntryFor;
import static com.ibm.di.test.utils.atom.AtomUtils.deserializeEntry;

import org.apache.wink.common.model.atom.AtomEntry;
import org.junit.Test;

import com.ibm.di.test.api.mock.ServerAPIMock;
import com.ibm.di.test.tp.UnitTestTPClientContext;
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
public class TPNodeEntryTest extends UnitTestTPClientContext {
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
	public void test_GET_Node_Entry_Representation() throws Exception {
		TdiNodeConfig[] cfgs = createNodeEntryContext(getTPServerConfig(), 2);

		ServerAPIMock m = createServerAPITypesContext(createServerAPIActiveProviderServerContext(null, cfgs[0].getProviderPort()),
				new String[0]);
		setServerAPIConnection(m);
		m.activateMocks();

		m = createServerAPITypesContext(createServerAPIActiveProviderServerContext(null, cfgs[1].getProviderPort()), new String[0]);
		setServerAPIConnection(m);
		m.activateMocks();

		initContext();

		AtomEntry exp0 = createNodeEntryFor(CONTEXT_ROOT_USED_BY_WINK, cfgs[0]);
		AtomEntry exp1 = createNodeEntryFor(CONTEXT_ROOT_USED_BY_WINK, cfgs[1]);

		AtomEntry act0 = deserializeEntry(app.getNodeEntry("Id0").getContentAsString());
		AtomEntry act1 = deserializeEntry(app.getNodeEntry("Id1").getContentAsString());

		atomEntryComparator.assertEquals(act0, exp0, true);
		atomEntryComparator.assertEquals(act1, exp1, true);
	}
}
