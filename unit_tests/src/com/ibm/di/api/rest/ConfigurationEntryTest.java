package com.ibm.di.api.rest;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.ibm.di.api.rest.internal.handler.config.ConfigurationFile;
import com.ibm.di.config.bind.BindUtil;
import com.ibm.di.config.bind.SolutionBinding;
import com.ibm.di.config.bind.SolutionContextBinding;
import com.ibm.di.config.bind.SolutionInterfaceBinding;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.test.api.mock.ServerAPIMock;
import com.ibm.di.test.api.mock.ServerAPIMockBuilder;
import com.ibm.di.test.config.bind.Match;
import com.ibm.di.test.rest.UnitTestRestClientContext;

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
public class ConfigurationEntryTest extends UnitTestRestClientContext {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Test
	public void test_Configuration_Check_Out() throws Exception {
		MetamergeConfig mc = BindUtil.toMetamergeConfig(getOperationalSolution());
		ServerAPIMock mock = new ServerAPIMockBuilder().configFile("a/cfg.xml", mc).build();
		setIServerAPIConnection(mock);

//		CheckOutConfig co = new CheckOutConfig();
//		SolutionBinding coSol = app.checkOutConfig("a/cfg.xml", co);
//
//		assertThat(coSol.getContext(), is(not(nullValue())));
//		assertThat(coSol.getContext().getInterface(), is(getInterfaceMatcher()));
//
//		AtomEntry cfg = app.navigateToConfigFile("a/cfg.xml");
//		assertTrue(RestAppHelper.isCategorySet(cfg, AppConstants.CAT_CONFIG_LOCKED));
//
//		assertThat(RestAppHelper.getResourceUrlFromEntryNoException(cfg, AppConstants.REL_CHECK_OUT), is(nullValue()));
//		assertThat(RestAppHelper.getResourceUrlFromEntry(cfg, AppConstants.REL_CHECK_IN), is(not(nullValue())));
//		assertThat(RestAppHelper.getResourceUrlFromEntry(cfg, AppConstants.REL_LOCK), is(not(nullValue())));
//
//		AftermathAccessor access = mock.verifyMockCalls();
//		assertTrue(access.isConfigFileChekedOut("a/cfg.xml"));
	}

	@Test
	public void test_Configuration_Check_In() throws Exception {
		MetamergeConfig mc = BindUtil.toMetamergeConfig(getOperationalSolution());
		ServerAPIMock mock = new ServerAPIMockBuilder().configFile("a/cfg.xml", mc).build();
		setIServerAPIConnection(mock);

		// KK: get back to these when there is some time for that.
//		CheckOutConfig co = new CheckOutConfig();
//		SolutionBinding coSol = app.checkOutConfig("a/cfg.xml", co);
//
//		assertThat(coSol.getContext(), is(not(nullValue())));
//		assertThat(coSol.getContext().getInterface(), is(getInterfaceMatcher()));
//
//		AtomEntry cfg = app.navigateToConfigFile("a/cfg.xml");
//		assertTrue(RestAppHelper.isCategorySet(cfg, AppConstants.CAT_CONFIG_LOCKED));
//
//		coSol.getContext().getInterface().setSolutionName("newSolName");
//		CheckInConfig ci = new CreateConfig();
//		ci.setSolution(coSol);
//		app.checkInConfig(cfg, ci);
//
//		cfg = app.navigateToConfigFile("a/cfg.xml");
//		assertFalse(RestAppHelper.isCategorySet(cfg, AppConstants.CAT_CONFIG_LOCKED));
//
//		assertThat(RestAppHelper.getResourceUrlFromEntryNoException(cfg, AppConstants.REL_CHECK_IN), is(nullValue()));
//		assertThat(RestAppHelper.getResourceUrlFromEntryNoException(cfg, AppConstants.REL_LOCK), is(nullValue()));
//		assertThat(RestAppHelper.getResourceUrlFromEntry(cfg, AppConstants.REL_CHECK_OUT), is(not(nullValue())));
//
//		AftermathAccessor access = mock.verifyMockCalls();
//		assertFalse(access.isConfigFileChekedOut("a/cfg.xml"));
//		mc = access.getConfigurationFile("a/cfg.xml");
//		SolutionInterface iface = (SolutionInterface) mc.lookup("Config/SolutionInterface");
//		assertThat(iface.getInstanceID(), is("newSolName"));
	}

	@Test
	public void test_Configuration_Unlock() throws Exception {
		MetamergeConfig mc = BindUtil.toMetamergeConfig(getOperationalSolution());
		ServerAPIMock mock = new ServerAPIMockBuilder().configFile("a/cfg.xml", mc).build();
		setIServerAPIConnection(mock);

//		CheckOutConfig co = new CheckOutConfig();
//		app.checkOutConfig("a/cfg.xml", co);
//		// we know checkin out works... tested above
//		app.unlockConfig("a/cfg.xml", new UnlockConfig());
//
//		AtomEntry cfg = app.navigateToConfigFile("a/cfg.xml");
//		assertFalse(RestAppHelper.isCategorySet(cfg, AppConstants.CAT_CONFIG_LOCKED));
//
//		assertThat(RestAppHelper.getResourceUrlFromEntryNoException(cfg, AppConstants.REL_CHECK_IN), is(nullValue()));
//		assertThat(RestAppHelper.getResourceUrlFromEntryNoException(cfg, AppConstants.REL_LOCK), is(nullValue()));
//		assertThat(RestAppHelper.getResourceUrlFromEntry(cfg, AppConstants.REL_CHECK_OUT), is(not(nullValue())));
//
//		AftermathAccessor access = mock.verifyMockCalls();
//		assertFalse(access.isConfigFileChekedOut("a/cfg.xml"));
//		mc = access.getConfigurationFile("a/cfg.xml");
//		SolutionInterface iface = (SolutionInterface) mc.lookup("Config/SolutionInterface");
//		assertThat(iface.getInstanceID(), is("solName"));
	}

	private SolutionBinding getOperationalSolution() {
		SolutionBinding sb = new SolutionBinding();
		SolutionContextBinding sc = new SolutionContextBinding();
		SolutionInterfaceBinding iface = new SolutionInterfaceBinding();
		iface.setEnabled(true);
		iface.setSolutionName("solName");
		sc.setInterface(iface);
		sb.setContext(sc);
		return sb;
	}

	private Matcher<SolutionInterfaceBinding> getInterfaceMatcher() {
		return Match.iface(true, "solName", null, -1, null, null);
	}

	@Test
	public void test_toApiConfigId() throws Exception {
		assertThat("a/b/c.xml", is(ConfigurationFile.toApiConfigId("http://localhost:1098/rest/config//a/b/e%3Ac.xml")));
		assertThat("config/a/b/c.xml", is(ConfigurationFile.toApiConfigId("http://localhost:1098/rest/config/config/a/b/e:c.xml")));
		assertThat("a/b/c.xml", is(ConfigurationFile.toApiConfigId("http://localhost:1098/rest/config/a/b/e%3Ac.xml/")));
		assertThat("a/<b?/c.xml", is(ConfigurationFile.toApiConfigId("http://localhost:1098/rest/config/a/%3Cb%3F/e%3Ac.xml/")));

		assertThat("c.xml", is(ConfigurationFile.toApiConfigId("http://localhost:1098/rest/config/e%3Ac.xml")));
	}
}
