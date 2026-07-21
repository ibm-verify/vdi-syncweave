package com.ibm.di.tp.server.handler;

import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createNodeEntryContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPIActiveProviderServerContext;
import static com.ibm.di.test.tp.mock.ServerAPIMockFactory.createServerAPITypesContext;

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
public class ServiceDocumentTest extends UnitTestTPClientContext {
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
	public void test_Verify_Service_Document_Has_Link_To_TP_Node_Feed() throws Exception {
		TdiNodeConfig[] cfgs = createNodeEntryContext(getTPServerConfig(), 2);
		ServerAPIMock mock = createServerAPIActiveProviderServerContext(null, 0);
		mock = createServerAPITypesContext(mock, new String[0]);
		mock.activateMocks();
		setServerAPIConnection(cfgs[0].getHost(), cfgs[0].getPort(), mock);
		setServerAPIConnection(cfgs[1].getHost(), cfgs[1].getPort(), mock);
		initContext();

		UnitAndFuncSharedTests.verify_Service_Document_Has_Link_To_TP_Node_Feed(this);
	}
}
