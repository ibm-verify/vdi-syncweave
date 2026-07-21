package com.ibm.di.test.tp.mock;

import org.easymock.EasyMock;

import com.ibm.di.api.local.ConfigInstance;
import com.ibm.di.api.local.Session;
import com.ibm.di.test.api.mock.ServerAPIMock;
import com.ibm.di.tp.server.util.TDIUtils;

/**
 * This mock is chained to the standard {@link TPInstanceServerAPIReactor} to
 * provide the ability to store the xml configuration when the
 * {@link Session#startTempConfigInstance(String, boolean, String, String)} is
 * called.<br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class ConfigInstanceRecorderMock extends ServerAPIMock {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final TPInstanceServerAPIReactor robMock;

	private String ciTpMetamergeConfigStr;

	public ConfigInstanceRecorderMock(TPInstanceServerAPIReactor reuse) throws Exception {
		super(reuse);
		robMock = reuse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.fwk.mock.ServerAPIConnectionMock#activateMocks()
	 */
	@Override
	public void activateMocks() throws Exception {

		// override the creation
		EasyMock.expect(
				sMock.startTempConfigInstance((String) EasyMock.anyObject(), EasyMock.eq(true), EasyMock.startsWith(TDIUtils
						.escapeRunName(robMock.getTypeId())
						+ "_"), (String) EasyMock.anyObject())).andStubAnswer(robMock.new Session_StartTempInstance() {
			/*
			 * (non-Javadoc)
			 * 
			 * @seecom.ibm.di.tp.fwk.mock.RobustALHandlerMock.
			 * Session_StartTempInstance#answer()
			 */
			@Override
			public ConfigInstance answer() throws Throwable {
				ConfigInstance ci = super.answer();
				if (ci != null) {
					// started a CI... keep the config string
					Object[] args = EasyMock.getCurrentArguments();
					ciTpMetamergeConfigStr = (String) args[0];
				}
				return ci;
			}
		});

		super.activateMocks();
	}

	/**
	 * @return the ciTpMetamergeConfigStr
	 */
	public String getCiTpMetamergeConfigStr() {
		return ciTpMetamergeConfigStr;
	}
}
