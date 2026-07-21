package com.ibm.di.test.tp.mock;

import org.easymock.EasyMock;
import org.easymock.IAnswer;

import com.ibm.di.api.DIException;
import com.ibm.di.api.local.AssemblyLine;
import com.ibm.di.api.local.ConfigInstance;
import com.ibm.di.api.local.TDIProperties;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.test.api.mock.AftermathAccessor;
import com.ibm.di.test.api.mock.ServerAPIMock;
import com.ibm.di.tp.server.model.TouchpointRole;
import com.ibm.di.tp.server.model.impl.tdi.TemplateConfigLoader;
import com.ibm.di.tp.server.util.TDIUtils;

/**
 * This class represents the pair of CI and AL that are started/stopped when a
 * TP Instance is started/stopped. <br>
 * <br>
 * The mock is not activated by this method so the user could chain it to
 * another mock. Once the chaining is complete the user should call
 * {@link ServerAPIMock#defineMockCalls()} on the last object in the chain. The
 * returned object calls the method of the <code>reuse</code> object before
 * defining its own mocks. <br>
 * <br>
 * Before using the mocks call {@link ServerAPIMock#activateMocks()} to activate
 * them. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class TPInstanceServerAPIReactor extends ServerAPIMock {

	private final String typeId;
	private final ConfigInstance ciMock = EasyMock.createMock(ConfigInstance.class);
	private final AssemblyLine alMock = EasyMock.createMock(AssemblyLine.class);
	private final TDIProperties propsMock = EasyMock.createMock(TDIProperties.class);
	private final TouchpointRole role;

	// control vars
	private boolean ciExists;
	private boolean alExists;
	private boolean alActive;

	/**
	 * @param reuse
	 * @throws Exception
	 */
	public TPInstanceServerAPIReactor(ServerAPIMock reuse, String typeId, TouchpointRole role, boolean ciExists, boolean alExists,
			boolean alActive) throws Exception {
		super(reuse);
		this.typeId = typeId;
		this.role = role;
		this.ciExists = ciExists;
		this.alExists = alExists;
		this.alActive = alActive;
	}

	public void activateMocks() throws Exception {

		// get CI
		EasyMock.expect(sMock.getConfigInstance(EasyMock.startsWith(TDIUtils.escapeRunName(getTypeId()) + "_"))).andStubAnswer(
				new Session_GetInstanceResponse());

		// start temp CI
		EasyMock.expect(
				sMock.startTempConfigInstance((String) EasyMock.anyObject(), EasyMock.eq(true), EasyMock.startsWith(TDIUtils
						.escapeRunName(getTypeId())
						+ "_"), (String) EasyMock.anyObject())).andStubAnswer(new Session_StartTempInstance());

		// stop CI
		getCiMock().stop();
		EasyMock.expectLastCall().andAnswer(new CI_Stop());

		// stop CI
		getCiMock().stop(EasyMock.anyBoolean());
		EasyMock.expectLastCall().andAnswer(new CI_Stop());

		// get ALs
		EasyMock.expect(getCiMock().getAssemblyLines()).andStubAnswer(new CI_GetALs());

		// start AL
		EasyMock.expect(getCiMock().startAssemblyLine(new TemplateConfigLoader().getAlNameForRole(role))).andStubAnswer(
				new CI_StartAL());

		// stop AL
		getAlMock().stop();
		EasyMock.expectLastCall().andAnswer(new AL_Stop());

		// AL getName
		EasyMock.expect(getAlMock().getName()).andReturn(
				MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/" + (new TemplateConfigLoader().getAlNameForRole(role))).anyTimes();

		// AL isActiver
		EasyMock.expect(getAlMock().isActive()).andStubAnswer(new AL_GetName());

		// get TDIProperties
		EasyMock.expect(getCiMock().getTDIProperties()).andReturn(propsMock).anyTimes();

		getPropsMock().setProperty((String) EasyMock.anyObject(), (String) EasyMock.anyObject(), EasyMock.anyObject());
		EasyMock.expectLastCall().anyTimes();

		super.activateMocks();
		EasyMock.replay(getCiMock(), getAlMock());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.fwk.mock.ServerAPIConnectionMock#resetMockCalls()
	 */
	@Override
	public void resetMockCalls() {
		super.resetMockCalls();
		EasyMock.reset(getCiMock(), getAlMock());
	}

	@Override
	public AftermathAccessor verifyMockCalls() {
		AftermathAccessor verifyMockCalls = super.verifyMockCalls();
		EasyMock.verify(getCiMock(), getAlMock());
		return verifyMockCalls;
	}

	public boolean isCiExist() {
		return ciExists;
	}

	public boolean isAlExist() {
		return alExists;
	}

	public boolean isAlActive() {
		return alActive;
	}

	/**
	 * @return the ciMock
	 */
	public ConfigInstance getCiMock() {
		return ciMock;
	}

	/**
	 * @return the alMock
	 */
	public AssemblyLine getAlMock() {
		return alMock;
	}

	/**
	 * @return the propsMock
	 */
	public TDIProperties getPropsMock() {
		return propsMock;
	}

	/**
	 * @return the typeId
	 */
	public String getTypeId() {
		return typeId;
	}

	public class Session_GetInstanceResponse implements IAnswer<ConfigInstance> {
		public ConfigInstance answer() throws Throwable {
			return ciExists ? getCiMock() : null;
		}
	}

	public class Session_StartTempInstance implements IAnswer<ConfigInstance> {
		public ConfigInstance answer() throws Throwable {
			if (ciExists) {
				throw new DIException("The runName is already in use.");
			}
			ciExists = true;
			return getCiMock();
		}
	}

	public class CI_Stop implements IAnswer<Object> {
		public Object answer() throws Throwable {
			alActive = false;
			alExists = false;
			ciExists = false;
			return null;
		}
	}

	public class CI_GetALs implements IAnswer<AssemblyLine[]> {
		public AssemblyLine[] answer() throws Throwable {
			return alExists ? new AssemblyLine[] { getAlMock() } : new AssemblyLine[] {};
		}
	}

	public class CI_StartAL implements IAnswer<AssemblyLine> {
		public AssemblyLine answer() throws Throwable {
			if (alExists) {
				throw new DIException("AL already started.");
			}
			alExists = true;
			return getAlMock();
		}
	}

	public class AL_Stop implements IAnswer<Object> {
		public Object answer() throws Throwable {
			alActive = false;
			alExists = false;
			return null;
		}
	}

	public class AL_GetName implements IAnswer<Boolean> {
		public Boolean answer() throws Throwable {
			return alActive;
		}
	}
}