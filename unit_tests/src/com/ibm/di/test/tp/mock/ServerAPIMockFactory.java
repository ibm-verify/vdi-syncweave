package com.ibm.di.test.tp.mock;

import org.easymock.EasyMock;

import com.ibm.di.api.connection.IServerAPIConnection;
import com.ibm.di.api.local.AssemblyLine;
import com.ibm.di.api.local.ConfigInstance;
import com.ibm.di.api.local.ServerInfo;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.test.api.mock.AftermathAccessor;
import com.ibm.di.test.api.mock.ServerAPIMock;
import com.ibm.di.test.tp.TpAppHelper;
import com.ibm.di.test.utils.atom.AtomUtils;
import com.ibm.di.tp.server.config.TPServerConfig;
import com.ibm.di.tp.server.config.node.TdiNodeConfig;
import com.ibm.di.tp.server.context.TPServerContext;
import com.ibm.di.tp.server.model.TouchpointRole;
import com.ibm.di.tp.server.model.impl.tdi.ConnectivityProviderImpl;
import com.ibm.di.tp.server.model.impl.tdi.TemplateConfigLoader;
import com.ibm.di.tp.server.util.TDIUtils;

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
public class ServerAPIMockFactory {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Creates a mock of the server api connection that would return the
	 * specified names when the server api is called. <br>
	 * <br>
	 * Before using the mocks call {@link ServerAPIMock#activateMocks()} to
	 * activate them.
	 * 
	 * @param reuse
	 *            an instance to the reusable mock object if non-null value
	 *            provided its functionality will be used by wrapping it
	 *            otherwise a new set of mocks is returned.
	 * @param connNames
	 *            the names returned by
	 *            {@link ServerInfo#getInstalledConnectorsNames()}
	 * @return an instance of the {@link ServerAPIMock} class. To extend the
	 *         mocks functionality you could wrap it inside another
	 *         {@link ServerAPIMock} instance and predefine its
	 *         {@link ServerAPIMock#activateMocks()} method. Don't forget to
	 *         call the super method at the end.
	 * @throws Exception
	 */
	public static ServerAPIMock createServerAPITypesContext(final ServerAPIMock reuse, final String[] connNames) throws Exception {

		return new ServerAPIMock(reuse) {
			@Override
			public void activateMocks() throws Exception {
				EasyMock.expect(siMock.getInstalledConnectorsNames()).andReturn(connNames).anyTimes();

				// activate super's mocks
				super.activateMocks();
			}
		};
	}

	/**
	 * Creates a mock that fouls the tdi {@link ConnectivityProviderImpl} that
	 * the provider AL is already running. The mock allows the CI to be
	 * retrieved, its ALs list to be obtained and to call isActive for the AL
	 * that represents the active ProviderServer AL. <br>
	 * <br>
	 * Before using the mocks call {@link ServerAPIMock#activateMocks()} to
	 * activate them.
	 * 
	 * @param reuse
	 *            another mock of the server api to build up on.
	 * @param providerPort
	 *            the port from the {@link TdiNodeConfig#getProviderPort()}
	 * @return the mock object.
	 * @throws Exception
	 */
	public static ServerAPIMock createServerAPIActiveProviderServerContext(final ServerAPIMock reuse, final int providerPort)
			throws Exception {

		return new ServerAPIMock(reuse) {
			private ConfigInstance cMock = EasyMock.createMock(ConfigInstance.class);
			private AssemblyLine alMock = EasyMock.createMock(AssemblyLine.class);

			@Override
			public void activateMocks() throws Exception {
				EasyMock.expect(sMock.getConfigInstance(ConnectivityProviderImpl.CI_PROVIDER_SERVER_PREFIX + providerPort))
						.andReturn(cMock).anyTimes();
				EasyMock.expect(cMock.getAssemblyLines()).andStubReturn(new AssemblyLine[] { alMock });
				EasyMock.expect(alMock.getName()).andStubReturn(
						MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/" + TemplateConfigLoader.AL_PROVIDER_SERVER);
				EasyMock.expect(alMock.isActive()).andStubReturn(true);

				super.activateMocks();
				EasyMock.replay(cMock, alMock);
			}

			@Override
			public AftermathAccessor verifyMockCalls() {
				AftermathAccessor verifyMockCalls = super.verifyMockCalls();
				EasyMock.verify(cMock, alMock);
				return verifyMockCalls;
			}

			@Override
			public void resetMockCalls() {
				super.resetMockCalls();
				EasyMock.reset(cMock, alMock);
			}
		};
	}

	/**
	 * Creates a mock that allows the TP API layer to ask for CI of the
	 * particular TP. The mock always returns null, as if the CI has not been
	 * started yet. <br>
	 * <br>
	 * Before using the mocks call {@link ServerAPIMock#activateMocks()} to
	 * activate them.
	 * 
	 * @param reuse
	 *            another mock of the server api to build up on.
	 * @return the mock object.
	 * @throws Exception
	 */
	public static ServerAPIMock createServerAPIMissingProviderHandlerContext(final ServerAPIMock reuse, final String typeId)
			throws Exception {

		return new ServerAPIMock(reuse) {
			@Override
			public void activateMocks() throws Exception {
				EasyMock.expect(sMock.getConfigInstance(EasyMock.startsWith(TDIUtils.escapeRunName(typeId) + "_"))).andStubReturn(
						null);
				super.activateMocks();
			}
		};
	}

	/**
	 * Creates a server api context that includes one node context (created
	 * using {@link #createNodeEntryContext(int)}), a type context (created
	 * using {@link #createServerAPITypesContext(ServerAPIMock, String[])}), an
	 * active provider AL context (created using
	 * {@link #createServerAPIActiveProviderServerContext(ServerAPIMock, int)})
	 * and also a TP AL context (created using
	 * {@link TPInstanceServerAPIReactor}) which is the final mock returned.
	 * 
	 * The returned object is attached to the {@link TPServerContext} using the
	 * {@link #setServerAPIConnection(String, IServerAPIConnection)} method.
	 * Even if you chain more mocks it is not necessary to set the last mock in
	 * the chain as the {@link IServerAPIConnection}. What is important is to
	 * call the {@link ServerAPIMock#activateMocks()} method on the last mock in
	 * the chain in order to activate them all.
	 * 
	 * @param isContextForProviderTP
	 *            specifies whether the context is for a provider TP.
	 * @param ciExist
	 *            specifies whether the TP config for the handler AL exists
	 * @param alExist
	 *            specifies whether the TP handler AL itself exists
	 * @param alActive
	 *            specifies whether the TP handler AL is active. Note this is
	 *            not related to the previous to flags. If the AL is stopped but
	 *            started later and this flag is set to false it will not change
	 *            when the al is started. This helps simulate ALs that have stop
	 *            their execution.
	 * @param typeId
	 *            the type of the Connector to use when creating the type
	 *            context.
	 * @param nodeCfg
	 *            the configuration object for this node
	 * @return the {@link TPInstanceServerAPIReactor} which needs to be
	 *         activated right before it is used.
	 * @throws Exception
	 *             if an error working with the Remote API occurs.
	 */
	public static TPInstanceServerAPIReactor createServerAPITPInstanceContext(TPServerConfig cfg, TouchpointRole tr,
			boolean ciExist, boolean alExist, boolean alActive, String typeId, TdiNodeConfig nodeCfg) throws Exception {
		// setup the server api context
		ServerAPIMock conn = createServerAPIActiveProviderServerContext(null, nodeCfg.getProviderPort());

		conn = createServerAPITypesContext(conn, new String[] { typeId });
		TPInstanceServerAPIReactor robMock = new TPInstanceServerAPIReactor(conn, typeId, tr, ciExist, alExist, alActive);

		return robMock;
	}

	/**
	 * Sets up a context of several local node entries only. The method
	 * {@link AtomUtils#createTdiNodeConfg(String)} is used with prefixes "i"
	 * where i is in the interval [0, count) for the creation of the configs.
	 * 
	 * This method only modifies the {@link TPServerConfig} of the TP Server.
	 * 
	 * @param count
	 *            the number of node entries to create.
	 * 
	 * @return the {@link TdiNodeConfig} objects used for creating the node
	 *         entries in an array.
	 */
	public static TdiNodeConfig[] createNodeEntryContext(TPServerConfig cfg, int count) {
		TdiNodeConfig[] array = new TdiNodeConfig[count];

		for (int i = 0; i < count; i++) {
			array[i] = TpAppHelper.createTdiNodeConfg(Integer.toString(i));
			cfg.getNodeConfigs().getTdiNodeConfigs().add(array[i]);
		}

		return array;
	}
}
